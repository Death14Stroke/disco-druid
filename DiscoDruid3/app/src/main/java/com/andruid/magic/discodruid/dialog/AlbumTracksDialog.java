package com.andruid.magic.discodruid.dialog;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.TrackAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.databinding.FullscreenAlbumDialogBinding;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.service.BackgroundAudioService;
import com.andruid.magic.discodruid.viewmodel.TrackViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

public class AlbumTracksDialog extends Fragment {
    private FullscreenAlbumDialogBinding binding;
    private Album album;
    private MediaBrowserCompat mediaBrowserCompat;
    private TrackAdapter trackAdapter;
    private AlbumDialogClickedListener mListener;
    private TrackViewModel trackViewModel;

    public static AlbumTracksDialog newInstance(Album album) {
        AlbumTracksDialog fragment = new AlbumTracksDialog();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ALBUM,album);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getContext(),"album tracks",Toast.LENGTH_SHORT).show();
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.ALBUM))
            album = args.getParcelable(Constants.ALBUM);
        trackAdapter = new TrackAdapter();
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fullscreen_album_dialog,container,false);
        binding.setAlbum(album);
        binding.toolbar.setTitle(album.getAlbum());
        binding.toolbar.setTitleTextColor(getResources().getColor(R.color.colorUnselectedTab));
        binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
//        binding.toolbar.setNavigationOnClickListener(view ->
//                dismiss());
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        binding.fullScreenRv.setAdapter(trackAdapter);
        binding.fullScreenRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fullScreenRv.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()),DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        binding.fullScreenRv.addItemDecoration(dividerItemDecoration);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(), new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),
                new MediaBrowserCompat.ConnectionCallback(){
                    @Override
                    public void onConnected() {
                        super.onConnected();
                        loadTracks();
                        mediaBrowserCompat.subscribe(Constants.CURRENT_TRACK, new Bundle(), new MediaBrowserCompat.SubscriptionCallback() {
                            @Override
                            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                                super.onChildrenLoaded(parentId, children, options);
                                if(children.size() == 1) {
                                    Bundle extras = children.get(0).getDescription().getExtras();
                                    if(extras!=null) {
                                        Track track = extras.getParcelable(Constants.TRACK);
                                        if (track != null) {
                                            trackAdapter.setPlayingTrackId(track.getAudioId());
                                        }
                                    }
                                }
                            }
                        });
                        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
                            binding.swipeRefreshLayout.setRefreshing(true);
                            loadTracks();
                        });
                    }
                },null);
        mediaBrowserCompat.connect();
        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AlbumDialogClickedListener) {
            mListener = (AlbumDialogClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AlbumDialogClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//            int width = ViewGroup.LayoutParams.MATCH_PARENT;
//            int height = ViewGroup.LayoutParams.MATCH_PARENT;
//            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.unsubscribe(Constants.CURRENT_TRACK);
        mediaBrowserCompat.disconnect();
    }

    private void loadTracks(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Bundle options = new Bundle();
                        options.putString(Constants.ALBUM_ID,album.getAlbumId());
                        trackViewModel.getTracks(mediaBrowserCompat,options).observe(AlbumTracksDialog.this, new Observer<PagedList<Track>>() {
                            @Override
                            public void onChanged(@Nullable PagedList<Track> tracks) {
                                trackAdapter.submitList(tracks);
                                binding.swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getContext(),response.getPermissionName()+" permission denied",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public interface AlbumDialogClickedListener{
        void onAlbumDialogClicked(List<Track> trackList, int pos);
    }
}
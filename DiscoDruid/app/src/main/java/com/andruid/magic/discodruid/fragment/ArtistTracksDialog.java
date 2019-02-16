package com.andruid.magic.discodruid.fragment;

import android.Manifest;
import android.app.Dialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.PagedTrackAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Artist;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.services.BackgroundAudioService;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.andruid.magic.discodruid.viewmodel.PagedTrackViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistTracksDialog extends DialogFragment{
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.full_screen_rv) RecyclerView recyclerView;
    private Artist artist;
    private MediaBrowserCompat mediaBrowserCompat;
    private PagedTrackAdapter pagedTrackAdapter;
    private ArtistDialogClickedListener mListener;
    private PagedTrackViewModel pagedTrackViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.ARTIST))
            artist = args.getParcelable(Constants.ARTIST);
        pagedTrackAdapter = new PagedTrackAdapter(getContext(),Constants.VIEW_ARTIST_TRACKS);
        pagedTrackViewModel = ViewModelProviders.of(this).get(PagedTrackViewModel.class);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(),new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),
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
                                            pagedTrackAdapter.setPlayingTrackId(track.getAudioId());
                                        }
                                    }
                                }
                            }
                        });
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                swipeRefreshLayout.setRefreshing(true);
                                loadTracks();
                            }
                        });
                    }
                },null);
        mediaBrowserCompat.connect();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fullscreen_dialog,container,false);
        ButterKnife.bind(this,view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(artist.getArtist());
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorUnselectedTab));
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        recyclerView.setAdapter(pagedTrackAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()),DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                pagedTrackAdapter.reset();
                mListener.onArtistDialogClicked(Objects.requireNonNull(Objects.requireNonNull(pagedTrackAdapter.getCurrentList()).snapshot()), position);
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ArtistDialogClickedListener) {
            mListener = (ArtistDialogClickedListener) context;
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
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }
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
                        options.putString(Constants.ARTIST_ID,artist.getArtistId());
                        pagedTrackViewModel.getTracks(mediaBrowserCompat,options).observe(ArtistTracksDialog.this, new Observer<PagedList<Track>>() {
                            @Override
                            public void onChanged(@Nullable PagedList<Track> tracks) {
                                pagedTrackAdapter.submitList(tracks);
                                swipeRefreshLayout.setRefreshing(false);
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

    public interface ArtistDialogClickedListener{
        void onArtistDialogClicked(List<Track> trackList, int pos);
    }
}
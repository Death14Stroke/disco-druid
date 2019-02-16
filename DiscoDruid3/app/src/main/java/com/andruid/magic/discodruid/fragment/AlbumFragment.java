package com.andruid.magic.discodruid.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.AlbumAdapter;
import com.andruid.magic.discodruid.databinding.AlbumFragmentBinding;
import com.andruid.magic.discodruid.dialog.AlbumTracksDialog;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.service.BackgroundAudioService;
import com.andruid.magic.discodruid.util.DetailsTransition;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.andruid.magic.discodruid.viewmodel.AlbumViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.transition.Fade;

public class AlbumFragment extends Fragment {
    private MediaBrowserCompat mediaBrowserCompat;
    private AlbumAdapter albumAdapter;
    private AlbumViewModel albumViewModel;
    private AlbumFragmentBinding binding;

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumAdapter = new AlbumAdapter();
        albumViewModel = ViewModelProviders.of(this).get(AlbumViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.album_fragment,container,false);
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        setRecyclerView();
        mediaBrowserCompat = new MediaBrowserCompat(getContext(),new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),
                new MediaBrowserCompat.ConnectionCallback(){
                    @Override
                    public void onConnected() {
                        super.onConnected();
                        loadAlbums();
                        binding.swipeRefresh.setOnRefreshListener(() -> {
                            binding.swipeRefresh.setRefreshing(true);
                            loadAlbums();
                        });
                    }
                },null);
        mediaBrowserCompat.connect();
        return binding.getRoot();
    }

    private void setRecyclerView(){
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(albumAdapter);
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                binding.recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(getFragmentManager()==null)
                    return;
                Album album = albumAdapter.getCurrentList().get(position);
                AlbumTracksDialog fragment = AlbumTracksDialog.newInstance(album);
                //fragment.setSharedElementEnterTransition(new DetailsTransition());
                fragment.setEnterTransition(new Fade());
                setExitTransition(new Fade());
                //fragment.setSharedElementReturnTransition(new DetailsTransition());
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        //.addSharedElement(view.findViewById(R.id.album_imageView),getString(R.string.album_shared_element))
                        .replace(R.id.container,fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
    }

    private void loadAlbums(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        albumViewModel.getAlbums(mediaBrowserCompat).observe(AlbumFragment.this, albums -> {
                            albumAdapter.submitList(albums);
                            binding.swipeRefresh.setRefreshing(false);
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
}
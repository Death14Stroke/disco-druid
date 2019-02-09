package com.andruid.magic.discodruid.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.BackgroundAudioService;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.PlaylistAdapter;
import com.andruid.magic.discodruid.databinding.PlaylistFragmentBinding;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.andruid.magic.discodruid.viewmodel.PlaylistViewModel;
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;
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
import androidx.recyclerview.widget.LinearLayoutManager;

public class PlaylistFragment extends Fragment {
    private MediaBrowserCompat mediaBrowserCompat;
    private PlaylistAdapter playlistAdapter;
    private PlaylistViewModel playlistViewModel;
    private PlaylistFragmentBinding binding;

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistAdapter = new PlaylistAdapter();
        playlistViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.playlist_fragment,container,false);
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
                        loadPlaylists();
                        binding.swipeRefresh.setOnRefreshListener(() -> {
                            binding.swipeRefresh.setRefreshing(true);
                            loadPlaylists();
                        });
                    }
                },null);
        mediaBrowserCompat.connect();
        return binding.getRoot();
    }

    private void setRecyclerView(){
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());
        binding.recyclerView.setAdapter(playlistAdapter);
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                binding.recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
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

    private void loadPlaylists(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        playlistViewModel.getPlayLists(mediaBrowserCompat).observe(PlaylistFragment.this, playLists -> {
                            playlistAdapter.submitList(playLists);
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
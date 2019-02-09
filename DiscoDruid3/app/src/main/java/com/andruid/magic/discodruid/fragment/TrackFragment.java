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
import com.andruid.magic.discodruid.adapter.TrackAdapter;
import com.andruid.magic.discodruid.databinding.TrackFragmentBinding;
import com.andruid.magic.discodruid.viewmodel.TrackViewModel;
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
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

public class TrackFragment extends Fragment {
    private MediaBrowserCompat mediaBrowserCompat;
    private TrackAdapter trackAdapter;
    private TrackViewModel trackViewModel;
    private TrackFragmentBinding binding;

    public static TrackFragment newInstance() {
        return new TrackFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackAdapter = new TrackAdapter();
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.track_fragment, container, false);
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()),DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
        binding.recyclerView.setAdapter(trackAdapter);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(),new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),
                new MediaBrowserCompat.ConnectionCallback(){
                    @Override
                    public void onConnected() {
                        super.onConnected();
                        loadTracks();
                        binding.swipeRefresh.setOnRefreshListener(() -> {
                            binding.swipeRefresh.setRefreshing(true);
                            loadTracks();
                        });
                    }
                },null);
        mediaBrowserCompat.connect();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
    }

    private void loadTracks(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        trackViewModel.getTracks(mediaBrowserCompat,null).observe(TrackFragment.this, pagedList -> {
                            trackAdapter.submitList(pagedList);
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
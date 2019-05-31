package com.andruid.magic.discodruid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;

import com.andruid.magic.discodruid.adapter.CustomPagerAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.databinding.ActivityMainBinding;
import com.andruid.magic.discodruid.fragment.TrackFragment;
import com.andruid.magic.mediareader.model.Track;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import bg.devlabs.transitioner.Transitioner;

public class MainActivity extends AppCompatActivity implements TrackFragment.TrackClickListener {
    private ActivityMainBinding binding;
    private MediaBrowserCompat mediaBrowserCompat;
    private MediaControllerCompat mediaControllerCompat;
    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            super.onShuffleModeChanged(shuffleMode);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Transitioner transitioner = new Transitioner(
                binding.bottomSheet.bottomCollapsed.trackThumbnail, binding.bottomSheet.trackDetailRecyclerView);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheetLl);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
            }
            @Override
            public void onSlide(@NonNull View view, float v) {
                transitioner.setProgress(v);
            }
        });
        setSupportActionBar(binding.toolbar);
        setViewPager();
        mediaBrowserCompat = new MediaBrowserCompat(MainActivity.this, new ComponentName(
                MainActivity.this, MyMusicService.class),
                new MediaBrowserCompat.ConnectionCallback(){
                    @Override
                    public void onConnected() {
                        super.onConnected();
                        try {
                            mediaControllerCompat = new MediaControllerCompat(MainActivity.this, mediaBrowserCompat.getSessionToken());
                            mediaControllerCompat.registerCallback(callback);
                            MediaControllerCompat.setMediaController(MainActivity.this,mediaControllerCompat);
                        } catch( RemoteException e ) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConnectionFailed() {
                        super.onConnectionFailed();
                        Toast.makeText(getApplicationContext(),"Connection failed",Toast.LENGTH_SHORT).show();
                    }
                }, null);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(!mediaBrowserCompat.isConnected())
                            mediaBrowserCompat.connect();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void setViewPager() {
        CustomPagerAdapter pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(Constants.NUMBER_OF_TABS);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
                int resource = R.drawable.track_bg;
                switch (tab.getPosition()){
                    case Constants.POSITION_ALBUM:
                        resource = R.drawable.album_bg;
                        break;
                    case Constants.POSITION_ARTIST:
                        resource = R.drawable.artist_bg;
                        break;
                    case Constants.POSITION_PLAYLIST:
                        resource = R.drawable.playlist_bg;
                }
                Glide.with(MainActivity.this)
                        .load(resource)
                        .into(binding.tabImageView);
                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(resource)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Palette.from(resource).generate(palette -> {
                                    if(palette==null)
                                        return;
                                    int vibrantLightColor = palette.getLightVibrantColor(getResources().getColor(R.color.colorPrimary));
                                    int vibrantDarkColor = palette.getDarkVibrantColor(getResources().getColor(R.color.colorPrimaryDark));
                                    binding.collapseToolBar.setContentScrimColor(vibrantDarkColor);
                                    binding.collapseToolBar.setStatusBarScrimColor(vibrantLightColor);
                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    @Override
    public void onTrackClicked(List<Track> trackList, int pos) {
        Bundle params = new Bundle();
        params.putParcelableArrayList(Constants.PLAY_QUEUE, new ArrayList<>(trackList));
        mediaControllerCompat.sendCommand(Constants.SET_QUEUE, params, null);
        mediaControllerCompat.getTransportControls().skipToQueueItem(pos);
    }
}
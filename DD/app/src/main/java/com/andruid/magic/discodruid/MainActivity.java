package com.andruid.magic.discodruid;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.andruid.magic.discodruid.adapter.CustomPagerAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.databinding.ActivityMainBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        setViewPager();
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
}
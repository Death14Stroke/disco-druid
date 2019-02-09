package com.andruid.magic.discodruid.adapter;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.fragment.AlbumFragment;
import com.andruid.magic.discodruid.fragment.ArtistFragment;
import com.andruid.magic.discodruid.fragment.PlaylistFragment;
import com.andruid.magic.discodruid.fragment.TrackFragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class CustomPagerAdapter extends FragmentStatePagerAdapter {
    public CustomPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case Constants.POSITION_TRACK:
                fragment = TrackFragment.newInstance();
                break;
            case Constants.POSITION_ALBUM:
                fragment = AlbumFragment.newInstance();
                break;
            case Constants.POSITION_ARTIST:
                fragment = ArtistFragment.newInstance();
                break;
            case Constants.POSITION_PLAYLIST:
                fragment = PlaylistFragment.newInstance();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return Constants.NUMBER_OF_TABS;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position){
            case Constants.POSITION_ALBUM:
                title = "Albums";
                break;
            case Constants.POSITION_ARTIST:
                title = "Artists";
                break;
            case Constants.POSITION_PLAYLIST:
                title = "PlayLists";
                break;
            default:
                title = "Tracks";
                break;
        }
        return title;
    }
}

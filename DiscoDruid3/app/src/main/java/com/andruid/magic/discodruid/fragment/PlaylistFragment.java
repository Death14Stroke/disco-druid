package com.andruid.magic.discodruid.fragment;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.viewmodel.PlaylistViewModel;
import com.andruid.magic.discodruid.R;

public class PlaylistFragment extends Fragment {

    private PlaylistViewModel mViewModel;

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
        // TODO: Use the ViewModel
    }

}

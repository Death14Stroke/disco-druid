package com.andruid.magic.discodruid.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.TrackDetailViewHolder;
import com.thesurix.gesturerecycler.GestureAdapter;

public class TrackDetailAdapter extends GestureAdapter<Track,TrackDetailViewHolder> {
    private Context mContext;

    public TrackDetailAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public TrackDetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_track_detail,viewGroup,false);
        return new TrackDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackDetailViewHolder trackDetailViewHolder, int pos) {
        Track track = getItem(pos);
        trackDetailViewHolder.setItem(track,mContext);
    }
}
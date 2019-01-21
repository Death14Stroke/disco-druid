package com.andruid.magic.discodruid.model;

import android.content.Context;
import android.view.View;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;
import com.xwray.groupie.Item;

import androidx.annotation.NonNull;

public class TrackItem extends Item<TrackViewHolder> {
    private Track track;
    private Context mContext;

    public TrackItem(Track track, Context mContext) {
        this.track = track;
        this.mContext = mContext;
    }

    @Override
    public void bind(@NonNull TrackViewHolder viewHolder, int position) {
        viewHolder.setItem(track,mContext);
    }

    @Override
    public int getLayout() {
        return R.layout.layout_track;
    }

    @NonNull
    @Override
    public TrackViewHolder createViewHolder(@NonNull View itemView) {
        return new TrackViewHolder(itemView);
    }
}

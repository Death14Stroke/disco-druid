package com.andruid.magic.discodruid.model;

import android.content.Context;
import android.view.View;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;
import com.xwray.groupie.Group;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

public class TrackItem extends Item {
    private Track track;
    private Context mContext;

    public TrackItem(Track track, Context mContext) {
        this.track = track;
        this.mContext = mContext;
    }

    public Track getTrack() {
        return track;
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

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        ((TrackViewHolder)viewHolder).setItem(track,mContext);
    }

    @Override
    public int getSwipeDirs() {
        return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    }

    @Override
    public int getDragDirs() {
        return ItemTouchHelper.DOWN | ItemTouchHelper.UP;
    }
}
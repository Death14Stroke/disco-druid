package com.andruid.magic.discodruid.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.GeneralViewHolder;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;
import com.thesurix.gesturerecycler.GestureAdapter;

public class GeneralAdapter extends GestureAdapter<Track,GeneralViewHolder> {
    private Context mContext;
    private long playingTrackId;
    private LongSparseArray<Integer> positionMap = new LongSparseArray<>();

    public GeneralAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setPlayingTrackId(long playingTrackId) {
        Integer integer = positionMap.get(this.playingTrackId);
        if(integer!=null) {
            int oldPos = integer;
            notifyItemChanged(oldPos);
        }
        integer = positionMap.get(playingTrackId);
        if(integer!=null) {
            int newPos = integer;
            notifyItemChanged(newPos);
        }
        this.playingTrackId = playingTrackId;
    }

    @NonNull
    @Override
    public GeneralViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_playlist_track, viewGroup, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GeneralViewHolder viewHolder, int position) {
        Track track = getItem(position);
        if (track != null) {
            positionMap.append(track.getAudioId(),position);
        }
        if (track != null) {
            if(track.getAudioId()==playingTrackId)
                viewHolder.itemView.setSelected(true);
            else
                viewHolder.itemView.setSelected(false);
        }
        TrackViewHolder trackViewHolder = (TrackViewHolder) viewHolder;
        trackViewHolder.setItem(track,mContext);
    }
}
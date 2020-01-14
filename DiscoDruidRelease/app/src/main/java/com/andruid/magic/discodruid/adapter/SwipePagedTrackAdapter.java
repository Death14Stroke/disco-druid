package com.andruid.magic.discodruid.adapter;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SwipePagedTrackAdapter extends PagedListAdapter<Track,TrackViewHolder>{
    private Context mContext;
    private List<String> selectedTrackIds = new ArrayList<>();
    private long playingTrackId;
    private LongSparseArray<Integer> positionMap = new LongSparseArray<>();

    public SwipePagedTrackAdapter(Context mContext) {
        super(DIFF_CALLBACK);
        this.mContext = mContext;
    }

    public void setSelectedTrackIds(List<String> selectedTrackIds) {
        this.selectedTrackIds = selectedTrackIds;
        notifyDataSetChanged();
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

    public void reset(){
        playingTrackId = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_track,viewGroup,false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        Track track = getItem(i);
        if (track != null) {
            positionMap.append(track.getAudioId(),i);
        }
        trackViewHolder.setItem(track,mContext);
        if (track != null) {
            if(track.getAudioId()==playingTrackId)
                trackViewHolder.itemView.setSelected(true);
            else
                trackViewHolder.itemView.setSelected(false);
        }
        if (selectedTrackIds.contains(String.valueOf(Objects.requireNonNull(track).getAudioId()))){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                trackViewHolder.itemView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
            else
                trackViewHolder.itemView.setBackground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                trackViewHolder.itemView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext,android.R.color.transparent)));
            else
                trackViewHolder.itemView.setBackground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
        }
    }

    private static final DiffUtil.ItemCallback<Track> DIFF_CALLBACK = new DiffUtil.ItemCallback<Track>() {
        @Override
        public boolean areItemsTheSame(@NonNull Track oldTrack, @NonNull Track newTrack) {
            return oldTrack.getAudioId()==newTrack.getAudioId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Track oldTrack, @NonNull Track newTrack) {
            return oldTrack.equals(newTrack);
        }
    };
}
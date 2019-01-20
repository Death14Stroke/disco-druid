package com.andruid.magic.discodruid.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.TrackDetailViewHolder;

import java.util.ArrayList;
import java.util.List;

public class TrackDetailAdapter extends RecyclerView.Adapter<TrackDetailViewHolder>{
    private Context mContext;
    private List<Track> trackList = new ArrayList<>();

    public TrackDetailAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public TrackDetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_track_detail,viewGroup,false);
        return new TrackDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackDetailViewHolder trackDetailViewHolder, int pos) {
        Track track = trackList.get(pos);
        trackDetailViewHolder.setItem(track,mContext);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }
}
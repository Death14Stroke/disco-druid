package com.andruid.magic.discodruid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.databinding.LayoutTrackDetailBinding;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.TrackDetailViewHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        LayoutTrackDetailBinding binding = LayoutTrackDetailBinding.inflate(layoutInflater,viewGroup,false);
        return new TrackDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackDetailViewHolder trackDetailViewHolder, int pos) {
        Track track = trackList.get(pos);
        trackDetailViewHolder.bind(track);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }
}
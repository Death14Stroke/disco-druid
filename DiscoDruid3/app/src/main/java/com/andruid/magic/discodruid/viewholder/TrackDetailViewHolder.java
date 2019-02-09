package com.andruid.magic.discodruid.viewholder;

import com.andruid.magic.discodruid.databinding.LayoutTrackDetailBinding;
import com.andruid.magic.discodruid.model.Track;

import androidx.recyclerview.widget.RecyclerView;

public class TrackDetailViewHolder extends RecyclerView.ViewHolder {
    private LayoutTrackDetailBinding binding;

    public TrackDetailViewHolder(LayoutTrackDetailBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Track track){
        binding.setTrack(track);
        binding.executePendingBindings();
    }
}
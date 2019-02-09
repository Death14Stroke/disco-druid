package com.andruid.magic.discodruid.viewholder;

import com.andruid.magic.discodruid.databinding.LayoutTrackBinding;
import com.andruid.magic.discodruid.model.Track;

import androidx.recyclerview.widget.RecyclerView;

public class TrackViewHolder extends RecyclerView.ViewHolder {
    private LayoutTrackBinding binding;

    public TrackViewHolder(LayoutTrackBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Track track){
        binding.setTrack(track);
        binding.executePendingBindings();
    }
}
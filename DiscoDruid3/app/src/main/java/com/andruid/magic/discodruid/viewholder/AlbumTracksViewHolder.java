package com.andruid.magic.discodruid.viewholder;

import com.andruid.magic.discodruid.databinding.LayoutAlbumTracksBinding;
import com.andruid.magic.discodruid.model.Track;

import androidx.recyclerview.widget.RecyclerView;

public class AlbumTracksViewHolder extends RecyclerView.ViewHolder {
    private LayoutAlbumTracksBinding binding;

    public AlbumTracksViewHolder(LayoutAlbumTracksBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Track track){
        binding.setTrack(track);
    }
}
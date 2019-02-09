package com.andruid.magic.discodruid.viewholder;

import com.andruid.magic.discodruid.databinding.LayoutArtistBinding;
import com.andruid.magic.discodruid.model.Artist;

import androidx.recyclerview.widget.RecyclerView;

public class ArtistViewHolder extends RecyclerView.ViewHolder {
    private LayoutArtistBinding binding;

    public ArtistViewHolder(LayoutArtistBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Artist artist){
        binding.setArtist(artist);
        binding.executePendingBindings();
    }
}
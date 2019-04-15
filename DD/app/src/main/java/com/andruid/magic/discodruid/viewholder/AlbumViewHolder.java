package com.andruid.magic.discodruid.viewholder;

import com.andruid.magic.discodruid.databinding.LayoutAlbumBinding;
import com.andruid.magic.mediareader.model.Album;

import androidx.recyclerview.widget.RecyclerView;

public class AlbumViewHolder extends RecyclerView.ViewHolder {
    private LayoutAlbumBinding binding;

    public AlbumViewHolder(LayoutAlbumBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Album album){
        binding.setAlbum(album);
        binding.executePendingBindings();
    }
}
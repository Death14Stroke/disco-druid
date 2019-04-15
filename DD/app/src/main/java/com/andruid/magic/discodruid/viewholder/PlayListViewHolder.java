package com.andruid.magic.discodruid.viewholder;

import com.andruid.magic.discodruid.databinding.LayoutPlaylistBinding;
import com.andruid.magic.mediareader.model.PlayList;

import androidx.recyclerview.widget.RecyclerView;

public class PlayListViewHolder extends RecyclerView.ViewHolder {
    private LayoutPlaylistBinding binding;

    public PlayListViewHolder(LayoutPlaylistBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(PlayList playList){
        binding.setPlaylist(playList);
        binding.executePendingBindings();
    }
}
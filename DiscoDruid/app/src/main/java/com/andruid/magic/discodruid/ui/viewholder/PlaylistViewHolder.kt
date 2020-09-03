package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutPlaylistBinding
import com.andruid.magic.medialoader.model.Playlist

class PlaylistViewHolder(private val binding: LayoutPlaylistBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): PlaylistViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutPlaylistBinding>(
                inflater,
                R.layout.layout_playlist,
                parent,
                false
            )
            return PlaylistViewHolder(binding)
        }
    }

    fun bind(playlist: Playlist) {
        binding.playlist = playlist
        binding.executePendingBindings()
    }
}
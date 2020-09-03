package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.ui.viewholder.PlaylistViewHolder
import com.andruid.magic.medialoader.model.Playlist

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist) =
        oldItem.playlistId == newItem.playlistId

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist) =
        oldItem == newItem
}

class PlaylistAdapter : PagingDataAdapter<Playlist, PlaylistViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PlaylistViewHolder.from(parent)

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        getItem(position)?.let { playlist -> holder.bind(playlist) }
    }
}
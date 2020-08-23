package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.ui.viewholder.AlbumViewHolder
import com.andruid.magic.medialoader.model.Album

class AlbumAdapter : PagedListAdapter<Album, AlbumViewHolder>(AlbumDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AlbumViewHolder.from(parent)

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = getItem(position)
        album?.let { holder.bind(it) }
    }

    class AlbumDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album) =
            oldItem.albumId == newItem.albumId

        override fun areContentsTheSame(oldItem: Album, newItem: Album) =
            oldItem == newItem
    }
}
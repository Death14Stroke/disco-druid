package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.ui.viewholder.AlbumViewHolder
import com.andruid.magic.medialoader.model.Album
import kotlinx.coroutines.CoroutineScope

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
    override fun areContentsTheSame(oldItem: Album, newItem: Album) =
        oldItem.albumId == newItem.albumId

    override fun areItemsTheSame(oldItem: Album, newItem: Album) =
        oldItem == newItem
}

class AlbumsAdapter(private val scope: CoroutineScope) : PagingDataAdapter<Album, AlbumViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AlbumViewHolder.from(parent)

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        getItem(position)?.let { album -> holder.bind(scope, album) }
    }
}
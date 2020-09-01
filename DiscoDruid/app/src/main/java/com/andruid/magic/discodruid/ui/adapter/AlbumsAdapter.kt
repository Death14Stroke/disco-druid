package com.andruid.magic.discodruid.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.data.VIEW_TYPE_ALL_ALBUMS
import com.andruid.magic.discodruid.data.VIEW_TYPE_ARTIST_ALBUMS
import com.andruid.magic.discodruid.ui.viewholder.AlbumViewHolder
import com.andruid.magic.discodruid.ui.viewholder.ArtistAlbumViewHolder
import com.andruid.magic.medialoader.model.Album
import kotlinx.coroutines.CoroutineScope

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
    override fun areContentsTheSame(oldItem: Album, newItem: Album) =
        oldItem.albumId == newItem.albumId

    override fun areItemsTheSame(oldItem: Album, newItem: Album) =
        oldItem == newItem
}

class AlbumsAdapter(
    private val context: Context,
    private val scope: CoroutineScope,
    private val viewType: Int = VIEW_TYPE_ALL_ALBUMS
) : PagingDataAdapter<Album, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ARTIST_ALBUMS -> ArtistAlbumViewHolder.from(parent)
            else -> AlbumViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { album ->
            if (holder is AlbumViewHolder)
                holder.bind(context, scope, album)
            else if (holder is ArtistAlbumViewHolder)
                holder.bind(context, scope, album)
        }
    }

    override fun getItemViewType(position: Int) = viewType

    fun getItemAtPosition(position: Int) = getItem(position)
}
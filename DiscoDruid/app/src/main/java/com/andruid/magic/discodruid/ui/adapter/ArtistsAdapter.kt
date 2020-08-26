package com.andruid.magic.discodruid.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.ui.viewholder.ArtistViewHolder
import com.andruid.magic.medialoader.model.Artist
import kotlinx.coroutines.CoroutineScope

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Artist>() {
    override fun areContentsTheSame(oldItem: Artist, newItem: Artist) =
        oldItem.artistId == newItem.artistId

    override fun areItemsTheSame(oldItem: Artist, newItem: Artist) =
        oldItem == newItem
}

class ArtistsAdapter(
    private val context: Context,
    private val scope: CoroutineScope
) : PagingDataAdapter<Artist, ArtistViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ArtistViewHolder.from(parent)

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        getItem(position)?.let { artist -> holder.bind(context, scope, artist) }
    }
}
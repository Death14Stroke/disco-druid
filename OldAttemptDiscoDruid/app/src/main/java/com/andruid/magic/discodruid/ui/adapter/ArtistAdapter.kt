package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.ui.viewholder.ArtistViewHolder
import com.andruid.magic.medialoader.model.Artist

class ArtistAdapter : PagedListAdapter<Artist, ArtistViewHolder>(ArtistDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ArtistViewHolder.from(parent)

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = getItem(position)
        artist?.let { holder.bind(it) }
    }

    class ArtistDiffCallback : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist) =
            oldItem.artistId == newItem.artistId

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist) =
            oldItem == newItem
    }
}
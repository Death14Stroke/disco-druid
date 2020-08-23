package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.ui.viewholder.TrackViewHolder
import com.andruid.magic.medialoader.model.Track

class TrackAdapter : PagedListAdapter<Track, TrackViewHolder>(TrackDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        track?.let { holder.bind(it) }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) =
            oldItem.audioId == newItem.audioId

        override fun areContentsTheSame(oldItem: Track, newItem: Track) =
            oldItem == newItem
    }
}
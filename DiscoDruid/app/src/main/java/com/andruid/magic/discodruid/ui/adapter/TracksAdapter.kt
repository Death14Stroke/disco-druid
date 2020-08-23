package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.ui.viewholder.TrackViewHolder
import com.andruid.magic.medialoader.model.Track

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Track>() {
    override fun areContentsTheSame(oldItem: Track, newItem: Track) =
        oldItem.audioId == newItem.audioId

    override fun areItemsTheSame(oldItem: Track, newItem: Track) =
        oldItem == newItem
}

class TracksAdapter : PagingDataAdapter<Track, TrackViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        getItem(position)?.let { track -> holder.bind(track) }
    }
}
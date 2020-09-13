package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.ui.viewholder.QueueTrackViewHolder
import com.andruid.magic.medialoader.model.Track

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackViewRepresentation>() {
    override fun areContentsTheSame(
        oldItem: TrackViewRepresentation, newItem: TrackViewRepresentation
    ) = oldItem.track.audioId == newItem.track.audioId

    override fun areItemsTheSame(
        oldItem: TrackViewRepresentation, newItem: TrackViewRepresentation
    ) = oldItem == newItem
}

class QueueTracksAdapter :
    ListAdapter<TrackViewRepresentation, QueueTrackViewHolder>(DIFF_CALLBACK) {
    var currentTrack: Track? = null
        set(value) {
            prevPosition?.let { notifyItemChanged(it) }
            field = value
        }
    private var prevPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        QueueTrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: QueueTrackViewHolder, position: Int) {
        getItem(position)?.let { viewRepresentation ->
            val track = viewRepresentation.track
            val activated = currentTrack?.audioId == track.audioId
            if (activated)
                prevPosition = position

            holder.bind(viewRepresentation, activated)
        }
    }

    fun getItemAtPosition(position: Int) =
        getItem(position)?.track
}
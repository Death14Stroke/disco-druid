package com.andruid.magic.discodruid.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.data.VIEW_TYPE_ALBUM_TRACKS
import com.andruid.magic.discodruid.data.VIEW_TYPE_ALL_TRACKS
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.ui.viewholder.AlbumTrackViewHolder
import com.andruid.magic.discodruid.ui.viewholder.TrackViewHolder
import com.andruid.magic.medialoader.model.Track
import kotlinx.coroutines.CoroutineScope

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackViewRepresentation>() {
    override fun areContentsTheSame(
        oldItem: TrackViewRepresentation, newItem: TrackViewRepresentation
    ) = oldItem.track.audioId == newItem.track.audioId

    override fun areItemsTheSame(
        oldItem: TrackViewRepresentation, newItem: TrackViewRepresentation
    ) = oldItem == newItem
}

class TracksAdapter(
    private val context: Context? = null,
    private val scope: CoroutineScope? = null,
    private val viewType: Int = VIEW_TYPE_ALL_TRACKS
) : PagingDataAdapter<TrackViewRepresentation, RecyclerView.ViewHolder>(DIFF_CALLBACK),
    BaseAdapter<Track, Long> {
    var tracker: SelectionTracker<Long>? = null
    var currentTrack: Track? = null
        set(value) {
            prevPosition?.let { notifyItemChanged(it) }
            field = value
        }
    private var prevPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ALBUM_TRACKS -> AlbumTrackViewHolder.from(parent)
            else -> TrackViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { viewRepresentation ->
            val track = viewRepresentation.track
            val selected = tracker?.isSelected(track.audioId) ?: false
            val activated = currentTrack?.audioId == track.audioId
            if (activated)
                prevPosition = position

            if (holder is TrackViewHolder) {
                holder.bind(context!!, scope!!, viewRepresentation, selected, activated)
            } else if (holder is AlbumTrackViewHolder)
                holder.bind(viewRepresentation, activated)
        }
    }

    override fun getItemViewType(position: Int) = viewType

    override fun getItemAtPosition(position: Int) = getItem(position)?.track

    override fun getPosition(key: Long): Int {
        return snapshot().indexOfFirst { it?.track?.audioId == key }
    }

    override fun getKey(position: Int) = getItemAtPosition(position)?.audioId
}
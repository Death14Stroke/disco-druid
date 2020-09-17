package com.andruid.magic.discodruid.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.ui.dragdrop.DragCallback
import com.andruid.magic.discodruid.ui.selection.SelectionAdapter
import com.andruid.magic.discodruid.ui.viewholder.QueueTrackViewHolder
import com.andruid.magic.medialoader.model.Track
import java.util.*

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackViewRepresentation>() {
    override fun areContentsTheSame(
        oldItem: TrackViewRepresentation, newItem: TrackViewRepresentation
    ) = oldItem.track.audioId == newItem.track.audioId

    override fun areItemsTheSame(
        oldItem: TrackViewRepresentation, newItem: TrackViewRepresentation
    ) = oldItem == newItem
}

class QueueTracksAdapter(
    private val dragListener: DragCallback.StartDragListener,
    private val onDragComplete: (fromPosition: Int, toPosition: Int) -> Unit
) :
    ListAdapter<TrackViewRepresentation, QueueTrackViewHolder>(DIFF_CALLBACK),
    DragCallback.IDragDropContract, SelectionAdapter<Track, Long> {
    var currentTrack: Track? = null
        set(value) {
            prevPosition?.let { notifyItemChanged(it) }
            field = value
        }
    private var prevPosition: Int? = null
    private var showHandle = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        QueueTrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: QueueTrackViewHolder, position: Int) {
        getItem(position)?.let { viewRepresentation ->
            val track = viewRepresentation.track
            val selected = false
            val activated = currentTrack?.audioId == track.audioId
            if (activated)
                prevPosition = position

            holder.bind(viewRepresentation, activated, selected, showHandle, dragListener)
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val data = currentList.toMutableList()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition)
                Collections.swap(data, i, i + 1)
        } else {
            for (i in fromPosition downTo toPosition + 1)
                Collections.swap(data, i, i - 1)
        }

        submitList(data)
    }

    override fun onRowSelected(viewHolder: RecyclerView.ViewHolder?) {
        (viewHolder as QueueTrackViewHolder?)?.getRootView()
            ?.setBackgroundResource(R.drawable.card_dragged)
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder?) {
        (viewHolder as QueueTrackViewHolder?)?.getRootView()
            ?.setBackgroundResource(R.drawable.selector_track)
    }

    override fun onDragComplete(fromPosition: Int, toPosition: Int) {
        onDragComplete.invoke(fromPosition, toPosition)
    }

    override fun getItemAtPosition(position: Int) = getItem(position)?.track

    override fun getPosition(key: Long): Int {
        return currentList.indexOfFirst { it?.track?.audioId == key }
    }

    override fun getKey(position: Int) = getItemAtPosition(position)?.audioId

    fun showDragHandles() {
        showHandle = true
        notifyItemRangeChanged(0, currentList.size)
    }

    fun hideDragHandles() {
        showHandle = false
        notifyItemRangeChanged(0, currentList.size)
    }
}
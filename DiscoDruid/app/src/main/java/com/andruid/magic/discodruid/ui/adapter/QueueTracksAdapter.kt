package com.andruid.magic.discodruid.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.LayoutQueueTrackBinding
import com.andruid.magic.discodruid.ui.dragdrop.ItemMoveCallback
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
    private val dragListener: StartDragListener
) :
    ListAdapter<TrackViewRepresentation, QueueTrackViewHolder>(DIFF_CALLBACK),
    ItemMoveCallback.ItemTouchHelperContract, BaseAdapter<Track, Long> {
    var currentTrack: Track? = null
        set(value) {
            prevPosition?.let { notifyItemChanged(it) }
            field = value
        }
    var tracker: SelectionTracker<Long>? = null
    private var prevPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueTrackViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<LayoutQueueTrackBinding>(
            inflater, R.layout.layout_queue_track, parent, false
        )
        return QueueTrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QueueTrackViewHolder, position: Int) {
        getItem(position)?.let { viewRepresentation ->
            val track = viewRepresentation.track
            val selected = tracker?.isSelected(track.audioId) ?: false
            val activated = currentTrack?.audioId == track.audioId
            if (activated)
                prevPosition = position

            holder.bind(viewRepresentation, activated, selected, dragListener)
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        Log.d("dragLog", "onRowMoved from $fromPosition to $toPosition")
        val data = currentList.toMutableList()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition)
                Collections.swap(data, i, i + 1)
        } else {
            for (i in fromPosition downTo toPosition + 1)
                Collections.swap(data, i, i - 1)
        }

        submitList(data)

        //notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: QueueTrackViewHolder) {
        myViewHolder.itemView.setBackgroundColor(Color.GREEN)
    }

    override fun onRowClear(myViewHolder: QueueTrackViewHolder) {
        myViewHolder.itemView.setBackgroundColor(Color.MAGENTA)
    }

    override fun getItemAtPosition(position: Int) = getItem(position)?.track

    override fun getPosition(key: Long): Int {
        return currentList.indexOfFirst { it?.track?.audioId == key }
    }

    override fun getKey(position: Int) = getItemAtPosition(position)?.audioId

    interface StartDragListener {
        fun requestDrag(viewHolder: RecyclerView.ViewHolder)
    }
}
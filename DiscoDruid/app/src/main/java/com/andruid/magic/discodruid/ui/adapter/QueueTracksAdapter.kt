package com.andruid.magic.discodruid.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.LayoutQueueTrackBinding
import com.andruid.magic.discodruid.ui.viewholder.BaseViewHolder
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
    ListAdapter<TrackViewRepresentation, QueueTracksAdapter.QueueTrackViewHolder>(DIFF_CALLBACK),
    BaseAdapter<Track, Long> {
    var tracker: SelectionTracker<Long>? = null
    var currentTrack: Track? = null
        set(value) {
            prevPosition?.let { notifyItemChanged(it) }
            field = value
        }
    private var prevPosition: Int? = null

    init {
        setHasStableIds(true)
    }

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

            holder.bind(viewRepresentation, activated, selected)
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemAtPosition(position: Int) = getItem(position)?.track

    override fun getPosition(key: Long): Int {
        return currentList.indexOfFirst { it?.track?.audioId == key }
    }

    override fun getKey(position: Int) = getItemAtPosition(position)?.audioId

    inner class QueueTrackViewHolder(
        private val binding: LayoutQueueTrackBinding
    ) : RecyclerView.ViewHolder(binding.root), BaseViewHolder {
        fun bind(
            viewRepresentation: TrackViewRepresentation,
            activated: Boolean,
            selected: Boolean
        ) {
            binding.viewRep = viewRepresentation
            //binding.rootLayout.isActivated = activated
            binding.rootLayout.isActivated = selected
            binding.executePendingBindings()
        }

        override fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
            return object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition() = bindingAdapterPosition
                override fun getSelectionKey() = getItemAtPosition(bindingAdapterPosition)?.audioId
            }
        }
    }
}
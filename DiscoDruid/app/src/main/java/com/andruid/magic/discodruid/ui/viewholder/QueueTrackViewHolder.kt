package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.LayoutQueueTrackBinding

class QueueTrackViewHolder(private val binding: LayoutQueueTrackBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): QueueTrackViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutQueueTrackBinding>(
                inflater,
                R.layout.layout_queue_track,
                parent,
                false
            )
            return QueueTrackViewHolder(binding)
        }
    }

    fun bind(viewRepresentation: TrackViewRepresentation, activated: Boolean) {
        binding.viewRep = viewRepresentation
        binding.rootLayout.isActivated = activated
        binding.executePendingBindings()
    }
}
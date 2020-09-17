package com.andruid.magic.discodruid.ui.viewholder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.LayoutQueueTrackBinding
import com.andruid.magic.discodruid.ui.adapter.QueueTracksAdapter
import com.andruid.magic.discodruid.ui.dragdrop.DragCallback
import com.andruid.magic.discodruid.ui.selection.SelectionViewHolder

class QueueTrackViewHolder(
    private val binding: LayoutQueueTrackBinding
) : RecyclerView.ViewHolder(binding.root), SelectionViewHolder {
    companion object {
        fun from(parent: ViewGroup): QueueTrackViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutQueueTrackBinding>(
                inflater, R.layout.layout_queue_track, parent, false
            )
            return QueueTrackViewHolder(binding)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bind(
        viewRepresentation: TrackViewRepresentation,
        activated: Boolean,
        selected: Boolean,
        showHandle: Boolean,
        dragListener: DragCallback.StartDragListener
    ) {
        binding.viewRep = viewRepresentation
        binding.rootLayout.isActivated = activated

        binding.dragHandleView.visibility = if (showHandle) View.VISIBLE else View.GONE

        binding.dragHandleView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN)
                dragListener.requestDrag(this)
            return@setOnTouchListener false
        }

        binding.executePendingBindings()
    }

    override fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
        return object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition() = bindingAdapterPosition

            override fun getSelectionKey() =
                (bindingAdapter as QueueTracksAdapter?)?.getItemAtPosition(bindingAdapterPosition)?.audioId
        }
    }

    fun getRootView() = binding.rootLayout
}
package com.andruid.magic.discodruid.ui.viewholder

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.LayoutQueueTrackBinding
import com.andruid.magic.discodruid.ui.adapter.QueueTracksAdapter

class QueueTrackViewHolder(
    private val binding: LayoutQueueTrackBinding
) : RecyclerView.ViewHolder(binding.root), BaseViewHolder {
    @SuppressLint("ClickableViewAccessibility")
    fun bind(
        viewRepresentation: TrackViewRepresentation,
        activated: Boolean,
        selected: Boolean,
        dragListener: QueueTracksAdapter.StartDragListener
    ) {
        binding.viewRep = viewRepresentation
        //binding.rootLayout.isActivated = activated
        binding.rootLayout.isActivated = selected

        binding.dragHandleView.setOnTouchListener { _, event ->
            if (event.action ==
                MotionEvent.ACTION_DOWN
            ) {
                dragListener.requestDrag(this)
            }
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
}
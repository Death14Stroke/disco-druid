package com.andruid.magic.discodruid.ui.dragdrop

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragCallback(
    private val adapter: IDragDropContract,
    private val dragOnLongPress: Boolean = true
) : ItemTouchHelper.Callback() {
    private var fromPosition = -1
    private var toPosition = -1

    override fun isLongPressDragEnabled() = dragOnLongPress

    override fun isItemViewSwipeEnabled() = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (fromPosition == -1)
            fromPosition = viewHolder.bindingAdapterPosition
        toPosition = target.bindingAdapterPosition

        adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)

        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
            adapter.onRowSelected(viewHolder)
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        if (fromPosition != -1 && toPosition != -1 && fromPosition != toPosition) {
            Log.d("queueLog", "drag from $fromPosition to $toPosition")
            adapter.onDragComplete(fromPosition, toPosition)
            fromPosition = -1
            toPosition = -1
        }

        adapter.onRowClear(viewHolder)
    }

    interface IDragDropContract {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(viewHolder: RecyclerView.ViewHolder?)
        fun onRowClear(viewHolder: RecyclerView.ViewHolder?)
        fun onDragComplete(fromPosition: Int, toPosition: Int)
    }

    interface StartDragListener {
        fun requestDrag(viewHolder: RecyclerView.ViewHolder)
    }
}
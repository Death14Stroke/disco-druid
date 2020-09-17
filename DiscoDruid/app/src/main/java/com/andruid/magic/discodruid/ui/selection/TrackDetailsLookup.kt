package com.andruid.magic.discodruid.ui.selection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class TrackDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        return recyclerView.findChildViewUnder(event.x, event.y)?.let { view ->
            (recyclerView.getChildViewHolder(view) as SelectionViewHolder).getItemDetails()
        }
    }
}
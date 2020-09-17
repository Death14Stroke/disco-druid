package com.andruid.magic.discodruid.ui.selection

import androidx.recyclerview.selection.ItemDetailsLookup

interface SelectionViewHolder {
    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long>?
}
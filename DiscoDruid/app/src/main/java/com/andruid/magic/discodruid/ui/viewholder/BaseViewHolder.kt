package com.andruid.magic.discodruid.ui.viewholder

import androidx.recyclerview.selection.ItemDetailsLookup

interface BaseViewHolder {
    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long>?
}
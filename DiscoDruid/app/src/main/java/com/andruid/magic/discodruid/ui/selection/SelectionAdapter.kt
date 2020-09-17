package com.andruid.magic.discodruid.ui.selection

interface SelectionAdapter<T, K> {
    fun getItemAtPosition(position: Int): T?
    fun getPosition(key: K): Int = -1
    fun getKey(position: Int): K? = null
}
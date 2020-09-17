package com.andruid.magic.discodruid.ui.selection

import androidx.recyclerview.selection.ItemKeyProvider
import com.andruid.magic.medialoader.model.Track

class TrackKeyProvider(
    private val adapter: SelectionAdapter<Track, Long>
) : ItemKeyProvider<Long>(SCOPE_MAPPED) {
    override fun getKey(position: Int) = adapter.getKey(position)

    override fun getPosition(key: Long) =
        adapter.getPosition(key)
}
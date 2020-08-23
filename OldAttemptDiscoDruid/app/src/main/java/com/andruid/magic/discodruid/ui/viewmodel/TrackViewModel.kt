package com.andruid.magic.discodruid.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.discodruid.paging.track.TrackDataSourceFactory
import com.andruid.magic.medialoader.model.Track

class TrackViewModel : ViewModel() {
    companion object {
        private const val TRACK_PAGE_SIZE = 10
    }

    val tracksLiveData: LiveData<PagedList<Track>>

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(TRACK_PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()
        tracksLiveData = LivePagedListBuilder<Int, Track>(TrackDataSourceFactory(), config)
            .build()
    }
}
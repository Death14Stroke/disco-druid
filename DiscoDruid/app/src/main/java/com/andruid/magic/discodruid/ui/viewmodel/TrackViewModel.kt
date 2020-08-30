package com.andruid.magic.discodruid.ui.viewmodel

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.andruid.magic.discodruid.data.PAGE_SIZE
import com.andruid.magic.discodruid.paging.TracksPagingSource
import kotlinx.coroutines.cancel

class TrackViewModel(
    private val mediaBrowserCompat: MediaBrowserCompat,
    private val options: Bundle? = null
) : ViewModel() {
    val tracksLiveData = liveData {
        val config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false)
        val pager = Pager(config) {
            TracksPagingSource(mediaBrowserCompat, options)
        }

        emitSource(
            pager.flow.cachedIn(viewModelScope)
                .asLiveData()
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
package com.andruid.magic.discodruid.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.andruid.magic.discodruid.data.PAGE_SIZE
import com.andruid.magic.discodruid.paging.AlbumsPagingSource
import kotlinx.coroutines.cancel

class AlbumViewModel(private val mediaBrowserCompat: MediaBrowserCompat) : ViewModel() {
    val albumsLiveData = liveData {
        val config = PagingConfig(PAGE_SIZE)
        val pager = Pager(config) {
            AlbumsPagingSource(mediaBrowserCompat)
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
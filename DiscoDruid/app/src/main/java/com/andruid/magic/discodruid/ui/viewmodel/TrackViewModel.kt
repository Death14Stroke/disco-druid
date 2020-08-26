package com.andruid.magic.discodruid.ui.viewmodel

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

class TrackViewModel(private val albumId: String? = null) : ViewModel() {
    val tracksLiveData = liveData {
        val config = PagingConfig(PAGE_SIZE)
        val pager = Pager(config) {
            TracksPagingSource(albumId)
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
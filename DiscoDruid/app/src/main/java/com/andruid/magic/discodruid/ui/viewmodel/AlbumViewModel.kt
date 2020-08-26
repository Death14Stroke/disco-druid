package com.andruid.magic.discodruid.ui.viewmodel

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.andruid.magic.discodruid.data.PAGE_SIZE
import com.andruid.magic.discodruid.event.Event
import com.andruid.magic.discodruid.paging.AlbumsPagingSource
import com.andruid.magic.medialoader.model.Album
import kotlinx.coroutines.cancel

class AlbumViewModel : ViewModel() {
    val albumsLiveData = liveData {
        val config = PagingConfig(PAGE_SIZE)
        val pager = Pager(config) {
            AlbumsPagingSource()
        }

        emitSource(
            pager.flow.cachedIn(viewModelScope)
                .asLiveData()
        )
    }

    private val _clickEvent = MutableLiveData<Event<Album>>()
    val clickEvent: LiveData<Event<Album>>
        get() = _clickEvent

    fun sendOpenAlbumDetailsEvent(album: Album) {
        _clickEvent.value = Event(album)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
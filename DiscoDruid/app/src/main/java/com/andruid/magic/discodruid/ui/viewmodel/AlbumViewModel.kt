package com.andruid.magic.discodruid.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.discodruid.paging.album.AlbumDataSourceFactory
import com.andruid.magic.medialoader.model.Album

class AlbumViewModel : ViewModel() {
    companion object {
        private const val ALBUM_PAGE_SIZE = 10
    }

    val albumsLiveData: LiveData<PagedList<Album>>

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(ALBUM_PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()
        albumsLiveData = LivePagedListBuilder<Int, Album>(AlbumDataSourceFactory(), config)
            .build()
    }
}
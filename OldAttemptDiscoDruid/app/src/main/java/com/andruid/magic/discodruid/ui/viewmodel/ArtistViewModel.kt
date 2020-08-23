package com.andruid.magic.discodruid.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.discodruid.paging.artist.ArtistDataSourceFactory
import com.andruid.magic.medialoader.model.Artist

class ArtistViewModel : ViewModel() {
    companion object {
        private const val ARTIST_PAGE_SIZE = 10
    }

    val artistsLiveData: LiveData<PagedList<Artist>>

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(ARTIST_PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()
        artistsLiveData = LivePagedListBuilder<Int, Artist>(ArtistDataSourceFactory(), config)
            .build()
    }
}
package com.andruid.magic.discodruid.ui.viewmodel

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.andruid.magic.discodruid.data.PAGE_SIZE
import com.andruid.magic.discodruid.data.model.UiModel
import com.andruid.magic.discodruid.paging.TracksPagingSource
import com.andruid.magic.medialoader.repository.ArtistRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map

class ArtistTracksViewModel(
    private val mediaBrowserCompat: MediaBrowserCompat,
    private val options: Bundle? = null
) : ViewModel() {
    val tracksLiveData = liveData {
        val config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false)
        val pager = Pager(config) {
            TracksPagingSource(mediaBrowserCompat, options)
        }

        val flow = pager.flow.map { pagingData ->
            pagingData.map { track -> UiModel.TrackModel(track) }
                .insertSeparators { before, after ->
                    when {
                        after == null -> null
                        before == null || after.track.albumId != before.track.albumId -> {
                            ArtistRepository.getAlbumForArtist(after.track.artistId, after.track.album)?.let { album ->
                                UiModel.AlbumSeparatorModel(album.copy(albumId = after.track.albumId))
                            }
                        }
                        else -> null
                    }
                }
        }

        emitSource(
            flow.cachedIn(viewModelScope)
                .asLiveData()
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
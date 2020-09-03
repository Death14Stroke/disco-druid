package com.andruid.magic.discodruid.ui.viewmodel

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.andruid.magic.discodruid.data.PAGE_SIZE
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
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
            pagingData.map { track -> UiModel.TrackModel(TrackViewRepresentation.fromTrack(track)) }
                .insertSeparators { before, after ->
                    val beforeTrack = before?.viewRepresentation?.track
                    val afterTrack = after?.viewRepresentation?.track
                    when {
                        afterTrack == null -> null
                        beforeTrack == null || afterTrack.albumId != beforeTrack.albumId -> {
                            ArtistRepository.getAlbumForArtist(
                                afterTrack.artistId,
                                afterTrack.album
                            )?.let { album ->
                                UiModel.AlbumSeparatorModel(album.copy(albumId = afterTrack.albumId))
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
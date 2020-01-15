package com.andruid.magic.discodruid.paging.artist

import androidx.paging.PositionalDataSource
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.repository.ArtistRepository.Companion.getInstance

class ArtistDataSource : PositionalDataSource<Artist>() {
    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Artist>) =
        callback.onResult(getInstance().getArtists(params.requestedLoadSize, params.requestedStartPosition),
            0)

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Artist>) =
        callback.onResult(getInstance().getArtists(params.loadSize, params.startPosition))
}
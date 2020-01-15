package com.andruid.magic.discodruid.paging.track

import androidx.paging.PositionalDataSource
import com.andruid.magic.medialoader.model.Track
import com.andruid.magic.medialoader.repository.TrackRepository.Companion.getInstance

class TrackDataSource : PositionalDataSource<Track>() {
    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Track>) =
        callback.onResult(getInstance().getTracks(params.requestedLoadSize, params.requestedStartPosition),
            0)

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Track>) =
        callback.onResult(getInstance().getTracks(params.loadSize, params.startPosition))
}
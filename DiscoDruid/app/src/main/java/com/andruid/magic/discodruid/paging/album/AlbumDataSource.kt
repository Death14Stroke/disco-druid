package com.andruid.magic.discodruid.paging.album

import androidx.paging.PositionalDataSource
import com.andruid.magic.medialoader.model.Album
import com.andruid.magic.medialoader.repository.AlbumRepository.Companion.getInstance

class AlbumDataSource : PositionalDataSource<Album>() {
    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Album>) =
        callback.onResult(getInstance().getAlbums(params.requestedLoadSize, params.requestedStartPosition),
            0)

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Album>) =
        callback.onResult(getInstance().getAlbums(params.loadSize, params.startPosition))
}
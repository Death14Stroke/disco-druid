package com.andruid.magic.discodruid.paging

import androidx.paging.PagingSource
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.repository.ArtistRepository

class ArtistsPagingSource : PagingSource<Int, Artist>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Artist> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        val result = ArtistRepository.getAllContent(pageSize, page * pageSize)
        return LoadResult.Page(
            data = result,
            prevKey = null,
            nextKey = if (result.isNotEmpty()) page + 1 else null
        )
    }
}
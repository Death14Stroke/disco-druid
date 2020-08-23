package com.andruid.magic.discodruid.paging

import androidx.paging.PagingSource
import com.andruid.magic.medialoader.model.Track
import com.andruid.magic.medialoader.repository.TrackRepository

class TracksPagingSource : PagingSource<Int, Track>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
        val page = params.key ?: 1
        val pageSize = params.loadSize

        val result = TrackRepository.getTracks(pageSize, page * pageSize)
        return LoadResult.Page(
            data = result,
            prevKey = null,
            nextKey = if (result.isNotEmpty()) page + 1 else null
        )
    }
}
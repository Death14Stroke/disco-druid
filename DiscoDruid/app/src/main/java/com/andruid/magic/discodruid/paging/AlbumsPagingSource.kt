package com.andruid.magic.discodruid.paging

import androidx.paging.PagingSource
import com.andruid.magic.medialoader.model.Album
import com.andruid.magic.medialoader.repository.AlbumRepository

class AlbumsPagingSource : PagingSource<Int, Album>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Album> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        val result = AlbumRepository.getAllContent(pageSize, page * pageSize)
        return LoadResult.Page(
            data = result,
            prevKey = null,
            nextKey = if (result.isNotEmpty()) page + 1 else null
        )
    }
}
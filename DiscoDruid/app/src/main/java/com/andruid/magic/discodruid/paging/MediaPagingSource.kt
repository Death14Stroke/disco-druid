package com.andruid.magic.discodruid.paging

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.core.os.bundleOf
import androidx.paging.PagingSource
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class MediaPagingSource<T : Any>(
    private val mediaBrowserCompat: MediaBrowserCompat
) : PagingSource<Int, T>() {
    abstract val loadType: String
    abstract val onMediaItem: (page: Int, children: MutableList<MediaBrowserCompat.MediaItem>) -> LoadResult.Page<Int, T>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 0
        val pageSize = params.loadSize
        val parentId = getParentId(page)
        val extras = getExtrasForPage(page, pageSize)
        lateinit var result: Continuation<LoadResult<Int, T>>

        mediaBrowserCompat.subscribe(
            parentId,
            extras,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>,
                    options: Bundle
                ) {
                    val loadResult = onMediaItem.invoke(page, children)
                    result.resume(loadResult)
                }
            })

        return suspendCoroutine { continuation -> result = continuation }

    }

    private fun getParentId(offset: Int): String {
        val rootId = mediaBrowserCompat.root
        return "${rootId}_${loadType}_$offset"
    }

    private fun getExtrasForPage(page: Int, pageSize: Int): Bundle {
        return bundleOf(
            MediaBrowserCompat.EXTRA_PAGE to page,
            MediaBrowserCompat.EXTRA_PAGE_SIZE to pageSize
        )
    }
}
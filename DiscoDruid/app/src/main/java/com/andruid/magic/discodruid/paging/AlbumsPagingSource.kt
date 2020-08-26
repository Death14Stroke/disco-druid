package com.andruid.magic.discodruid.paging

import android.support.v4.media.MediaBrowserCompat
import com.andruid.magic.discodruid.data.LOAD_ALBUM
import com.andruid.magic.discodruid.util.toAlbum
import com.andruid.magic.medialoader.model.Album

class AlbumsPagingSource(mediaBrowserCompat: MediaBrowserCompat) :
    MediaPagingSource<Album>(mediaBrowserCompat) {
    override val loadType: String
        get() = LOAD_ALBUM

    override val onMediaItem = { page: Int, children: MutableList<MediaBrowserCompat.MediaItem> ->
        val result = children.mapNotNull { mediaItem -> mediaItem.toAlbum() }
        LoadResult.Page(
            data = result,
            prevKey = null,
            nextKey = if (result.isNotEmpty()) page + 1 else null
        )
    }
}
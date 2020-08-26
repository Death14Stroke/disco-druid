package com.andruid.magic.discodruid.paging

import android.support.v4.media.MediaBrowserCompat
import com.andruid.magic.discodruid.data.LOAD_ARTIST
import com.andruid.magic.discodruid.util.toArtist
import com.andruid.magic.medialoader.model.Artist

class ArtistsPagingSource(mediaBrowserCompat: MediaBrowserCompat) :
    MediaPagingSource<Artist>(mediaBrowserCompat) {
    override val loadType: String
        get() = LOAD_ARTIST

    override val onMediaItem = { page: Int, children: MutableList<MediaBrowserCompat.MediaItem> ->
        val result = children.mapNotNull { mediaItem -> mediaItem.toArtist() }
        LoadResult.Page(
            data = result,
            prevKey = null,
            nextKey = if (result.isNotEmpty()) page + 1 else null
        )
    }

}
package com.andruid.magic.discodruid.paging

import android.support.v4.media.MediaBrowserCompat
import com.andruid.magic.discodruid.data.MB_LOAD_ALBUM
import com.andruid.magic.discodruid.util.toAlbum
import com.andruid.magic.medialoader.model.Album

class AlbumsPagingSource(mediaBrowserCompat: MediaBrowserCompat) :
    MediaPagingSource<Album>(mediaBrowserCompat) {
    override val loadType: String
        get() = MB_LOAD_ALBUM

    override val mediaItemConverter = { mediaItem: MediaBrowserCompat.MediaItem ->
        mediaItem.toAlbum()
    }
}
package com.andruid.magic.discodruid.paging

import android.support.v4.media.MediaBrowserCompat
import com.andruid.magic.discodruid.data.MB_LOAD_ARTIST
import com.andruid.magic.discodruid.util.toArtist
import com.andruid.magic.medialoader.model.Artist

class ArtistsPagingSource(mediaBrowserCompat: MediaBrowserCompat) :
    MediaPagingSource<Artist>(mediaBrowserCompat) {
    override val loadType: String
        get() = MB_LOAD_ARTIST

    override val mediaItemConverter = { mediaItem: MediaBrowserCompat.MediaItem ->
        mediaItem.toArtist()
    }
}
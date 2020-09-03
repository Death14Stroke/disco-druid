package com.andruid.magic.discodruid.paging

import android.support.v4.media.MediaBrowserCompat
import com.andruid.magic.discodruid.data.MB_LOAD_PLAYLIST
import com.andruid.magic.discodruid.util.toPlaylist
import com.andruid.magic.medialoader.model.Playlist

class PlaylistsPagingSource(mediaBrowserCompat: MediaBrowserCompat) :
    MediaPagingSource<Playlist>(mediaBrowserCompat) {
    override val loadType: String
        get() = MB_LOAD_PLAYLIST

    override val mediaItemConverter = { mediaItem: MediaBrowserCompat.MediaItem ->
        mediaItem.toPlaylist()
    }
}
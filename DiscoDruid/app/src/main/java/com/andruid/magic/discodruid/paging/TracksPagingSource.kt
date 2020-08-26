package com.andruid.magic.discodruid.paging

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.core.os.bundleOf
import com.andruid.magic.discodruid.data.LOAD_TRACK
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Track

class TracksPagingSource(
    mediaBrowserCompat: MediaBrowserCompat, options: Bundle?
) : MediaPagingSource<Track>(mediaBrowserCompat, options) {
    override val loadType: String
        get() = LOAD_TRACK

    override val mediaItemConverter = { mediaItem: MediaBrowserCompat.MediaItem ->
        mediaItem.toTrack()
    }
}
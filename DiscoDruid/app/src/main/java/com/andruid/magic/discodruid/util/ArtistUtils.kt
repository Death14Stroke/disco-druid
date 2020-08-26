package com.andruid.magic.discodruid.util

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.core.os.bundleOf
import com.andruid.magic.discodruid.data.EXTRA_ARTIST
import com.andruid.magic.medialoader.model.Artist

fun Artist.toMediaItem(): MediaBrowserCompat.MediaItem {
    val extras = bundleOf(EXTRA_ARTIST to this)
    val mediaDescriptionCompat = MediaDescriptionCompat.Builder()
        .setMediaId(artistId)
        .setTitle(artist)
        .setExtras(extras)
        .build()
    return MediaBrowserCompat.MediaItem(
        mediaDescriptionCompat,
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )
}

fun MediaBrowserCompat.MediaItem.toArtist(): Artist? =
    description.extras?.getParcelable(EXTRA_ARTIST)
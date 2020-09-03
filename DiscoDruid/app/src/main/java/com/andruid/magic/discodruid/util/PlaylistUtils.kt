package com.andruid.magic.discodruid.util

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.core.os.bundleOf
import com.andruid.magic.discodruid.data.EXTRA_ARTIST
import com.andruid.magic.discodruid.data.EXTRA_PLAYLIST
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.model.Playlist

fun Playlist.toMediaItem(): MediaBrowserCompat.MediaItem {
    val extras = bundleOf(EXTRA_PLAYLIST to this)
    val mediaDescriptionCompat = MediaDescriptionCompat.Builder()
        .setMediaId(playlistId.toString())
        .setTitle(name)
        .setExtras(extras)
        .build()

    return MediaBrowserCompat.MediaItem(
        mediaDescriptionCompat,
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )
}

fun MediaBrowserCompat.MediaItem.toPlaylist(): Playlist? =
    description.extras?.getParcelable(EXTRA_PLAYLIST)
package com.andruid.magic.discodruid.util

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.os.bundleOf
import com.andruid.magic.discodruid.data.EXTRA_TRACK
import com.andruid.magic.medialoader.model.Track
import com.andruid.magic.medialoader.repository.AlbumRepository

fun Track.buildMediaDescription(context: Context): MediaDescriptionCompat {
    val bitmap = context.getAlbumArtBitmap(albumId)
    val extras = bundleOf(
        MediaMetadataCompat.METADATA_KEY_ALBUM_ART to bitmap,
        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON to bitmap
    )

    return MediaDescriptionCompat.Builder()
        .setMediaId(path)
        .setIconBitmap(bitmap)
        .setTitle(title)
        .setDescription(album)
        .setExtras(extras)
        .build()
}

fun Track.buildMediaMetaData(context: Context): MediaMetadataCompat {
    val bitmap = context.getAlbumArtBitmap(albumId)
    return MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumId)
        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, audioId)
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        .putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
            AlbumRepository.getAlbumArtUri(albumId)?.path
        )
        .build()
}

fun Track.toMediaItem(): MediaBrowserCompat.MediaItem {
    val extras = bundleOf(EXTRA_TRACK to this)
    val mediaDescriptionCompat = MediaDescriptionCompat.Builder()
        .setMediaId(path)
        .setTitle(title)
        .setExtras(extras)
        .build()
    return MediaBrowserCompat.MediaItem(
        mediaDescriptionCompat,
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )
}

fun MediaBrowserCompat.MediaItem.toTrack(): Track? =
    description.extras?.getParcelable(EXTRA_TRACK)

fun MediaMetadataCompat.toTrack(): Track {
    return Track(
        audioId = getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER),
        title = getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "Loading",
        album = getString(MediaMetadataCompat.METADATA_KEY_ALBUM) ?: "Loading",
        artist = getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: "Loading",
        albumId = getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) ?: "-1",
        duration = getLong(MediaMetadataCompat.METADATA_KEY_DURATION),
        path = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI) ?: ""
    )
}
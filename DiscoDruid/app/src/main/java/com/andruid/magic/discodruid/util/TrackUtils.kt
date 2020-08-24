package com.andruid.magic.discodruid.util

import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.os.bundleOf
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
    val builder = MediaMetadataCompat.Builder()

    context.getAlbumArtBitmap(albumId)?.let { bitmap ->
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            .putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                AlbumRepository.getAlbumArtUri(albumId)
            )
    }

    builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, audioId)
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)

    return builder.build()
}
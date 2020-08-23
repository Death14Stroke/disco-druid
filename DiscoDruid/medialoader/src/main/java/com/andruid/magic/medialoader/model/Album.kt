package com.andruid.magic.medialoader.model

import android.database.Cursor
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(
    val albumId: String,
    val album: String = "Unknown",
    val artist: String = "Unknown",
    val songsCount: Int = 0
) : Parcelable

fun Cursor.readAlbum(): Album {
    return Album(
        albumId = getString(getColumnIndex(MediaStore.Audio.Albums._ID)),
        album = getString(getColumnIndex(MediaStore.Audio.Albums.ALBUM)),
        artist = getString(getColumnIndex(MediaStore.Audio.Albums.ARTIST)),
        songsCount = getInt(getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
    )
}
package com.andruid.magic.medialoader.model

import android.database.Cursor
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Artist(
    val artistId: String,
    val artist: String,
    val tracksCount: Int = 0,
    val albumsCount: Int = 0
) : Parcelable

fun Cursor.readArtist(): Artist {
    return Artist(
        artistId = getString(getColumnIndex(MediaStore.Audio.Artists._ID)),
        artist = getString(getColumnIndex(MediaStore.Audio.Artists.ARTIST)),
        albumsCount = getInt(getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)),
        tracksCount = getInt(getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
    )
}
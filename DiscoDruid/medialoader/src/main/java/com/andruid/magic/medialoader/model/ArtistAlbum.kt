package com.andruid.magic.medialoader.model

import android.annotation.SuppressLint
import android.database.Cursor
import android.provider.MediaStore

data class ArtistAlbum(
    val albumId: String,
    val album: String,
    val artistId: String,
    val artist: String,
    val songsCount: Int
)

@SuppressLint("InlinedApi")
fun Cursor.readArtistAlbum(): ArtistAlbum {
    return ArtistAlbum(
        albumId = "getString(getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM_ID))",
        album = getString(getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM)),
        artistId = getString(getColumnIndex(MediaStore.Audio.Artists.Albums.ARTIST_ID)),
        artist = getString(getColumnIndex(MediaStore.Audio.Artists.Albums.ARTIST)),
        songsCount = getInt(getColumnIndex(MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS_FOR_ARTIST))
    )
}
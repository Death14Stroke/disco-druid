package com.andruid.magic.medialoader.util

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.andruid.magic.medialoader.model.Album
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.model.Track

@SuppressLint("InlinedApi")
fun Cursor.getTrack(): Track {
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val trackId = getLong(getColumnIndex(MediaStore.Audio.Media._ID))
    return Track(
        audioId = trackId,
        artist = getString(getColumnIndex(MediaStore.Audio.Media.ARTIST)),
        title = getString(getColumnIndex(MediaStore.Audio.Media.TITLE)),
        path = Uri.withAppendedPath(uri, "$trackId").path!!,
        duration = getLong(getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000,
        albumId = getString(getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
        album = getString(getColumnIndex(MediaStore.Audio.Media.ALBUM))
    )
}

fun Cursor.getAlbum(): Album {
    return Album(
        albumId = getString(getColumnIndex(MediaStore.Audio.Albums._ID)),
        album = getString(getColumnIndex(MediaStore.Audio.Albums.ALBUM)),
        artist = getString(getColumnIndex(MediaStore.Audio.Albums.ARTIST)),
        songsCount = getInt(getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
    )
}

fun Cursor.getArtist(): Artist {
    return Artist(
        artistId = getString(getColumnIndex(MediaStore.Audio.Artists._ID)),
        artist = getString(getColumnIndex(MediaStore.Audio.Artists.ARTIST)),
        albumsCount = getInt(getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)),
        tracksCount = getInt(getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
    )
}
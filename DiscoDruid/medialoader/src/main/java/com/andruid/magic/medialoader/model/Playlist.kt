package com.andruid.magic.medialoader.model

import android.database.Cursor
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Playlist(
    val name: String,
    val playlistId: Long,
    val dateCreated: Long,
    val songsCount: Int = 0
) : Parcelable

fun Cursor.readPlaylist(): Playlist {
    return Playlist(
        name = getString(getColumnIndex(MediaStore.Audio.Playlists.NAME)),
        playlistId = getLong(getColumnIndex(MediaStore.Audio.Playlists._ID)),
        dateCreated = getLong(getColumnIndex(MediaStore.Audio.Playlists.DATE_ADDED))
    )
}
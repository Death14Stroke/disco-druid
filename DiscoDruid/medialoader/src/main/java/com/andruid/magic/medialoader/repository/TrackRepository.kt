package com.andruid.magic.medialoader.repository

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Track
import com.andruid.magic.medialoader.model.readTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TrackRepository : MediaRepository<Track>() {
    override val uri: Uri
        get() = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    override val projection: Array<String>
        @SuppressLint("InlinedApi")
        get() = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
        )
    override val baseSelection: String?
        get() = "${MediaStore.Audio.Media.IS_MUSIC} != 0" +
                " AND ${MediaStore.Audio.Media.IS_ALARM} == 0 AND ${MediaStore.Audio.Media.IS_NOTIFICATION} == 0" +
                " AND ${MediaStore.Audio.Media.IS_PODCAST} == 0 AND ${MediaStore.Audio.Media.IS_RINGTONE} == 0"

    private var baseSortOrder: String = "${MediaStore.Audio.Media.TITLE} ASC"

    override fun getSortOrder(limit: Int, offset: Int) =
        "$baseSortOrder LIMIT $offset, $limit"

    override suspend fun fetchUtil(
        selection: String?,
        selectionArgs: Array<String>?,
        limit: Int,
        offset: Int
    ): List<Track> {
        val tracks = mutableListOf<Track>()
        val sortOrder = if (limit == Int.MAX_VALUE)
            "${MediaStore.Audio.Media.TITLE} ASC"
        else
            getSortOrder(limit, offset)
        return withContext(Dispatchers.IO) {
            val query = ContentResolverCompat.query(
                contentResolver,
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder,
                null
            )

            query?.use { cursor ->
                while (cursor.moveToNext())
                    tracks.add(cursor.readTrack())
            }

            tracks.toList()
        }
    }

    override suspend fun getAllPagedContent(limit: Int, offset: Int): List<Track> {
        baseSortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        return fetchUtil(baseSelection, null, limit, offset)
    }

    suspend fun getTracksForAlbum(
        albumId: String,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<Track> {
        val selection = "$baseSelection AND ${MediaStore.Audio.Media.ALBUM_ID} = ?"
        baseSortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        return fetchUtil(selection, arrayOf(albumId), limit, offset)
    }

    suspend fun getTracksForArtist(
        artistId: String,
        artist: String,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<Track> {
        val selection =
            "$baseSelection AND ${MediaStore.Audio.Media.ARTIST_ID} = ? AND ${MediaStore.Audio.Media.ARTIST} = ?"
        baseSortOrder = "${MediaStore.Audio.Media.ALBUM}, ${MediaStore.Audio.Media.TITLE} ASC"
        return fetchUtil(selection, arrayOf(artistId, artist), limit, offset)
    }
}
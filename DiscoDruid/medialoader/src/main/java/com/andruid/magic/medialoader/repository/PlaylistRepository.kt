package com.andruid.magic.medialoader.repository

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContentResolverCompat
import androidx.core.database.getLongOrNull
import com.andruid.magic.medialoader.model.Playlist
import com.andruid.magic.medialoader.model.readPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object PlaylistRepository : MediaRepository<Playlist>() {
    override val projection: Array<String>
        get() = arrayOf(
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME,
            MediaStore.Audio.Playlists.DATE_ADDED
        )
    override val uri: Uri
        get() = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
    override val baseSelection: String?
        get() = null

    override fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER} LIMIT $limit OFFSET $offset"

    override suspend fun fetchUtil(
        selection: String?,
        selectionArgs: Array<String>?,
        limit: Int,
        offset: Int
    ): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        return withContext(Dispatchers.IO) {
            val query = ContentResolverCompat.query(
                contentResolver,
                uri,
                projection,
                null,
                null,
                getSortOrder(limit, offset),
                null
            )

            query?.use { cursor ->
                while (cursor.moveToNext()) {
                    var playlist = cursor.readPlaylist()
                    playlist = playlist.copy(songsCount = getPlaylistSongsCount(playlist))
                    playlists.add(playlist)
                }
            }

            playlists.toList()
        }
    }

    override suspend fun getAllPagedContent(limit: Int, offset: Int): List<Playlist> {
        return fetchUtil(baseSelection, null, limit, offset)
    }

    private suspend fun getPlaylistSongsCount(playlist: Playlist): Int {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.playlistId)
        val projection = arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID)

        return withContext(Dispatchers.IO) {
            val query =
                ContentResolverCompat.query(
                    contentResolver,
                    uri,
                    projection,
                    null,
                    null,
                    null,
                    null
                )

            query?.use { cursor ->
                cursor.count
            } ?: 0
        }
    }

    suspend fun getPlaylistId(name: String): Long {
        val projection = arrayOf(MediaStore.Audio.Playlists._ID)
        val selection = "${MediaStore.Audio.Playlists.NAME} = ?"

        return withContext(Dispatchers.IO) {
            val query =
                ContentResolverCompat.query(
                    contentResolver,
                    uri,
                    projection,
                    selection,
                    arrayOf(name),
                    null,
                    null
                )

            query?.use { cursor ->
                if (cursor.moveToFirst())
                    cursor.getLongOrNull(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID))
                else
                    0L
            } ?: 0L
        }
    }

    suspend fun createPlaylist(name: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Playlists.NAME, name)
            put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis())
        }

        try {
            withContext(Dispatchers.IO) {
                contentResolver.insert(uri, values)
            }
        } catch (e: Exception) {
            Log.e("playlistLog", "Could not create playlist", e)
            e.printStackTrace()
        }
    }

    suspend fun addTracksToPlaylist(playlistId: Long, vararg trackIds: Long) {
        val values = trackIds.mapIndexed { index, trackId ->
            ContentValues().apply {
                put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, index)
                put(MediaStore.Audio.Playlists.Members.AUDIO_ID, trackId)
            }
        }.toTypedArray()

        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        try {
            withContext(Dispatchers.IO) {
                contentResolver.bulkInsert(uri, values)
            }
        } catch (e: Exception) {
            Log.e("playlistLog", "Could not add tracks to playlist", e)
            e.printStackTrace()
        }
    }

    suspend fun clearPlaylist(playlistId: Long) {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)

        try {
            withContext(Dispatchers.IO) {
                contentResolver.delete(uri, null, null)
            }
        } catch (e: Exception) {
            Log.e("playlistLog", "Could not delete playlist", e)
            e.printStackTrace()
        }
    }
}
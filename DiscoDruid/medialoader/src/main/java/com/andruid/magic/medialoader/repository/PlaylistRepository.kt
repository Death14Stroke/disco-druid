package com.andruid.magic.medialoader.repository

import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
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
        val projection = arrayOf(MediaStore.Audio.Playlists.Members._COUNT)

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
                if (cursor.moveToFirst())
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members._COUNT))
                else
                    0
            } ?: 0
        }
    }
}
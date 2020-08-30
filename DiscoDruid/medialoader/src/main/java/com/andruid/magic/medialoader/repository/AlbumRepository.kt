package com.andruid.magic.medialoader.repository

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Album
import com.andruid.magic.medialoader.model.readAlbum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AlbumRepository : MediaRepository<Album>() {
    override val uri: Uri
        get() = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    override val projection: Array<String>
        get() = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )
    override val baseSelection: String?
        get() = null

    override fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Albums.ALBUM} ASC LIMIT $limit OFFSET $offset"

    override suspend fun fetchUtil(
        selection: String?,
        selectionArgs: Array<String>?,
        limit: Int,
        offset: Int
    ): List<Album> {
        val albums = mutableListOf<Album>()
        return withContext(Dispatchers.IO) {
            val query = ContentResolverCompat.query(
                contentResolver,
                uri,
                projection,
                selection,
                selectionArgs,
                getSortOrder(limit, offset),
                null
            )
            query?.use { cursor ->
                while (cursor.moveToNext())
                    albums.add(cursor.readAlbum())
            }

            albums.toList()
        }
    }

    override suspend fun getAllPagedContent(limit: Int, offset: Int): List<Album> {
        return fetchUtil(baseSelection, null, limit, offset)
    }

    fun getAlbumArtUri(albumId: String): Uri? {
        return try {
            ContentUris.withAppendedId(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumId.toLong()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
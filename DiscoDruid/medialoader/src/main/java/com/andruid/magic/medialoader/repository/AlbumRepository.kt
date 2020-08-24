package com.andruid.magic.medialoader.repository

import android.app.Application
import android.content.ContentResolver
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Album
import com.andruid.magic.medialoader.model.readAlbum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AlbumRepository {
    private val projection = arrayOf(
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Albums.ARTIST,
        MediaStore.Audio.Albums.NUMBER_OF_SONGS
    )

    private lateinit var contentResolver: ContentResolver

    fun init(application: Application) {
        contentResolver = application.contentResolver
    }

    //@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    suspend fun getAlbums(limit: Int, offset: Int): List<Album> {
        val albums = mutableListOf<Album>()
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

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
                while (cursor.moveToNext())
                    albums.add(cursor.readAlbum())
            }

            albums.toList()
        }
    }

    //@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getAlbumArtUri(albumId: String): String? {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        val selection = "${MediaStore.Audio.Albums._ID}=?"

        val query = ContentResolverCompat.query(
            contentResolver, uri, projection, selection,
            arrayOf(albumId), null, null
        )

        return query?.use { cursor ->
            if (cursor.moveToFirst())
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
            else
                null
        }
    }

    private fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Albums.ALBUM} ASC LIMIT $limit OFFSET $offset"
}
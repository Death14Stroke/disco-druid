package com.andruid.magic.medialoader.repository

import android.app.Application
import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Album
import com.andruid.magic.medialoader.util.getAlbum
import java.util.jar.Manifest

class AlbumRepository {
    companion object {
        private val TAG = AlbumRepository::class.java.simpleName

        private lateinit var INSTANCE: AlbumRepository
        private lateinit var contentResolver: ContentResolver
        private val LOCK = Any()

        @JvmStatic
        fun init(application: Application) {
            contentResolver = application.contentResolver
        }

        @JvmStatic
        fun getInstance(): AlbumRepository {
            if(!::contentResolver.isInitialized)
                throw Exception("must call init() first in Application class")
            if (!::INSTANCE.isInitialized) {
                synchronized(LOCK) {
                    Log.d(TAG, "Created album repository instance")
                    INSTANCE = AlbumRepository()
                }
            }
            return INSTANCE
        }
    }

    @RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getAlbums(limit: Int, offset: Int): List<Album> {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )
        val cursor = ContentResolverCompat.query(contentResolver, uri, projection, null,
            null, getSortOrder(limit, offset), null)
        cursor.moveToFirst()
        val albums = mutableListOf<Album>()
        while(!cursor.isAfterLast) {
            albums.add(cursor.getAlbum())
            cursor.moveToNext()
        }
        cursor.close()
        return albums.toList()
    }

    private fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Albums.ALBUM} ASC LIMIT $limit OFFSET $offset"
}
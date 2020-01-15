package com.andruid.magic.medialoader.repository

import android.app.Application
import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.util.getArtist

class ArtistRepository {
    companion object {
        private val TAG = ArtistRepository::class.java.simpleName

        private lateinit var INSTANCE: ArtistRepository
        private lateinit var contentResolver: ContentResolver
        private val LOCK = Any()

        @JvmStatic
        fun init(application: Application) {
            contentResolver = application.contentResolver
        }

        @JvmStatic
        fun getInstance(): ArtistRepository {
            if(!::contentResolver.isInitialized)
                throw Exception("must call init() first in Application class")
            if (!::INSTANCE.isInitialized) {
                synchronized(LOCK) {
                    Log.d(TAG, "Created artist repository instance")
                    INSTANCE = ArtistRepository()
                }
            }
            return INSTANCE
        }
    }

    fun getArtists(limit: Int, offset: Int): List<Artist> {
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )
        val cursor = ContentResolverCompat.query(contentResolver, uri, projection, null,
            null, getSortOrder(limit, offset), null)
        cursor.moveToFirst()
        val artists = mutableListOf<Artist>()
        while(!cursor.isAfterLast) {
            artists.add(cursor.getArtist())
            cursor.moveToNext()
        }
        cursor.close()
        return artists.toList()
    }

    private fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Artists.ARTIST} ASC LIMIT $limit OFFSET $offset"
}
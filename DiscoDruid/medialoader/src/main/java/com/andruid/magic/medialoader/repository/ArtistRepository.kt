package com.andruid.magic.medialoader.repository

import android.app.Application
import android.content.ContentResolver
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.model.readArtist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ArtistRepository {
    private val projection = arrayOf(
        MediaStore.Audio.Artists._ID,
        MediaStore.Audio.Artists.ARTIST,
        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
        MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    )

    private lateinit var contentResolver: ContentResolver

    fun init(application: Application) {
        contentResolver = application.contentResolver
    }

    @RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    suspend fun getArtists(limit: Int, offset: Int): List<Artist> {
        val artists = mutableListOf<Artist>()

        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

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
                    artists.add(cursor.readArtist())
            }

            artists.toList()
        }
    }

    private fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Artists.ARTIST} ASC LIMIT $limit OFFSET $offset"
}
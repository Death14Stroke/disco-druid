package com.andruid.magic.medialoader.repository

import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.model.readArtist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ArtistRepository : MediaRepository<Artist>() {
    override val uri: Uri
        get() = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    override val projection: Array<String>
        get() = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )
    override val baseSelection: String?
        get() = null

    override fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Artists.ARTIST} ASC LIMIT $limit OFFSET $offset"

    override suspend fun fetchUtil(
        selection: String?,
        selectionArgs: Array<String>?,
        limit: Int,
        offset: Int
    ): List<Artist> {
        val artists = mutableListOf<Artist>()
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

    override suspend fun getAllContent(limit: Int, offset: Int): List<Artist> {
        return fetchUtil(baseSelection, null, limit, offset)
    }
}
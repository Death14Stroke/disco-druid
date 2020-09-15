package com.andruid.magic.medialoader.repository

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Artist
import com.andruid.magic.medialoader.model.ArtistAlbum
import com.andruid.magic.medialoader.model.readArtist
import com.andruid.magic.medialoader.model.readArtistAlbum
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
                while (cursor.moveToNext()) {
                    var artist = cursor.readArtist()
                    getAlbumIdForArtist(artist)?.let { artist = artist.copy(albumId = it) }
                    artists.add(artist)
                }
            }

            artists.toList()
        }
    }

    override suspend fun getAllPagedContent(limit: Int, offset: Int): List<Artist> {
        return fetchUtil(baseSelection, null, limit, offset)
    }

    private suspend fun getAlbumIdForArtist(artist: Artist): String? {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums._ID)
        val selection = "${MediaStore.Audio.Albums.ARTIST} = ?"

        return withContext(Dispatchers.IO) {
            val query =
                contentResolver.query(uri, projection, selection, arrayOf(artist.artist), null)
            query?.use { cursor ->
                if (cursor.moveToFirst())
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums._ID))
                else
                    null
            }
        }
    }

    @SuppressLint("InlinedApi")
    suspend fun getAlbumForArtist(artistId: String, album: String): ArtistAlbum? {
        val uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId.toLong())
        val projection = arrayOf(
            MediaStore.Audio.Artists.Albums.ALBUM,
            MediaStore.Audio.Artists.Albums.ARTIST_ID,
            MediaStore.Audio.Artists.Albums.ARTIST,
            MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS_FOR_ARTIST
        )
        val selection = "${MediaStore.Audio.Artists.Albums.ALBUM} = ?"

        return withContext(Dispatchers.IO) {
            val query = contentResolver.query(uri, projection, selection, arrayOf(album), null)
            query?.use { cursor ->
                if (cursor.moveToFirst())
                    cursor.readArtistAlbum()
                else
                    null
            }
        }
    }
}
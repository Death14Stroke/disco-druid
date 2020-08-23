package com.andruid.magic.medialoader.repository

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Track
import com.andruid.magic.medialoader.model.readTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TrackRepository {
    @SuppressLint("InlinedApi")
    private val projection =
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
        )

    private lateinit var contentResolver: ContentResolver

    fun init(application: Application) {
        contentResolver = application.contentResolver
    }

    //@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    suspend fun getTracks(limit: Int, offset: Int): List<Track> {
        val tracks = mutableListOf<Track>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = ("(${MediaStore.Audio.Media.IS_MUSIC} !=0 ) AND " +
                "(${MediaStore.Audio.Media.IS_ALARM} ==0 ) AND (${MediaStore.Audio.Media.IS_NOTIFICATION} ==0 )" +
                " AND (${MediaStore.Audio.Media.IS_PODCAST} ==0 ) AND (${MediaStore.Audio.Media.IS_RINGTONE} ==0 )")

        return withContext(Dispatchers.IO) {
            val query = ContentResolverCompat.query(
                contentResolver,
                uri,
                projection,
                selection,
                null,
                getSortOrder(limit, offset),
                null
            )

            query?.use { cursor ->
                while (cursor.moveToNext())
                    tracks.add(cursor.readTrack())
            }

            tracks.toList()
        }
    }

    private fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Media.TITLE} ASC LIMIT $limit OFFSET $offset"
}
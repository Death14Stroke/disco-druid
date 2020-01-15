package com.andruid.magic.medialoader.repository

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContentResolverCompat
import com.andruid.magic.medialoader.model.Track
import com.andruid.magic.medialoader.util.getTrack

class TrackRepository {
    companion object {
        private val TAG = TrackRepository::class.java.simpleName

        private lateinit var INSTANCE: TrackRepository
        private lateinit var contentResolver: ContentResolver
        private val LOCK = Any()

        @JvmStatic
        fun init(application: Application) {
            contentResolver = application.contentResolver
        }

        @JvmStatic
        fun getInstance(): TrackRepository {
            if(!::contentResolver.isInitialized)
                throw Exception("must call init() first in Application class")
            if (!::INSTANCE.isInitialized) {
                synchronized(LOCK) {
                    Log.d(TAG, "Created track repository instance")
                    INSTANCE = TrackRepository()
                }
            }
            return INSTANCE
        }
    }

    @RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getTracks(limit: Int, offset: Int): List<Track> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = ("(${MediaStore.Audio.Media.IS_MUSIC} !=0 ) AND " +
                "(${MediaStore.Audio.Media.IS_ALARM} ==0 ) AND (${MediaStore.Audio.Media.IS_NOTIFICATION} ==0 )" +
                " AND (${MediaStore.Audio.Media.IS_PODCAST} ==0 ) AND (${MediaStore.Audio.Media.IS_RINGTONE} ==0 )")
        val cursor = ContentResolverCompat.query(contentResolver, uri, getProjection(),
            selection, null, getSortOrder(limit, offset), null)
        cursor.moveToFirst()
        val tracks = mutableListOf<Track>()
        while(!cursor.isAfterLast) {
            tracks.add(cursor.getTrack())
            cursor.moveToNext()
        }
        cursor.close()
        return tracks.toList()
    }

    private fun getSortOrder(limit: Int, offset: Int) =
        "${MediaStore.Audio.Media.TITLE} ASC LIMIT $limit OFFSET $offset"

    @SuppressLint("InlinedApi")
    private fun getProjection() =
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
        )
}
package com.andruid.magic.medialoader.util

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat

object MediaUtil {
    @JvmStatic
    fun getAlbumArtUri(contentResolver: ContentResolver, albumId: String): String? {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        val selection = "${MediaStore.Audio.Albums._ID}=?"
        val cursor = ContentResolverCompat.query(contentResolver, uri, projection, selection,
            arrayOf(albumId), null, null)
        var path: String? = null
        if (cursor.moveToFirst())
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
        cursor.close()
        return path
    }
}
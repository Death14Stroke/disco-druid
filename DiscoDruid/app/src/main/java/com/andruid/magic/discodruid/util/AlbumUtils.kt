package com.andruid.magic.discodruid.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.andruid.magic.discodruid.R
import com.andruid.magic.medialoader.repository.AlbumRepository
import java.io.FileNotFoundException

@Suppress("BlockingMethodInNonBlockingContext")
fun Context.getAlbumArtBitmap(albumId: String): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val albumArtUri = ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId.toLong()
        )
        val size = resources.getDimension(R.dimen.album_art_size).toInt()

        try {
            contentResolver.loadThumbnail(albumArtUri, Size(size, size), null)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    } else {
        val albumArtUri = AlbumRepository.getAlbumArtUri(albumId)
        BitmapFactory.decodeFile(albumArtUri)
    }
}
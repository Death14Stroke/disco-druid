package com.andruid.magic.discodruid.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Size
import com.andruid.magic.discodruid.R
import com.andruid.magic.medialoader.repository.AlbumRepository
import java.io.FileNotFoundException

@Suppress("BlockingMethodInNonBlockingContext")
fun Context.getAlbumArtBitmap(albumId: String): Bitmap {
    return AlbumRepository.getAlbumArtUri(albumId)?.let { albumArtUri ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val size = resources.getDimension(R.dimen.album_art_size).toInt()

            try {
                contentResolver.loadThumbnail(albumArtUri, Size(size, size), null)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                BitmapFactory.decodeResource(resources, R.drawable.logo)
            }
        } else
            BitmapFactory.decodeResource(resources, R.drawable.logo)
    } ?: run {
        BitmapFactory.decodeResource(resources, R.drawable.logo)
    }
}
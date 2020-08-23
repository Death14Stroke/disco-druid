package com.andruid.magic.discodruid.util

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.andruid.magic.discodruid.R
import com.andruid.magic.medialoader.repository.AlbumRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.*

@BindingAdapter("albumArt")
fun ImageView.loadAlbumArt(albumId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val albumArtUri = ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId.toLong()
        )
        try {
            val size = resources.getDimension(R.dimen.album_art_size).toInt()
            val bitmap =
                context.contentResolver.loadThumbnail(albumArtUri, Size(size, size), null)
            setImageBitmap(bitmap)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    } else {
        GlobalScope.launch {
            val albumArtUri = AlbumRepository.getAlbumArtUri(albumId)
            setImageURI(Uri.parse(albumArtUri))
        }
    }
}

@BindingAdapter("timeMillis")
fun TextView.formatTime(sec: Long) {
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60)
    text = timeStr
}
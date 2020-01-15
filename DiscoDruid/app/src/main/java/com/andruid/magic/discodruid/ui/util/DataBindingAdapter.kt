package com.andruid.magic.discodruid.ui.util

import android.net.Uri
import android.os.Build
import android.util.Size
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.transform.RoundedCornersTransformation
import com.andruid.magic.discodruid.R
import com.andruid.magic.medialoader.util.MediaUtil
import java.io.File
import java.util.*

object DataBindingAdapter {
    @BindingAdapter("timeFormat")
    @JvmStatic
    fun formatTimeString(textView: TextView, sec: Long) {
        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60)
        textView.text = timeStr
    }

    @BindingAdapter(value = ["uri", "albumId"])
    @JvmStatic
    fun loadAlbumArt(imageView: ImageView, uri: String, albumId: String) {
        val contentResolver = imageView.context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            contentResolver.loadThumbnail(
                Uri.parse(uri),
                Size(imageView.width, imageView.height), null
            )
        else {
            val path = MediaUtil.getAlbumArtUri(contentResolver, albumId)
            if (path != null)
                imageView.load(File(path)) { transformations(RoundedCornersTransformation(50f)) }
            else
                imageView.load(R.drawable.music) { transformations(RoundedCornersTransformation(50f)) }
        }
    }
}
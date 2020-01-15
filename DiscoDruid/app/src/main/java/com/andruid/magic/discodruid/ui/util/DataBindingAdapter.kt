package com.andruid.magic.discodruid.ui.util

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.api.load
import java.util.*

object DataBindingAdapter {
    @BindingAdapter("imageUrl")
    @JvmStatic
    fun loadImage(imageView: ImageView, path: String) = imageView.load(path)

    @BindingAdapter("timeFormat")
    @JvmStatic
    fun formatTimeString(textView: TextView, sec: Long) {
        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60)
        textView.text = timeStr
    }
}
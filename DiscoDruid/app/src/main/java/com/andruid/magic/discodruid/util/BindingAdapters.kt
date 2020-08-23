package com.andruid.magic.discodruid.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.util.*

@BindingAdapter("timeMillis")
fun TextView.formatTime(sec: Long) {
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60)
    text = timeStr
}
package com.andruid.magic.discodruid.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.andruid.magic.discodruid.R
import java.text.SimpleDateFormat
import java.util.*

fun Long.toTimeString() =
    String.format(Locale.getDefault(), "%02d:%02d", this / 60, this % 60)

@BindingAdapter("createdOn")
fun TextView.showCreatedOn(ms: Long) {
    val dateFormat = SimpleDateFormat.getDateInstance()
    val time = dateFormat.format(Date(ms * 1000))

    text = context.getString(R.string.created_on, time)
}
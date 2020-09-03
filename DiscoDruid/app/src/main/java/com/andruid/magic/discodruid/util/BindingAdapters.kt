package com.andruid.magic.discodruid.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.andruid.magic.discodruid.R
import com.andruid.magic.medialoader.model.Artist
import java.text.SimpleDateFormat
import java.util.*

fun Long.toTimeString() =
    String.format(Locale.getDefault(), "%02d:%02d", this / 60, this % 60)

@BindingAdapter("timeMillis")
fun TextView.formatTime(sec: Long) {
    val timeStr = sec.toTimeString()
    text = timeStr
}

@BindingAdapter("artistStats")
fun TextView.showArtistStats(artist: Artist) {
    val stats = context.getString(R.string.artist_stats, artist.albumsCount, artist.tracksCount)
    text = stats
}

@BindingAdapter("createdOn")
fun TextView.showCreatedOn(ms: Long) {
    val dateFormat = SimpleDateFormat("dd M, yyyy", Locale.getDefault())
    val time = dateFormat.format(Date(ms))

    text = context.getString(R.string.created_on, time)
}
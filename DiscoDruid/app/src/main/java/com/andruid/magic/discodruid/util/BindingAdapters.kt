package com.andruid.magic.discodruid.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.andruid.magic.discodruid.R
import com.andruid.magic.medialoader.model.Artist
import java.util.*

@BindingAdapter("timeMillis")
fun TextView.formatTime(sec: Long) {
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60)
    text = timeStr
}

@BindingAdapter("artistStats")
fun TextView.showArtistStats(artist: Artist) {
    val stats = context.getString(R.string.artist_stats, artist.albumsCount, artist.tracksCount)
    text = stats
}
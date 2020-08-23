package com.andruid.magic.medialoader.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(
    val path: String,
    val title: String,
    val artist: String = "Unknown",
    val album: String = "Unknown",
    val albumId: String,
    val audioId: Long,
    val duration: Long = 0
) : Parcelable
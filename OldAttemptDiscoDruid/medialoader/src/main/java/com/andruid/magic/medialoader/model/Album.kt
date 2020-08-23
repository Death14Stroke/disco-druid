package com.andruid.magic.medialoader.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(
    val albumId: String,
    val album: String = "Unknown",
    val artist: String = "Unknown",
    val songsCount: Int = 0
) : Parcelable
package com.andruid.magic.medialoader.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Artist(
    val artistId: String,
    val artist: String,
    val tracksCount: Int = 0,
    val albumsCount: Int = 0
) : Parcelable
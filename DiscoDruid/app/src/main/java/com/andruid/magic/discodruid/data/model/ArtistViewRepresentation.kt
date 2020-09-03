package com.andruid.magic.discodruid.data.model

import android.content.Context
import com.andruid.magic.discodruid.R
import com.andruid.magic.medialoader.model.Artist

data class ArtistViewRepresentation(
    val artist: Artist,
    val stats: String
) {
    companion object {
        fun fromArtist(context: Context, artist: Artist): ArtistViewRepresentation {
            val albumsCount = context.resources.getQuantityString(
                R.plurals.albums_count,
                artist.albumsCount,
                artist.albumsCount
            )
            val tracksCount = context.resources.getQuantityString(
                R.plurals.tracks_count,
                artist.tracksCount,
                artist.tracksCount
            )

            return ArtistViewRepresentation(
                artist = artist,
                stats = "$albumsCount | $tracksCount"
            )
        }
    }
}
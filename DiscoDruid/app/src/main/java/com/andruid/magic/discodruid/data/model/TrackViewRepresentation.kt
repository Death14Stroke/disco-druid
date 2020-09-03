package com.andruid.magic.discodruid.data.model

import com.andruid.magic.discodruid.util.toTimeString
import com.andruid.magic.medialoader.model.Track

data class TrackViewRepresentation (
    val track: Track,
    val timeString: String
) {
    companion object {
        fun fromTrack(track: Track): TrackViewRepresentation {
            return TrackViewRepresentation(
                track = track,
                timeString = track.duration.toTimeString()
            )
        }
    }
}
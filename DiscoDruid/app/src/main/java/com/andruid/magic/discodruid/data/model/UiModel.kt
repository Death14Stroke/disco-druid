package com.andruid.magic.discodruid.data.model

import com.andruid.magic.medialoader.model.ArtistAlbum
import com.andruid.magic.medialoader.model.Track

sealed class UiModel {
    class TrackModel(val viewRepresentation: TrackViewRepresentation) : UiModel()
    class AlbumSeparatorModel(val album: ArtistAlbum) : UiModel()
}
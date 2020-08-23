package com.andruid.magic.discodruid.paging.artist

import androidx.paging.DataSource
import com.andruid.magic.medialoader.model.Artist

class ArtistDataSourceFactory : DataSource.Factory<Int, Artist>() {
    override fun create() = ArtistDataSource()
}
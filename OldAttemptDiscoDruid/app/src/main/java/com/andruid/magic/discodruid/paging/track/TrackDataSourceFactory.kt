package com.andruid.magic.discodruid.paging.track

import androidx.paging.DataSource
import com.andruid.magic.medialoader.model.Track

class TrackDataSourceFactory : DataSource.Factory<Int, Track>() {
    override fun create() = TrackDataSource()
}
package com.andruid.magic.discodruid.paging.album

import androidx.paging.DataSource
import com.andruid.magic.medialoader.model.Album

class AlbumDataSourceFactory : DataSource.Factory<Int, Album>() {
    override fun create() = AlbumDataSource()
}
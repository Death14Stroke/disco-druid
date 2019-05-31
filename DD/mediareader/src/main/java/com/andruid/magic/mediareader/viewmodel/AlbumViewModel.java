package com.andruid.magic.mediareader.viewmodel;

import android.app.Application;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.mediareader.data.ReaderConstants;
import com.andruid.magic.mediareader.datasourcefactory.AlbumDataSourceFactory;
import com.andruid.magic.mediareader.model.Album;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class AlbumViewModel extends AndroidViewModel {
    private Application application;

    public AlbumViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<Album>> getAlbums(MediaBrowserCompat mediaBrowserCompat){
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(ReaderConstants.PAGE_SIZE)
                .build();
        return new LivePagedListBuilder<>(new AlbumDataSourceFactory(mediaBrowserCompat,application.getApplicationContext()),config).build();
    }
}
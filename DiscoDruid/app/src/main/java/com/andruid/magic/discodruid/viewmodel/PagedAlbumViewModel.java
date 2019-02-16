package com.andruid.magic.discodruid.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.datasourcefactory.AlbumDataSourceFactory;
import com.andruid.magic.discodruid.model.Album;

public class PagedAlbumViewModel extends AndroidViewModel {
    private Application application;

    public PagedAlbumViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<Album>> getAlbums(MediaBrowserCompat mediaBrowserCompat){
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .build();
        return new LivePagedListBuilder<>(new AlbumDataSourceFactory(mediaBrowserCompat,application.getApplicationContext()),config).build();
    }
}

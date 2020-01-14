package com.andruid.magic.discodruid.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.datasourcefactory.TrackDataSourceFactory;
import com.andruid.magic.discodruid.model.Track;

import java.util.List;

public class PagedTrackViewModel extends AndroidViewModel {
    private TrackDataSourceFactory dataSourceFactory;
    private Application application;

    public PagedTrackViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<Track>> getTracks(MediaBrowserCompat mediaBrowserCompat, Bundle options){
        dataSourceFactory = new TrackDataSourceFactory(application.getApplicationContext(),mediaBrowserCompat,options);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .build();
        return new LivePagedListBuilder<>(dataSourceFactory,config).build();
    }
}
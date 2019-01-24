package com.andruid.magic.discodruid.viewmodel;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.datasourcefactory.TrackDataSourceFactory;
import com.andruid.magic.discodruid.model.Track;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class PagedTrackViewModel extends AndroidViewModel {
    private Application application;

    public PagedTrackViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<Track>> getTracks(MediaBrowserCompat mediaBrowserCompat, Bundle options){
        TrackDataSourceFactory dataSourceFactory = new TrackDataSourceFactory(application.getApplicationContext(), mediaBrowserCompat, options);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build();
        return new LivePagedListBuilder<>(dataSourceFactory,config).build();
    }
}
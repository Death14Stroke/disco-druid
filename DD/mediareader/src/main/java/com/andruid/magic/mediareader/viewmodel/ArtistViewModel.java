package com.andruid.magic.mediareader.viewmodel;

import android.app.Application;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.mediareader.data.Constants;
import com.andruid.magic.mediareader.datasourcefactory.ArtistDataSourceFactory;
import com.andruid.magic.mediareader.model.Artist;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class ArtistViewModel extends AndroidViewModel {
    private Application application;

    public ArtistViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<Artist>> getArtists(MediaBrowserCompat mediaBrowserCompat){
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .build();
        return new LivePagedListBuilder<>(new ArtistDataSourceFactory(mediaBrowserCompat,application.getApplicationContext()),config).build();
    }
}
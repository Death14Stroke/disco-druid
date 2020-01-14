package com.andruid.magic.discodruid.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.datasourcefactory.PlaylistDataSourceFactory;
import com.andruid.magic.discodruid.model.PlayList;

public class PagedPlaylistViewModel extends AndroidViewModel {
    private PlaylistDataSourceFactory playlistDataSourceFactory;
    private Application application;

    public PagedPlaylistViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<PlayList>> getPlayLists(MediaBrowserCompat mediaBrowserCompat){
        playlistDataSourceFactory = new PlaylistDataSourceFactory(mediaBrowserCompat,application.getApplicationContext());
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .build();
        return new LivePagedListBuilder<>(playlistDataSourceFactory,config).build();
    }
}
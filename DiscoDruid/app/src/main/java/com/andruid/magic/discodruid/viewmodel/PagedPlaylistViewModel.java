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
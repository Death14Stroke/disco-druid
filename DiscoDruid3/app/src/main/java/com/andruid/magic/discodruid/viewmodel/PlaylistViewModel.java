package com.andruid.magic.discodruid.viewmodel;

import android.app.Application;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.datasourcefactory.PlaylistDataSourceFactory;
import com.andruid.magic.discodruid.model.PlayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class PlaylistViewModel extends AndroidViewModel {

    private Application application;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<PlayList>> getPlayLists(MediaBrowserCompat mediaBrowserCompat){
        PlaylistDataSourceFactory playlistDataSourceFactory = new PlaylistDataSourceFactory(mediaBrowserCompat, application.getApplicationContext());
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .build();
        return new LivePagedListBuilder<>(playlistDataSourceFactory,config).build();
    }
}

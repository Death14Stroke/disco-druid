package com.andruid.magic.mediareader.datasource;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.mediareader.data.ReaderConstants;
import com.andruid.magic.mediareader.model.Track;
import com.andruid.magic.mediareader.util.PagingUtils;
import com.andruid.magic.mediareader.util.TrackUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

public class TrackDataSource extends PositionalDataSource<Track> {
    private final MediaBrowserCompat mediaBrowserCompat;
    private String rootId;
    private Bundle options;
    private Set<Integer> loadedPages = new HashSet<>();

    public TrackDataSource(MediaBrowserCompat mediaBrowserCompat, Bundle options) {
        this.mediaBrowserCompat = mediaBrowserCompat;
        this.options = options;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<Track> callback) {
        String parentId = getParentId(params.requestedStartPosition);
        Bundle extra = getInitialPageBundle(params);
        if(options!=null)
            extra.putAll(options);
        mediaBrowserCompat.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(0);
                List<Track> trackList = TrackUtils.getTracksFromMediaItems(children);
                callback.onResult(trackList,params.requestedStartPosition);
            }
        });
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<Track> callback) {
        final int pageIndex = PagingUtils.getPageIndex(params);
        if(loadedPages.contains(pageIndex)){
            callback.onResult(new ArrayList<Track>());
            return;
        }
        String parentId = getParentId(params.startPosition);
        Bundle extras = PagingUtils.getRangeBundle(params);
        mediaBrowserCompat.subscribe(parentId, extras, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(pageIndex);
                List<Track> trackList = TrackUtils.getTracksFromMediaItems(children);
                callback.onResult(trackList);
            }
        });
    }

    private String getParentId(int requestedStartPosition) {
        if (rootId == null)
            rootId = mediaBrowserCompat.getRoot();
        String key = "_track_";
        if(options!=null){
            if(options.containsKey(ReaderConstants.ALBUM_ID))
                key = "_" + ReaderConstants.ALBUM_TRACK + "_";
            else if(options.containsKey(ReaderConstants.ARTIST_ID))
                key = "_" + ReaderConstants.ARTIST_TRACK + "_";
        }
        return rootId + key + requestedStartPosition;
    }

    @NonNull
    private Bundle getInitialPageBundle(@NonNull LoadInitialParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize);
        return extra;
    }
}
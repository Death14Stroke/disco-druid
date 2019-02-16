package com.andruid.magic.discodruid.datasource;

import androidx.paging.PositionalDataSource;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                List<Track> trackList = getTracksFromMediaItems(children);
                callback.onResult(trackList,params.requestedStartPosition);
            }
        });
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<Track> callback) {
        final int pageIndex = getPageIndex(params);
        if(loadedPages.contains(pageIndex)){
            callback.onResult(new ArrayList<Track>());
            return;
        }
        String parentId = getParentId(params.startPosition);
        Bundle extras = getRangeBundle(params);
        mediaBrowserCompat.subscribe(parentId, extras, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(pageIndex);
                List<Track> trackList = getTracksFromMediaItems(children);
                callback.onResult(trackList);
            }
        });
    }

    private Bundle getRangeBundle(LoadRangeParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE,getPageIndex(params));
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE,params.loadSize);
        return extra;
    }

    private int getPageIndex(LoadRangeParams params){
        return params.startPosition/params.loadSize;
    }

    private String getParentId(int requestedStartPosition) {
        if (rootId == null)
            rootId = mediaBrowserCompat.getRoot();
        String key = "_track_";
        if(options!=null){
            if(options.containsKey(Constants.ALBUM_ID))
                key = "_" + Constants.ALBUM_TRACK + "_";
            else if(options.containsKey(Constants.ARTIST_ID))
                key = "_" + Constants.ARTIST_TRACK + "_";
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

    private List<Track> getTracksFromMediaItems(List<MediaBrowserCompat.MediaItem> children) {
        List<Track> trackList = new ArrayList<>();
        Bundle extras;
        for(MediaBrowserCompat.MediaItem mediaItem : children){
            extras = mediaItem.getDescription().getExtras();
            if(extras!=null) {
                Track track = extras.getParcelable(Constants.TRACK);
                trackList.add(track);
            }
        }
        return trackList;
    }
}
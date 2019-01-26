package com.andruid.magic.discodruid.datasource;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.model.TrackItem;
import com.andruid.magic.discodruid.util.MediaUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

public class TrackDataSource extends PositionalDataSource<TrackItem> {
    private final MediaBrowserCompat mediaBrowserCompat;
    private String rootId;
    private Bundle options;
    private Set<Integer> loadedPages = new HashSet<>();
    private Context mContext;

    public TrackDataSource(MediaBrowserCompat mediaBrowserCompat, Bundle options, Context mContext) {
        this.mediaBrowserCompat = mediaBrowserCompat;
        this.options = options;
        this.mContext = mContext;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<TrackItem> callback) {
        String parentId = getParentId(params.requestedStartPosition);
        Bundle extra = getInitialPageBundle(params);
        if(options!=null)
            extra.putAll(options);
        mediaBrowserCompat.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(0);
                List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                Log.d("adapterlog","initial datasource:"+trackList.size());
                List<TrackItem> trackItemList = new ArrayList<>();
                for(Track track : trackList)
                    trackItemList.add(new TrackItem(track,mContext));
                callback.onResult(trackItemList,params.requestedStartPosition);
            }
        });
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<TrackItem> callback) {
        final int pageIndex = getPageIndex(params);
        if(loadedPages.contains(pageIndex)){
            callback.onResult(new ArrayList<>());
            return;
        }
        String parentId = getParentId(params.startPosition);
        Bundle extras = getRangeBundle(params);
        mediaBrowserCompat.subscribe(parentId, extras, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(pageIndex);
                List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                Log.d("adapterlog","loadrange datasource:"+trackList.size());
                List<TrackItem> trackItemList = new ArrayList<>();
                for(Track track : trackList)
                    trackItemList.add(new TrackItem(track,mContext));
                callback.onResult(trackItemList);
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
}
package com.andruid.magic.discodruid.datasource;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.util.MediaUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

public class TrackDataSource extends ItemKeyedDataSource<Character,Track> {
    private final MediaBrowserCompat mediaBrowserCompat;
    private String rootId;
    private Bundle options;
    private Set<Character> loadedPages = new HashSet<>();

    public TrackDataSource(MediaBrowserCompat mediaBrowserCompat, Bundle options) {
        this.mediaBrowserCompat = mediaBrowserCompat;
        this.options = options;
    }

    private String getParentId(Character requestedKey) {
        if (rootId == null)
            rootId = mediaBrowserCompat.getRoot();
        String key = "_track_";
        if(options!=null){
            if(options.containsKey(Constants.ALBUM_ID))
                key = "_" + Constants.ALBUM_TRACK + "_";
            else if(options.containsKey(Constants.ARTIST_ID))
                key = "_" + Constants.ARTIST_TRACK + "_";
        }
        return rootId + key + requestedKey;
    }

    @NonNull
    private Bundle getInitialPageBundle(@NonNull LoadInitialParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.requestedLoadSize);
        extra.putChar(Constants.LOAD_KEY,Constants.SPECIAL_SYMBOLS);
        return extra;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Character> params, @NonNull LoadInitialCallback<Track> callback) {
        Log.d("itemds","load initial");
        String parentId = getParentId(Constants.SPECIAL_SYMBOLS);
        Bundle extra = getInitialPageBundle(params);
        if(options!=null)
            extra.putAll(options);
        mediaBrowserCompat.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                for(Track track : trackList)
                    Log.d("itemds","initial item: "+track.getTitle());
                loadedPages.add(Constants.SPECIAL_SYMBOLS);
                callback.onResult(trackList,0,trackList.size());
            }
        });
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Character> params, @NonNull LoadCallback<Track> callback) {
        Log.d("itemds","load after");
        if(loadedPages.contains(params.key)) {
            callback.onResult(new ArrayList<>());
            return;
        }
        String parentId = getParentId(params.key);
        Bundle extras = getRangeBundle(params);
        mediaBrowserCompat.subscribe(parentId, extras, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                loadedPages.add(trackList.get(0).getTitle().charAt(0));
                callback.onResult(trackList);
            }
        });
    }

    private Bundle getRangeBundle(LoadParams<Character> params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE,1);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE,params.requestedLoadSize);
        extra.putChar(Constants.LOAD_KEY,params.key);
        return extra;
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Character> params, @NonNull LoadCallback<Track> callback) { }

    @NonNull
    @Override
    public Character getKey(@NonNull Track track) {
        return track.getTitle().charAt(0);
    }
}
package com.andruid.magic.discodruid.datasource;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;

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
    private Set<Integer> loadedPages = new HashSet<>();

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

    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback callback) {
        Log.d("itemdslog", String.valueOf(params.requestedInitialKey));
    }

    @Override
    public void loadAfter(@NonNull LoadParams params, @NonNull LoadCallback callback) {

    }

    @Override
    public void loadBefore(@NonNull LoadParams params, @NonNull LoadCallback callback) {

    }

    @NonNull
    @Override
    public Character getKey(@NonNull Track track) {
        return track.getTitle().charAt(0);
    }
}
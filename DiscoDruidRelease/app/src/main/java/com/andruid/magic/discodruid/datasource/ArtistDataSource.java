package com.andruid.magic.discodruid.datasource;

import android.arch.paging.PositionalDataSource;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Artist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArtistDataSource extends PositionalDataSource<Artist> {
    private final MediaBrowserCompat mediaBrowserCompat;
    private String rootId;
    private Set<Integer> loadedPages = new HashSet<>();

    public ArtistDataSource(MediaBrowserCompat mediaBrowserCompat) {
        this.mediaBrowserCompat = mediaBrowserCompat;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<Artist> callback) {
        String parentId = getParentId(params.requestedStartPosition);
        Bundle extra = getInitialPageBundle(params);
        mediaBrowserCompat.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(0);
                List<Artist> artistList = getArtistsFromMediaItems(children);
                callback.onResult(artistList,params.requestedStartPosition);
            }
        });
    }

    @Override
    public void loadRange(@NonNull final LoadRangeParams params, @NonNull final LoadRangeCallback<Artist> callback) {
        final int pageIndex = getPageIndex(params);
        if(loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<Artist>());
            return;
        }
        String parentId = getParentId(params.startPosition);
        Bundle extras = getRangeBundle(params);
        mediaBrowserCompat.subscribe(parentId, extras, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(pageIndex);
                List<Artist> artistList = getArtistsFromMediaItems(children);
                callback.onResult(artistList);
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
        return rootId + "_artist_" + requestedStartPosition;
    }

    @NonNull
    private Bundle getInitialPageBundle(@NonNull LoadInitialParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize);
        return extra;
    }

    private List<Artist> getArtistsFromMediaItems(List<MediaBrowserCompat.MediaItem> children){
        List<Artist> artistList = new ArrayList<>();
        Bundle extras;
        for(MediaBrowserCompat.MediaItem mediaItem : children) {
            extras = mediaItem.getDescription().getExtras();
            if (extras != null) {
                Artist artist = extras.getParcelable(Constants.ARTIST);
                artistList.add(artist);
            }
        }
        return artistList;
    }
}
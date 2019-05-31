package com.andruid.magic.mediareader.datasource;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.mediareader.data.ReaderConstants;
import com.andruid.magic.mediareader.model.Album;
import com.andruid.magic.mediareader.util.PagingUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

public class AlbumDataSource extends PositionalDataSource<Album> {
    private final MediaBrowserCompat mediaBrowserCompat;
    private String rootId;
    private Set<Integer> loadedPages = new HashSet<>();

    public AlbumDataSource(MediaBrowserCompat mediaBrowserCompat) {
        this.mediaBrowserCompat = mediaBrowserCompat;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<Album> callback) {
        String parentId = getParentId(params.requestedStartPosition);
        Bundle extra = getInitialPageBundle(params);
        mediaBrowserCompat.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(0);
                List<Album> albumList = getAlbumsFromMediaItems(children);
                callback.onResult(albumList,params.requestedStartPosition);
            }
        });
    }

    @Override
    public void loadRange(@NonNull final LoadRangeParams params, @NonNull final LoadRangeCallback<Album> callback) {
        final int pageIndex = PagingUtils.getPageIndex(params);
        if(loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<Album>());
            return;
        }
        String parentId = getParentId(params.startPosition);
        Bundle extras = PagingUtils.getRangeBundle(params);
        mediaBrowserCompat.subscribe(parentId, extras, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(pageIndex);
                List<Album> albumList = getAlbumsFromMediaItems(children);
                callback.onResult(albumList);
            }
        });
    }

    private String getParentId(int requestedStartPosition) {
        if (rootId == null)
            rootId = mediaBrowserCompat.getRoot();
        return rootId + "_album_" + requestedStartPosition;
    }

    @NonNull
    private Bundle getInitialPageBundle(@NonNull LoadInitialParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize);
        return extra;
    }

    private List<Album> getAlbumsFromMediaItems(List<MediaBrowserCompat.MediaItem> children){
        List<Album> albumList = new ArrayList<>();
        Bundle extras;
        for(MediaBrowserCompat.MediaItem mediaItem : children) {
            extras = mediaItem.getDescription().getExtras();
            if (extras != null) {
                Album album = extras.getParcelable(ReaderConstants.ALBUM);
                albumList.add(album);
            }
        }
        return albumList;
    }
}
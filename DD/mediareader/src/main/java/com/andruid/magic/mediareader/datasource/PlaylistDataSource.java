package com.andruid.magic.mediareader.datasource;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.mediareader.data.Constants;
import com.andruid.magic.mediareader.model.PlayList;
import com.andruid.magic.mediareader.util.PagingUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

public class PlaylistDataSource extends PositionalDataSource<PlayList> {
    private final MediaBrowserCompat mediaBrowserCompat;
    private String rootId;
    private Set<Integer> loadedPages = new HashSet<>();

    public PlaylistDataSource(MediaBrowserCompat mediaBrowserCompat) {
        this.mediaBrowserCompat = mediaBrowserCompat;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<PlayList> callback) {
        String parentId = getParentId(params.requestedStartPosition);
        Bundle extra = getInitialPageBundle(params);
        mediaBrowserCompat.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(0);
                List<PlayList> playLists = getPlayListsFromMediaItems(children);
                callback.onResult(playLists,params.requestedStartPosition);
            }
        });
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<PlayList> callback) {
        final int pageIndex = PagingUtils.getPageIndex(params);
        if(loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<PlayList>());
            return;
        }
        String parentId = getParentId(params.startPosition);
        Bundle extras = PagingUtils.getRangeBundle(params);
        mediaBrowserCompat.subscribe(parentId, extras, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(pageIndex);
                List<PlayList> playLists = getPlayListsFromMediaItems(children);
                callback.onResult(playLists);
            }
        });
    }

    private String getParentId(int requestedStartPosition) {
        if (rootId == null)
            rootId = mediaBrowserCompat.getRoot();
        return rootId + "_" + Constants.PLAYLIST + "_" + requestedStartPosition;
    }

    @NonNull
    private Bundle getInitialPageBundle(@NonNull LoadInitialParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize);
        return extra;
    }

    private List<PlayList> getPlayListsFromMediaItems(List<MediaBrowserCompat.MediaItem> children){
        List<PlayList> playLists = new ArrayList<>();
        Bundle extras;
        for(MediaBrowserCompat.MediaItem mediaItem : children){
            extras = mediaItem.getDescription().getExtras();
            if(extras!=null){
                PlayList playList = extras.getParcelable(Constants.PLAYLIST);
                playLists.add(playList);
            }
        }
        return playLists;
    }
}
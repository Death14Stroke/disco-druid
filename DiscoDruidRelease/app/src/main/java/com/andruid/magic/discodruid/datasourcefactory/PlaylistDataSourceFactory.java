package com.andruid.magic.discodruid.datasourcefactory;

import android.arch.paging.DataSource;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.datasource.PlaylistDataSource;
import com.andruid.magic.discodruid.model.PlayList;

public class PlaylistDataSourceFactory extends DataSource.Factory<Integer,PlayList> {
    private PlaylistDataSource playlistDataSource;

    public PlaylistDataSourceFactory(final MediaBrowserCompat mediaBrowserCompat, Context context) {
        playlistDataSource = new PlaylistDataSource(mediaBrowserCompat);
        context.getContentResolver().registerContentObserver(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                true, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        if(uri.getLastPathSegment()!=null && !selfChange) {
                            playlistDataSource.invalidate();
                            playlistDataSource = new PlaylistDataSource(mediaBrowserCompat);
                            create();
                        }
                    }
                });
    }

    @Override
    public DataSource<Integer, PlayList> create() {
        return playlistDataSource;
    }
}
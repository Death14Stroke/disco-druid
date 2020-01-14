package com.andruid.magic.discodruid.datasourcefactory;

import android.arch.paging.DataSource;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.datasource.AlbumDataSource;
import com.andruid.magic.discodruid.model.Album;

public class AlbumDataSourceFactory extends DataSource.Factory<Integer,Album> {
    private AlbumDataSource albumDataSource;

    public AlbumDataSourceFactory(final MediaBrowserCompat mediaBrowserCompat, Context context) {
        albumDataSource = new AlbumDataSource(mediaBrowserCompat);
        context.getContentResolver().registerContentObserver(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                true, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        if(uri.getLastPathSegment()!=null && !selfChange){
                            albumDataSource.invalidate();
                            albumDataSource = new AlbumDataSource(mediaBrowserCompat);
                            create();
                        }
                    }
                });
    }

    @Override
    public DataSource<Integer, Album> create() {
        return albumDataSource;
    }
}
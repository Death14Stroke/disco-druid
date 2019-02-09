package com.andruid.magic.discodruid.datasourcefactory;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.datasource.TrackDataSource;
import com.andruid.magic.discodruid.model.Track;

import androidx.paging.DataSource;

public class TrackDataSourceFactory extends DataSource.Factory<Integer, Track> {
    private TrackDataSource trackDataSource;

    public TrackDataSourceFactory(Context context,final MediaBrowserCompat mediaBrowserCompat, final Bundle options) {
        trackDataSource = new TrackDataSource(mediaBrowserCompat,options);
        context.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        if(uri.getLastPathSegment()!=null && !selfChange) {
                            trackDataSource.invalidate();
                            trackDataSource = new TrackDataSource(mediaBrowserCompat, options);
                            create();
                        }
                    }
                });
    }

    @Override
    public DataSource<Integer, Track> create() {
        return trackDataSource;
    }
}
package com.andruid.magic.discodruid.datasourcefactory;

import androidx.paging.DataSource;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.datasource.ArtistDataSource;
import com.andruid.magic.discodruid.model.Artist;

public class ArtistDataSourceFactory extends DataSource.Factory<Integer,Artist> {
    private ArtistDataSource artistDataSource;

    public ArtistDataSourceFactory(final MediaBrowserCompat mediaBrowserCompat, Context context) {
        artistDataSource = new ArtistDataSource(mediaBrowserCompat);
        context.getContentResolver().registerContentObserver(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                true, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        if(uri.getLastPathSegment()!=null && !selfChange){
                            artistDataSource.invalidate();
                            artistDataSource = new ArtistDataSource(mediaBrowserCompat);
                            create();
                        }
                    }
                });
    }

    @Override
    public DataSource<Integer, Artist> create() {
        return artistDataSource;
    }
}
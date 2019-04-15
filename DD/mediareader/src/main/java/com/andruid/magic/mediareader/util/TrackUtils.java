package com.andruid.magic.mediareader.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.mediareader.data.Constants;
import com.andruid.magic.mediareader.model.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackUtils {

    public static Track getTrackFromCursor(Cursor cursor, ContentResolver contentResolver){
        Track track = new Track();
        track.setAudioId(cursor.getLong(0));
        track.setArtist(cursor.getString(1));
        track.setTitle(cursor.getString(2));
        track.setPath(cursor.getString(3));
        track.setDuration(cursor.getLong(4) / 1000);
        track.setAlbumId(cursor.getString(5));
        track.setAlbum(cursor.getString(6));
        Cursor c = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?", new String[]{track.getAlbumId()},
                null);
        if (c != null){
            if(c.moveToFirst()) {
                String path = c.getString(0);
                track.setAlbumArtUri(path);
            }
            c.close();
        }
        return track;
    }

    public static List<Track> getTracksFromMediaItems(List<MediaBrowserCompat.MediaItem> children) {
        List<Track> trackList = new ArrayList<>();
        Bundle extras;
        for(MediaBrowserCompat.MediaItem mediaItem : children){
            extras = mediaItem.getDescription().getExtras();
            if(extras!=null) {
                Track track = extras.getParcelable(Constants.TRACK);
                trackList.add(track);
            }
        }
        return trackList;
    }
}
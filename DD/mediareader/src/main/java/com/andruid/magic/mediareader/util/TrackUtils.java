package com.andruid.magic.mediareader.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.andruid.magic.mediareader.data.ReaderConstants;
import com.andruid.magic.mediareader.model.Track;
import com.andruid.magic.mediareader.provider.TrackProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                Track track = extras.getParcelable(ReaderConstants.TRACK);
                trackList.add(track);
            }
        }
        return trackList;
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItemsFromTracks(List<Track> trackList) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(Track track : trackList){
            Bundle extras = new Bundle();
            extras.putParcelable(ReaderConstants.TRACK,track);
            MediaDescriptionCompat mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                    .setMediaId(track.getPath())
                    .setTitle(track.getTitle())
                    .setExtras(extras)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    public static List<Track> getTracksForPage(TrackProvider trackProvider, int page, int pageSize) {
        int start = page*pageSize;
        return trackProvider.getTracksAtRange(start,Math.min(start+pageSize, trackProvider.getListSize()));
    }

    public static MediaDescriptionCompat getMediaDescription(Track track) {
        Bundle extras = new Bundle();
        Bitmap bitmap = BitmapFactory.decodeFile(track.getAlbumArtUri());
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,bitmap);
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,bitmap);
        return new MediaDescriptionCompat.Builder()
                .setMediaId(track.getPath())
                .setIconBitmap(bitmap)
                .setTitle(track.getTitle())
                .setDescription(track.getAlbum())
                .setExtras(extras)
                .build();
    }

    public static MediaMetadataCompat.Builder buildMetaData(Track track) {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        if (Objects.requireNonNull(track).getAlbumArtUri() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(track.getAlbumArtUri());
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getAlbumArtUri());
        }
        builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, track.getAudioId());
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.getDuration());
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAlbum());
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist());
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle());
        return builder;
    }
}
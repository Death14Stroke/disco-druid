package com.andruid.magic.mediareader.util;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.andruid.magic.mediareader.data.ReaderConstants;
import com.andruid.magic.mediareader.model.Artist;
import com.andruid.magic.mediareader.provider.ArtistProvider;

import java.util.ArrayList;
import java.util.List;

public class ArtistUtils {

    public static Artist getArtistFromCursor(Cursor cursor) {
        Artist artist = new Artist();
        artist.setArtistId(cursor.getString(0));
        artist.setArtist(cursor.getString(1));
        artist.setAlbumsCount(cursor.getInt(2));
        artist.setTracksCount(cursor.getInt(3));
        return artist;
    }

    public static List<Artist> getArtistsForPage(ArtistProvider artistProvider, int page, int pageSize) {
        int start = page*pageSize;
        return artistProvider.getArtistsAtRange(start,Math.min(start+pageSize, artistProvider.getListSize()));
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItemsFromArtists(List<Artist> artistList) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(Artist artist : artistList){
            Bundle extras = new Bundle();
            extras.putParcelable(ReaderConstants.ARTIST,artist);
            MediaDescriptionCompat mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                    .setMediaId(artist.getArtistId())
                    .setTitle(artist.getArtist())
                    .setExtras(extras)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }
}

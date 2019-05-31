package com.andruid.magic.mediareader.util;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.andruid.magic.mediareader.data.ReaderConstants;
import com.andruid.magic.mediareader.model.Album;
import com.andruid.magic.mediareader.provider.AlbumProvider;

import java.util.ArrayList;
import java.util.List;

public class AlbumUtils {

    public static Album getAlbumFromCursor(Cursor cursor) {
        Album album = new Album();
        album.setAlbumId(cursor.getString(0));
        album.setAlbum(cursor.getString(1));
        album.setArtist(cursor.getString(2));
        album.setAlbumArtUri(cursor.getString(3));
        album.setSongsCount(cursor.getInt(4));
        return album;
    }


    public static List<Album> getAlbumsForPage(AlbumProvider albumProvider, int page, int pageSize) {
        int start = page*pageSize;
        return albumProvider.getAlbumsAtRange(start,Math.min(start+pageSize, albumProvider.getListSize()));
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItemsFromAlbums(List<Album> albumList) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(Album album : albumList){
            Bundle extras = new Bundle();
            extras.putParcelable(ReaderConstants.ALBUM,album);
            MediaDescriptionCompat mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                    .setMediaId(album.getAlbumId())
                    .setTitle(album.getAlbum())
                    .setExtras(extras)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }
}
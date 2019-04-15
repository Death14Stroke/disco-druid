package com.andruid.magic.mediareader.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.andruid.magic.mediareader.data.Constants;
import com.andruid.magic.mediareader.model.PlayList;
import com.andruid.magic.mediareader.provider.PlaylistProvider;

import java.util.ArrayList;
import java.util.List;

public class PlaylistUtils {

    public static PlayList getPlaylistFromCursor(Cursor cursor, ContentResolver contentResolver) {
        PlayList playList = new PlayList();
        playList.setPlayListId(cursor.getLong(0));
        playList.setName(cursor.getString(1));
        playList.setDateCreated(cursor.getLong(2));
        String[] p = {"count(*)"};
        Cursor c = contentResolver.query(MediaStore.Audio.Playlists.Members.getContentUri("external",
                playList.getPlayListId()), p, null, null, null);
        if (c != null) {
            if (c.moveToFirst())
                playList.setSongCount(c.getInt(0));
            c.close();
        }
        return playList;
    }

    public static List<PlayList> getPlayListsForPage(PlaylistProvider playlistProvider, int page, int pageSize) {
        int start = page*pageSize;
        return playlistProvider.getPlaylistAtRange(start,Math.min(start+pageSize, playlistProvider.getListSize()));
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItemsFromPlayLists(List<PlayList> playLists) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(PlayList playList : playLists){
            Bundle extras = new Bundle();
            extras.putParcelable(Constants.PLAYLIST,playList);
            MediaDescriptionCompat mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                    .setMediaId(String.valueOf(playList.getPlayListId()))
                    .setTitle(playList.getName())
                    .setExtras(extras)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }
}
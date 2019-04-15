package com.andruid.magic.mediareader.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.andruid.magic.mediareader.model.PlayList;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContentResolverCompat;

public class PlaylistProvider {
    private final Cursor cursor;
    private final ContentResolver contentResolver;

    public PlaylistProvider(Context context) {
        contentResolver = context.getContentResolver();
        cursor = ContentResolverCompat.query(contentResolver, getUri(), getProjection(), getSelection(),
                getSelectionArgs(), getSortOrder(), null);
    }

    public int getListSize() {
        return cursor.getCount();
    }

    public List<PlayList> getAllPlaylist() {
        List<PlayList> playLists = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++)
            playLists.add(getPlaylistAtPosition(i));
        return playLists;
    }

    public List<PlayList> getPlaylistAtRange(int start, int end) {
        List<PlayList> playLists = new ArrayList<>();
        for (int i = start; i < end; i++) {
            PlayList playList = getPlaylistAtPosition(i);
            if (playList != null)
                playLists.add(playList);
        }
        return playLists;
    }

    private PlayList getPlaylistAtPosition(int position) {
        if (!cursor.moveToPosition(position))
            return null;
        return getPlaylistFromCursor(cursor);
    }

    private PlayList getPlaylistFromCursor(Cursor cursor) {
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

    private Uri getUri() {
        return MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
    }

    private String[] getProjection() {
        return new String[]{MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME,
                MediaStore.Audio.Playlists.DATE_ADDED, MediaStore.Audio.Playlists.DATE_MODIFIED,
                MediaStore.Audio.Playlists.DATA};
    }

    private String getSelection() {
        return null;
    }

    private String[] getSelectionArgs() {
        return null;
    }

    private String getSortOrder() {
        return MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
    }
}
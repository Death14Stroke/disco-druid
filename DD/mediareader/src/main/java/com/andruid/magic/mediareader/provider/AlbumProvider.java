package com.andruid.magic.mediareader.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.andruid.magic.mediareader.model.Album;
import com.andruid.magic.mediareader.util.AlbumUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContentResolverCompat;

public class AlbumProvider {
    private final Cursor cursor;

    public AlbumProvider(Context context) {
        cursor = ContentResolverCompat.query(context.getContentResolver(), getUri(), getProjection(),
                getSelection(), getSelectionArgs(), getSortOrder(), null);
    }

    public int getListSize(){
        return cursor.getCount();
    }

    public List<Album> getAlbumsAtRange(int start, int end){
        List<Album> albumList = new ArrayList<>();
        for(int i=start;i<end;i++){
            Album album = getAlbumAtPosition(i);
            if(album!=null)
                albumList.add(album);
        }
        return albumList;
    }

    private Album getAlbumAtPosition(int position) {
        if(!cursor.moveToPosition(position))
            return null;
        return AlbumUtils.getAlbumFromCursor(cursor);
    }

    private Uri getUri() {
        return MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    }

    private String[] getProjection(){
        return new String[]{
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        };
    }

    private String getSelection(){
        return null;
    }

    private String[] getSelectionArgs(){
        return null;
    }

    private String getSortOrder(){
        return MediaStore.Audio.Albums.ALBUM + " ASC";
    }
}
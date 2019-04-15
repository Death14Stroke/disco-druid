package com.andruid.magic.mediareader.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.andruid.magic.mediareader.model.Artist;
import com.andruid.magic.mediareader.util.ArtistUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContentResolverCompat;

public class ArtistProvider {
    private final Cursor cursor;

    public ArtistProvider(Context context) {
        cursor = ContentResolverCompat.query(context.getContentResolver(), getUri(), getProjection(),
                getSelection(), getSelectionArgs(), getSortOrder(), null);
    }

    public int getListSize(){
        return cursor.getCount();
    }

    public List<Artist> getArtistsAtRange(int start, int end){
        List<Artist> artistList = new ArrayList<>();
        for(int i=start;i<end;i++){
            Artist artist = getArtistAtPosition(i);
            if(artist!=null)
                artistList.add(artist);
        }
        return artistList;
    }

    private Artist getArtistAtPosition(int position) {
        if(!cursor.moveToPosition(position))
            return null;
        return ArtistUtils.getArtistFromCursor(cursor);
    }

    private Uri getUri() {
        return MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    }

    private String[] getProjection() {
        return new String[]{MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS, MediaStore.Audio.Artists.NUMBER_OF_TRACKS};
    }

    private String getSelection() {
        return null;
    }

    private String[] getSelectionArgs() {
        return null;
    }

    private String getSortOrder() {
        return MediaStore.Audio.Artists.ARTIST + " ASC";
    }
}
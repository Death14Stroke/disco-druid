package com.andruid.magic.discodruid.util;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class ReadContent {
    public static void deleteSongsFromPlayList(Context context, List<String> audioIdList, long playListId){
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ContentProviderOperation operation;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",playListId);
        for(String id : audioIdList){
            operation = ContentProviderOperation
                    .newDelete(uri)
                    .withSelection(MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", new String[]{id})
                    .build();
            operations.add(operation);
        }
        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY,operations);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSongsFromStorage(Context context, List<String> audioIdList){
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ContentProviderOperation operation;
        Uri uri;
        for(String id : audioIdList){
            uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
            operation = ContentProviderOperation
                    .newDelete(uri)
                    .build();
            operations.add(operation);
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY,operations);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void moveSongsInPlayList(Context context, long playListId, int from,int to){
        ContentResolver contentResolver = context.getContentResolver();
        MediaStore.Audio.Playlists.Members.moveItem(contentResolver,playListId,from,to);
    }

    public static void addTracksToPlayList(Context context, long playListId, List<String> songIds) {
        ContentValues[] values = new ContentValues[songIds.size()];
        for (int i = 0; i < songIds.size(); i++) {
            values[i] = new ContentValues();
            values[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,i);
            values[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, Long.parseLong(songIds.get(i)));
        }
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
        ContentResolver resolver = context.getContentResolver();
        resolver.bulkInsert(uri, values);
    }

    public static void createNewPlayList(Context context, String name){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME,name);
        values.put(MediaStore.Audio.Playlists.DATE_ADDED,System.currentTimeMillis());
        values.put(MediaStore.Audio.Playlists.DATE_MODIFIED,System.currentTimeMillis());
        contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,values);
    }

    public static void deletePlayList(Context context, List<String> playListIds){
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        StringBuilder builder = new StringBuilder(MediaStore.Audio.Playlists._ID);
        builder.append(" IN (");
        for(int i=0;i<playListIds.size()-1;i++){
            builder.append("?,");
        }
        builder.append("?)");
        String where = builder.toString();
        String[] selectionArgs = playListIds.toArray(new String[0]);
        contentResolver.delete(uri, where, selectionArgs);
    }
}
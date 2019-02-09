package com.andruid.magic.discodruid.util;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andruid.magic.discodruid.MainActivity;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.model.Artist;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.provider.AlbumProvider;
import com.andruid.magic.discodruid.provider.ArtistProvider;
import com.andruid.magic.discodruid.provider.PlaylistProvider;
import com.andruid.magic.discodruid.provider.TrackProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.BindingAdapter;

public class MediaUtils {
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

    public static List<MediaBrowserCompat.MediaItem> getMediaItemsFromArtists(List<Artist> artistList) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(Artist artist : artistList){
            Bundle extras = new Bundle();
            extras.putParcelable(Constants.ARTIST,artist);
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

    public static List<PlayList> getPlayListsForPage(PlaylistProvider playlistProvider, int page, int pageSize) {
        int start = page*pageSize;
        return playlistProvider.getPlaylistAtRange(start,Math.min(start+pageSize, playlistProvider.getListSize()));
    }

    public static List<Artist> getArtistsForPage(ArtistProvider artistProvider, int page, int pageSize) {
        int start = page*pageSize;
        return artistProvider.getArtistsAtRange(start,Math.min(start+pageSize, artistProvider.getListSize()));
    }

    public static List<Album> getAlbumsForPage(AlbumProvider albumProvider, int page, int pageSize) {
        int start = page*pageSize;
        return albumProvider.getAlbumsAtRange(start,Math.min(start+pageSize, albumProvider.getListSize()));
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItemsFromAlbums(List<Album> albumList) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(Album album : albumList){
            Bundle extras = new Bundle();
            extras.putParcelable(Constants.ALBUM,album);
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

    public static List<MediaBrowserCompat.MediaItem> getMediaItemsFromTracks(List<Track> trackList) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(Track track : trackList){
            Bundle extras = new Bundle();
            extras.putParcelable(Constants.TRACK,track);
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

    public static Track getTrackFromMetaData(MediaMetadataCompat metadataCompat) {
        Track track = new Track();
        track.setAudioId(metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER));
        track.setTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        track.setAlbum(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        track.setArtist(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        track.setAlbumArtUri(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        track.setDuration(metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        return track;
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

    public static AlertDialog.Builder getPermissionsDialogBuilder(Context context) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.storage_permission)
                .setMessage("Storage permission is needed to view your music")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(R.string.settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts(context.getString(R.string.str_package),context.getPackageName(),null));
                    dialog.dismiss();
                    context.startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                        dialog.dismiss());
    }

    public static AlertDialog.Builder getPlaylistDialogBuilder(Context context){
        final EditText input = new EditText(context);
        input.setHint(context.getString(R.string.playlist_name));
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.setMarginEnd(margin);
        params.setMarginStart(margin);
        input.setLayoutParams(params);
        container.addView(input);
        return new AlertDialog.Builder(context)
                .setTitle("Create New Playlist")
                .setView(container)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Create", (dialog, which) -> {
//                    new MainActivity.CreatePlayListAsyncTask(context.getApplicationContext(),
//                            input.getText().toString().trim()).execute();
                    dialog.cancel();
                })
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.cancel());
    }

    public static void createNewPlayList(Context context, String name){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME,name);
        values.put(MediaStore.Audio.Playlists.DATE_ADDED,System.currentTimeMillis());
        values.put(MediaStore.Audio.Playlists.DATE_MODIFIED,System.currentTimeMillis());
        contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,values);
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

    public static List<Track> getTracksForPage(TrackProvider trackProvider, int page, int pageSize) {
        int start = page*pageSize;
        return trackProvider.getTracksAtRange(start,Math.min(start+pageSize, trackProvider.getListSize()));
    }

    @BindingAdapter("android:imageUrl")
    public static void imageUrl(ImageView imageView, String path){
        if(path!=null)
            Glide.with(imageView.getContext())
                    .load(path)
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(50))
                            .placeholder(R.drawable.music)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageView);
        else
            Glide.with(imageView.getContext())
                    .load(R.drawable.music)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageView);
    }

    @BindingAdapter("android:timeFormat")
    public static void getTimeString(TextView textView, long sec){
        String s = String.format(Locale.getDefault(),"%02d:%02d",sec/60,sec%60);
        textView.setText(s);
    }
}
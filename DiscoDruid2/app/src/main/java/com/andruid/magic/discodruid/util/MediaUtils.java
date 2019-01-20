package com.andruid.magic.discodruid.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.model.Artist;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.provider.AlbumProvider;
import com.andruid.magic.discodruid.provider.ArtistProvider;
import com.andruid.magic.discodruid.provider.PlaylistProvider;
import com.andruid.magic.discodruid.provider.TrackProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat,MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
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
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat,MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
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
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat,MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
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
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescriptionCompat,MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    public static List<Track> getTracksForPage(TrackProvider trackProvider, int page, int pageSize) {
        int start = page*pageSize;
        return trackProvider.getTracksAtRange(start,Math.min(start+pageSize, trackProvider.getListSize()));
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

    public static String getTimeString(long sec){
        return String.format(Locale.getDefault(),"%02d:%02d",sec/60,sec%60);
    }
}
package com.andruid.magic.discodruid;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.session.MediaSessionCompat;

import com.andruid.magic.mediareader.data.Constants;
import com.andruid.magic.mediareader.model.Album;
import com.andruid.magic.mediareader.model.Artist;
import com.andruid.magic.mediareader.model.PlayList;
import com.andruid.magic.mediareader.model.Track;
import com.andruid.magic.mediareader.provider.AlbumProvider;
import com.andruid.magic.mediareader.provider.ArtistProvider;
import com.andruid.magic.mediareader.provider.PlaylistProvider;
import com.andruid.magic.mediareader.provider.TrackProvider;
import com.andruid.magic.mediareader.util.AlbumUtils;
import com.andruid.magic.mediareader.util.ArtistUtils;
import com.andruid.magic.mediareader.util.PlaylistUtils;
import com.andruid.magic.mediareader.util.TrackUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.media.MediaBrowserServiceCompat;
import timber.log.Timber;

public class MyMusicService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mSession;
    private TrackProvider generalTrackProvider;
    private AlbumProvider albumProvider;
    private ArtistProvider artistProvider;
    private PlaylistProvider playlistProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        mSession = new MediaSessionCompat(this, "MyMusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    @Override
    public void onDestroy() {
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        Bundle extras = new Bundle();
        extras.putBoolean(BrowserRoot.EXTRA_OFFLINE,true);
        extras.putBoolean(BrowserRoot.EXTRA_RECENT,true);
        return new BrowserRoot(getString(R.string.app_name), extras);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        result.sendResult(new ArrayList<>());
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaItem>> result, @NonNull Bundle options) {
        result.detach();
        if(parentId.contains(Constants.TRACK) && !parentId.equals(Constants.CURRENT_TRACK)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                generalTrackProvider = new TrackProvider(getApplicationContext());
            List<Track> trackList = TrackUtils.getTracksForPage(generalTrackProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = TrackUtils.getMediaItemsFromTracks(trackList);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(Constants.ALBUM)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                albumProvider = new AlbumProvider(getApplicationContext());
            List<Album> albumList = AlbumUtils.getAlbumsForPage(albumProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = AlbumUtils.getMediaItemsFromAlbums(albumList);
            Timber.tag("adapterlog").d("mediabrowser onloadchildren album:" + mediaItems.size() + ":" + parentId);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(Constants.ARTIST)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                artistProvider = new ArtistProvider(getApplicationContext());
            List<Artist> artistList = ArtistUtils.getArtistsForPage(artistProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = ArtistUtils.getMediaItemsFromArtists(artistList);
            Timber.tag("adapterlog").d("mediabrowser onloadchildren artist:" + mediaItems.size() + ":" + parentId);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(Constants.PLAYLIST)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                playlistProvider = new PlaylistProvider(getApplicationContext());
            List<PlayList> playLists = PlaylistUtils.getPlayListsForPage(playlistProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = PlaylistUtils.getMediaItemsFromPlayLists(playLists);
            Timber.tag("adapterlog").d("mediabrowser onloadchildren playlist:" + mediaItems.size() + ":" + parentId);
            result.sendResult(mediaItems);
        }
    }
}
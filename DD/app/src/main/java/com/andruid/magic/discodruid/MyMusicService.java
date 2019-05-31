package com.andruid.magic.discodruid;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.util.NotificationHelper;
import com.andruid.magic.mediareader.data.ReaderConstants;
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
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.IllegalSeekPositionException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import timber.log.Timber;

public class MyMusicService extends MediaBrowserServiceCompat {
    private List<Track> trackList;
    private MediaSessionCompat mediaSessionCompat;
    private MediaSessionCallback mediaSessionCallback;
    private SimpleExoPlayer exoPlayer;
    private NotificationCompat.Builder notificationBuilder;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private DataSource.Factory dataSourceFactory;
    private Intent mediaButtonIntent;
    private TrackProvider generalTrackProvider;
    private AlbumProvider albumProvider;
    private ArtistProvider artistProvider;
    private PlaylistProvider playlistProvider;
    private MediaSessionConnector mediaSessionConnector;
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaSessionCallback.onPause();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaSession();
        initExoPlayer();
    }

    private void initMediaSession(){
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(this, getClass().getSimpleName(),mediaButtonReceiver,pendingIntent);
        setSessionToken(mediaSessionCompat.getSessionToken());
        mediaSessionCallback = new MediaSessionCallback();
        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        mediaSessionCompat.setActive(true);
        mediaSessionConnector = new MediaSessionConnector(mediaSessionCompat);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSessionCompat) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return TrackUtils.getMediaDescription(trackList.get(windowIndex));
            }
        });
        mediaSessionConnector.setPlayer(exoPlayer,null);
    }

    private void initExoPlayer() {
        final TrackSelector trackSelector = new DefaultTrackSelector();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                Util.getUserAgent(MyMusicService.this, getString(R.string.app_name)));
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState==Player.STATE_READY){
                    if(mediaButtonIntent!=null){
                        MediaButtonReceiver.handleIntent(mediaSessionCompat,mediaButtonIntent);
                        mediaButtonIntent = null;
                    }
                }
                int icon = android.R.drawable.ic_media_play;
                if(playWhenReady)
                    icon = android.R.drawable.ic_media_pause;
                MediaMetadataCompat metadataCompat = mediaSessionCompat.getController().getMetadata();
                if(metadataCompat==null)
                    return;
                notificationBuilder = NotificationHelper.buildNotification(icon,
                        MyMusicService.this, metadataCompat, mediaSessionCompat.getSessionToken());
                Notification notification = Objects.requireNonNull(notificationBuilder).build();
                if(playWhenReady)
                    startForeground(Constants.MEDIA_NOTI_ID, notification);
                else {
                    NotificationManagerCompat.from(MyMusicService.this).notify(
                            Constants.MEDIA_NOTI_ID, notification);
                    stopForeground(false);
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                if(reason==Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                        reason==Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason==Player.DISCONTINUITY_REASON_INTERNAL) {
                    int pos = exoPlayer.getCurrentWindowIndex();
                    if(pos>=0 && pos<trackList.size()) {
                        setSong(exoPlayer.getCurrentWindowIndex());
                    }
                }
            }
        });
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes,true);
    }

    private void setSong(int pos) {
        if(pos>=trackList.size() || pos<0)
            return;
        Track track = trackList.get(pos);
        mediaSessionCompat.setMetadata(TrackUtils.buildMetaData(track).build());
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREF,0).edit();
        editor.putInt(Constants.PLAYLIST_RESUME_INDEX,pos);
        editor.apply();
    }

    private void setMediaSourceFromTrackList() {
        MediaSource[] mediaSources = new MediaSource[trackList.size()];
        for(int i=0;i<mediaSources.length;i++){
            Track track = trackList.get(i);
            mediaSources[i] = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(track.getPath()));
        }
        concatenatingMediaSource = new ConcatenatingMediaSource(mediaSources);
        notifyChildrenChanged(Constants.PLAY_QUEUE);
    }

    private void setMediaPlaybackState(int state, int pos) {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        playbackStateBuilder.setActiveQueueItemId(pos);
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS || state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP);
        else
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP);
        if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_PLAYING) {
            try {
                playbackStateBuilder.setState(state, exoPlayer.getCurrentPosition(), 0);
                Bundle extras = new Bundle();
                extras.putLong(Constants.SEEK_POSITION, exoPlayer.getCurrentPosition());
                playbackStateBuilder.setExtras(extras);
            }
            catch (IllegalStateException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else
            playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(trackList!=null)
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        else
            mediaButtonIntent = intent;
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mediaSessionCallback.onStop();
    }

    @Override
    public void onDestroy() {
        mediaSessionCompat.release();
        mediaSessionConnector.setPlayer(null,null);
        exoPlayer.release();
        exoPlayer = null;
        super.onDestroy();
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback{
        @Override
        public void onPlay() {
            super.onPlay();
            if(trackList==null)
                return;
            exoPlayer.setPlayWhenReady(true);
            setSong(exoPlayer.getCurrentWindowIndex());
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING,exoPlayer.getCurrentWindowIndex());
            mediaSessionCompat.setActive(true);
            registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            int pos = Integer.parseInt(String.valueOf(queueId));
            try {
                setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM, pos);
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.seekTo(pos, 0);
                mediaSessionCallback.onPlay();
            }
            catch (IllegalSeekPositionException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSeekTo(long position) {
            exoPlayer.seekTo(Integer.parseInt(String.valueOf(position)));
        }

        @Override
        public void onPause() {
            unregisterReceiver(mNoisyReceiver);
            exoPlayer.setPlayWhenReady(false);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED,exoPlayer.getCurrentWindowIndex());
        }

        @Override
        public void onStop() {
            Toast.makeText(getApplicationContext(),"stop",Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            if(exoPlayer.hasNext()) {
                exoPlayer.next();
                exoPlayer.setPlayWhenReady(true);
            }
        }

        @Override
        public void onSkipToPrevious() {
            if(exoPlayer.hasPrevious()) {
                exoPlayer.setPlayWhenReady(true);
                exoPlayer.previous();
            }
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            switch (command){
                case Constants.SET_QUEUE:
                    trackList = extras.getParcelableArrayList(Constants.PLAY_QUEUE);
                    if (trackList != null) {
                        setMediaSourceFromTrackList();
                        exoPlayer.prepare(concatenatingMediaSource);
                        exoPlayer.setPlayWhenReady(true);
                    }
                    break;
            }
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            mediaSessionCompat.setShuffleMode(shuffleMode);
            if(shuffleMode==PlaybackStateCompat.SHUFFLE_MODE_ALL)
                exoPlayer.setShuffleModeEnabled(true);
            else
                exoPlayer.setShuffleModeEnabled(false);
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            mediaSessionCompat.setRepeatMode(repeatMode);
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL)
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE)
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE)
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        }
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
        if(parentId.contains(ReaderConstants.TRACK) && !parentId.equals(ReaderConstants.CURRENT_TRACK)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                generalTrackProvider = new TrackProvider(getApplicationContext());
            List<Track> trackList = TrackUtils.getTracksForPage(generalTrackProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = TrackUtils.getMediaItemsFromTracks(trackList);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(ReaderConstants.ALBUM)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                albumProvider = new AlbumProvider(getApplicationContext());
            List<Album> albumList = AlbumUtils.getAlbumsForPage(albumProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = AlbumUtils.getMediaItemsFromAlbums(albumList);
            Timber.tag("adapterlog").d("mediabrowser onloadchildren album:" + mediaItems.size() + ":" + parentId);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(ReaderConstants.ARTIST)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                artistProvider = new ArtistProvider(getApplicationContext());
            List<Artist> artistList = ArtistUtils.getArtistsForPage(artistProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = ArtistUtils.getMediaItemsFromArtists(artistList);
            Timber.tag("adapterlog").d("mediabrowser onloadchildren artist:" + mediaItems.size() + ":" + parentId);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(ReaderConstants.PLAYLIST)){
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
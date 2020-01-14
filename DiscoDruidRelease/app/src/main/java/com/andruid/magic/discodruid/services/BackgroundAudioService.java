package com.andruid.magic.discodruid.services;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.model.Artist;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.provider.AlbumProvider;
import com.andruid.magic.discodruid.provider.ArtistProvider;
import com.andruid.magic.discodruid.provider.PlaylistProvider;
import com.andruid.magic.discodruid.provider.TrackProvider;
import com.andruid.magic.discodruid.util.MediaUtils;
import com.andruid.magic.discodruid.util.NotificationHelper;
import com.andruid.magic.discodruid.widget.MusicWidget;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.IllegalSeekPositionException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BackgroundAudioService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener {
    private ExoPlayer exoPlayer;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSessionCompat;
    private ArrayList<Track> trackList = new ArrayList<>();
    private ConcatenatingMediaSource concatenatingMediaSource;
    private DataSource.Factory dataSourceFactory;
    private NotificationCompat.Builder notificationBuilder;
    private Intent mediaButtonIntent;
    private TrackProvider trackProvider;
    private AlbumProvider albumProvider;
    private ArtistProvider artistProvider;
    private PlaylistProvider playlistProvider;
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaSessionCallback.onPause();
        }
    };

    public BackgroundAudioService() {}

    private void initMediaSession() {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), Constants.MEDIA_SERVICE_TAG, mediaButtonReceiver, pendingIntent);
        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                                        | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        setSessionToken(mediaSessionCompat.getSessionToken());
    }

    private void initExoPlayer() {
        final TrackSelector trackSelector = new DefaultTrackSelector();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this,trackSelector);
        dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                Util.getUserAgent(BackgroundAudioService.this,getString(R.string.app_name)));
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState==Player.STATE_READY){
                    if(mediaButtonIntent!=null){
                        MediaButtonReceiver.handleIntent(mediaSessionCompat,mediaButtonIntent);
                        mediaButtonIntent = null;
                    }
                }
                if(playWhenReady)
                    updateWidgetButton(android.R.drawable.ic_media_pause);
                else
                    updateWidgetButton(android.R.drawable.ic_media_play);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                if(reason==Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                        reason==Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason==Player.DISCONTINUITY_REASON_INTERNAL) {
                    setSong(exoPlayer.getCurrentWindowIndex());
                    updateWidget(trackList.get(exoPlayer.getCurrentWindowIndex()));
                    notificationBuilder = NotificationHelper.buildNotification(android.R.drawable.ic_media_pause,
                            BackgroundAudioService.this, mediaSessionCompat.getController().getMetadata(), mediaSessionCompat.getSessionToken());
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Objects.requireNonNull(notificationManager).notify(Constants.MEDIA_NOTI_ID, notificationBuilder.build());
                }
            }
        });
    }

    private boolean isAudioFocusGranted() {
        int focusRequest = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
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
                Toast.makeText(getApplicationContext(),"Unable to play",Toast.LENGTH_SHORT).show();
            }
        } else
            playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build());
    }

    private void updateWidgetButton(int resource) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.music_widget);
        ComponentName appWidget = new ComponentName(BackgroundAudioService.this, MusicWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(BackgroundAudioService.this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);
        views.setImageViewResource(R.id.widget_play_btn, resource);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    public void setSong(int pos) {
        if(pos>=trackList.size() || pos<0)
            return;
        Track track = trackList.get(pos);
        mediaSessionCompat.setMetadata(MediaUtils.buildMetaData(track).build());
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREF,0).edit();
        editor.putInt(Constants.PLAYLIST_RESUME_INDEX,pos);
        editor.apply();
        updateWidget(track);
        notifyChildrenChanged(Constants.CURRENT_TRACK);
    }

    private void updateWidget(Track track) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.music_widget);
        views.setTextViewText(R.id.widget_song_name, track.getTitle());
        views.setTextViewText(R.id.widget_artist_name, track.getArtist());
        ComponentName appWidget = new ComponentName(this, MusicWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);
        if (appWidgetIds.length == 0)
            return;
        AppWidgetTarget awt = new AppWidgetTarget(this, R.id.widget_image_view, views, appWidgetIds) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
            }
        };
        String path = track.getAlbumArtUri();
        if (path != null)
            Glide.with(this)
                    .asBitmap()
                    .load(path)
                    .into(awt);
        else
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.music)
                    .into(awt);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initExoPlayer();
        initMediaSession();
        getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(final Activity activity, Bundle bundle) {
                if (activity.getLocalClassName().equalsIgnoreCase(Constants.MAIN_ACTIVITY)) {
                    if (mediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING, exoPlayer.getCurrentWindowIndex());
                    }
                }
            }
            @Override
            public void onActivityStarted(Activity activity) {
            }
            @Override
            public void onActivityResumed(Activity activity) {
            }
            @Override
            public void onActivityPaused(Activity activity) {
            }
            @Override
            public void onActivityStopped(Activity activity) {
            }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
        new LoadQueueAsyncTask(this, new LoadQueueAsyncTask.QueueLoadedListener() {
            @Override
            public void onQueueLoaded(Bundle bundle) {
                if(bundle==null) {
                    Toast.makeText(getApplicationContext(),"No songs in queue",Toast.LENGTH_SHORT).show();
                    return;
                }
                trackList = bundle.getParcelableArrayList(Constants.PLAY_QUEUE);
                notifyChildrenChanged(Constants.PLAY_QUEUE);
                int pos = bundle.getInt(Constants.PLAYLIST_RESUME_INDEX);
                if(pos<0 || pos>=trackList.size())
                    pos = 0;
                setMediaSourceFromTrackList();
                exoPlayer.prepare(concatenatingMediaSource);
                setSong(pos);
                exoPlayer.seekTo(pos,0);
                exoPlayer.setPlayWhenReady(false);
                setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING, exoPlayer.getCurrentWindowIndex());
                Toast.makeText(getApplicationContext(),"Queue Loaded",Toast.LENGTH_SHORT).show();
                notifyChildrenChanged(Constants.CURRENT_TRACK);
            }
        }).execute();
    }

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri);
            concatenatingMediaSource = new ConcatenatingMediaSource(mediaSource);
            exoPlayer.prepare(concatenatingMediaSource);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(getApplicationContext(),uri);
            Track track = new Track();
            String album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            track.setAudioId(-1);
            track.setDuration(Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000);
            track.setAlbum(album!=null?album:"Album");
            track.setArtist(artist!=null?artist:"Artist");
            track.setTitle(title!=null?title:"Title");
            track.setPath(uri.getPath());
            track.setAlbumId("Album Id");
            trackList.clear();
            trackList.add(track);
            new AddToRecentQueueAsyncTask(getApplicationContext(), trackList, new AddToRecentQueueAsyncTask.TaskCompletedListener() {
                @Override
                public void onTaskCompleted() {
                    notifyChildrenChanged(Constants.PLAY_QUEUE);
                    onPlay();
                }
            });
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
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            }
            else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
            else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            }
        }

        @Override
        public void onPlay() {
            super.onPlay();
            if (!isAudioFocusGranted()) {
                Toast.makeText(BackgroundAudioService.this, "No audio focus", Toast.LENGTH_SHORT).show();
                return;
            }
            if(trackList==null)
                return;
            exoPlayer.setPlayWhenReady(true);
            setSong(exoPlayer.getCurrentWindowIndex());
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING,exoPlayer.getCurrentWindowIndex());
            mediaSessionCompat.setActive(true);
            updateWidgetButton(android.R.drawable.ic_media_pause);
            MediaMetadataCompat metadataCompat = mediaSessionCompat.getController().getMetadata();
            if(metadataCompat==null)
                return;
            notificationBuilder = NotificationHelper.buildNotification(android.R.drawable.ic_media_pause,
                    BackgroundAudioService.this, metadataCompat, mediaSessionCompat.getSessionToken());
            Notification notification = Objects.requireNonNull(notificationBuilder).build();
            startForeground(Constants.MEDIA_NOTI_ID,notification);
            startService(new Intent(BackgroundAudioService.this,BackgroundAudioService.class));
            registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        @Override
        public void onPause() {
            super.onPause();
            updateWidgetButton(android.R.drawable.ic_media_play);
            MediaMetadataCompat metadataCompat = mediaSessionCompat.getController().getMetadata();
            if(metadataCompat==null)
                return;
            unregisterReceiver(mNoisyReceiver);
            exoPlayer.setPlayWhenReady(false);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED,exoPlayer.getCurrentWindowIndex());
            notificationBuilder = NotificationHelper.buildNotification(android.R.drawable.ic_media_play,
                    BackgroundAudioService.this, mediaSessionCompat.getController().getMetadata(), mediaSessionCompat.getSessionToken());
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Objects.requireNonNull(notificationManager).notify(Constants.MEDIA_NOTI_ID, notificationBuilder.build());
            stopForeground(false);
        }

        @Override
        public void onStop() {
            super.onStop();
            updateWidgetButton(android.R.drawable.ic_media_play);
            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            if(exoPlayer.hasNext()) {
                exoPlayer.next();
                exoPlayer.setPlayWhenReady(true);
            }
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            if(exoPlayer.hasPrevious()) {
                exoPlayer.setPlayWhenReady(true);
                exoPlayer.previous();
            }
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            exoPlayer.seekTo(Integer.parseInt(String.valueOf(pos)));
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
            int pos = Integer.parseInt(String.valueOf(id));
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
        public void onCommand(String command, final Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            switch (command){
                case Constants.REMOVE_QUEUE:
                    final int pos = extras.getInt(Constants.POSITION);
                    boolean update = false;
                    if(pos==exoPlayer.getCurrentWindowIndex())
                        update = true;
                    final boolean finalUpdate = update;
                    concatenatingMediaSource.removeMediaSource(pos, new Runnable() {
                        @Override
                        public void run() {
                            trackList.remove(pos);
                            new AddToRecentQueueAsyncTask(BackgroundAudioService.this, trackList, new AddToRecentQueueAsyncTask.TaskCompletedListener() {
                                @Override
                                public void onTaskCompleted() {
                                    if(finalUpdate){
                                        setSong(exoPlayer.getCurrentWindowIndex());
                                        notificationBuilder = NotificationHelper.buildNotification(android.R.drawable.ic_media_pause,
                                                BackgroundAudioService.this, mediaSessionCompat.getController().getMetadata(), mediaSessionCompat.getSessionToken());
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        Objects.requireNonNull(notificationManager).notify(Constants.MEDIA_NOTI_ID, notificationBuilder.build());
                                    }
                                    notifyChildrenChanged(Constants.PLAY_QUEUE);
                                }
                            }).execute();
                        }
                    });
                    break;
                case Constants.SET_QUEUE:
                    trackList = extras.getParcelableArrayList(Constants.PLAY_QUEUE);
                    if (trackList != null) {
                        setMediaSourceFromTrackList();
                        exoPlayer.prepare(concatenatingMediaSource);
                        exoPlayer.setPlayWhenReady(true);
                    }
                    new AddToRecentQueueAsyncTask(BackgroundAudioService.this, trackList, new AddToRecentQueueAsyncTask.TaskCompletedListener() {
                        @Override
                        public void onTaskCompleted() {
                            notifyChildrenChanged(Constants.PLAY_QUEUE);
                        }
                    }).execute();
                    break;
                case Constants.DRAG_QUEUE:
                    int from = extras.getInt(Constants.FROM), to = extras.getInt(Constants.TO);
                    concatenatingMediaSource.moveMediaSource(from,to);
                    if(from>to){
                        trackList.add(to,trackList.get(from));
                        trackList.remove(from+1);
                    }
                    else{
                        trackList.add(to+1,trackList.get(from));
                        trackList.remove(from);
                    }
                    new AddToRecentQueueAsyncTask(BackgroundAudioService.this, trackList, new AddToRecentQueueAsyncTask.TaskCompletedListener() {
                        @Override
                        public void onTaskCompleted() {
                            notifyChildrenChanged(Constants.PLAY_QUEUE);
                        }
                    }).execute();
                    break;
            }
        }
    };

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
        if(trackList!=null){
            Track track = trackList.get(exoPlayer.getCurrentWindowIndex());
            if(track!=null) {
                Gson gson = new Gson();
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREF,0).edit();
                editor.putString(Constants.TRACK, gson.toJson(track));
                editor.apply();
            }
        }
        exoPlayer.release();
        mediaSessionCompat.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(trackList!=null)
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        else
            mediaButtonIntent = intent;
        return START_NOT_STICKY;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mediaSessionCallback.onPlay();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mediaSessionCallback.onPause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                mediaSessionCallback.onStop();
                break;
        }
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int i, @Nullable Bundle bundle) {
        Bundle extras = new Bundle();
        extras.putBoolean(BrowserRoot.EXTRA_OFFLINE,true);
        extras.putBoolean(BrowserRoot.EXTRA_RECENT,true);
        return new BrowserRoot(getString(R.string.app_name), extras);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result, @NonNull Bundle options) {
        result.detach();
        List<Track> tracks = trackList, queue = trackList;
        if(parentId.equals(Constants.CURRENT_TRACK) && tracks!=null){
            if(exoPlayer.getCurrentWindowIndex()<0 || exoPlayer.getCurrentWindowIndex()>=tracks.size())
                return;
            Track track = tracks.get(exoPlayer.getCurrentWindowIndex());
            Bundle extras = new Bundle();
            extras.putParcelable(Constants.TRACK,track);
            MediaDescriptionCompat descriptionCompat = new MediaDescriptionCompat.Builder()
                    .setExtras(extras)
                    .setMediaId(track.getPath())
                    .setTitle(track.getTitle())
                    .setMediaUri(Uri.parse(track.getPath()))
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(descriptionCompat,MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            result.sendResult(Collections.singletonList(mediaItem));
        }
        else if(parentId.contains(Constants.ALBUM_TRACK) || parentId.contains(Constants.ARTIST_TRACK)){
            TrackProvider trackProvider = new TrackProvider(getApplicationContext(), options);
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            List<Track> trackList = MediaUtils.getTracksForPage(trackProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(trackList);
            result.sendResult(mediaItems);
        }
        else if(parentId.equals(Constants.PLAYLIST_TRACK)){
            TrackProvider trackProvider = new TrackProvider(getApplicationContext(), options);
            List<Track> trackList = trackProvider.getAllTracks();
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(trackList);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(Constants.TRACK)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                trackProvider = new TrackProvider(getApplicationContext());
            List<Track> trackList = MediaUtils.getTracksForPage(trackProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(trackList);
            result.sendResult(mediaItems);
        }
        else if(parentId.equals(Constants.PLAY_QUEUE)){
            if(queue!=null){
                List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(queue);
                result.sendResult(mediaItems);
            }
            else
                result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        }
        else if(parentId.contains(Constants.ALBUM)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                albumProvider = new AlbumProvider(getApplicationContext());
            List<Album> albumList = MediaUtils.getAlbumsForPage(albumProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromAlbums(albumList);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(Constants.ARTIST)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                artistProvider = new ArtistProvider(getApplicationContext());
            List<Artist> artistList = MediaUtils.getArtistsForPage(artistProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromArtists(artistList);
            result.sendResult(mediaItems);
        }
        else if(parentId.contains(Constants.PLAYLIST)){
            int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if(page==0)
                playlistProvider = new PlaylistProvider(getApplicationContext());
            List<PlayList> playLists = MediaUtils.getPlayListsForPage(playlistProvider,page,pageSize);
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromPlayLists(playLists);
            result.sendResult(mediaItems);
        }
    }

    @Override
    public void onCustomAction(@NonNull String action, final Bundle extras, @NonNull final Result<Bundle> result) {
        result.detach();
        switch (action){
            case Constants.ADD_QUEUE:
                List<Track> tracks = extras.getParcelableArrayList(Constants.PLAY_QUEUE);
                if (tracks != null) {
                    trackList.addAll(tracks);
                    setMediaSourceFromTrackList();
                    new AddToRecentQueueAsyncTask(this, trackList, new AddToRecentQueueAsyncTask.TaskCompletedListener() {
                        @Override
                        public void onTaskCompleted() {
                            result.sendResult(extras);
                        }
                    }).execute();
                }
                break;
        }
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {}

    public static class LoadQueueAsyncTask extends AsyncTask<Void, Void, Bundle> {
        private WeakReference<Context> contextRef;
        private QueueLoadedListener mListener;

        LoadQueueAsyncTask(Context context, QueueLoadedListener mListener) {
            this.contextRef = new WeakReference<>(context);
            this.mListener = mListener;
        }

        public interface QueueLoadedListener {
            void onQueueLoaded(Bundle result);
        }

        @Override
        protected Bundle doInBackground(Void... voids) {
            try {
                FileInputStream inputStream = contextRef.get().openFileInput(Constants.PLAY_LIST_FILE);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String fileData = reader.readLine();
                inputStream.close();
                reader.close();
                Type listOfString = new TypeToken<List<String>>(){}.getType();
                Gson gson = new Gson();
                ArrayList<String> audioIdList = gson.fromJson(fileData, listOfString);
                Bundle params = new Bundle();
                params.putStringArrayList(Constants.AUDIO_ID_ARRAYLIST,audioIdList);
                TrackProvider trackProvider = new TrackProvider(contextRef.get(),params);
                List<Track> trackList = trackProvider.getAllTracks();
                int currIndex = contextRef.get().getSharedPreferences(Constants.SHARED_PREF,0).getInt(Constants.PLAYLIST_RESUME_INDEX,0);
                params.clear();
                params.putInt(Constants.PLAYLIST_RESUME_INDEX,currIndex);
                params.putParcelableArrayList(Constants.PLAY_QUEUE, (ArrayList<? extends Parcelable>) trackList);
                return params;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bundle result) {
            super.onPostExecute(result);
            mListener.onQueueLoaded(result);
        }
    }

    public static class AddToRecentQueueAsyncTask extends AsyncTask<Void,Void,Void> {
        private WeakReference<Context> contextRef;
        private List<Track> trackList;
        private TaskCompletedListener mListener;

        public interface TaskCompletedListener{
            void onTaskCompleted();
        }

        AddToRecentQueueAsyncTask(Context context, List<Track> trackList, TaskCompletedListener mListener) {
            this.contextRef = new WeakReference<>(context);
            this.trackList = trackList;
            this.mListener = mListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<String> audioIdList = new ArrayList<>();
            for(Track track : trackList){
                audioIdList.add(String.valueOf(track.getAudioId()));
            }
            Gson gson = new Gson();
            Type listOfLong = new TypeToken<List<Long>>(){}.getType();
            String fileContents = gson.toJson(audioIdList,listOfLong);
            FileOutputStream outputStream;
            try {
                outputStream = contextRef.get().openFileOutput(Constants.PLAY_LIST_FILE, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListener.onTaskCompleted();
        }
    }
}
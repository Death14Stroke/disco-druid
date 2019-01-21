package com.andruid.magic.discodruid;

import android.app.Notification;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;

import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.model.Artist;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.provider.AlbumProvider;
import com.andruid.magic.discodruid.provider.ArtistProvider;
import com.andruid.magic.discodruid.provider.PlaylistProvider;
import com.andruid.magic.discodruid.provider.TrackProvider;
import com.andruid.magic.discodruid.util.MediaUtils;
import com.andruid.magic.discodruid.util.NotificationHelper;
import com.andruid.magic.discodruid.widget.MusicWidget;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
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

public class BackgroundAudioService extends MediaBrowserServiceCompat {
    private MediaSessionCompat mediaSessionCompat;
    private MediaSessionCallback mediaSessionCallback;
    private SimpleExoPlayer exoPlayer;
    private NotificationCompat.Builder notificationBuilder;
    private ArrayList<Track> trackList = new ArrayList<>();
    private ConcatenatingMediaSource concatenatingMediaSource;
    private DataSource.Factory dataSourceFactory;
    private Intent mediaButtonIntent;
    private TrackProvider trackProvider;
    private AlbumProvider albumProvider;
    private ArtistProvider artistProvider;
    private PlaylistProvider playlistProvider;
    private Track currentTrack;
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
                return MediaUtils.getMediaDescription(BackgroundAudioService.this,trackList.get(windowIndex));
            }
        });
        mediaSessionConnector.setPlayer(exoPlayer,null);
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
                int icon = android.R.drawable.ic_media_play;
                if(playWhenReady)
                    icon = android.R.drawable.ic_media_pause;
                updateWidgetButton(android.R.drawable.ic_media_play);
                MediaMetadataCompat metadataCompat = mediaSessionCompat.getController().getMetadata();
                if(metadataCompat==null)
                    return;
                notificationBuilder = NotificationHelper.buildNotification(icon,
                        BackgroundAudioService.this, metadataCompat, mediaSessionCompat.getSessionToken());
                Notification notification = Objects.requireNonNull(notificationBuilder).build();
                if(playWhenReady)
                    startForeground(Constants.MEDIA_NOTI_ID,notification);
                else
                    stopForeground(false);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                if(reason==Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                        reason==Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason==Player.DISCONTINUITY_REASON_INTERNAL) {
                    setSong(exoPlayer.getCurrentWindowIndex());
                    updateWidget(trackList.get(exoPlayer.getCurrentWindowIndex()));
                }
            }
        });
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes,true);
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

    private void setSong(int pos) {
        if(pos>=trackList.size() || pos<0)
            return;
        Track track = trackList.get(pos);
        currentTrack = track;
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

    private void updateWidgetButton(int resource) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.music_widget);
        ComponentName appWidget = new ComponentName(BackgroundAudioService.this, MusicWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(BackgroundAudioService.this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);
        views.setImageViewResource(R.id.widget_play_btn, resource);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
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

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        Bundle extras = new Bundle();
        extras.putBoolean(BrowserRoot.EXTRA_OFFLINE,true);
        extras.putBoolean(BrowserRoot.EXTRA_RECENT,true);
        return new BrowserRoot(getString(R.string.app_name), extras);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaItem>> result) {
        result.detach();
        List<Track> tracks = new ArrayList<>(trackList);
        if(parentMediaId.equals(Constants.CURRENT_TRACK)){
            if(exoPlayer.getCurrentWindowIndex()<0 || exoPlayer.getCurrentWindowIndex()>=tracks.size())
                return;
            Track track = currentTrack;
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
        else if(parentMediaId.equalsIgnoreCase(Constants.TRACK)){
            if(trackProvider==null)
                trackProvider = new TrackProvider(getApplicationContext());
            List<Track> trackList = trackProvider.getAllTracks();
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(trackList);
            result.sendResult(mediaItems);
        }
        else if(parentMediaId.equalsIgnoreCase(Constants.PLAY_QUEUE)){
            if(!tracks.isEmpty()) {
                List<MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(tracks);
                result.sendResult(mediaItems);
            }
            else
                new LoadQueueAsyncTask(this, bundle -> {
                    if(bundle==null) {
                        Toast.makeText(getApplicationContext(),"No songs in queue",Toast.LENGTH_SHORT).show();
                        result.sendResult(new ArrayList<>());
                        return;
                    }
                    trackList = bundle.getParcelableArrayList(Constants.PLAY_QUEUE);
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
                    List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(new ArrayList<>(trackList));
                    result.sendResult(mediaItems);
                }).execute();
        }
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaItem>> result, @NonNull Bundle options) {
        result.detach();
        if(parentId.equalsIgnoreCase(Constants.ALBUM_TRACK) ||
                parentId.equalsIgnoreCase(Constants.ARTIST_TRACK) || parentId.equalsIgnoreCase(Constants.PLAYLIST_TRACK)){
            TrackProvider trackProvider = new TrackProvider(getApplicationContext(), options);
            List<Track> trackList = trackProvider.getAllTracks();
            List<MediaBrowserCompat.MediaItem> mediaItems = MediaUtils.getMediaItemsFromTracks(trackList);
            result.sendResult(mediaItems);
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
    public void onCustomAction(@NonNull String action, Bundle extras, @NonNull Result<Bundle> result) {
        super.onCustomAction(action, extras, result);
        result.detach();
        switch (action){
            case Constants.ADD_QUEUE:
                List<Track> tracks = extras.getParcelableArrayList(Constants.PLAY_QUEUE);
                if (tracks != null) {
                    trackList.addAll(tracks);
                    setMediaSourceFromTrackList();
                    new AddToRecentQueueAsyncTask(this, new ArrayList<>(trackList), () ->
                            result.sendResult(extras)).execute();
                }
                break;
        }
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            super.onPlay();
            if(trackList==null)
                return;
            exoPlayer.setPlayWhenReady(true);
            setSong(exoPlayer.getCurrentWindowIndex());
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING,exoPlayer.getCurrentWindowIndex());
            mediaSessionCompat.setActive(true);
            updateWidgetButton(android.R.drawable.ic_media_pause);
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
            updateWidgetButton(android.R.drawable.ic_media_play);
            unregisterReceiver(mNoisyReceiver);
            exoPlayer.setPlayWhenReady(false);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED,exoPlayer.getCurrentWindowIndex());
        }

        @Override
        public void onStop() {
            updateWidgetButton(android.R.drawable.ic_media_play);
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
                case Constants.REMOVE_QUEUE:
                    final int pos = extras.getInt(Constants.POSITION);
                    boolean update = false;
                    if(pos==exoPlayer.getCurrentWindowIndex())
                        update = true;
                    final boolean finalUpdate = update;
                    concatenatingMediaSource.removeMediaSource(pos, () -> {
                        trackList.remove(pos);
                        new AddToRecentQueueAsyncTask(BackgroundAudioService.this, trackList, () -> {
                            if(finalUpdate)
                                setSong(exoPlayer.getCurrentWindowIndex());
                            notifyChildrenChanged(Constants.PLAY_QUEUE);
                        }).execute();
                    });
                    break;
                case Constants.SET_QUEUE:
                    trackList = extras.getParcelableArrayList(Constants.PLAY_QUEUE);
                    if (trackList != null) {
                        setMediaSourceFromTrackList();
                        exoPlayer.prepare(concatenatingMediaSource);
                        exoPlayer.setPlayWhenReady(true);
                    }
                    new AddToRecentQueueAsyncTask(BackgroundAudioService.this, trackList, () ->
                            notifyChildrenChanged(Constants.PLAY_QUEUE)).execute();
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
                    new AddToRecentQueueAsyncTask(BackgroundAudioService.this, trackList, () ->
                            notifyChildrenChanged(Constants.PLAY_QUEUE)).execute();
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

    private static class AddToRecentQueueAsyncTask extends AsyncTask<Void,Void,Void> {
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

    private static class LoadQueueAsyncTask extends AsyncTask<Void, Void, Bundle> {
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
}
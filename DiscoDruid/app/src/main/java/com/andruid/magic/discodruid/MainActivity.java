package com.andruid.magic.discodruid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andruid.magic.discodruid.adapter.CustomPagerAdapter;
import com.andruid.magic.discodruid.adapter.TrackDetailAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.fragment.AlbumTracksDialog;
import com.andruid.magic.discodruid.fragment.ArtistTracksDialog;
import com.andruid.magic.discodruid.fragment.PlaylistTracksDialog;
import com.andruid.magic.discodruid.fragment.QueueDialogFragment;
import com.andruid.magic.discodruid.fragment.TrackFragment;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.services.BackgroundAudioService;
import com.andruid.magic.discodruid.util.MediaUtils;
import com.andruid.magic.discodruid.util.ReadContent;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;
import com.takusemba.multisnaprecyclerview.OnSnapListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class MainActivity extends AppCompatActivity implements TrackFragment.TrackClickListener,
        PlaylistTracksDialog.PlaylistDialogClickedListener, QueueDialogFragment.QueueDialogClickListener,
        AlbumTracksDialog.AlbumDialogClickedListener, ArtistTracksDialog.ArtistDialogClickedListener {

    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.track_detail_recyclerView) MultiSnapRecyclerView multiSnapRecyclerView;
    @BindView(R.id.track_detail_songName) TextView songTV;
    @BindView(R.id.track_detail_albumName) TextView albumTV;
    @BindView(R.id.track_detail_artistName) TextView artistTV;
    @BindView(R.id.track_detail_current_pos) TextView currentPosTV;
    @BindView(R.id.track_detail_duration) TextView durationTV;
    @BindView(R.id.track_nameTV) TextView songCollapsedTV;
    @BindView(R.id.track_artistTV) TextView artistCollapsedTV;
    @BindView(R.id.track_thumbnail) ImageView imageView;
    @BindView(R.id.bottom_sheet_arrow) ImageView arrowImageView;
    @BindView(R.id.track_detail_seekBar) SeekBar seekBar;
    @BindView(R.id.track_detail_playBtn) ImageButton playButton;
    @BindView(R.id.track_playBtn) ImageButton playCollapsedBtn;
    @BindView(R.id.track_detail_shuffleBtn) ImageButton shuffleBtn;
    @BindView(R.id.track_detail_repeatBtn) ImageButton repeatBtn;
    @BindView(R.id.bottom_sheet_ll) LinearLayout bottomSheetLL;
    @BindView(R.id.bottom_collapsed_view) View bottomSheetCollapsedView;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private TrackDetailAdapter trackDetailAdapter;
    private String mode = "";
    private boolean userScroll = false;
    private MediaBrowserCompat mediaBrowserCompat;
    private MediaControllerCompat mediaControllerCompat;
    private Handler mSeekBarUpdateHandler = new Handler();
    private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(seekBar.getProgress()+1);
            mSeekBarUpdateHandler.postDelayed(this, 1000);
        }
    };

    @OnClick({R.id.track_detail_playBtn, R.id.track_playBtn})
    public void play(){
        mediaControllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    @OnClick(R.id.track_detail_prevBtn)
    public void prev(){
        mediaControllerCompat.getTransportControls().skipToPrevious();
    }

    @OnClick(R.id.track_detail_nextBtn)
    public void next(){
        mediaControllerCompat.getTransportControls().skipToNext();
    }

    @OnClick(R.id.track_detail_shuffleBtn)
    public void shuffle(){
        int mode = mediaControllerCompat.getShuffleMode();
        if(mode==PlaybackStateCompat.SHUFFLE_MODE_ALL)
            mediaControllerCompat.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
        else if(mode==PlaybackStateCompat.REPEAT_MODE_NONE)
            mediaControllerCompat.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
    }

    @OnClick(R.id.track_detail_repeatBtn)
    public void repeat(){
        int mode = mediaControllerCompat.getRepeatMode();
        if(mode==PlaybackStateCompat.REPEAT_MODE_NONE)
            mediaControllerCompat.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
        else if(mode==PlaybackStateCompat.REPEAT_MODE_ALL)
            mediaControllerCompat.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
        else if(mode==PlaybackStateCompat.REPEAT_MODE_ONE)
            mediaControllerCompat.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
    }

    @OnClick({R.id.bottom_sheet_arrow, R.id.bottom_sheet_ll})
    public void toggleBottomSheet(){
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        else if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @OnTouch(R.id.track_detail_recyclerView)
    public boolean rvTouch(){
        userScroll = true;
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addListeners(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        multiSnapRecyclerView.setLayoutManager(layoutManager);
        multiSnapRecyclerView.setAdapter(trackDetailAdapter);
        multiSnapRecyclerView.setOnSnapListener(new OnSnapListener() {
            @Override
            public void snapped(int position) {
                if(userScroll) {
                    seekBar.setProgress(0);
                    mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
                    mediaControllerCompat.getTransportControls().skipToQueueItem(position);
                    userScroll = false;
                }
            }
        });
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLL);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                switch (state){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        arrowImageView.setImageResource(android.R.drawable.arrow_up_float);
                        bottomSheetCollapsedView.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        arrowImageView.setImageResource(android.R.drawable.arrow_down_float);
                        bottomSheetCollapsedView.setVisibility(View.GONE);
                        break;
                }
            }
            @Override
            public void onSlide(@NonNull View view, float v) {}
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentPosTV.setText(MediaUtils.getTimeString(i));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                currentPosTV.setText(MediaUtils.getTimeString(seekBar.getProgress()));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaControllerCompat.getTransportControls().seekTo(seekBar.getProgress()*1000);
            }
        });
        CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(customPagerAdapter);
        viewPager.setOffscreenPageLimit(Constants.NUMBER_OF_TABS-1);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setButtonStates(int state) {
        if(state==PlaybackStateCompat.STATE_PLAYING){
            playButton.setImageResource(android.R.drawable.ic_media_pause);
            playCollapsedBtn.setImageResource(android.R.drawable.ic_media_pause);
        }
        else{
            playButton.setImageResource(android.R.drawable.ic_media_play);
            playCollapsedBtn.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void setActivityUI(Track track) {
        songTV.setText(track.getTitle());
        albumTV.setText(track.getAlbum());
        artistTV.setText(track.getArtist());
        seekBar.setMax(Integer.parseInt(String.valueOf(track.getDuration())));
        durationTV.setText(MediaUtils.getTimeString(seekBar.getMax()));
        if(track.getAlbumArtUri()!=null) {
            Glide.with(MainActivity.this)
                    .load(track.getAlbumArtUri())
                    .apply(new RequestOptions().transform(new RoundedCorners(50)))
                    .into(imageView);
        }
        else
            Glide.with(MainActivity.this)
                    .load(R.drawable.music)
                    .into(imageView);
        artistCollapsedTV.setText(track.getArtist());
        songCollapsedTV.setText(track.getTitle());
        int pos;
        List<Track> trackList = trackDetailAdapter.getData();
        for(pos=0;pos<trackList.size();pos++){
            if(track.getAudioId()==trackList.get(pos).getAudioId())
                break;
        }
        multiSnapRecyclerView.scrollToPosition(pos);
    }

    private void changePlayList(List<Track> trackList) {
        Bundle params = new Bundle();
        params.putParcelableArrayList(Constants.PLAY_QUEUE, new ArrayList<>(trackList));
        mediaControllerCompat.sendCommand(Constants.SET_QUEUE, params, null);
    }

    private void setRepeatButton(int rMode) {
        if(rMode==PlaybackStateCompat.REPEAT_MODE_ALL)
            repeatBtn.setImageResource(R.drawable.ic_repeat_all);
        else if(rMode==PlaybackStateCompat.REPEAT_MODE_NONE)
            repeatBtn.setImageResource(R.drawable.ic_repeat_off);
        else if(rMode==PlaybackStateCompat.REPEAT_MODE_ONE)
            repeatBtn.setImageResource(R.drawable.ic_repeat_one);
    }

    private void setShuffleButton(int sMode) {
        if(sMode==PlaybackStateCompat.SHUFFLE_MODE_ALL)
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
        else if(sMode==PlaybackStateCompat.REPEAT_MODE_NONE)
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);

    }

    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mediaControllerCompat = new MediaControllerCompat(MainActivity.this, mediaBrowserCompat.getSessionToken());
                mediaControllerCompat.registerCallback(callback);
                MediaControllerCompat.setMediaController(MainActivity.this,mediaControllerCompat);
                if(Objects.equals(getIntent().getAction(), Intent.ACTION_VIEW)){
                    Log.d("intentview",getIntent().toString());
                    mediaControllerCompat.getTransportControls().playFromUri(getIntent().getData(),null);
                    return;
                }
                mediaBrowserCompat.subscribe(Constants.PLAY_QUEUE,new Bundle(),subscriptionCallback);
                setShuffleButton(mediaControllerCompat.getShuffleMode());
                setRepeatButton(mediaControllerCompat.getRepeatMode());
                PlaybackStateCompat ps = mediaControllerCompat.getPlaybackState();
                if(ps!=null){
                    int state = ps.getState();
                    Bundle extras = ps.getExtras();
                    if(state == PlaybackStateCompat.STATE_PLAYING && extras!=null){
                        int pos = Integer.parseInt(String.valueOf(extras.getLong(Constants.SEEK_POSITION)));
                        seekBar.setProgress(pos/1000);
                        mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar,1000);
                    }
                    setButtonStates(state);
                }
            } catch( RemoteException e ) {
                e.printStackTrace();
            }
        }
    };

    private MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
            switch (parentId){
                case Constants.PLAY_QUEUE:
                    List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                    trackDetailAdapter.setData(trackList);
                    trackDetailAdapter.notifyDataSetChanged();
                    if(trackList.isEmpty()){
                        seekBar.setProgress(0);
                        mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
                    }
                    MediaMetadataCompat metadataCompat = mediaControllerCompat.getMetadata();
                    if(metadataCompat!=null) {
                        Track track = MediaUtils.getTrackFromMetaData(metadataCompat);
                        setActivityUI(track);
                    }
                    break;
            }
        }
    };

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            Track track = MediaUtils.getTrackFromMetaData(metadata);
            setActivityUI(track);
            seekBar.setProgress(0);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            super.onShuffleModeChanged(shuffleMode);
            setShuffleButton(shuffleMode);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
            setRepeatButton(repeatMode);
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if(state==null){
                return;
            }
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_STOPPED:
                case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                    seekBar.setProgress(0);
                    mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
                    break;
                case PlaybackStateCompat.STATE_BUFFERING:
                    seekBar.setProgress(0);
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    setButtonStates(PlaybackStateCompat.STATE_PLAYING);
                    int pos = Integer.parseInt(String.valueOf(state.getPosition()));
                    seekBar.setProgress(pos/1000);
                    mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar,0);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    setButtonStates(PlaybackStateCompat.STATE_PAUSED);
                    pos = Integer.parseInt(String.valueOf(state.getPosition()));
                    seekBar.setProgress(pos/1000);
                    mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        addListeners();
        trackDetailAdapter = new TrackDetailAdapter(this);
        multiSnapRecyclerView.setAdapter(trackDetailAdapter);
        mediaBrowserCompat = new MediaBrowserCompat(MainActivity.this,
                new ComponentName(MainActivity.this, BackgroundAudioService.class),
                connectionCallback, null);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(!mediaBrowserCompat.isConnected())
                            mediaBrowserCompat.connect();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this,response.getPermissionName()+" permission denied. Stopping activity",Toast.LENGTH_SHORT).show();
                        finishAffinity();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaControllerCompat!=null)
            mediaControllerCompat.unregisterCallback(callback);
        if(mediaBrowserCompat!=null)
            mediaBrowserCompat.disconnect();
    }

    @Override
    public void onTrackClicked(List<Track> trackList, int pos) {
        changePlayList(trackList);
        mode = Constants.MODE_ALL_TRACKS;
        mediaControllerCompat.getTransportControls().skipToQueueItem(pos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_create:
                showNameInputDialog();
                break;
            case R.id.menu_queue:
                QueueDialogFragment dialog = new QueueDialogFragment();
                FragmentTransaction fragmentTransaction = null;
                if (getSupportFragmentManager() != null) {
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                }
                if (fragmentTransaction != null) {
                    dialog.show(fragmentTransaction,Constants.QUEUE_DIALOG);
                }
                break;
        }
        return true;
    }

    private void showNameInputDialog() {
        final EditText input = new EditText(this);
        input.setHint("Playlist name");
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.setMarginEnd(margin);
        params.setMarginStart(margin);
        input.setLayoutParams(params);
        container.addView(input);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Create New Playlist")
                .setView(container)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CreatePlayListAsyncTask(getApplicationContext(),
                                input.getText().toString().trim()).execute();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    @Override
    public void onQueueTrackClicked(int position) {
        mediaControllerCompat.getTransportControls().skipToQueueItem(position);
    }

    @Override
    public void onQueueTrackRemoved(int position) {
        trackDetailAdapter.remove(position);
        Bundle params = new Bundle();
        params.putInt(Constants.POSITION,position);
        mediaControllerCompat.sendCommand(Constants.REMOVE_QUEUE,params,null);
    }

    @Override
    public void onQueueTrackDragged(int from, int to) {
        trackDetailAdapter.move(from,to);
        Bundle params = new Bundle();
        params.putInt(Constants.FROM,from);
        params.putInt(Constants.TO,to);
        mediaControllerCompat.sendCommand(Constants.DRAG_QUEUE,params,null);
    }

    private void changeMode(String modeNew, List<Track> trackList, int pos){
        seekBar.setProgress(0);
        mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
        if(!mode.equalsIgnoreCase(modeNew)) {
            changePlayList(trackList);
            mode = modeNew;
        }
        mediaControllerCompat.getTransportControls().skipToQueueItem(pos);
    }

    @Override
    public void onAlbumDialogClicked(List<Track> trackList, int pos) {
        String albumId = trackList.get(pos).getAlbumId();
        String modeNew = Constants.MODE_ALBUM.concat(albumId);
        changeMode(modeNew,trackList,pos);
    }

    @Override
    public void onArtistDialogClicked(List<Track> trackList, int pos) {
        String artist = trackList.get(pos).getArtist();
        String modeNew = Constants.MODE_ARTIST.concat(artist);
        changeMode(modeNew,trackList,pos);
    }

    @Override
    public void onPlaylistDialogClicked(List<Track> trackList, int pos, Bundle params) {
        long playListId = params.getLong(Constants.ARG_PLAYLIST);
        String modeNew = Constants.MODE_PLAYLIST.concat(String.valueOf(playListId));
        changeMode(modeNew,trackList,pos);
    }

    public static class CreatePlayListAsyncTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<Context> contextRef;
        private String name;

        CreatePlayListAsyncTask(Context context, String name) {
            this.contextRef = new WeakReference<>(context);
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReadContent.createNewPlayList(contextRef.get(),name);
            return null;
        }
    }
}
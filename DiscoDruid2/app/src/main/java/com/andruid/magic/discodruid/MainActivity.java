package com.andruid.magic.discodruid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.andruid.magic.discodruid.fragment.QueueDialogFragment;
import com.andruid.magic.discodruid.fragment.TrackFragment;
import com.andruid.magic.discodruid.model.Track;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.andruid.magic.discodruid.adapter.CustomPagerAdapter;
import com.andruid.magic.discodruid.adapter.TrackDetailAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.util.MediaUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class MainActivity extends AppCompatActivity implements TrackFragment.TrackClickListener {
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.track_detail_recyclerView) RecyclerView recyclerView;
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
    private AlertDialog alertDialog, inputDialog;
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
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
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
        else if(mode== PlaybackStateCompat.REPEAT_MODE_ONE)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        alertDialog = MediaUtils.getPermissionsDialogBuilder(this).create();
        inputDialog = MediaUtils.getPlaylistDialogBuilder(this).create();
        setViewPager();
        setRecyclerView();
        setBottomSheet();
        setSeekBar();
        trackDetailAdapter = new TrackDetailAdapter(this);
        recyclerView.setAdapter(trackDetailAdapter);
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
                        if(!alertDialog.isShowing())
                            alertDialog.show();
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


    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mediaControllerCompat = new MediaControllerCompat(MainActivity.this, mediaBrowserCompat.getSessionToken());
                mediaControllerCompat.registerCallback(callback);
                MediaControllerCompat.setMediaController(MainActivity.this,mediaControllerCompat);
                if(Intent.ACTION_VIEW.equalsIgnoreCase(getIntent().getAction())){
                    mediaControllerCompat.getTransportControls().playFromUri(getIntent().getData(),null);
                    return;
                }
                mediaBrowserCompat.subscribe(Constants.PLAY_QUEUE,subscriptionCallback);
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
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            switch (parentId){
                case Constants.PLAY_QUEUE:
                    List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                    trackDetailAdapter.setTrackList(trackList);
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

    private void changePlayList(List<Track> trackList) {
        Bundle params = new Bundle();
        params.putParcelableArrayList(Constants.PLAY_QUEUE, new ArrayList<>(trackList));
        mediaControllerCompat.sendCommand(Constants.SET_QUEUE, params, null);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_create:
                if(!inputDialog.isShowing())
                    inputDialog.show();
                break;
            case R.id.menu_queue:
                QueueDialogFragment dialog = new QueueDialogFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                dialog.show(fragmentTransaction,Constants.QUEUE_DIALOG);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    private void setSeekBar(){
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
    }

    private void setBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLL);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                switch (state) {
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
    }

    private void setRecyclerView() {
        trackDetailAdapter = new TrackDetailAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(trackDetailAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    View centerView = snapHelper.findSnapView(layoutManager);
                    int position = 0;
                    if (centerView != null)
                        position = layoutManager.getPosition(centerView);
                    if(userScroll) {
                        seekBar.setProgress(0);
                        mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
                        mediaControllerCompat.getTransportControls().skipToQueueItem(position);
                        userScroll = false;
                    }
                }
            }
        });
    }

    private void setViewPager() {
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
        if(track.getAlbumArtUri()!=null)
            Glide.with(MainActivity.this)
                    .load(track.getAlbumArtUri())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.music)
                            .transform(new RoundedCorners(50)))
                    .into(imageView);
        else
            Glide.with(MainActivity.this)
                    .load(R.drawable.music)
                    .into(imageView);
        artistCollapsedTV.setText(track.getArtist());
        songCollapsedTV.setText(track.getTitle());
        List<Track> trackList = trackDetailAdapter.getTrackList();
        int pos;
        for(pos=0;pos<trackList.size();pos++){
            if(track.getAudioId()==trackList.get(pos).getAudioId())
                break;
        }
        recyclerView.scrollToPosition(pos);
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

    @Override
    public void onTrackClicked(List<Track> trackList, int pos) {
        changeMode(Constants.MODE_ALL_TRACKS,trackList,pos);
        mediaControllerCompat.getTransportControls().skipToQueueItem(pos);
    }

    public static class CreatePlayListAsyncTask extends AsyncTask<Void,Void,Void> {
        private WeakReference<Context> contextRef;
        private String name;

        public CreatePlayListAsyncTask(Context context, String name) {
            this.contextRef = new WeakReference<>(context);
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MediaUtils.createNewPlayList(contextRef.get(),name);
            return null;
        }
    }
}
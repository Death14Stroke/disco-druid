package com.andruid.magic.discodruid.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.*
import com.andruid.magic.discodruid.databinding.ActivityMainBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.POSITION_ALBUMS
import com.andruid.magic.discodruid.ui.adapter.POSITION_TRACKS
import com.andruid.magic.discodruid.ui.adapter.TabsAdapter
import com.andruid.magic.discodruid.ui.adapter.TrackDetailAdapter
import com.andruid.magic.discodruid.ui.custom.DepthPageTransformer
import com.andruid.magic.discodruid.ui.fragment.TrackFragment
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.min

class MainActivity : AppCompatActivity(), LifecycleObserver, TrackFragment.ITracksListener {
    private val askStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                initTabs()
                initBottomSheet()

                if (!mediaBrowserCompat.isConnected)
                    mediaBrowserCompat.connect()
            } else
                finish()
        }
    private val mbSubscriptionCallback = MBSubscriptionCallback()
    private val mbConnectionCallback = MBConnectionCallback()
    private val mediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            mbConnectionCallback,
            null
        )
    }
    private val trackSelectReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_SELECT_TRACK) {
                val track = intent.extras?.getParcelable<Track>(EXTRA_TRACK) ?: return
                val albumId = intent.extras?.getString(EXTRA_ALBUM_ID) ?: ""
                val extras = bundleOf(
                    EXTRA_TRACK_MODE to MODE_ALBUM_TRACKS,
                    EXTRA_ALBUM_ID to albumId,
                    EXTRA_TRACK to track
                )
                mediaBrowserCompat.sendCustomAction(CMD_PREPARE_QUEUE, extras, null)
            }
        }
    }
    private val trackDetailAdapter by lazy { TrackDetailAdapter() }
    private val mediaControllerCallback = MediaControllerCallback()
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as MusicService.ServiceBinder?)?.service
            binding.bottomSheetLayout.playerControlView.player = service?.exoPlayer
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            binding.bottomSheetLayout.playerControlView.player = null
        }
    }

    private var service: MusicService? = null

    private lateinit var mediaControllerCompat: MediaControllerCompat
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        askStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        initViewPager()
        initListeners()

        lifecycle.addObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaBrowserCompat.isConnected)
            mediaBrowserCompat.disconnect()
    }

    override fun onTrackClicked(track: Track, position: Int) {
        Log.d("clickLog", "track clicked in activity $position = ${track.title}")
        val extras = bundleOf(EXTRA_TRACK_MODE to MODE_ALL_TRACKS, EXTRA_TRACK to track)
        mediaBrowserCompat.sendCustomAction(CMD_PREPARE_QUEUE, extras, null)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun bindService() {
        val intent = Intent(this, MusicService::class.java)
            .setAction(ACTION_GET_INSTANCE)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(trackSelectReceiver, IntentFilter(ACTION_SELECT_TRACK))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun unbindService() {
        unbindService(serviceConnection)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(trackSelectReceiver)
    }

    @SuppressLint("SwitchIntDef")
    private fun initListeners() {
        binding.bottomSheetLayout.playBtn.setOnClickListener {
            mediaControllerCompat.dispatchMediaButtonEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                )
            )
        }

        val sheetBehaviour = BottomSheetBehavior.from(binding.bottomSheetLayout.motionLayout)
        binding.bottomSheetLayout.bottomSheetArrow.setOnClickListener {
            when (sheetBehaviour.state) {
                BottomSheetBehavior.STATE_EXPANDED ->
                    sheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.STATE_COLLAPSED ->
                    sheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun initTabs() {
        binding.viewPager.adapter = TabsAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                POSITION_TRACKS -> getString(R.string.tab_tracks)
                POSITION_ALBUMS -> getString(R.string.tab_albums)
                else -> getString(R.string.tab_artists)
            }
        }.attach()
    }

    private fun initViewPager() {
        binding.bottomSheetLayout.trackDetailViewpager.apply {
            adapter = trackDetailAdapter
            offscreenPageLimit = 1
            setPageTransformer(DepthPageTransformer())
        }
    }

    private fun initBottomSheet() {
        val sheetBehaviour = BottomSheetBehavior.from(binding.bottomSheetLayout.motionLayout)
        sheetBehaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.bottomSheetLayout.motionLayout.progress = slideOffset

                var alpha = min(1f - 2f * slideOffset, 1f)
                binding.bottomSheetLayout.apply {
                    songNameTv.alpha = alpha
                    songAlbumTv.alpha = alpha
                    playBtn.alpha = alpha
                }

                alpha = 2f * (slideOffset - 0.5f)
                binding.bottomSheetLayout.apply {
                    expandedTrackTv.alpha = alpha
                    expandedAlbumTv.alpha = alpha
                    expandedArtistTv.alpha = alpha
                    playerControlView.alpha = alpha
                }

                binding.bottomSheetLayout.bottomSheetArrow.rotation = slideOffset * 180
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("pagerLog", "expanded state")
                        binding.bottomSheetLayout.trackDetailViewpager.isUserInputEnabled = true
                    }
                    else -> {
                        Log.d("pagerLog", "other state")
                        binding.bottomSheetLayout.trackDetailViewpager.isUserInputEnabled = false
                    }
                }
            }
        })
    }

    private fun setPlayButton(@DrawableRes icon: Int) {
        binding.bottomSheetLayout.playBtn.setImageResource(icon)
    }

    private fun updateUI(track: Track) {
        binding.bottomSheetLayout.apply {
            songNameTv.text = track.title
            songAlbumTv.text = track.album

            expandedTrackTv.text = track.title
            expandedAlbumTv.text = track.album
            expandedArtistTv.text = track.artist
        }
    }

    private inner class MBConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()

            mediaControllerCompat =
                MediaControllerCompat(this@MainActivity, mediaBrowserCompat.sessionToken).apply {
                    registerCallback(mediaControllerCallback)
                    MediaControllerCompat.setMediaController(this@MainActivity, this)
                }

            mediaBrowserCompat.subscribe(MB_PLAY_QUEUE, bundleOf(), mbSubscriptionCallback)
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            mediaBrowserCompat.unsubscribe(MB_PLAY_QUEUE)
        }
    }

    private inner class MBSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle
        ) {
            super.onChildrenLoaded(parentId, children, options)
            if (parentId == MB_PLAY_QUEUE) {
                val tracks = children.map { mediaItem -> mediaItem.toTrack() }
                Log.d("queueLog", "queueLoaded")
                trackDetailAdapter.submitList(tracks)
            }
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            val track = metadata?.toTrack() ?: return
            if (track.title == "Loading" || track.artist == "Loading" || track.album == "Loading")
                return

            updateUI(track)
        }

        @SuppressLint("SwitchIntDef")
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> setPlayButton(R.drawable.exo_controls_pause)
                PlaybackStateCompat.STATE_PAUSED -> setPlayButton(R.drawable.exo_controls_play)
            }
        }
    }
}
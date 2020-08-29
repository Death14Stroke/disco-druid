package com.andruid.magic.discodruid.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.ACTION_GET_INSTANCE
import com.andruid.magic.discodruid.data.ACTION_PREPARE_QUEUE
import com.andruid.magic.discodruid.data.EXTRA_TRACK_MODE
import com.andruid.magic.discodruid.data.MODE_ALL_TRACKS
import com.andruid.magic.discodruid.databinding.ActivityMainBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.POSITION_ALBUMS
import com.andruid.magic.discodruid.ui.adapter.POSITION_TRACKS
import com.andruid.magic.discodruid.ui.adapter.TabsAdapter
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.min

class MainActivity : AppCompatActivity(), LifecycleObserver {
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
    private val mbConnectionCallback = MBConnectionCallback()
    private val mediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            mbConnectionCallback,
            null
        )
    }
    private val mediaControllerCallback = MediaControllerCallback()
    private var service: MusicService? = null
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

    private lateinit var mediaControllerCompat: MediaControllerCompat
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        askStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        initListeners()

        lifecycle.addObserver(this)
    }

    private fun initListeners() {
        // TODO: remove this
        binding.bottomSheetLayout.thumbnailImage.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
                .setAction(ACTION_PREPARE_QUEUE)
                .putExtra(EXTRA_TRACK_MODE, MODE_ALL_TRACKS)
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaBrowserCompat.isConnected)
            mediaBrowserCompat.disconnect()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun bindService() {
        val intent = Intent(this, MusicService::class.java)
            .setAction(ACTION_GET_INSTANCE)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun unbindService() {
        unbindService(serviceConnection)
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

            override fun onStateChanged(bottomSheet: View, newState: Int) {}
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
        }
    }

    private inner class MBSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

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
                PlaybackStateCompat.STATE_PLAYING -> setPlayButton(R.drawable.exo_controls_play)
                PlaybackStateCompat.STATE_PAUSED -> setPlayButton(R.drawable.exo_controls_pause)
            }
        }
    }
}
package com.andruid.magic.discodruid.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.ACTION_PREPARE_QUEUE
import com.andruid.magic.discodruid.data.EXTRA_TRACK_MODE
import com.andruid.magic.discodruid.data.MODE_ALL_TRACKS
import com.andruid.magic.discodruid.databinding.ActivityMainBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.POSITION_ALBUMS
import com.andruid.magic.discodruid.ui.adapter.POSITION_TRACKS
import com.andruid.magic.discodruid.ui.adapter.TabsAdapter
import com.andruid.magic.discodruid.ui.custom.MediaControlView
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.min

class MainActivity : AppCompatActivity() {
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
    private val uiHandler = Handler()
    private val sliderUpdateRunnable = object : Runnable {
        override fun run() {
            binding.bottomSheetLayout.mediaProgressView.updateProgress()
            uiHandler.postDelayed(this, 1000)
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
    }

    private fun initListeners() {
        // TODO: remove this
        binding.bottomSheetLayout.thumbnailImage.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
                .setAction(ACTION_PREPARE_QUEUE)
                .putExtra(EXTRA_TRACK_MODE, MODE_ALL_TRACKS)
            startService(intent)
        }

        binding.bottomSheetLayout.apply {
            mediaControlView.callback = object : MediaControlView.MediaControlsCallback {
                override fun onPlayPause() {
                    mediaControllerCompat.dispatchMediaButtonEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                        )
                    )
                }

                override fun onNext() {
                    mediaControllerCompat.transportControls.skipToNext()
                }

                override fun onPrevious() {
                    mediaControllerCompat.transportControls.skipToPrevious()
                }

                override fun onRepeat() {
                    mediaControllerCompat.transportControls.setRepeatMode(
                        when (mediaControllerCompat.repeatMode) {
                            PlaybackStateCompat.REPEAT_MODE_NONE -> PlaybackStateCompat.REPEAT_MODE_ALL
                            PlaybackStateCompat.REPEAT_MODE_ALL -> PlaybackStateCompat.REPEAT_MODE_ONE
                            else -> PlaybackStateCompat.REPEAT_MODE_NONE
                        }
                    )
                }

                override fun onShuffle() {
                    mediaControllerCompat.transportControls.setShuffleMode(
                        when (mediaControllerCompat.shuffleMode) {
                            PlaybackStateCompat.SHUFFLE_MODE_NONE -> PlaybackStateCompat.SHUFFLE_MODE_ALL
                            else -> PlaybackStateCompat.SHUFFLE_MODE_NONE
                        }
                    )
                }
            }
            mediaProgressView.addOnUserSlide { pos ->
                mediaController.transportControls.seekTo(pos * 1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaBrowserCompat.isConnected)
            mediaBrowserCompat.disconnect()
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
                binding.bottomSheetLayout.songNameTv.alpha = alpha
                binding.bottomSheetLayout.songAlbumTv.alpha = alpha
                binding.bottomSheetLayout.playBtn.alpha = alpha

                alpha = 2f * (slideOffset - 0.5f)
                binding.bottomSheetLayout.expandedTrackTv.alpha = alpha
                binding.bottomSheetLayout.expandedAlbumTv.alpha = alpha
                binding.bottomSheetLayout.expandedArtistTv.alpha = alpha
                binding.bottomSheetLayout.mediaControlView.alpha = alpha
                binding.bottomSheetLayout.mediaProgressView.alpha = alpha

                binding.bottomSheetLayout.bottomSheetArrow.rotation = slideOffset * 180
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })
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

            Log.d("metadataLog", "onMetadataChanged: duration = ${track.duration}")

            binding.bottomSheetLayout.mediaProgressView.apply {
                duration = track.duration
            }

            updateUI(track)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            super.onShuffleModeChanged(shuffleMode)
            binding.bottomSheetLayout.mediaControlView.setShuffleMode(shuffleMode)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            super.onRepeatModeChanged(repeatMode)
            binding.bottomSheetLayout.mediaControlView.setRepeatMode(repeatMode)
        }

        @SuppressLint("SwitchIntDef")
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            when (state?.state) {
                PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM -> {
                    binding.bottomSheetLayout.mediaProgressView.current = 0
                    uiHandler.removeCallbacks(sliderUpdateRunnable)
                }
                PlaybackStateCompat.STATE_BUFFERING -> {
                    binding.bottomSheetLayout.mediaProgressView.current = 0
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    Log.d("stateLog", "play position = ${state.position / 1000}")
                    binding.bottomSheetLayout.mediaControlView.setPlayState(PlaybackStateCompat.STATE_PLAYING)
                    binding.bottomSheetLayout.mediaProgressView.apply {
                        current = state.position / 1000
                    }
                    uiHandler.removeCallbacks(sliderUpdateRunnable)
                    uiHandler.post(sliderUpdateRunnable)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    Log.d("stateLog", "paused position = ${state.position / 1000}")
                    binding.bottomSheetLayout.mediaControlView.setPlayState(PlaybackStateCompat.STATE_PAUSED)
                    binding.bottomSheetLayout.mediaProgressView.apply {
                        current = state.position / 1000
                    }
                    uiHandler.removeCallbacks(sliderUpdateRunnable)
                }
            }
        }
    }
}
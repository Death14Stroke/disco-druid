package com.andruid.magic.discodruid.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.paging.map
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.data.*
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.ActivityPlaylistDetailsBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.TracksAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.viewbinding.viewBinding
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.TrackViewModel
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Playlist

class PlaylistDetailsActivity : AppCompatActivity() {
    val binding by viewBinding(ActivityPlaylistDetailsBinding::inflate)
    val playlist by lazy { intent.extras!!.getParcelable<Playlist>(EXTRA_PLAYLIST)!! }
    private val trackViewModel by viewModels<TrackViewModel> {
        val options = bundleOf(
            EXTRA_TRACK_MODE to MODE_PLAYLIST_TRACKS,
            EXTRA_PLAYLIST_ID to playlist.playlistId
        )
        BaseViewModelFactory { TrackViewModel(mediaBrowserCompat, options) }
    }
    private val tracksAdapter = TracksAdapter(this, lifecycleScope)
    private val mbSubscriptionCallback = MBSubscriptionCallback()
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()

                    Log.d("blinkLog", "mediaBrowser connected")

                    trackViewModel.tracksLiveData.observe(this@PlaylistDetailsActivity, {
                        Log.d("blinkLog", "livedata observer")
                        tracksAdapter.submitData(
                            lifecycle,
                            it.map { track -> TrackViewRepresentation.fromTrack(track) })
                    })

                    mediaBrowserCompat.subscribe(
                        MB_CURRENT_TRACK,
                        bundleOf(),
                        mbSubscriptionCallback
                    )
                }

                override fun onConnectionSuspended() {
                    super.onConnectionSuspended()
                    mediaBrowserCompat.unsubscribe(MB_CURRENT_TRACK, mbSubscriptionCallback)
                }
            },
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = playlist.name
        }
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        initRecyclerView()

        mediaBrowserCompat.connect()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = tracksAdapter
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(object : ItemClickListener(this@PlaylistDetailsActivity, this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)
                    val intent = Intent(ACTION_SELECT_TRACK)
                        .putExtra(EXTRA_TRACK_MODE, MODE_PLAYLIST_TRACKS)
                        .putExtra(EXTRA_PLAYLIST_ID, playlist.playlistId)
                        .putExtra(
                            EXTRA_TRACK,
                            tracksAdapter.getItemAtPosition(position) ?: return
                        )
                    LocalBroadcastManager.getInstance(this@PlaylistDetailsActivity)
                        .sendBroadcast(intent)
                }
            })
        }
    }

    private inner class MBSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle
        ) {
            super.onChildrenLoaded(parentId, children, options)
            if (parentId == MB_CURRENT_TRACK) {
                val track = children.map { mediaItem -> mediaItem.toTrack() }[0]
                Log.d("currentLog", "received ${track?.title ?: "null"}")
                tracksAdapter.currentTrack = track
                val position =
                    tracksAdapter.snapshot()
                        .indexOfFirst { t -> track?.audioId == t?.track?.audioId }
                if (position != -1)
                    tracksAdapter.notifyItemChanged(position)
            }
        }
    }
}
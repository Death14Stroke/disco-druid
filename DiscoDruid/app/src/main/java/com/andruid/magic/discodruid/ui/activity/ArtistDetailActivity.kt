package com.andruid.magic.discodruid.ui.activity

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.data.*
import com.andruid.magic.discodruid.data.model.UiModel
import com.andruid.magic.discodruid.databinding.ActivityArtistDetailBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.ArtistTracksAdapter
import com.andruid.magic.discodruid.ui.viewmodel.ArtistTracksViewModel
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Artist

class ArtistDetailActivity : AppCompatActivity() {
    private val artist by lazy { intent.extras!!.getParcelable<Artist>(EXTRA_ARTIST)!! }
    private val trackViewModel by viewModels<ArtistTracksViewModel> {
        val options = bundleOf(
            EXTRA_TRACK_MODE to MODE_ARTIST_TRACKS,
            EXTRA_ARTIST_ID to artist.artistId,
            EXTRA_ARTIST to artist.artist
        )
        BaseViewModelFactory { ArtistTracksViewModel(mediaBrowserCompat, options) }
    }
    private val artistTracksAdapter by lazy { ArtistTracksAdapter(this, lifecycleScope) }
    private val mbSubscriptionCallback = MBSubscriptionCallback()
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()
                    Log.d("blinkLog", "mediaBrowser connected")

                    trackViewModel.tracksLiveData.observe(this@ArtistDetailActivity, {
                        artistTracksAdapter.submitData(lifecycle, it)
                    })

                    mediaBrowserCompat.subscribe(
                        MB_CURRENT_TRACK,
                        bundleOf(),
                        mbSubscriptionCallback
                    )
                }
            },
            null
        )
    }

    private lateinit var binding: ActivityArtistDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = artist.artist
        }
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        initRecyclerView()

        mediaBrowserCompat.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowserCompat.disconnect()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = artistTracksAdapter
            itemAnimator = DefaultItemAnimator()
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
                artistTracksAdapter.currentTrack = track
                val position =
                    artistTracksAdapter.snapshot().indexOfFirst { uiModel ->
                        uiModel is UiModel.TrackModel && uiModel.track.audioId == track?.audioId
                    }
                if (position != -1)
                    artistTracksAdapter.notifyItemChanged(position)
            }
        }
    }
}
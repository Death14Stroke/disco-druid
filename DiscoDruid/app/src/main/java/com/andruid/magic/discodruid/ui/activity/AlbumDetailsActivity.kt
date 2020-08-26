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
import com.andruid.magic.discodruid.databinding.ActivityAlbumDetailsBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.TracksAdapter
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.TrackViewModel
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumDetailsActivity : AppCompatActivity() {
    private val album by lazy { intent.extras!!.getParcelable<Album>(EXTRA_ALBUM)!! }
    private val trackViewModel by viewModels<TrackViewModel> {
        val options = bundleOf(
            EXTRA_TRACK_MODE to MODE_ALBUM_TRACKS,
            EXTRA_ALBUM_ID to album.albumId
        )
        BaseViewModelFactory { TrackViewModel(mediaBrowserCompat, options) }
    }
    private val tracksAdapter = TracksAdapter(viewType = VIEW_TYPE_ALBUM_TRACKS)
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()

                    Log.d("blinkLog", "mediaBrowser connected")

                    trackViewModel.tracksLiveData.observe(this@AlbumDetailsActivity, {
                        Log.d("blinkLog", "livedata observer")
                        tracksAdapter.submitData(lifecycle, it)
                    })

                    lifecycleScope.launch { initViews() }
                }
            },
            null
        )
    }

    private lateinit var binding: ActivityAlbumDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = album.album
        }

        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        postponeEnterTransition()

        initRecyclerView()

        mediaBrowserCompat.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowserCompat.disconnect()
    }

    private suspend fun initViews() {
        val bitmap =
            withContext(Dispatchers.IO) { getAlbumArtBitmap(album.albumId) }
        binding.albumArtImage.setImageBitmap(bitmap)
        binding.albumArtImage.transitionName = "iv_${album.albumId}"
        startPostponedEnterTransition()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = tracksAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}
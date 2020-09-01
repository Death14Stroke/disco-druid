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
import com.andruid.magic.discodruid.databinding.ActivityArtistDetailBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.AlbumsAdapter
import com.andruid.magic.discodruid.ui.viewmodel.AlbumViewModel
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.medialoader.model.Artist

class ArtistDetailActivity : AppCompatActivity() {
    private val artist by lazy { intent.extras!!.getParcelable<Artist>(EXTRA_ARTIST)!! }
    private val albumsAdapter by lazy {
        AlbumsAdapter(
            this,
            lifecycleScope,
            VIEW_TYPE_ARTIST_ALBUMS
        )
    }
    private val albumViewModel by viewModels<AlbumViewModel> {
        val options = bundleOf(
            EXTRA_ALBUM_MODE to MODE_ARTIST_ALBUMS,
            EXTRA_ARTIST to artist.artist,
            EXTRA_ARTIST_ID to artist.artistId
        )
        BaseViewModelFactory { AlbumViewModel(mediaBrowserCompat, options) }
    }
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()
                    Log.d("blinkLog", "mediaBrowser connected")

                    albumViewModel.albumsLiveData.observe(this@ArtistDetailActivity) {
                        albumsAdapter.submitData(lifecycle, it)
                    }
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
            adapter = albumsAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}
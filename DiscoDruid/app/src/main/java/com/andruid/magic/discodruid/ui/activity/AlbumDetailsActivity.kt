package com.andruid.magic.discodruid.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.data.EXTRA_ALBUM
import com.andruid.magic.discodruid.data.VIEW_TYPE_ALBUM_TRACKS
import com.andruid.magic.discodruid.databinding.ActivityAlbumDetailsBinding
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
        BaseViewModelFactory { TrackViewModel(album.albumId) }
    }
    private val tracksAdapter = TracksAdapter(viewType = VIEW_TYPE_ALBUM_TRACKS)

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
        lifecycleScope.launch { initViews() }

        trackViewModel.tracksLiveData.observe(this, {
            tracksAdapter.submitData(lifecycle, it)
        })
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
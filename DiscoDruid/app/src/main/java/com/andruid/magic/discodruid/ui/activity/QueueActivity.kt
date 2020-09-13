package com.andruid.magic.discodruid.ui.activity

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.data.MB_PLAY_QUEUE
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.ActivityQueueBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.QueueTracksAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.viewbinding.viewBinding
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.QueueTracksViewModel

class QueueActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityQueueBinding::inflate)
    private val mbConnectionCallback = MBConnectionCallback()
    private val mediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            mbConnectionCallback,
            null
        )
    }
    private val trackViewModel by viewModels<QueueTracksViewModel> {
        BaseViewModelFactory { QueueTracksViewModel(mediaBrowserCompat) }
    }
    private val tracksAdapter = QueueTracksAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initRecyclerView()

        if (!mediaBrowserCompat.isConnected)
            mediaBrowserCompat.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaBrowserCompat.isConnected)
            mediaBrowserCompat.disconnect()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = tracksAdapter
            itemAnimator = DefaultItemAnimator()
            /*addOnItemTouchListener(object : ItemClickListener(this@QueueActivity, this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)
                    Log.d("clickLog", "track clicked")
                    tracksAdapter.getItemAtPosition(position)?.let { track ->
                        tracksAdapter.currentTrack = track
                        tracksAdapter.notifyItemChanged(position)
                    }
                }
            })*/
        }
    }

    private inner class MBConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            trackViewModel.tracksLiveData.observe(this@QueueActivity, { tracks ->
                Log.d("queueLog", "tracks observer in activity")
                tracksAdapter.submitList(tracks.map { track ->
                    TrackViewRepresentation.fromTrack(track)
                }) {
                    Log.d("queueLog", "tracks adapter submitted")
                }
            })
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            mediaBrowserCompat.unsubscribe(MB_PLAY_QUEUE)
        }
    }
}
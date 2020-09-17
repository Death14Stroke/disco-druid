package com.andruid.magic.discodruid.ui.activity

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.CMD_REORDER_QUEUE
import com.andruid.magic.discodruid.data.EXTRA_FROM_POS
import com.andruid.magic.discodruid.data.EXTRA_TO_POS
import com.andruid.magic.discodruid.data.MB_PLAY_QUEUE
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.ActivityQueueBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.QueueTracksAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.dragdrop.DragCallback
import com.andruid.magic.discodruid.ui.viewbinding.viewBinding
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.QueueTracksViewModel
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Track

class QueueActivity : AppCompatActivity(), DragCallback.StartDragListener {
    private val touchHelper by lazy {
        val callback = DragCallback(tracksAdapter, false)
        ItemTouchHelper(callback)
    }
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
    private val mediaControllerCallback = MediaControllerCallback()
    private val tracksAdapter by lazy {
        QueueTracksAdapter(this) { fromPosition, toPosition ->
            val extras = bundleOf(
                EXTRA_FROM_POS to fromPosition,
                EXTRA_TO_POS to toPosition
            )
            mediaBrowserCompat.sendCustomAction(CMD_REORDER_QUEUE, extras, null)
        }
    }
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            actionMode = mode
            mode.menuInflater.inflate(R.menu.menu_actions, menu)
            tracksAdapter.showDragHandles()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onDestroyActionMode(mode: ActionMode) {
            tracksAdapter.hideDragHandles()
            actionMode = null
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.remove_item -> {
                    Toast.makeText(this@QueueActivity, "Deleted -1", Toast.LENGTH_SHORT).show()
                }
            }

            return true
        }
    }

    private var actionMode: ActionMode? = null

    private lateinit var mediaControllerCompat: MediaControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

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
            touchHelper.attachToRecyclerView(this)

            adapter = tracksAdapter
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(object : ItemClickListener(this@QueueActivity, this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)
                    if (actionMode == null)
                        mediaControllerCompat.transportControls.skipToQueueItem(position.toLong())
                }

                override fun onLongClick(view: View, position: Int) {
                    super.onLongClick(view, position)
                    if (actionMode == null)
                        showActionMode()
                }
            })
        }
    }

    private fun updateUI(track: Track?, position: Int) {
        tracksAdapter.currentTrack = track
        tracksAdapter.notifyItemChanged(position)
    }

    private fun hideActionMode() {
        actionMode?.finish()
    }

    private fun showActionMode() {
        if (actionMode == null)
            startSupportActionMode(actionModeCallback)
    }

    private inner class MBConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()

            mediaControllerCompat =
                MediaControllerCompat(this@QueueActivity, mediaBrowserCompat.sessionToken).apply {
                    registerCallback(mediaControllerCallback)
                    MediaControllerCompat.setMediaController(this@QueueActivity, this)
                }

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

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            val track = metadata?.toTrack() ?: return
            if (track.title == "Loading" || track.artist == "Loading" || track.album == "Loading")
                return

            Log.d("updateLog", "onMetadataChanged")
            val pos =
                tracksAdapter.currentList.indexOfFirst { t -> t.track.audioId == track.audioId }
            updateUI(track, pos)
        }
    }
}
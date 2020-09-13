package com.andruid.magic.discodruid.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.map
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.MB_CURRENT_TRACK
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.FragmentTrackBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.TracksAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.viewbinding.viewBinding
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.TrackViewModel
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Track

class TrackFragment : Fragment(R.layout.fragment_track) {
    companion object {
        fun newInstance() = TrackFragment()
    }

    private val binding by viewBinding(FragmentTrackBinding::bind)
    private val tracksViewModel by viewModels<TrackViewModel> {
        BaseViewModelFactory { TrackViewModel(mediaBrowserCompat) }
    }
    private val tracksAdapter by lazy { TracksAdapter(requireContext(), lifecycleScope) }
    private val mbSubscriptionCallback = MBSubscriptionCallback()
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            requireContext(),
            ComponentName(requireActivity(), MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()

                    tracksViewModel.tracksLiveData.observe(viewLifecycleOwner, { tracks ->
                        tracksAdapter.submitData(
                            lifecycle,
                            tracks.map { track -> TrackViewRepresentation.fromTrack(track) })
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

    private var mListener: ITracksListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowserCompat.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowserCompat.disconnect()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is ITracksListener)
            context
        else
            throw RuntimeException("$context must implement TrackClickListener")
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = tracksAdapter
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(object : ItemClickListener(requireContext(), this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)
                    Log.d("clickLog", "track clicked")
                    tracksAdapter.getItemAtPosition(position)?.let { track ->
                        mListener?.onTrackClicked(track, position)

                        tracksAdapter.currentTrack = track
                        tracksAdapter.notifyItemChanged(position)
                    }
                }
            })
        }
    }

    interface ITracksListener {
        fun onTrackClicked(track: Track, position: Int)
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
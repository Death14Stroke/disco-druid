package com.andruid.magic.discodruid.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.databinding.FragmentTrackBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.TracksAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.TrackViewModel
import com.andruid.magic.medialoader.model.Track

class TrackFragment : Fragment() {
    companion object {
        fun newInstance() = TrackFragment()
    }

    private val tracksViewModel by viewModels<TrackViewModel> {
        BaseViewModelFactory { TrackViewModel(mediaBrowserCompat) }
    }
    private val tracksAdapter by lazy {
        TracksAdapter(requireContext(), lifecycleScope)
    }
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            requireContext(),
            ComponentName(requireActivity(), MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()

                    tracksViewModel.tracksLiveData.observe(viewLifecycleOwner, { tracks ->
                        tracksAdapter.submitData(lifecycle, tracks)
                    })
                }
            },
            null
        )
    }

    private var mListener: ITracksListener? = null
    private lateinit var binding: FragmentTrackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowserCompat.connect()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackBinding.inflate(inflater, container, false)

        initRecyclerView()

        return binding.root
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
            addOnItemTouchListener(object: ItemClickListener(requireContext(), this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)
                    Log.d("clickLog", "track clicked")
                    tracksAdapter.getItemAtPosition(position)?.let { track ->
                        mListener?.onTrackClicked(track, position)
                    }
                }
            })
        }
    }

    interface ITracksListener {
        fun onTrackClicked(track: Track, position: Int)
    }
}
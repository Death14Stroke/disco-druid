package com.andruid.magic.discodruid.ui.fragment

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.databinding.FragmentPlaylistBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.PlaylistAdapter
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.PlaylistViewModel

class PlaylistFragment : Fragment() {
    companion object {
        fun newInstance() = PlaylistFragment()
    }

    private val playlistAdapter by lazy { PlaylistAdapter() }
    private val playlistViewModel by viewModels<PlaylistViewModel> {
        BaseViewModelFactory { PlaylistViewModel(mediaBrowserCompat) }
    }
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            requireContext(),
            ComponentName(requireActivity(), MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()

                    playlistViewModel.playlistLiveData.observe(viewLifecycleOwner, { pagingData ->
                        playlistAdapter.submitData(lifecycle, pagingData)
                    })
                }
            },
            null
        )
    }

    private lateinit var binding: FragmentPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowserCompat.connect()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        initRecyclerView()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowserCompat.disconnect()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = playlistAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}
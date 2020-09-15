package com.andruid.magic.discodruid.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.FragmentPlaylistBinding
import com.andruid.magic.discodruid.ui.adapter.PlaylistAdapter
import com.andruid.magic.discodruid.ui.viewbinding.viewBinding
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.PlaylistViewModel

class PlaylistFragment : MediaBrowserFragment(R.layout.fragment_playlist) {
    companion object {
        fun newInstance() = PlaylistFragment()
    }

    private val binding by viewBinding(FragmentPlaylistBinding::bind)
    private val playlistAdapter by lazy { PlaylistAdapter() }
    private val playlistViewModel by viewModels<PlaylistViewModel> {
        BaseViewModelFactory { PlaylistViewModel(mediaBrowserCompat) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onMBConnected() {
        playlistViewModel.playlistLiveData.observe(viewLifecycleOwner, { pagingData ->
            playlistAdapter.submitData(lifecycle, pagingData)
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = playlistAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}
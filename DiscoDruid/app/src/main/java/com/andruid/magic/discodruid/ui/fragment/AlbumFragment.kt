package com.andruid.magic.discodruid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.databinding.FragmentAlbumBinding
import com.andruid.magic.discodruid.event.EventObserver
import com.andruid.magic.discodruid.ui.adapter.AlbumsAdapter
import com.andruid.magic.discodruid.ui.viewmodel.AlbumViewModel
import com.andruid.magic.medialoader.model.Album

class AlbumFragment : Fragment() {
    companion object {
        fun newInstance() = AlbumFragment()
    }

    private val albumsAdapter by lazy {
        AlbumsAdapter(requireContext(), lifecycleScope) { albumViewModel.sendOpenAlbumDetailsEvent(it) }
    }
    private val albumViewModel by viewModels<AlbumViewModel>()

    private lateinit var binding: FragmentAlbumBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumBinding.inflate(inflater, container, false)

        initRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumViewModel.albumsLiveData.observe(viewLifecycleOwner, { pagingData ->
            albumsAdapter.submitData(lifecycle, pagingData)
        })

        albumViewModel.clickEvent.observe(viewLifecycleOwner, EventObserver { album ->
            launchDetailsDialog(album)
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = albumsAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun launchDetailsDialog(album: Album) {
        val dialogFragment = AlbumDetailsFragment.newInstance(album)

        dialogFragment.show(childFragmentManager, "Album details")
    }
}
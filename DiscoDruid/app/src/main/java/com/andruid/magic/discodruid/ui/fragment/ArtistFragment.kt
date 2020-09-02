package com.andruid.magic.discodruid.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.data.EXTRA_ARTIST
import com.andruid.magic.discodruid.databinding.FragmentArtistBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.activity.ArtistDetailsActivity
import com.andruid.magic.discodruid.ui.adapter.ArtistsAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.viewmodel.ArtistViewModel
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory

class ArtistFragment : Fragment() {
    companion object {
        fun newInstance() = ArtistFragment()
    }

    private val artistsAdapter by lazy {
        ArtistsAdapter(requireContext(), lifecycleScope)
    }
    private val artistViewModel by viewModels<ArtistViewModel> {
        BaseViewModelFactory { ArtistViewModel(mediaBrowserCompat) }
    }
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            requireContext(),
            ComponentName(requireActivity(), MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()

                    artistViewModel.artistLiveData.observe(viewLifecycleOwner, { pagingData ->
                        artistsAdapter.submitData(lifecycle, pagingData)
                    })
                }
            },
            null
        )
    }

    private lateinit var binding: FragmentArtistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowserCompat.connect()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArtistBinding.inflate(inflater, container, false)

        initRecyclerView()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowserCompat.disconnect()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = artistsAdapter
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(object: ItemClickListener(requireContext(), this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)

                    artistsAdapter.getItemAtPosition(position)?.let { artist ->
                        val intent = Intent(requireContext(), ArtistDetailsActivity::class.java)
                            .putExtra(EXTRA_ARTIST, artist)
                        startActivity(intent)
                    }
                }
            })
        }
    }
}
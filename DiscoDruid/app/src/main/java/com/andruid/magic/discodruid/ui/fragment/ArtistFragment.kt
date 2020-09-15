package com.andruid.magic.discodruid.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.map
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.EXTRA_ARTIST
import com.andruid.magic.discodruid.data.model.ArtistViewRepresentation
import com.andruid.magic.discodruid.databinding.FragmentArtistBinding
import com.andruid.magic.discodruid.ui.activity.ArtistDetailsActivity
import com.andruid.magic.discodruid.ui.adapter.ArtistsAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.viewbinding.viewBinding
import com.andruid.magic.discodruid.ui.viewmodel.ArtistViewModel
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory

class ArtistFragment : MediaBrowserFragment(R.layout.fragment_artist) {
    companion object {
        fun newInstance() = ArtistFragment()
    }

    private val binding by viewBinding(FragmentArtistBinding::bind)
    private val artistsAdapter by lazy {
        ArtistsAdapter(requireContext(), lifecycleScope)
    }
    private val artistViewModel by viewModels<ArtistViewModel> {
        BaseViewModelFactory { ArtistViewModel(mediaBrowserCompat) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onMBConnected() {
        artistViewModel.artistLiveData.observe(viewLifecycleOwner, { pagingData ->
            artistsAdapter.submitData(
                lifecycle,
                pagingData.map { artist ->
                    ArtistViewRepresentation.fromArtist(
                        requireContext(),
                        artist
                    )
                })
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = artistsAdapter
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(object : ItemClickListener(requireContext(), this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)

                    artistsAdapter.getItemAtPosition(position)?.artist?.let { artist ->
                        val intent = Intent(requireContext(), ArtistDetailsActivity::class.java)
                            .putExtra(EXTRA_ARTIST, artist)
                        startActivity(intent)
                    }
                }
            })
        }
    }
}
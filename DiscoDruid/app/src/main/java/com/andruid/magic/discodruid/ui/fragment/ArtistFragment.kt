package com.andruid.magic.discodruid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.databinding.FragmentArtistBinding
import com.andruid.magic.discodruid.ui.adapter.ArtistsAdapter
import com.andruid.magic.discodruid.ui.viewmodel.ArtistViewModel

class ArtistFragment : Fragment() {
    companion object {
        fun newInstance() = ArtistFragment()
    }

    private val artistsAdapter by lazy {
        ArtistsAdapter(requireContext(), lifecycleScope)
    }
    private val artistViewModel by viewModels<ArtistViewModel>()

    private lateinit var binding: FragmentArtistBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArtistBinding.inflate(inflater, container, false)

        initRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        artistViewModel.artistLiveData.observe(viewLifecycleOwner, { pagingData ->
            artistsAdapter.submitData(lifecycle, pagingData)
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = artistsAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}
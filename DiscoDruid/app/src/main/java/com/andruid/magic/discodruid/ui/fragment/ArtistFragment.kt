package com.andruid.magic.discodruid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.FragmentArtistBinding
import com.andruid.magic.discodruid.ui.adapter.ArtistAdapter
import com.andruid.magic.discodruid.ui.viewmodel.ArtistViewModel

class ArtistFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance() = ArtistFragment()
    }

    private lateinit var binding: FragmentArtistBinding
    private lateinit var viewModel: ArtistViewModel

    private val artistAdapter = ArtistAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_artist, container, false)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = artistAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ArtistViewModel::class.java)
        viewModel.artistsLiveData.observe(this, Observer { artists ->
            artistAdapter.submitList(artists)
        })
    }
}
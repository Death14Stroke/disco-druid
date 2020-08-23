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
import com.andruid.magic.discodruid.databinding.FragmentTrackBinding
import com.andruid.magic.discodruid.ui.adapter.TrackAdapter
import com.andruid.magic.discodruid.ui.viewmodel.TrackViewModel

class TrackFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance() = TrackFragment()
    }

    private lateinit var binding: FragmentTrackBinding
    private lateinit var viewModel: TrackViewModel

    private val trackAdapter = TrackAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track, container, false)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = trackAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TrackViewModel::class.java)
        viewModel.tracksLiveData.observe(this, Observer { tracks ->
            trackAdapter.submitList(tracks)
        })
    }
}
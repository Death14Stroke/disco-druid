package com.andruid.magic.discodruid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.databinding.FragmentTrackBinding
import com.andruid.magic.discodruid.ui.adapter.TracksAdapter
import com.andruid.magic.discodruid.ui.viewmodel.TrackViewModel

class TrackFragment : Fragment() {
    companion object {
        fun newInstance() = TrackFragment()
    }

    private val tracksViewModel by viewModels<TrackViewModel>()
    private val tracksAdapter = TracksAdapter()

    private lateinit var binding: FragmentTrackBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackBinding.inflate(inflater, container, false)

        initRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracksViewModel.tracksLiveData.observe(viewLifecycleOwner, { tracks ->
            tracksAdapter.submitData(lifecycle, tracks)
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = tracksAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}
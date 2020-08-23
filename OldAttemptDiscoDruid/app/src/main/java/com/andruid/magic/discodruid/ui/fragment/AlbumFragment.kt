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
import com.andruid.magic.discodruid.databinding.FragmentAlbumBinding
import com.andruid.magic.discodruid.ui.adapter.AlbumAdapter
import com.andruid.magic.discodruid.ui.viewmodel.AlbumViewModel

class AlbumFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance() = AlbumFragment()
    }

    private lateinit var binding: FragmentAlbumBinding
    private lateinit var viewModel: AlbumViewModel

    private val albumAdapter = AlbumAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album, container, false)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = albumAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AlbumViewModel::class.java)
        viewModel.albumsLiveData.observe(this, Observer { albums ->
            albumAdapter.submitList(albums)
        })
    }
}
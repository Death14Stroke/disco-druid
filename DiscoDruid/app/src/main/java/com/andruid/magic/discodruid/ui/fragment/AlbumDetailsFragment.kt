package com.andruid.magic.discodruid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.EXTRA_ALBUM
import com.andruid.magic.discodruid.data.VIEW_TYPE_ALBUM_TRACKS
import com.andruid.magic.discodruid.databinding.DialogAlbumDetailsBinding
import com.andruid.magic.discodruid.ui.adapter.TracksAdapter
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.discodruid.ui.viewmodel.TrackViewModel
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumDetailsFragment : DialogFragment() {
    companion object {
        fun newInstance(album: Album) =
            AlbumDetailsFragment().apply {
                arguments = bundleOf(EXTRA_ALBUM to album)
            }
    }

    private val album by lazy { requireArguments().getParcelable<Album>(EXTRA_ALBUM)!! }
    private val trackViewModel by viewModels<TrackViewModel> {
        BaseViewModelFactory { TrackViewModel(album.albumId) }
    }
    private val tracksAdapter = TracksAdapter(viewType = VIEW_TYPE_ALBUM_TRACKS)

    private lateinit var binding: DialogAlbumDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_Main_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAlbumDetailsBinding.inflate(inflater, container, false)
        binding.toolBar.title = album.album

        initRecyclerView()
        lifecycleScope.launch { initViews() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackViewModel.tracksLiveData.observe(viewLifecycleOwner, {
            tracksAdapter.submitData(lifecycle, it)
        })
    }

    private suspend fun initViews() {
        val bitmap =
            withContext(Dispatchers.IO) { requireContext().getAlbumArtBitmap(album.albumId) }
        binding.albumArtImage.setImageBitmap(bitmap)
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = tracksAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}
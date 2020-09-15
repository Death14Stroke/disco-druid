package com.andruid.magic.discodruid.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.EXTRA_ALBUM
import com.andruid.magic.discodruid.databinding.FragmentAlbumBinding
import com.andruid.magic.discodruid.ui.activity.AlbumDetailsActivity
import com.andruid.magic.discodruid.ui.adapter.AlbumsAdapter
import com.andruid.magic.discodruid.ui.custom.ItemClickListener
import com.andruid.magic.discodruid.ui.viewbinding.viewBinding
import com.andruid.magic.discodruid.ui.viewmodel.AlbumViewModel
import com.andruid.magic.discodruid.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.medialoader.model.Album

class AlbumFragment : MediaBrowserFragment(R.layout.fragment_album) {
    companion object {
        fun newInstance() = AlbumFragment()
    }

    private val binding by viewBinding(FragmentAlbumBinding::bind)
    private val albumsAdapter by lazy { AlbumsAdapter(requireContext(), lifecycleScope) }
    private val albumViewModel by viewModels<AlbumViewModel> {
        BaseViewModelFactory { AlbumViewModel(mediaBrowserCompat) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onMBConnected() {
        albumViewModel.albumsLiveData.observe(viewLifecycleOwner, { pagingData ->
            albumsAdapter.submitData(lifecycle, pagingData)
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = albumsAdapter
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(object : ItemClickListener(requireContext(), this) {
                override fun onClick(view: View, position: Int) {
                    super.onClick(view, position)
                    val album = albumsAdapter.getItemAtPosition(position) ?: return
                    launchDetailsDialog(view, album)
                }
            })

            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
    }

    private fun launchDetailsDialog(view: View, album: Album) {
        val thumbnailIV = view.findViewById<ImageView>(R.id.thumbnailIV)
        val intent = Intent(requireContext(), AlbumDetailsActivity::class.java)
            .putExtra(EXTRA_ALBUM, album)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            Pair.create(thumbnailIV, thumbnailIV.transitionName)
        )

        startActivity(intent, options.toBundle())
    }
}
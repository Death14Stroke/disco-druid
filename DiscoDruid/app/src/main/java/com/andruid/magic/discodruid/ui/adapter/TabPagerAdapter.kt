package com.andruid.magic.discodruid.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.andruid.magic.discodruid.ui.fragment.AlbumFragment
import com.andruid.magic.discodruid.ui.fragment.ArtistFragment
import com.andruid.magic.discodruid.ui.fragment.PlaylistFragment
import com.andruid.magic.discodruid.ui.fragment.TrackFragment

class TabPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    companion object {
        const val NUMBER_OF_TABS = 4
        const val POS_TRACKS = 0
        const val POS_ALBUM = 1
        const val POS_ARTIST = 2
    }

    override fun getItemCount() = NUMBER_OF_TABS

    override fun createFragment(position: Int) =
        when (position) {
            POS_TRACKS -> TrackFragment.newInstance()
            POS_ALBUM -> AlbumFragment.newInstance()
            POS_ARTIST -> ArtistFragment.newInstance()
            else -> PlaylistFragment.newInstance()
        }
}
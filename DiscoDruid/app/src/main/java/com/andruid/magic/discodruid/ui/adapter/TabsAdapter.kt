package com.andruid.magic.discodruid.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.andruid.magic.discodruid.ui.fragment.AlbumFragment
import com.andruid.magic.discodruid.ui.fragment.ArtistFragment
import com.andruid.magic.discodruid.ui.fragment.PlaylistFragment
import com.andruid.magic.discodruid.ui.fragment.TrackFragment

const val POSITION_TRACKS = 0
const val POSITION_ALBUMS = 1
const val POSITION_ARTISTS = 2

class TabsAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POSITION_TRACKS -> TrackFragment.newInstance()
            POSITION_ALBUMS -> AlbumFragment.newInstance()
            POSITION_ARTISTS -> ArtistFragment.newInstance()
            else -> PlaylistFragment.newInstance()
        }
    }
}
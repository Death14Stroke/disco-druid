package com.andruid.magic.discodruid.ui.adapter

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.ui.fragment.AlbumFragment
import com.andruid.magic.discodruid.ui.fragment.ArtistFragment
import com.andruid.magic.discodruid.ui.fragment.PlaylistFragment
import com.andruid.magic.discodruid.ui.fragment.TrackFragment

class TabPagerAdapter(private val context: Context, fm: FragmentManager, behavior: Int) :
    FragmentStatePagerAdapter(fm, behavior) {
    companion object {
        const val NUMBER_OF_TABS = 4
        const val POS_TRACKS = 0
        const val POS_ALBUM = 1
        const val POS_ARTIST = 2
    }

    override fun getItem(position: Int) =
        when (position) {
            POS_TRACKS -> TrackFragment.newInstance()
            POS_ALBUM -> AlbumFragment.newInstance()
            POS_ARTIST -> ArtistFragment.newInstance()
            else -> PlaylistFragment.newInstance()
        }

    override fun getCount() = NUMBER_OF_TABS

    override fun getPageTitle(position: Int) =
        context.getString(
            when (position) {
                POS_TRACKS -> R.string.tracks
                POS_ALBUM -> R.string.albums
                POS_ARTIST -> R.string.artists
                else -> R.string.playlists
            }
        )
}
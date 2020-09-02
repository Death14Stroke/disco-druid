package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutArtistTrackBinding
import com.andruid.magic.medialoader.model.Track

class ArtistTrackViewHolder(private val binding: LayoutArtistTrackBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): ArtistTrackViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutArtistTrackBinding>(
                inflater,
                R.layout.layout_artist_track,
                parent,
                false
            )
            return ArtistTrackViewHolder(binding)
        }
    }

    fun bind(track: Track, activated: Boolean) {
        binding.track = track
        binding.rootLayout.isActivated = activated
        binding.executePendingBindings()
    }
}
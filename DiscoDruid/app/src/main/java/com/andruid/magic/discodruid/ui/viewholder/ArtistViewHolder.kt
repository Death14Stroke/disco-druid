package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutArtistBinding
import com.andruid.magic.medialoader.model.Artist

class ArtistViewHolder(private val binding: LayoutArtistBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        @JvmStatic
        fun from(parent: ViewGroup): ArtistViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutArtistBinding>(inflater, R.layout.layout_artist,
                parent, false)
            return ArtistViewHolder(binding)
        }
    }

    fun bind(artist: Artist) {
        binding.artist = artist
        binding.executePendingBindings()
    }
}
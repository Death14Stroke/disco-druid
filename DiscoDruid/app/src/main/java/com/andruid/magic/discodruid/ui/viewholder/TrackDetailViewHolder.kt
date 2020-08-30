package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.databinding.LayoutTrackDetailBinding
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Track

class TrackDetailViewHolder(private val binding: LayoutTrackDetailBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): TrackDetailViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = LayoutTrackDetailBinding.inflate(inflater, parent, false)
            return TrackDetailViewHolder(binding)
        }
    }

    fun bind(track: Track) {
        val bitmap = binding.root.context.getAlbumArtBitmap(track.albumId)
        binding.trackDetailViewpager.setImageBitmap(bitmap)
    }
}
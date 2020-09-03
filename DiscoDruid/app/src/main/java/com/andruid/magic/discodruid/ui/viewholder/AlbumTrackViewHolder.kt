package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.LayoutAlbumTrackBinding

class AlbumTrackViewHolder(private val binding: LayoutAlbumTrackBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): AlbumTrackViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutAlbumTrackBinding>(
                inflater,
                R.layout.layout_album_track,
                parent,
                false
            )

            return AlbumTrackViewHolder(binding)
        }
    }

    fun bind(viewRepresentation: TrackViewRepresentation, activated: Boolean) {
        binding.viewRep = viewRepresentation
        binding.rootLayout.isActivated = activated
        binding.executePendingBindings()
    }
}
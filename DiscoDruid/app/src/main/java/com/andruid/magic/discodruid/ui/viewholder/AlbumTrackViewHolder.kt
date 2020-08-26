package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutAlbumTrackBinding
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun bind(track: Track) {
        binding.track = track
        binding.executePendingBindings()
    }
}
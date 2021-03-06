package com.andruid.magic.discodruid.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.databinding.LayoutTrackDetailBinding
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackDetailViewHolder(private val binding: LayoutTrackDetailBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): TrackDetailViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = LayoutTrackDetailBinding.inflate(inflater, parent, false)
            return TrackDetailViewHolder(binding)
        }
    }

    fun bind(context: Context, scope: CoroutineScope, track: Track) {
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) { context.getAlbumArtBitmap(track.albumId) }
            binding.trackDetailViewpager.setImageBitmap(bitmap)
        }
    }
}
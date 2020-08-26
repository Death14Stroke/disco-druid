package com.andruid.magic.discodruid.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutArtistBinding
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Artist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtistViewHolder(private val binding: LayoutArtistBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): ArtistViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutArtistBinding>(
                inflater,
                R.layout.layout_artist,
                parent,
                false
            )
            return ArtistViewHolder(binding)
        }
    }

    fun bind(context: Context, scope: CoroutineScope, artist: Artist) {
        binding.artist = artist

        scope.launch {
            val bitmap = withContext(Dispatchers.IO) { context.getAlbumArtBitmap(artist.albumId) }
            binding.thumbnailIV.setImageBitmap(bitmap)
        }

        binding.executePendingBindings()
    }
}
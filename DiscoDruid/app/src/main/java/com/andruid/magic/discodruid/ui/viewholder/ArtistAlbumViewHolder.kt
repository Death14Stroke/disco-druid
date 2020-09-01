package com.andruid.magic.discodruid.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutArtistAlbumBinding
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Album
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtistAlbumViewHolder(private val binding: LayoutArtistAlbumBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): ArtistAlbumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutArtistAlbumBinding>(
                inflater,
                R.layout.layout_artist_album,
                parent,
                false
            )
            return ArtistAlbumViewHolder(binding)
        }
    }

    fun bind(context: Context, scope: CoroutineScope, album: Album) {
        binding.album = album

        scope.launch {
            val bitmap = withContext(Dispatchers.IO) { context.getAlbumArtBitmap(album.albumId) }
            binding.thumbnailIV.setImageBitmap(bitmap)
        }

        binding.executePendingBindings()
    }
}
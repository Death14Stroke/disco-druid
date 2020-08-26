package com.andruid.magic.discodruid.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutAlbumBinding
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import com.andruid.magic.medialoader.model.Album
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumViewHolder(private val binding: LayoutAlbumBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): AlbumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutAlbumBinding>(
                inflater,
                R.layout.layout_album,
                parent,
                false
            )
            return AlbumViewHolder(binding)
        }
    }

    fun bind(
        context: Context,
        scope: CoroutineScope,
        album: Album,
        onAlbumClicked: (view: View, album: Album) -> Unit
    ) {
        binding.album = album

        binding.thumbnailIV.transitionName = "iv_${album.albumId}"
        binding.root.setOnClickListener { onAlbumClicked.invoke(binding.thumbnailIV, album) }

        scope.launch {
            val bitmap = withContext(Dispatchers.IO) { context.getAlbumArtBitmap(album.albumId) }
            binding.thumbnailIV.setImageBitmap(bitmap)
        }

        binding.executePendingBindings()
    }
}
package com.andruid.magic.discodruid.ui.viewholder

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutAlbumBinding
import com.andruid.magic.medialoader.model.Album
import com.andruid.magic.medialoader.repository.AlbumRepository
import kotlinx.coroutines.*
import java.io.FileNotFoundException

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

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("imageLog", "Caught $exception")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun bind(scope: CoroutineScope, album: Album) {
        binding.album = album

        val albumId = album.albumId
        val context = binding.root.context

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val albumArtUri = ContentUris.withAppendedId(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumId.toLong()
            )
            val size = context.resources.getDimension(R.dimen.album_art_size).toInt()

            scope.launch(handler) {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        context.contentResolver.loadThumbnail(albumArtUri, Size(size, size), null)
                    }
                    binding.thumbnailIV.setImageBitmap(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        } else {
            scope.launch {
                val albumArtUri = AlbumRepository.getAlbumArtUri(albumId)
                binding.thumbnailIV.setImageURI(Uri.parse(albumArtUri))
            }
        }
    }
}
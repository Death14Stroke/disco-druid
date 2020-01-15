package com.andruid.magic.discodruid.ui.viewholder

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentResolverCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.RoundedCornersTransformation
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutAlbumBinding
import com.andruid.magic.medialoader.model.Album
import java.io.File

class AlbumViewHolder(private val binding: LayoutAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        @JvmStatic
        fun from(parent: ViewGroup): AlbumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutAlbumBinding>(inflater, R.layout.layout_album,
                parent, false)
            return AlbumViewHolder(binding)
        }
    }

    fun bind(album: Album) {
        binding.album = album
        binding.executePendingBindings()

        binding.thumbnailIV.badgeValue = album.songsCount

        val contentResolver = binding.root.context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            contentResolver.loadThumbnail(
                Uri.parse(album.album),
                Size(binding.thumbnailIV.width, binding.thumbnailIV.height), null)
        else {
            val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
            val selection = "${MediaStore.Audio.Albums._ID}=?"
            val cursor = ContentResolverCompat.query(
                contentResolver, uri, projection, selection,
                arrayOf(album.albumId), null, null
            )
            if (cursor.moveToFirst()) {
                val path =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                if(path != null)
                    binding.thumbnailIV.load(File(path)) {
                        transformations(RoundedCornersTransformation(50f)) }
                else
                    binding.thumbnailIV.load(R.drawable.music) {
                        transformations(RoundedCornersTransformation(50f)) }
            }
            cursor.close()
        }
    }
}
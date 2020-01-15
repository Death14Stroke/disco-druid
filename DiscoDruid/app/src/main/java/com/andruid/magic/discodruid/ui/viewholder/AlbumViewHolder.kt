package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.Constants.ALBUM_TRACKS
import com.andruid.magic.discodruid.databinding.LayoutAlbumBinding
import com.andruid.magic.discodruid.eventbus.ViewTracksEvent
import com.andruid.magic.medialoader.model.Album
import org.greenrobot.eventbus.EventBus

class AlbumViewHolder(private val binding: LayoutAlbumBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        @JvmStatic
        fun from(parent: ViewGroup): AlbumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutAlbumBinding>(
                inflater, R.layout.layout_album,
                parent, false
            )
            return AlbumViewHolder(binding)
        }
    }

    fun bind(album: Album) {
        binding.album = album
        binding.executePendingBindings()

        binding.thumbnailIV.badgeValue = album.songsCount
        binding.root.setOnClickListener {
            EventBus.getDefault().post(ViewTracksEvent(ALBUM_TRACKS, album.albumId))
        }
    }
}
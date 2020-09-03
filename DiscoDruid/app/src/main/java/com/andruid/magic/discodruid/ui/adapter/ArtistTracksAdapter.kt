package com.andruid.magic.discodruid.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.data.VIEW_TYPE_ARTIST_ALBUM
import com.andruid.magic.discodruid.data.VIEW_TYPE_ARTIST_TRACK
import com.andruid.magic.discodruid.data.model.UiModel
import com.andruid.magic.discodruid.ui.viewholder.ArtistAlbumViewHolder
import com.andruid.magic.discodruid.ui.viewholder.ArtistTrackViewHolder
import com.andruid.magic.medialoader.model.Track
import kotlinx.coroutines.CoroutineScope

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiModel>() {
    override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
        return when {
            oldItem is UiModel.TrackModel && newItem is UiModel.TrackModel ->
                oldItem.viewRepresentation.track.audioId == newItem.viewRepresentation.track.audioId
            oldItem is UiModel.AlbumSeparatorModel && newItem is UiModel.AlbumSeparatorModel ->
                oldItem.album.albumId == newItem.album.albumId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel) =
        oldItem == newItem
}

class ArtistTracksAdapter(
    private val context: Context,
    private val scope: CoroutineScope
) : PagingDataAdapter<UiModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    var currentTrack: Track? = null
        set(value) {
            prevPosition?.let { notifyItemChanged(it) }
            field = value
        }
    private var prevPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ARTIST_ALBUM -> ArtistAlbumViewHolder.from(parent)
            else -> ArtistTrackViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ArtistTrackViewHolder) {
            val trackModel = getItem(position)
            if (trackModel is UiModel.TrackModel) {
                val track = trackModel.viewRepresentation.track
                val activated = currentTrack?.audioId == track.audioId
                if (activated)
                    prevPosition = position
                holder.bind(trackModel.viewRepresentation, activated)
            }
        } else if (holder is ArtistAlbumViewHolder) {
            val albumModel = getItem(position)
            if (albumModel is UiModel.AlbumSeparatorModel)
                holder.bind(context, scope, albumModel.album)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UiModel.AlbumSeparatorModel -> VIEW_TYPE_ARTIST_ALBUM
            else -> VIEW_TYPE_ARTIST_TRACK
        }
    }

    fun getItemAtPosition(position: Int): Track? {
        return try {
            (getItem(position) as UiModel.TrackModel?)?.viewRepresentation?.track
        } catch (e: ClassCastException) {
            e.printStackTrace()
            null
        }
    }
}
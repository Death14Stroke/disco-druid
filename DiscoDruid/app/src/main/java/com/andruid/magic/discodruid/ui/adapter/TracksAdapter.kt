package com.andruid.magic.discodruid.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.data.VIEW_TYPE_ALBUM_TRACKS
import com.andruid.magic.discodruid.data.VIEW_TYPE_ALL_TRACKS
import com.andruid.magic.discodruid.ui.viewholder.AlbumTrackViewHolder
import com.andruid.magic.discodruid.ui.viewholder.TrackViewHolder
import com.andruid.magic.medialoader.model.Track
import kotlinx.coroutines.CoroutineScope

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Track>() {
    override fun areContentsTheSame(oldItem: Track, newItem: Track) =
        oldItem.audioId == newItem.audioId

    override fun areItemsTheSame(oldItem: Track, newItem: Track) =
        oldItem == newItem
}

class TracksAdapter(
    private val context: Context? = null,
    private val scope: CoroutineScope? = null,
    private val viewType: Int = VIEW_TYPE_ALL_TRACKS
) : PagingDataAdapter<Track, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ALBUM_TRACKS -> AlbumTrackViewHolder.from(parent)
            else -> TrackViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { track ->
            if (holder is TrackViewHolder)
                holder.bind(context!!, scope!!, track)
            else if (holder is AlbumTrackViewHolder)
                holder.bind(track)
        }
    }

    override fun getItemViewType(position: Int) = viewType
}
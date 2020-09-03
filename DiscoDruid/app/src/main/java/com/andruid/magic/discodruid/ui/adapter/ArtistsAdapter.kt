package com.andruid.magic.discodruid.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.discodruid.data.model.ArtistViewRepresentation
import com.andruid.magic.discodruid.ui.viewholder.ArtistViewHolder
import kotlinx.coroutines.CoroutineScope

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArtistViewRepresentation>() {
    override fun areContentsTheSame(
        oldItem: ArtistViewRepresentation,
        newItem: ArtistViewRepresentation
    ) =
        oldItem.artist.artistId == newItem.artist.artistId

    override fun areItemsTheSame(
        oldItem: ArtistViewRepresentation,
        newItem: ArtistViewRepresentation
    ) =
        oldItem == newItem
}

class ArtistsAdapter(
    private val context: Context,
    private val scope: CoroutineScope
) : PagingDataAdapter<ArtistViewRepresentation, ArtistViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ArtistViewHolder.from(parent)

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        getItem(position)?.let { viewRepresentation ->
            holder.bind(
                context,
                scope,
                viewRepresentation
            )
        }
    }

    fun getItemAtPosition(position: Int) = getItem(position)
}
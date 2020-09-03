package com.andruid.magic.discodruid.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.model.TrackViewRepresentation
import com.andruid.magic.discodruid.databinding.LayoutTrackBinding
import com.andruid.magic.discodruid.util.getAlbumArtBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackViewHolder(private val binding: LayoutTrackBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): TrackViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutTrackBinding>(
                inflater,
                R.layout.layout_track,
                parent,
                false
            )

            return TrackViewHolder(binding)
        }
    }

    fun bind(
        context: Context,
        scope: CoroutineScope,
        viewRepresentation: TrackViewRepresentation,
        activated: Boolean
    ) {
        binding.viewRep = viewRepresentation

        scope.launch {
            val track = viewRepresentation.track
            val bitmap = withContext(Dispatchers.IO) { context.getAlbumArtBitmap(track.albumId) }
            binding.thumbnailIV.setImageBitmap(bitmap)
        }

        binding.rootLayout.isActivated = activated

        binding.executePendingBindings()
    }
}
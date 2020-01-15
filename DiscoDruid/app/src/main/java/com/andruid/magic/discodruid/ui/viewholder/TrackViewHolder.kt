package com.andruid.magic.discodruid.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutTrackBinding
import com.andruid.magic.medialoader.model.Track

class TrackViewHolder(private val binding: LayoutTrackBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        @JvmStatic
        fun from(parent: ViewGroup): TrackViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutTrackBinding>(
                inflater, R.layout.layout_track,
                parent, false
            )
            return TrackViewHolder(binding)
        }
    }

    fun bind(track: Track) {
        binding.track = track
        binding.executePendingBindings()
    }
}
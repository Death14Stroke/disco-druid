package com.andruid.magic.discodruid.ui.custom

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutMediaControlsBinding

class MediaControlView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var callback: MediaControlsCallback? = null

    private val binding: LayoutMediaControlsBinding

    init {
        val view = View.inflate(context, R.layout.layout_media_controls, this)
        binding = LayoutMediaControlsBinding.bind(view)

        initView()
    }

    private fun initView() {
        with(binding) {
            nextBtn.setOnClickListener { callback?.onNext() }
            prevBtn.setOnClickListener { callback?.onPrevious() }

            playBtn.setOnClickListener { callback?.onPlayPause() }

            shuffleBtn.setOnClickListener { callback?.onShuffle() }
            repeatBtn.setOnClickListener { callback?.onRepeat() }
        }
    }

    fun setShuffleMode(shuffleMode: Int) {
        val icon = when (shuffleMode) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL -> R.drawable.exo_controls_shuffle_on
            else -> R.drawable.exo_controls_shuffle_off
        }
        binding.shuffleBtn.setImageResource(icon)
    }

    fun setRepeatMode(repeatMode: Int) {
        val icon = when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ALL -> R.drawable.exo_controls_repeat_all
            PlaybackStateCompat.REPEAT_MODE_ONE -> R.drawable.exo_controls_repeat_one
            else -> R.drawable.exo_controls_repeat_off
        }
        binding.repeatBtn.setImageResource(icon)
    }

    fun setPlayState(state: Int) {
        val icon = when (state) {
            PlaybackStateCompat.STATE_PLAYING -> R.drawable.exo_controls_pause
            else -> R.drawable.exo_controls_play
        }
        binding.playBtn.setImageResource(icon)
    }

    interface MediaControlsCallback {
        fun onPlayPause()
        fun onNext()
        fun onPrevious()
        fun onRepeat()
        fun onShuffle()
    }
}
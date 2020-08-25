package com.andruid.magic.discodruid.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutMediaControlsBinding

class MediaControlView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var callback: MediaControlsCallback? = null

    init {
        initView()
    }

    private fun initView() {
        val view = View.inflate(context, R.layout.layout_media_controls, this)
        val binding = LayoutMediaControlsBinding.bind(view)

        with (binding) {
            nextBtn.setOnClickListener { callback?.onNext() }
            prevBtn.setOnClickListener { callback?.onPrevious() }

            playBtn.setOnClickListener { callback?.onPlayPause() }

            shuffleBtn.setOnClickListener { callback?.onShuffle() }
            repeatBtn.setOnClickListener { callback?.onRepeat() }
        }
    }

    interface MediaControlsCallback {
        fun onPlayPause()
        fun onNext()
        fun onPrevious()
        fun onRepeat()
        fun onShuffle()
    }
}
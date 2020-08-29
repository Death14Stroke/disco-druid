package com.andruid.magic.discodruid.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutMediaProgressBinding
import com.andruid.magic.discodruid.util.toTimeString
import kotlin.math.max
import kotlin.math.min

class MediaProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var duration: Long = 0
        set(value) {
            field = value

            current = 0
            updateProgress()
            binding.progressSlider.valueTo = max(value.toFloat(), 1f)
            binding.progressSlider.valueFrom = 0f
            binding.durationTv.text = value.toTimeString()
        }
    var current: Long = 0
    private var sliderDragListener: ((progress: Long) -> Unit)? = null

    private val binding: LayoutMediaProgressBinding

    init {
        val view = View.inflate(context, R.layout.layout_media_progress, this)
        binding = LayoutMediaProgressBinding.bind(view)

        binding.progressSlider.setLabelFormatter { progress ->
            progress.toLong().toTimeString()
        }
        binding.progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                Log.d("sliderLog", "on user change value = $value")
                sliderDragListener?.invoke(value.toLong())
            }
        }
    }

    fun updateProgress() {
        current = min(current + 1, binding.progressSlider.valueTo.toLong())
        binding.progressSlider.value = current.toFloat()
        binding.currentTv.text = current.toTimeString()
    }

    fun addOnUserSlide(dragListener: (progress: Long) -> Unit) {
        sliderDragListener = dragListener
    }
}
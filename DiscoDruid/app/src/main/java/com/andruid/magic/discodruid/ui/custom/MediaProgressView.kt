package com.andruid.magic.discodruid.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.LayoutMediaProgressBinding

class MediaProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: LayoutMediaProgressBinding

    init {
        val view = View.inflate(context, R.layout.layout_media_progress, this)
        binding = LayoutMediaProgressBinding.bind(view)
    }
}
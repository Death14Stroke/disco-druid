package com.andruid.magic.discodruid.ui.fragment

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.andruid.magic.discodruid.service.MusicService

abstract class MediaBrowserFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            requireContext(),
            ComponentName(requireActivity(), MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()
                    onMBConnected()
                }

                override fun onConnectionSuspended() {
                    super.onConnectionSuspended()
                    onMBConnectionSuspended()
                }
            },
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowserCompat.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowserCompat.disconnect()
    }

    abstract fun onMBConnected()
    open fun onMBConnectionSuspended() {}
}
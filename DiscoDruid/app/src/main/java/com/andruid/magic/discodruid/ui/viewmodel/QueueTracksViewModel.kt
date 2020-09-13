package com.andruid.magic.discodruid.ui.viewmodel

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.discodruid.data.MB_PLAY_QUEUE
import com.andruid.magic.discodruid.util.toTrack
import com.andruid.magic.medialoader.model.Track
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class QueueTracksViewModel(private val mediaBrowserCompat: MediaBrowserCompat) : ViewModel() {
    val tracksLiveData = liveData {
        val tracks = getQueueTracks()
        emit(tracks)
    }

    private suspend fun getQueueTracks(): List<Track> {
        lateinit var result: Continuation<List<Track>>

        mediaBrowserCompat.subscribe(
            MB_PLAY_QUEUE,
            bundleOf(),
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>,
                    options: Bundle
                ) {
                    Log.d("queueLog", "tracks loaded in viewmodel")
                    val tracks = children.mapNotNull { mediaItem -> mediaItem.toTrack() }
                    result.resume(tracks)
                }
            })

        return suspendCoroutine { continuation -> result = continuation }
    }
}
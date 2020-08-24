package com.andruid.magic.discodruid.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.media.session.MediaButtonReceiver
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.ui.activity.MainActivity
import com.andruid.magic.medialoader.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

const val MUSIC_CHANNEL_NAME = "Music Playback"
const val MUSIC_CHANNEL_ID = "music_playback_channel"

suspend fun Context.buildNotification(
    icon: Int,
    track: Track,
    token: MediaSessionCompat.Token
): NotificationCompat.Builder {
    val intent = Intent(this, MainActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)

    val notificationManager = getSystemService<NotificationManager>()!!
    var importance = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        importance = NotificationManager.IMPORTANCE_HIGH

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            MUSIC_CHANNEL_ID,
            MUSIC_CHANNEL_NAME, importance
        ).apply {
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    return NotificationCompat.Builder(this, MUSIC_CHANNEL_ID).apply {
        setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
        )
        setSmallIcon(R.mipmap.ic_launcher_round)

        priority = NotificationCompat.PRIORITY_MAX
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setOnlyAlertOnce(true)

        setContentIntent(
            PendingIntent.getActivity(
                this@buildNotification,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        setContentTitle(track.title)
        setContentText(track.album)
        setSubText(track.artist)
        setShowWhen(true)

        var pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this@buildNotification,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
        addAction(android.R.drawable.ic_media_previous, "previous", pendingIntent)

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this@buildNotification,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        addAction(icon, "play", pendingIntent)

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this@buildNotification,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
        addAction(android.R.drawable.ic_media_next, "next", pendingIntent)

        try {
            Log.d("serviceLog", "before bitmap")
            val bitmap = withContext(Dispatchers.IO) { getAlbumArtBitmap(track.albumId) }
            Log.d("serviceLog", "after bitmap")
            setLargeIcon(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
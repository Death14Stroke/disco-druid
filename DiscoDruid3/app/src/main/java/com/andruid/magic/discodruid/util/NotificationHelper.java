package com.andruid.magic.discodruid.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.andruid.magic.discodruid.MainActivity;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.data.Constants;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

public class NotificationHelper {
    public static NotificationCompat.Builder buildNotification(int playButtonIcon, Context context, MediaMetadataCompat metadataCompat, MediaSessionCompat.Token token){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager==null)
            return null;
        String channelId = Constants.CHANNEL_MEDIA_CONTROL;
        String channelName = Constants.MEDIA_CONTROLS;
        int importance = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId,channelName,importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.CYAN);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        PendingIntent deletePendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelId);
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(token)
                                .setShowActionsInCompactView(0,1,2)
                                .setShowCancelButton(true)
                                .setCancelButtonIntent(deletePendingIntent))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(PendingIntent.getActivity(context,0,new Intent(context, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .setSubText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setShowWhen(false)
                .setAutoCancel(true);
        String albumArtUri = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        if(albumArtUri!=null)
            builder.setLargeIcon(BitmapFactory.decodeFile(albumArtUri));
        else {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
            builder.setLargeIcon(bitmap);
        }

        PendingIntent pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        builder.addAction(android.R.drawable.ic_media_previous,"previous",pendingIntent);

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE);
        builder.addAction(playButtonIcon,"play",pendingIntent);

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        builder.addAction(android.R.drawable.ic_media_next,"next",pendingIntent);
        return builder;
    }
}
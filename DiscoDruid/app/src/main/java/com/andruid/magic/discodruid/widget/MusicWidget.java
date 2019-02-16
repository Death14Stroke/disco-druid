package com.andruid.magic.discodruid.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.andruid.magic.discodruid.MainActivity;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

public class MusicWidget extends AppWidgetProvider {
    private static Track currentTrack;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_outer_layout,pendingIntent);
        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context,PlaybackStateCompat.ACTION_PLAY_PAUSE);
        views.setOnClickPendingIntent(R.id.widget_play_btn,pendingIntent);
        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context,PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        views.setOnClickPendingIntent(R.id.widget_next_btn,pendingIntent);
        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context,PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        views.setOnClickPendingIntent(R.id.widget_prev_btn,pendingIntent);
        if(currentTrack == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF, 0);
            String serializedTrack = sharedPreferences.getString(Constants.TRACK, null);
            if(serializedTrack!=null){
                currentTrack = Track.create(serializedTrack);
            }
            else{
                appWidgetManager.updateAppWidget(appWidgetId, views);
                return;
            }
        }
        views.setTextViewText(R.id.widget_artist_name,currentTrack.getArtist());
        views.setTextViewText(R.id.widget_song_name,currentTrack.getTitle());
        int[] appWidgetIds = {appWidgetId};
        AppWidgetTarget awt = new AppWidgetTarget(context, R.id.widget_image_view, views, appWidgetIds) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
            }
        };
        String path = currentTrack.getAlbumArtUri();
        if(path!=null)
            Glide.with(context)
                    .asBitmap()
                    .load(path)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.music)
                            .transform(new RoundedCorners(50))
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(awt);
        else
            Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.music)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(awt);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF,0);
        String serializedTrack = sharedPreferences.getString(Constants.TRACK,null);
        if(serializedTrack!=null){
            currentTrack = Track.create(serializedTrack);
        }
    }
}
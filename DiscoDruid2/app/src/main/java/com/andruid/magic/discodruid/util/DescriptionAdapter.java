package com.andruid.magic.discodruid.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.andruid.magic.discodruid.MainActivity;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import androidx.annotation.Nullable;

public class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {
    private Track track = new Track();
    private Context context;

    public DescriptionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        Log.d("descAdapter","title:"+track.getTitle());
        return track.getTitle();
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        Log.d("descAdapter","intent");
        return PendingIntent.getActivity(context,0,new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        Log.d("descAdapter","text");
        return track.getAlbum();
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        Log.d("descAdapter","large icon");
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
        if(track.getAlbumArtUri()!=null)
            largeIcon = BitmapFactory.decodeFile(track.getAlbumArtUri());
        return largeIcon;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
package com.andruid.magic.discodruid.viewholder;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.util.MediaUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumTracksViewHolder extends GeneralViewHolder {
    @BindView(R.id.album_track_name) TextView trackTV;
    @BindView(R.id.album_track_artist) TextView artistTV;
    @BindView(R.id.album_track_duration) TextView durationTV;

    public AlbumTracksViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setItem(Track track){
        trackTV.setText(track.getTitle());
        artistTV.setText(track.getArtist());
        durationTV.setText(MediaUtils.getTimeString(track.getDuration()));
    }
}
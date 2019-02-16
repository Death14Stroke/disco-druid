package com.andruid.magic.discodruid.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Artist;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.artist_name) TextView artistTV;
    @BindView(R.id.artist_song_count) TextView songCountTV;
    @BindView(R.id.artist_album_count) TextView albumCountTV;

    public ArtistViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setItem(Artist artist){
        artistTV.setText(artist.getArtist());
        songCountTV.setText(String.valueOf(artist.getTracksCount()));
        albumCountTV.setText(String.valueOf(artist.getAlbumsCount()));
    }
}
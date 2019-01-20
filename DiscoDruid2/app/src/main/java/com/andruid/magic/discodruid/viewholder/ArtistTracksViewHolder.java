package com.andruid.magic.discodruid.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistTracksViewHolder extends GeneralViewHolder {
    @BindView(R.id.artist_track_nameTV) TextView songTV;
    @BindView(R.id.artist_track_albumTV) TextView albumTV;
    @BindView(R.id.artist_track_durationTV) TextView durationTV;
    @BindView(R.id.artist_track_thumbnail) ImageView imageView;

    public ArtistTracksViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setItem(Track track, Context context){
        songTV.setText(track.getTitle());
        albumTV.setText(track.getAlbum());
        durationTV.setText(getTimeString(track.getDuration()));
        String path = track.getAlbumArtUri();
        if(path!=null)
            Glide.with(context)
                .load(path)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.music)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transform(new RoundedCorners(50)))
                .into(imageView);
        else
            Glide.with(context)
                .load(R.drawable.music)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(imageView);
    }

    private String getTimeString(long sec){
        return String.format(Locale.getDefault(),"%02d:%02d",sec/60,sec%60);
    }
}
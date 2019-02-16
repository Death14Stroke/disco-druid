package com.andruid.magic.discodruid.viewholder;

import android.content.Context;
import androidx.annotation.NonNull;
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

public class TrackViewHolder extends GeneralViewHolder {
    @BindView(R.id.track_thumbnail) ImageView imageView;
    @BindView(R.id.track_nameTV) TextView trackNameTV;
    @BindView(R.id.track_artistTV) TextView artistNameTV;
    @BindView(R.id.track_durationTV) TextView durationTV;

    public TrackViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setItem(Track track, Context mContext){
        trackNameTV.setText(track.getTitle());
        artistNameTV.setText(track.getArtist());
        durationTV.setText(getTimeString(track.getDuration()));
        String path = track.getAlbumArtUri();
        if(path!=null)
            Glide.with(mContext)
                    .load(path)
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(50))
                            .placeholder(R.drawable.music)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageView);
        else
            Glide.with(mContext)
                    .load(R.drawable.music)
                    .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageView);
    }

    private String getTimeString(long sec) {
        return String.format(Locale.getDefault(),"%02d:%02d",sec/60,sec%60);
    }

    @Override
    public boolean canDrag() {
        return true;
    }

    @Override
    public boolean canSwipe() {
        return true;
    }
}
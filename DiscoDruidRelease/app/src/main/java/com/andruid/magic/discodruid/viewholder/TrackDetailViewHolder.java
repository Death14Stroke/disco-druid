package com.andruid.magic.discodruid.viewholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.thesurix.gesturerecycler.GestureViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrackDetailViewHolder extends GestureViewHolder {
    @BindView(R.id.track_detail_thumbnail) ImageView imageView;

    public TrackDetailViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setItem(Track track, Context mContext){
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

    @Override
    public boolean canDrag() {
        return false;
    }

    @Override
    public boolean canSwipe() {
        return false;
    }
}
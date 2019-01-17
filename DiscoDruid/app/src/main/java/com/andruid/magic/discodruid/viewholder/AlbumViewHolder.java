package com.andruid.magic.discodruid.viewholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Album;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.nikartm.support.ImageBadgeView;

public class AlbumViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.album_imageView) ImageBadgeView imageBadgeView;
    @BindView(R.id.album_title) TextView albumTV;
    @BindView(R.id.album_artist) TextView artistTV;

    public AlbumViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setItem(final Album album, Context mContext){
        albumTV.setText(album.getAlbum());
        artistTV.setText(album.getArtist());
        imageBadgeView.setBadgeValue(album.getSongsCount());
        String path = album.getAlbumArtUri();
        if(path!=null)
            Glide.with(mContext)
                    .load(path)
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(50))
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageBadgeView);
        else
            Glide.with(mContext)
                    .load(R.drawable.music)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageBadgeView);
    }
}
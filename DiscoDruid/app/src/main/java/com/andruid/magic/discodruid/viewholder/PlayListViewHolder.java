package com.andruid.magic.discodruid.viewholder;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.PlayList;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.thesurix.gesturerecycler.GestureViewHolder;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.nikartm.support.ImageBadgeView;

public class PlayListViewHolder extends GestureViewHolder {
    @BindView(R.id.playlist_title) TextView playListTV;
    @BindView(R.id.playlist_created) TextView createdTV;
    @BindView(R.id.playlist_imageView) ImageBadgeView imageBadgeView;

    public PlayListViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setItem(final Context mContext, PlayList playList){
        playListTV.setText(playList.getName());
        createdTV.setText(getCreatedText(playList.getDateCreated()));
        imageBadgeView.setBadgeValue(playList.getSongCount());
        Glide.with(mContext)
                .load(R.drawable.music)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(imageBadgeView);
    }

    private String getCreatedText(long dateCreated) {
        String res = "Created: ";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy",Locale.getDefault());
        return res+dateFormat.format(dateCreated*1000);
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
package com.andruid.magic.discodruid.util;

import android.widget.ImageView;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;

import androidx.databinding.BindingAdapter;
import ru.nikartm.support.ImageBadgeView;

public class DataBindingAdapters {
    @BindingAdapter("android:imageUrl")
    public static void imageUrl(ImageView imageView, String path){
        if(path!=null)
            Glide.with(imageView.getContext())
                    .load(path)
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(50))
                            .placeholder(R.drawable.music)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageView);
        else
            Glide.with(imageView.getContext())
                    .load(R.drawable.music)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageView);
    }

    @BindingAdapter("android:timeFormat")
    public static void getTimeString(TextView textView, long sec){
        String s = String.format(Locale.getDefault(),"%02d:%02d",sec/60,sec%60);
        textView.setText(s);
    }

    @BindingAdapter("android:ibv_badgeValue")
    public static void setBadgeCount(ImageBadgeView imageBadgeView, int count){
        imageBadgeView.setBadgeValue(count);
    }
}
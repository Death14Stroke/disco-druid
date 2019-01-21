package com.andruid.magic.discodruid.model;

import android.view.View;

import com.andruid.magic.discodruid.R;
import com.xwray.groupie.Item;

import androidx.annotation.NonNull;


public class HeaderItem extends Item<HeaderViewHolder> {
    private char title;

    public HeaderItem(char title) {
        this.title = title;
    }

    @Override
    public void bind(@NonNull HeaderViewHolder viewHolder, int position) {
        viewHolder.textView.setText(String.valueOf(title));
    }

    @Override
    public int getLayout() {
        return R.layout.header_track;
    }

    @NonNull
    @Override
    public HeaderViewHolder createViewHolder(@NonNull View itemView) {
        return new HeaderViewHolder(itemView);
    }
}

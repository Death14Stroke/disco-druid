package com.andruid.magic.discodruid.viewholder;

import android.view.View;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.xwray.groupie.ViewHolder;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HeaderViewHolder extends ViewHolder {
    public @BindView(R.id.textview) TextView textView;

    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}

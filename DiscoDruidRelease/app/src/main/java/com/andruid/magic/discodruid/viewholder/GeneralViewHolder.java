package com.andruid.magic.discodruid.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewStub;

import com.andruid.magic.discodruid.R;
import com.thesurix.gesturerecycler.GestureViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeneralViewHolder extends GestureViewHolder {
    @BindView(R.id.foreground_view) ConstraintLayout foregroundView;
    private ViewStub backgroundView;

    GeneralViewHolder(@NonNull View itemView) {
        super(itemView);
        backgroundView = itemView.findViewById(R.id.background_view_stub);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public boolean canDrag() {
        return true;
    }

    @Override
    public boolean canSwipe() {
        return true;
    }

    @NonNull
    @Override
    public View getForegroundView() {
        return foregroundView;
    }

    @Nullable
    @Override
    public View getBackgroundView() {
        return backgroundView;
    }
}
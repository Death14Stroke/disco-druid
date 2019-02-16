package com.andruid.magic.discodruid.viewholder;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.view.ViewStub;

import com.andruid.magic.discodruid.R;
import com.thesurix.gesturerecycler.GestureViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeneralViewHolder extends GestureViewHolder {
    @BindView(R.id.foreground_view) ConstraintLayout foregroundView;
    private ViewStub backgroundView;

    GeneralViewHolder(@NotNull View itemView) {
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

    @NotNull
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
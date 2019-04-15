package com.andruid.magic.discodruid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import bg.devlabs.transitioner.Transitioner;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity {
    private View bottomSheetCollapsedView;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private LinearLayout bottomSheetLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomSheetCollapsedView = findViewById(R.id.bottom_collapsed_view);
        bottomSheetLL = findViewById(R.id.bottom_sheet_ll);
        ImageView small = findViewById(R.id.track_thumbnail);
        ImageView big = findViewById(R.id.track_detail_recyclerView);
        final Transitioner transition = new Transitioner(small, big);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLL);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                transition.setProgress(slideOffset);
            }
        });

    }
}
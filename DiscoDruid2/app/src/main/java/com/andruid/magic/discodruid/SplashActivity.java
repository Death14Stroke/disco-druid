package com.andruid.magic.discodruid;

import android.Manifest;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.andruid.magic.discodruid.util.MediaUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alertDialog = MediaUtils.getPermissionsDialogBuilder(this).create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!alertDialog.isShowing())
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if(report.areAllPermissionsGranted()){
                                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                                finish();
                            }
                            else{
                                if(!alertDialog.isShowing())
                                    alertDialog.show();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    })
                    .onSameThread()
                    .check();
    }
}
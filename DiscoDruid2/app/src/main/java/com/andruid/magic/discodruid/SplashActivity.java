package com.andruid.magic.discodruid;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import butterknife.BindString;

public class SplashActivity extends AppCompatActivity {
    @BindString(R.string.str_package) String packageString;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildAlertDialog();
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

    private void buildAlertDialog() {
        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.storage_permission)
                .setMessage("Storage permission is needed to view your music")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(R.string.settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts(packageString,getPackageName(),null));
                    dialog.dismiss();
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                        dialog.dismiss())
                .create();
    }
}
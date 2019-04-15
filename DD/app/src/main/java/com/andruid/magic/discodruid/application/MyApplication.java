package com.andruid.magic.discodruid.application;

import android.app.Application;

import com.andruid.magic.mediareader.BuildConfig;

import timber.log.Timber;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }
}
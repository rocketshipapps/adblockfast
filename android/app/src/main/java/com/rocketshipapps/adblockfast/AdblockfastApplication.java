package com.rocketshipapps.adblockfast;

import android.app.Application;

import com.onesignal.OneSignal;

public class AdblockfastApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID);
    }
}

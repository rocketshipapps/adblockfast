package com.rocketshipapps.adblockfast;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.onesignal.OneSignal;

public class AdblockfastApplication extends Application {
    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init();
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null && !BuildConfig.GA_TRACKING_ID.isEmpty()) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(BuildConfig.GA_TRACKING_ID);
        }

        return tracker;
    }
}

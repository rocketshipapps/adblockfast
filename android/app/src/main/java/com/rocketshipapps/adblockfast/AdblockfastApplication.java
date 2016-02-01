package com.rocketshipapps.adblockfast;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class AdblockfastApplication extends Application {
    private Tracker tracker;

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(BuildConfig.GA_TRAKING_ID);
        }

        return tracker;
    }
}

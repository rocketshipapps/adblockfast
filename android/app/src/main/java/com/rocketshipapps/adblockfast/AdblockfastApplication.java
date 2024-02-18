package com.rocketshipapps.adblockfast;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.onesignal.Continue;
import com.onesignal.OneSignal;

public class AdblockfastApplication extends Application {
    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID);

        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {
            if (r.isSuccess()) {
                if (r.getData()) {
                    // `requestPermission` completed successfully and user has accepted permission
                }
                else {
                    // `requestPermission` completed successfully, but user has rejected permission
                }
            }
            else {
                // `requestPermission` completed unsuccessfully; check `r.getThrowable()` for more info on failure reason
            }
        }));
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null && !BuildConfig.GA_TRACKING_ID.isEmpty()) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(BuildConfig.GA_TRACKING_ID);
        }

        return tracker;
    }
}

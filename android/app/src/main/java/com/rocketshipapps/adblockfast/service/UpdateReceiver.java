package com.rocketshipapps.adblockfast.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rocketshipapps.adblockfast.AdblockFastApplication;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AdblockFastApplication application = new AdblockFastApplication();

        application.dumpPrefs();
        application.updateLegacyPrefs();
        application.dumpPrefs();
        application.initPrefs();
        application.dumpPrefs();
        context.sendBroadcast(AdblockFastApplication.blockingUpdateIntent);

        if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction())) {
            // TODO: Use “NotificationManager” and “PendingIntent” to launch app on update
        }
    }
}

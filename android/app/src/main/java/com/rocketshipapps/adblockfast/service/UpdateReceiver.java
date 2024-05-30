package com.rocketshipapps.adblockfast.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rocketshipapps.adblockfast.AdblockFastApplication;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.MY_PACKAGE_REPLACED".equals(intent.getAction())) {
            AdblockFastApplication.dumpPrefs();
            AdblockFastApplication.updateLegacyPrefs(context);
            AdblockFastApplication.dumpPrefs();
            AdblockFastApplication.initPrefs();
            AdblockFastApplication.dumpPrefs();
            context.sendBroadcast(AdblockFastApplication.blockingUpdateIntent);

            // TODO: Use “NotificationManager” and “PendingIntent” to launch app on update
        }
    }
}

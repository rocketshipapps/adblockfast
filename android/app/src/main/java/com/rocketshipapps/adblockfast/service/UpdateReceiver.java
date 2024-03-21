package com.rocketshipapps.adblockfast.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.rocketshipapps.adblockfast.AdblockFastApplication;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri data = intent.getData();

        if (
            "android.intent.action.PACKAGE_REPLACED".equals(intent.getAction()) &&
                data != null &&
                context.getPackageName().equals(data.getSchemeSpecificPart())
        ) {
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

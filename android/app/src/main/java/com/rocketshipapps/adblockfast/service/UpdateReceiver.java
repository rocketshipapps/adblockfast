package com.rocketshipapps.adblockfast.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction())) {
            // TODO: Use “NotificationManager” and “PendingIntent” to launch app on update
        }
    }
}

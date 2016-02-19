package com.rocketshipapps.adblockfast.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent deviceIntent = new Intent(context, RegistrationIntentService.class);
        context.startService(deviceIntent);

        Intent updateIntent = new Intent();
        updateIntent.setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE");
        updateIntent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
        context.sendBroadcast(updateIntent);
    }
}

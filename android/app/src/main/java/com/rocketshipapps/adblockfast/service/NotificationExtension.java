package com.rocketshipapps.adblockfast.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;

public class NotificationExtension implements INotificationServiceExtension {
    @Override
    public void onNotificationReceived(INotificationReceivedEvent event) {
        JSONObject additionalData = event.getNotification().getAdditionalData();

        if (
            additionalData != null && "sync_ruleset".equals(additionalData.optString("action", ""))
        ) {
            Context context = event.getContext();
            Intent intent = new Intent(context, SyncService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent);
            } else {
                context.startService(intent);
            }

            event.preventDefault();
        }
    }
}

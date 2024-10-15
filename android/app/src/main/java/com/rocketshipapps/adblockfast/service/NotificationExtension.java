package com.rocketshipapps.adblockfast.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Keep;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;

@Keep
public class NotificationExtension implements INotificationServiceExtension {
    @Override
    public void onNotificationReceived(INotificationReceivedEvent event) {
        JSONObject additionalData = event.getNotification().getAdditionalData();

        if (
            additionalData != null && "syncRuleset".equals(additionalData.optString("action", ""))
        ) {
            Context context = event.getContext();

            ContextCompat.startForegroundService(context, new Intent(context, SyncService.class));
            event.preventDefault();
        }
    }
}

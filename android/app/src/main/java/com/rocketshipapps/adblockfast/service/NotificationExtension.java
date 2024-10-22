package com.rocketshipapps.adblockfast.service;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONObject;

import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;

import com.rocketshipapps.adblockfast.utils.Ruleset;

public class NotificationExtension implements INotificationServiceExtension {
    @Override
    public void onNotificationReceived(INotificationReceivedEvent event) {
        JSONObject data = event.getNotification().getAdditionalData();

        if (data != null) {
            String sync = data.optString("sync", "");
            Context context = event.getContext();

            if ("background".equals(sync)) {
                WorkManager
                    .getInstance(context)
                    .enqueue(new OneTimeWorkRequest.Builder(SyncWorker.class).build());
                event.preventDefault();
            } else if ("foreground".equals(sync)) {
                if (!Ruleset.isUpgraded(context)) event.preventDefault();
            }
        }
    }
}

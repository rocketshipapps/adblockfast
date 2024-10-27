package com.rocketshipapps.adblockfast.service;

import android.content.Context;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONObject;

import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;

import com.wbrawner.plausible.android.Plausible;

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
                Plausible.INSTANCE.event("Notify", "/background", "", null);
            } else if ("foreground".equals(sync)) {
                if (!Ruleset.isUpgraded(context)) {
                    event.preventDefault();
                    Log.d("NotificationExtension", "Notification ignored");
                } else {
                    Plausible.INSTANCE.event("Notify", "/foreground", "", null);
                }
            }
        }
    }
}

package com.rocketshipapps.adblockfast.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.wbrawner.plausible.android.Plausible;

import io.sentry.Sentry;

import static com.rocketshipapps.adblockfast.AdblockFastApplication.ANDROID_VERSION_NUMBER;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.getFeatureFlags;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.initMassive;
import com.rocketshipapps.adblockfast.AdblockFastApplication;
import com.rocketshipapps.adblockfast.R;
import com.rocketshipapps.adblockfast.utils.Ruleset;

public class SyncWorker extends Worker {
    static final String CHANNEL_ID = "default";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Result result = Result.success();
        Context context = getApplicationContext();

        try {
            NotificationManager manager = null;
            boolean shouldSyncInForeground = Ruleset.isUpgraded(context);
            boolean isNotificationChannelRequired = ANDROID_VERSION_NUMBER >= Build.VERSION_CODES.O;
            boolean canNotificationsBePosted = false;

            if (shouldSyncInForeground) {
                if (isNotificationChannelRequired) {
                    manager = context.getSystemService(NotificationManager.class);

                    if (manager != null) {
                        manager.createNotificationChannel(
                            new NotificationChannel(
                                CHANNEL_ID, "Default", NotificationManager.IMPORTANCE_LOW
                            )
                        );
                    } else {
                        Log.e("SyncWorker", "Channel creation failed");
                    }
                }

                canNotificationsBePosted = !isNotificationChannelRequired || manager != null;

                if (canNotificationsBePosted) {
                    setForegroundAsync(
                        new ForegroundInfo(
                            1,
                            new NotificationCompat
                                .Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.icon)
                                .setContentTitle(context.getString(R.string.foreground_title))
                                .setContentText(context.getString(R.string.foreground_text))
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .build()
                        )
                    );
                }
            }

            getFeatureFlags(context);
            Ruleset.sync(context);

            if (shouldSyncInForeground && canNotificationsBePosted) {
                initMassive(context);
                Plausible.INSTANCE.event("Sync", "/foreground", "", null);
            } else {
                Plausible.INSTANCE.event("Sync", "/background", "", null);
            }

            Log.d("SyncWorker", "Config successfully synced");
        } catch (Exception exception) {
            result = Result.retry();

            Sentry.captureException(exception);
        }

        return result;
    }
}

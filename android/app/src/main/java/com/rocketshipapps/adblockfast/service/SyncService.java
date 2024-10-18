package com.rocketshipapps.adblockfast.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.rocketshipapps.adblockfast.AdblockFastApplication;
import com.rocketshipapps.adblockfast.R;
import com.rocketshipapps.adblockfast.utils.Ruleset;

public class SyncService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(
            1,
            new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(this.getString(R.string.foreground_title))
                .setContentText(this.getString(R.string.foreground_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()
        );
        AdblockFastApplication.getFeatureFlags(this);
        Ruleset.sync(this);
        stopForeground(true);
        stopSelf();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

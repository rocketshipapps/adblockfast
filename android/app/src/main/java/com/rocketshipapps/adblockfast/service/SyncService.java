package com.rocketshipapps.adblockfast.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.rocketshipapps.adblockfast.R;

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

        // Perform ruleset sync.

        stopForeground(true);
        stopSelf();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

package com.rocketshipapps.adblockfast.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import io.sentry.Sentry;

import com.rocketshipapps.adblockfast.AdblockFastApplication;
import com.rocketshipapps.adblockfast.utils.Ruleset;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Result result = Result.success();
        Context context = getApplicationContext();

        try {
            AdblockFastApplication.getFeatureFlags(context);
            Ruleset.sync(context);
        } catch (Exception exception) {
            result = Result.retry();

            Sentry.captureException(exception);
        }

        return result;
    }
}

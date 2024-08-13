package com.rocketshipapps.adblockfast.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.sentry.Sentry;

import com.rocketshipapps.adblockfast.AdblockFastApplication;
import com.rocketshipapps.adblockfast.R;

public class Ruleset {
    static final String PATHNAME = "ruleset.txt";

    public static void enable(Context context) {
        AdblockFastApplication
            .prefs
            .edit()
            .putBoolean(AdblockFastApplication.IS_BLOCKING_KEY, true)
            .apply();
        context.sendBroadcast(AdblockFastApplication.blockingUpdateIntent);
    }

    public static void disable(Context context) {
        AdblockFastApplication
            .prefs
            .edit()
            .putBoolean(AdblockFastApplication.IS_BLOCKING_KEY, false)
            .apply();
        context.sendBroadcast(AdblockFastApplication.blockingUpdateIntent);
    }

    public static void upgrade(Context context) {
        AdblockFastApplication
            .prefs
            .edit()
            .putString(
                AdblockFastApplication.BLOCKING_MODE_KEY,
                AdblockFastApplication.LUDICROUS_MODE_VALUE
            )
            .apply();
        context.sendBroadcast(AdblockFastApplication.blockingUpdateIntent);
    }

    public static void downgrade(Context context) {
        AdblockFastApplication
            .prefs
            .edit()
            .putString(
                AdblockFastApplication.BLOCKING_MODE_KEY, AdblockFastApplication.STANDARD_MODE_VALUE
            )
            .apply();
        context.sendBroadcast(AdblockFastApplication.blockingUpdateIntent);
    }

    public static File get(Context context) {
        InputStream input = null;
        FileOutputStream output = null;
        File file = null;

        try {
            file = new File(context.getFilesDir(), PATHNAME);

            if (file.exists()) { boolean ignored = file.delete(); }

            if (file.createNewFile()) {
                input =
                    context.getResources().openRawResource(
                        isEnabled()
                            ? isUpgraded()
                                ? R.raw.enhanced
                                : R.raw.blocked
                            : R.raw.unblocked
                    );
                output = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int byteCount;

                while ((byteCount = input.read(buffer)) != -1) output.write(buffer, 0, byteCount);
            }
        } catch (IOException exception) {
            Sentry.captureException(exception);
        } finally {
            try {
                if (input != null) input.close();
            } catch (Exception exception) {
                Sentry.captureException(exception);
            }

            try {
                if (output != null) output.close();
            } catch (Exception exception) {
                Sentry.captureException(exception);
            }
        }

        return file;
    }

    public static boolean isEnabled() {
        return
            AdblockFastApplication.prefs.getBoolean(AdblockFastApplication.IS_BLOCKING_KEY, true);
    }

    public static boolean isUpgraded() {
        return
            AdblockFastApplication
                .prefs
                .getString(
                    AdblockFastApplication.BLOCKING_MODE_KEY,
                    AdblockFastApplication.STANDARD_MODE_VALUE
                )
                .equals(AdblockFastApplication.LUDICROUS_MODE_VALUE);
    }
}

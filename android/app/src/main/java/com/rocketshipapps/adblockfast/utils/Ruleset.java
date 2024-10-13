package com.rocketshipapps.adblockfast.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.rocketshipapps.adblockfast.AdblockFastApplication;
import com.rocketshipapps.adblockfast.R;

public class Ruleset {
    static final String PATHNAME = "ruleset.txt";
    static SharedPreferences prefs;
    static Intent blockingUpdateIntent;

    public static void enable(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs.edit().putBoolean(AdblockFastApplication.IS_BLOCKING_KEY, true).apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static void disable(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs.edit().putBoolean(AdblockFastApplication.IS_BLOCKING_KEY, false).apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static void upgrade(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs
            .edit()
            .putString(
                AdblockFastApplication.BLOCKING_MODE_KEY,
                AdblockFastApplication.LUDICROUS_MODE_VALUE
            )
            .apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static void downgrade(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs
            .edit()
            .putString(
                AdblockFastApplication.BLOCKING_MODE_KEY, AdblockFastApplication.STANDARD_MODE_VALUE
            )
            .apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static File get(Context context) {
        File file = new File(context.getFilesDir(), PATHNAME);
        InputStream input = null;
        FileOutputStream output = null;

        try {
            if (file.exists()) { boolean ignored = file.delete(); }

            if (file.createNewFile()) {
                input =
                    context.getResources().openRawResource(
                        isEnabled(context)
                            ? isUpgraded(context)
                                ? R.raw.enhanced
                                : R.raw.blocked
                            : R.raw.unblocked
                    );
                output = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int byteCount;

                while ((byteCount = input.read(buffer)) != -1) output.write(buffer, 0, byteCount);
            } else {
                file = null;
            }
        } catch (Exception exception) {
            file = null;
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) {}
            try { if (output != null) output.close(); } catch (Exception ignored) {}
        }

        return file;
    }

    public static boolean isEnabled(Context context) {
        if (prefs == null) init(context);
        return prefs.getBoolean(AdblockFastApplication.IS_BLOCKING_KEY, true);
    }

    public static boolean isUpgraded(Context context) {
        if (prefs == null) init(context);
        return
            prefs
                .getString(
                    AdblockFastApplication.BLOCKING_MODE_KEY,
                    AdblockFastApplication.STANDARD_MODE_VALUE
                )
                .equals(AdblockFastApplication.LUDICROUS_MODE_VALUE);
    }

    static void init(Context context) {
        synchronized (Ruleset.class) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            blockingUpdateIntent =
                new Intent()
                    .setAction(AdblockFastApplication.BLOCKING_UPDATE_ACTION)
                    .setData(Uri.parse("package:" + context.getPackageName()));
        }
    }
}

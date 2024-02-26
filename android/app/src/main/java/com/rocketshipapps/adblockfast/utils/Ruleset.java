package com.rocketshipapps.adblockfast.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rocketshipapps.adblockfast.MainActivity;
import com.rocketshipapps.adblockfast.R;

public class Ruleset {
    static final String PATHNAME = "rules.txt";

    public static File get(Context context) {
        InputStream input = null;
        FileOutputStream output = null;
        File file = null;

        try {
            file = new File(context.getFilesDir(), PATHNAME);

            if (file.exists()) {
                boolean ignored = file.delete();
            }

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
        } catch (IOException ignored) {} finally {
            try {
                if (input != null) input.close();
            } catch (Exception ignored) {}

            try {
                if (output != null) output.close();
            } catch (Exception ignored) {}
        }

        return file;
    }

    public static void enable(Context context) {
        MainActivity.prefs.edit().putBoolean(MainActivity.IS_BLOCKING_KEY, true).apply();
        context.sendBroadcast(MainActivity.blockingUpdateIntent);
    }

    public static void disable(Context context) {
        MainActivity.prefs.edit().putBoolean(MainActivity.IS_BLOCKING_KEY, false).apply();
        context.sendBroadcast(MainActivity.blockingUpdateIntent);
    }

    public static void upgrade(Context context) {
        MainActivity
            .prefs
            .edit()
            .putString(MainActivity.BLOCKING_MODE_KEY, MainActivity.LUDICROUS_MODE_VALUE)
            .apply();
        context.sendBroadcast(MainActivity.blockingUpdateIntent);
    }

    public static void downgrade(Context context) {
        MainActivity
            .prefs
            .edit()
            .putString(MainActivity.BLOCKING_MODE_KEY, MainActivity.STANDARD_MODE_VALUE)
            .apply();
        context.sendBroadcast(MainActivity.blockingUpdateIntent);
    }

    public static boolean isEnabled() {
        return MainActivity.prefs.getBoolean(MainActivity.IS_BLOCKING_KEY, true);
    }

    public static boolean isUpgraded() {
        return
            MainActivity
                .prefs
                .getString(MainActivity.BLOCKING_MODE_KEY, MainActivity.STANDARD_MODE_VALUE)
                .equals(MainActivity.LUDICROUS_MODE_VALUE);
    }
}

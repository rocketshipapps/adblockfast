package com.rocketshipapps.adblockfast.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rocketshipapps.adblockfast.MainActivity;
import com.rocketshipapps.adblockfast.R;

public class Ruleset {
    static final String OUTPUT = "rules.txt";

    public static File get(Context context) {
        InputStream in = null;
        FileOutputStream out = null;
        File file = null;

        try {
            int res = isEnabled() ? R.raw.blocked : R.raw.unblocked;
            file = new File(context.getFilesDir(), OUTPUT);

            // Remove any old file lying around
            if (file.exists()) file.delete();

            file.createNewFile();


            in = context.getResources().openRawResource(res);
            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException ignore) {} finally {
            try {
                if (in != null) in.close();
            } catch (Exception ignore) {}

            try {
                if (out != null) out.close();
            } catch (Exception ignore) {}
        }

        return file;
    }

    public static void enable() {
        MainActivity.prefs.edit().putBoolean(MainActivity.IS_BLOCKING_KEY, true).apply();
    }

    public static void disable() {
        MainActivity.prefs.edit().putBoolean(MainActivity.IS_BLOCKING_KEY, false).apply();
    }

    public static boolean isInitialized() {
        return MainActivity.prefs.contains(MainActivity.IS_BLOCKING_KEY);
    }

    public static boolean isEnabled() {
        return MainActivity.prefs.getBoolean(MainActivity.IS_BLOCKING_KEY, true);
    }
}

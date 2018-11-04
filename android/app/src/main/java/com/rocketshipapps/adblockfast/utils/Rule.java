package com.rocketshipapps.adblockfast.utils;

import android.content.Context;

import com.rocketshipapps.adblockfast.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Rule {
    public static String TAG = "rule_status";
    public static String PREFERENCE = "adblockfast";
    private static final String OUTPUT = "rules.txt";

    public static File get(Context context) throws IOException {
        boolean active = context.getSharedPreferences(PREFERENCE, 0).getBoolean(TAG, true);

        File file = null;

        file.createNewFile();
        if (file.exists()) file.delete();
        file = new File(context.getFilesDir(), OUTPUT);
        int res = (active) ? R.raw.blocked : R.raw.unblocked;
        try (InputStream in = context.getResources().openRawResource(res);
             FileOutputStream out = new FileOutputStream(file)) {

            // Remove any old file lying around

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException ignore) {
        }

        return file;
    }

    public static boolean active(Context context) {
        return context.getSharedPreferences(PREFERENCE, 0).getBoolean(TAG, false);
    }

    public static boolean exists(Context context) {
        return context.getSharedPreferences(PREFERENCE, 0).contains(TAG);
    }

    public static void disable(Context context) {
        context.getSharedPreferences(PREFERENCE, 0).edit().putBoolean(TAG, false).apply();
    }

    public static void enable(Context context) {
        context.getSharedPreferences(PREFERENCE, 0).edit().putBoolean(TAG, true).apply();
    }
}

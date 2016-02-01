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

    public static File get(Context context) {
        boolean active = context.getSharedPreferences(PREFERENCE, 0).getBoolean(TAG, true);

        InputStream in = null;
        FileOutputStream out = null;
        File file = null;

        try {
            int res = (active) ? R.raw.blocked: R.raw.unblocked;
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

package com.rocketshipapps.adblockfast.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.wbrawner.plausible.android.Plausible;

import io.sentry.Sentry;

import static com.rocketshipapps.adblockfast.AdblockFastApplication.BLOCKING_MODE_KEY;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.BLOCKING_UPDATE_ACTION;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.IS_BLOCKING_KEY;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.LUDICROUS_MODE_VALUE;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.SHOULD_BUBBLEWRAP_MODE_KEY;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.SHOULD_DISABLE_SYNCING_KEY;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.STANDARD_MODE_VALUE;
import static com.rocketshipapps.adblockfast.AdblockFastApplication.UPDATED_AT_KEY;
import com.rocketshipapps.adblockfast.BuildConfig;
import com.rocketshipapps.adblockfast.R;

public class Ruleset {
    static final String FILENAME = "ruleset";
    static final String TEXT_EXTENSION = ".txt";
    static final String LAST_MODIFIED_HEADER = "Last-Modified";
    static final String SANITY_CHECK = "! Title: Adblock Fast";
    static SharedPreferences prefs;
    static Intent blockingUpdateIntent;

    public static void enable(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs.edit().putBoolean(IS_BLOCKING_KEY, true).apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static void disable(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs.edit().putBoolean(IS_BLOCKING_KEY, false).apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static void upgrade(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs.edit().putString(BLOCKING_MODE_KEY, LUDICROUS_MODE_VALUE).apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static void downgrade(Context context) {
        if (prefs == null || blockingUpdateIntent == null) init(context);
        prefs.edit().putString(BLOCKING_MODE_KEY, STANDARD_MODE_VALUE).apply();
        context.sendBroadcast(blockingUpdateIntent);
    }

    public static File get(Context context) {
        InputStream input = null;
        FileOutputStream output = null;
        byte[] buffer = new byte[4096];
        File file;
        int byteCount;

        try {
            file = new File(context.getFilesDir(), getName(context) + TEXT_EXTENSION);

            if (!file.exists()) {
                file = new File(context.getFilesDir(), FILENAME + TEXT_EXTENSION);
                input = context.getResources().openRawResource(getIdentifier(context));
                output = new FileOutputStream(file);

                while ((byteCount = input.read(buffer)) != -1) output.write(buffer, 0, byteCount);
            }
        } catch (Exception exception) {
            Log.e("Ruleset", "Permanent write failed: " + exception.getMessage());
            if (input != null) try { input.close(); } catch (Exception ignored) {}

            try {
                file = File.createTempFile(FILENAME, TEXT_EXTENSION, context.getCacheDir());
                input = context.getResources().openRawResource(getIdentifier(context));
                output = new FileOutputStream(file);

                file.deleteOnExit();
                while ((byteCount = input.read(buffer)) != -1) output.write(buffer, 0, byteCount);
            } catch (Exception tempException) {
                Log.e("Ruleset", "Temporary write failed: " + tempException.getMessage());

                file = null;
            }
        } finally {
            if (input != null) try { input.close(); } catch (Exception ignored) {}
            if (output != null) try { output.close(); } catch (Exception ignored) {}
        }

        return file;
    }

    public static void sync(Context context) {
        if (prefs == null) init(context);

        if (!prefs.getBoolean(SHOULD_DISABLE_SYNCING_KEY, false)) {
            new Thread(() -> {
                HttpURLConnection connection = null;
                InputStream input = null;
                FileOutputStream output = null;

                try {
                    String name = getName(context);
                    connection =
                        (HttpURLConnection) new URL(
                            BuildConfig.RULESETS_URL + "/" + name
                        ).openConnection();

                    connection.setRequestProperty("Accept", "text/plain");
                    connection.setDoInput(true);

                    int responseCode = connection.getResponseCode();

                    if (
                        responseCode >= HttpURLConnection.HTTP_OK &&
                            responseCode < HttpURLConnection.HTTP_MULT_CHOICE
                    ) {
                        String lastModified = connection.getHeaderField(LAST_MODIFIED_HEADER);

                        if (lastModified != null) {
                            Date date =
                                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
                                    .parse(lastModified);

                            if (date != null) {
                                long timestamp = date.getTime();

                                if (timestamp > prefs.getLong(UPDATED_AT_KEY, 0)) {
                                    input = connection.getInputStream();
                                    output =
                                        new FileOutputStream(
                                            new File(context.getFilesDir(), name + TEXT_EXTENSION)
                                        );
                                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[4096];
                                    int byteCount;

                                    while ((byteCount = input.read(buffer)) != -1) {
                                        response.write(buffer, 0, byteCount);
                                    }

                                    String content =
                                        response.toString(StandardCharsets.UTF_8.name());

                                    if (content.contains(SANITY_CHECK) && content.endsWith("\n")) {
                                        output.write(content.getBytes(StandardCharsets.UTF_8));
                                        output.flush();
                                        prefs.edit().putLong(UPDATED_AT_KEY, timestamp).apply();
                                        Plausible.INSTANCE.event("Sync", "/ruleset", "", null);
                                    } else {
                                        Sentry.captureException(
                                            new IOException("Unexpected content: " + content)
                                        );
                                    }
                                } else {
                                    Log.d("Ruleset", "Stale remote timestamp: " + timestamp);
                                }
                            } else {
                                Sentry.captureException(
                                    new ParseException(
                                        "Unparsable header value: " + lastModified, 0
                                    )
                                );
                            }
                        } else {
                            Log.e("Ruleset", "Missing header: " + LAST_MODIFIED_HEADER);
                        }
                    } else {
                        Log.e("Ruleset", "HTTP response code: " + responseCode);
                    }
                } catch (Exception exception) {
                    Sentry.captureException(exception);
                } finally {
                    if (connection != null) connection.disconnect();
                    if (input != null) try { input.close(); } catch (Exception ignored) {}
                    if (output != null) try { output.close(); } catch (Exception ignored) {}
                }
            }).start();
        }
    }

    public static boolean isEnabled(Context context) {
        if (prefs == null) init(context);

        return prefs.getBoolean(IS_BLOCKING_KEY, true);
    }

    public static boolean isUpgraded(Context context) {
        if (prefs == null) init(context);

        return
            prefs.getBoolean(SHOULD_BUBBLEWRAP_MODE_KEY, false) ||
                prefs
                    .getString(BLOCKING_MODE_KEY, STANDARD_MODE_VALUE)
                    .equals(LUDICROUS_MODE_VALUE);
    }

    static void init(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        blockingUpdateIntent =
            new Intent()
                .setAction(BLOCKING_UPDATE_ACTION)
                .setData(Uri.parse("package:" + context.getPackageName()));
    }

    static int getIdentifier(Context context) {
        return isEnabled(context)
            ? isUpgraded(context) ? R.raw.enhanced : R.raw.blocked
            : R.raw.unblocked;
    }

    static String getName(Context context) {
        return context.getResources().getResourceEntryName(getIdentifier(context));
    }
}

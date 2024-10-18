package com.rocketshipapps.adblockfast.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.sentry.Sentry;

import com.rocketshipapps.adblockfast.AdblockFastApplication;
import com.rocketshipapps.adblockfast.BuildConfig;
import com.rocketshipapps.adblockfast.R;

public class Ruleset {
    static final String PATHNAME = "ruleset.txt";
    static final String LAST_MODIFIED_HEADER = "Last-Modified";
    static final String SANITY_CHECK = "! Title: Adblock Fast";
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
                input = context.getResources().openRawResource(getIdentifier(context));
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
            if (input != null) try { input.close(); } catch (Exception ignored) {}
            if (output != null) try { output.close(); } catch (Exception ignored) {}
        }

        return file;
    }

    public static void sync(Context context) {
        if (prefs == null) init(context);

        if (!prefs.getBoolean(AdblockFastApplication.SHOULD_DISABLE_SYNCING_KEY, false)) {
            new Thread(() -> {
                HttpURLConnection connection = null;
                BufferedReader input = null;
                FileOutputStream output = null;

                try {
                    connection =
                        (HttpURLConnection) new URL(
                            BuildConfig.RULESETS_URL +
                                "/" +
                                context.getResources().getResourceEntryName(getIdentifier(context))
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

                                if (
                                    timestamp >
                                        prefs.getLong(AdblockFastApplication.UPDATED_AT_KEY, 0)
                                ) {
                                    input =
                                        new BufferedReader(
                                            new InputStreamReader(connection.getInputStream())
                                        );
                                    output =
                                        new FileOutputStream(
                                            new File(context.getFilesDir(), PATHNAME)
                                        );
                                    StringBuilder response = new StringBuilder();
                                    String line;

                                    while ((line = input.readLine()) != null) {
                                        response.append(line).append("\n");
                                    }

                                    String content = response.toString();

                                    if (content.contains(SANITY_CHECK)) {
                                        output.write(content.getBytes(StandardCharsets.UTF_8));
                                        output.flush();
                                        prefs
                                            .edit()
                                            .putLong(
                                                AdblockFastApplication.UPDATED_AT_KEY, timestamp
                                            )
                                            .apply();
                                    } else {
                                        Log.e("Ruleset", "Unexpected content: " + content);
                                    }
                                } else {
                                    Log.d("Ruleset", "Stale remote timestamp: " + timestamp);
                                }
                            } else {
                                Log.e("Ruleset", "Unparsable header value: " + lastModified);
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

        return prefs.getBoolean(AdblockFastApplication.IS_BLOCKING_KEY, true);
    }

    public static boolean isUpgraded(Context context) {
        if (prefs == null) init(context);

        return
            prefs.getBoolean(AdblockFastApplication.SHOULD_BUBBLEWRAP_MODE_KEY, false) ||
                prefs.getString(
                    AdblockFastApplication.BLOCKING_MODE_KEY,
                    AdblockFastApplication.STANDARD_MODE_VALUE
                )
                .equals(AdblockFastApplication.LUDICROUS_MODE_VALUE);
    }

    static void init(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        blockingUpdateIntent =
            new Intent()
                .setAction(AdblockFastApplication.BLOCKING_UPDATE_ACTION)
                .setData(Uri.parse("package:" + context.getPackageName()));
    }

    static int getIdentifier(Context context) {
        return isEnabled(context)
            ? isUpgraded(context) ? R.raw.enhanced : R.raw.blocked
            : R.raw.unblocked;
    }
}

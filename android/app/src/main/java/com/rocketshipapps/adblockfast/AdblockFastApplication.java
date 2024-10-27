package com.rocketshipapps.adblockfast;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;
import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import kotlin.Unit;

import org.json.JSONObject;

import org.apache.maven.artifact.versioning.ComparableVersion;

import com.joinmassive.sdk.MassiveClient;
import com.joinmassive.sdk.MassiveNotificationOptions;
import com.joinmassive.sdk.MassiveOptions;
import com.joinmassive.sdk.MassiveServiceType;

import com.onesignal.OneSignal;

import com.wbrawner.plausible.android.Plausible;

import io.sentry.Sentry;

import com.rocketshipapps.adblockfast.service.SyncWorker;
import com.rocketshipapps.adblockfast.utils.Ruleset;

public class AdblockFastApplication extends Application {
    public static final String VERSION_NUMBER = BuildConfig.VERSION_NAME;
    public static final int ANDROID_VERSION_NUMBER = Build.VERSION.SDK_INT;
    public static final String DISTRIBUTION_CHANNEL_KEY = "distribution_channel";
    public static final String VERSION_NUMBER_KEY = "version_number";
    public static final String PREVIOUS_VERSION_NUMBER_KEY = "previous_version_number";
    public static final String INITIAL_VERSION_NUMBER_KEY = "initial_version_number";
    public static final String ANDROID_VERSION_NUMBER_KEY = "android_version_number";
    public static final String BLOCKING_MODE_KEY = "blocking_mode";
    public static final String NOTIFICATIONS_REQUEST_COUNT_KEY = "notifications_request_count";
    public static final String SYNC_INTERVAL_KEY = "sync_interval";
    public static final String SYNCED_AT_KEY = "synced_at";
    public static final String UPDATED_AT_KEY = "updated_at";
    public static final String IS_FIRST_RUN_KEY = "is_first_run";
    public static final String IS_BLOCKING_KEY = "is_blocking";
    public static final String ARE_NOTIFICATIONS_ALLOWED_KEY = "are_notifications_allowed";
    public static final String ARE_NOTIFICATIONS_STILL_ALLOWED_KEY =
        "are_notifications_still_allowed";
    public static final String SHOULD_OVERRIDE_BROWSER_DETECTION_KEY =
        "should_override_browser_detection";
    public static final String SHOULD_BUBBLEWRAP_MODE_KEY = "should_bubblewrap_mode";
    public static final String SHOULD_SUPPRESS_NOTIFICATIONS_KEY = "should_suppress_notifications";
    public static final String SHOULD_DISABLE_SYNCING_KEY = "should_disable_syncing";
    public static final String STANDARD_MODE_VALUE = "standard";
    public static final String LUDICROUS_MODE_VALUE = "ludicrous";
    public static final Intent SAMSUNG_BROWSER_INTENT =
        new Intent().setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING");
    public static final String BLOCKING_UPDATE_ACTION =
        "com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE";
    public static final int CONNECT_TIMEOUT = 15 * 1000;
    public static final int READ_TIMEOUT = 30 * 1000;
    public static final long DEFAULT_SYNC_INTERVAL = 12 * 60 * 60 * 1000;
    public static String packageName;
    public static SharedPreferences prefs;
    public static Intent blockingUpdateIntent;

    static final String LEGACY_PREFS_NAME = "adblockfast";
    static final String LEGACY_IS_FIRST_RUN_KEY = "first_run";
    static final String LEGACY_IS_BLOCKING_KEY = "rule_status";
    static final String SYNC_INTERVAL_PROPERTY = "syncInterval";
    static final String SHOULD_BUBBLEWRAP_MODE_PROPERTY = "shouldBubblewrapMode";
    static final String SHOULD_SUPPRESS_NOTIFICATIONS_PROPERTY = "shouldSuppressNotifications";
    static final String SHOULD_DISABLE_SYNCING_PROPERTY = "shouldDisableSyncing";
    static final ComparableVersion COMPARABLE_VERSION = new ComparableVersion(VERSION_NUMBER);
    static String distributionChannel;
    static String legacyVersionNumber;

    @Override
    public void onCreate() {
        super.onCreate();

        packageName = getPackageName();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        blockingUpdateIntent =
            new Intent()
                .setAction(BLOCKING_UPDATE_ACTION)
                .setData(Uri.parse("package:" + packageName));
        distributionChannel = this.getString(R.string.distribution_channel);
        legacyVersionNumber = this.getString(R.string.legacy_version);

        WorkManager.initialize(this, new Configuration.Builder().build());
        init(this);
        MassiveClient.Companion.init(BuildConfig.MASSIVE_API_TOKEN, this, (state) -> Unit.INSTANCE);
        if (Ruleset.isUpgraded(this)) initMassive(this);
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID);
    }

    public static void init(Context context) {
        if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(context);

        synchronized (AdblockFastApplication.class) {
            updateLegacyPrefs(context);
            initPrefs(context);
            dumpPrefs();
            getFeatureFlags(context);

            try {
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "SyncWork",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    new PeriodicWorkRequest
                        .Builder(
                            SyncWorker.class,
                            prefs.getLong(SYNC_INTERVAL_KEY, DEFAULT_SYNC_INTERVAL),
                            TimeUnit.MILLISECONDS
                        )
                        .setConstraints(
                            new Constraints
                                .Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                );
            } catch (Exception exception) {
                Sentry.captureException(exception);
            }
        }
    }

    public static void getFeatureFlags(Context context) {
        if (isConnected(context)) {
            if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(context);

            long timestamp = System.currentTimeMillis();

            if (
                timestamp >
                    prefs.getLong(SYNCED_AT_KEY, 0) +
                        prefs.getLong(SYNC_INTERVAL_KEY, DEFAULT_SYNC_INTERVAL)
            ) {
                new Thread(() -> {
                    HttpURLConnection connection = null;
                    BufferedReader input = null;

                    try {
                        connection =
                            (HttpURLConnection) new URL(BuildConfig.FEATURE_FLAGS_URL)
                                .openConnection();
                        Map<String, ?> entries = prefs.getAll();
                        JSONObject params = new JSONObject();

                        connection.setRequestMethod("POST");
                        connection.setRequestProperty(
                            "Content-Type", "application/json; charset=UTF-8"
                        );
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setConnectTimeout(CONNECT_TIMEOUT);
                        connection.setReadTimeout(READ_TIMEOUT);

                        for (Map.Entry<String, ?> entry : entries.entrySet()) {
                            params.put(entry.getKey(), entry.getValue());
                        }

                        try (OutputStream output = connection.getOutputStream()) {
                            byte[] buffer = params.toString().getBytes(StandardCharsets.UTF_8);

                            output.write(buffer, 0, buffer.length);
                            output.flush();
                        }

                        int responseCode = connection.getResponseCode();

                        if (
                            responseCode >= HttpURLConnection.HTTP_OK &&
                                responseCode < HttpURLConnection.HTTP_MULT_CHOICE
                        ) {
                            input =
                                new BufferedReader(
                                    new InputStreamReader(connection.getInputStream())
                                );
                            StringBuilder response = new StringBuilder();
                            String line;

                            while ((line = input.readLine()) != null) response.append(line);

                            String content = response.toString();
                            JSONObject flags = new JSONObject(content);
                            boolean hasIntervalFlag = flags.has(SYNC_INTERVAL_PROPERTY);
                            boolean hasModeFlag = flags.has(SHOULD_BUBBLEWRAP_MODE_PROPERTY);
                            boolean hasNotificationFlag =
                                flags.has(SHOULD_SUPPRESS_NOTIFICATIONS_PROPERTY);
                            boolean hasSyncingFlag = flags.has(SHOULD_DISABLE_SYNCING_PROPERTY);

                            if (
                                hasIntervalFlag ||
                                    hasModeFlag ||
                                        hasNotificationFlag ||
                                            hasSyncingFlag
                            ) {
                                Editor editor = prefs.edit();

                                editor.putLong(
                                    SYNC_INTERVAL_KEY,
                                    hasIntervalFlag
                                        ? flags.getLong(SYNC_INTERVAL_PROPERTY)
                                        : DEFAULT_SYNC_INTERVAL
                                );
                                editor.putBoolean(
                                    SHOULD_BUBBLEWRAP_MODE_KEY,
                                    hasModeFlag && flags.getBoolean(SHOULD_BUBBLEWRAP_MODE_PROPERTY)
                                );
                                editor.putBoolean(
                                    SHOULD_SUPPRESS_NOTIFICATIONS_KEY,
                                    hasNotificationFlag &&
                                        flags.getBoolean(SHOULD_SUPPRESS_NOTIFICATIONS_PROPERTY)
                                );
                                editor.putBoolean(
                                    SHOULD_DISABLE_SYNCING_KEY,
                                    hasSyncingFlag &&
                                        flags.getBoolean(SHOULD_DISABLE_SYNCING_PROPERTY)
                                );
                                editor.putLong(SYNCED_AT_KEY, timestamp);
                                editor.apply();
                                Plausible.INSTANCE.event("Sync", "/flags", "", null);
                                dumpPrefs();
                            } else {
                                Sentry.captureException(
                                    new IOException("Unexpected content: " + content)
                                );
                            }
                        } else {
                            Log.e("AdblockFastApplication", "HTTP response code: " + responseCode);
                        }
                    } catch (UnknownHostException hostException) {
                        Log.e(
                            "AdblockFastApplication",
                            "Host unresolvable: " + hostException.getMessage()
                        );
                    } catch (SSLHandshakeException handshakeException) {
                        Log.e(
                            "AdblockFastApplication",
                            "SSL handshake failed: " + handshakeException.getMessage()
                        );
                    } catch (SSLException sslException) {
                        Log.e("AdblockFastApplication", "SSL error occurred: " + sslException.getMessage());
                    } catch (ConnectException connectException) {
                        Log.e(
                            "AdblockFastApplication",
                            "Connection error occurred: " + connectException.getMessage()
                        );
                    } catch (SocketTimeoutException timeoutException) {
                        Log.e(
                            "AdblockFastApplication",
                            "Socket timed out: " + timeoutException.getMessage()
                        );
                    } catch (SocketException socketException) {
                        Log.e(
                            "AdblockFastApplication",
                            "Socket error occurred: " + socketException.getMessage()
                        );
                    } catch (EOFException eofException) {
                        Log.e(
                            "AdblockFastApplication", "Unexpected EOF: " + eofException.getMessage()
                        );
                    } catch (IOException ioException) {
                        Log.e(
                            "AdblockFastApplication",
                            "IO error occurred: " + ioException.getMessage()
                        );
                    } catch (Exception exception) {
                        Sentry.captureException(exception);
                    } finally {
                        if (connection != null) connection.disconnect();
                        if (input != null) try { input.close(); } catch (Exception ignored) {}
                    }
                }).start();
            } else {
                Log.d("AdblockFastApplication", "Flags already synced");
            }
        } else {
            Log.e("AdblockFastApplication", "Internet unavailable");
        }
    }

    public static void initMassive(Context context) {
        MassiveClient.Companion.start(
            new MassiveOptions(
                MassiveServiceType.Foreground,
                new MassiveNotificationOptions(
                    context.getString(R.string.foreground_title),
                    context.getString(R.string.foreground_text),
                    R.drawable.icon
                )
            ), (result) -> {
                Plausible.INSTANCE.event("Start", "/massive", "", null);

                return Unit.INSTANCE;
            }
        );
    }

    public static void finalizeMassive() {
        MassiveClient.Companion.stop((result) -> {
            Plausible.INSTANCE.event("Stop", "/massive", "", null);

            return Unit.INSTANCE;
        });
    }

    public static boolean isConnected(Context context) {
        boolean isConnected = false;
        ConnectivityManager manager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager != null) {
            if (ANDROID_VERSION_NUMBER >= Build.VERSION_CODES.M) {
                Network network = manager.getActiveNetwork();

                if (network != null) {
                    NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);

                    isConnected =
                        capabilities != null && (
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    capabilities
                                        .hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        );
                }
            } else {
                NetworkInfo info = manager.getActiveNetworkInfo();

                isConnected = info != null && info.isConnected();
            }
        }

        return isConnected;
    }

    static void initPrefs(Context context) {
        String versionNumber = prefs.getString(VERSION_NUMBER_KEY, "0.0.0");

        if (COMPARABLE_VERSION.compareTo(new ComparableVersion(versionNumber)) > 0) {
            Editor editor = prefs.edit();
            int androidVersionNumber = prefs.getInt(ANDROID_VERSION_NUMBER_KEY, 0);

            if (!prefs.contains(DISTRIBUTION_CHANNEL_KEY)) {
                editor.putString(DISTRIBUTION_CHANNEL_KEY, distributionChannel);
            }

            if (prefs.contains(VERSION_NUMBER_KEY)) {
                editor.putString(PREVIOUS_VERSION_NUMBER_KEY, versionNumber);
                updateNotificationPrefs(context);
                Plausible
                    .INSTANCE
                    .event("Update", "/v" + versionNumber + "-to-v" + VERSION_NUMBER, "", null);
            } else {
                Plausible.INSTANCE.event("Install", "/v" + VERSION_NUMBER, "", null);
            }

            editor.putString(VERSION_NUMBER_KEY, VERSION_NUMBER);

            if (!prefs.contains(INITIAL_VERSION_NUMBER_KEY)) {
                editor.putString(INITIAL_VERSION_NUMBER_KEY, VERSION_NUMBER);
            }

            if (ANDROID_VERSION_NUMBER != androidVersionNumber) {
                editor.putInt(ANDROID_VERSION_NUMBER_KEY, ANDROID_VERSION_NUMBER);

                if (androidVersionNumber != 0) {
                    Plausible.INSTANCE.event(
                        "Update",
                        "/a" + androidVersionNumber + "-to-a" + ANDROID_VERSION_NUMBER,
                        "",
                        null
                    );
                }
            }

            if (!prefs.contains(NOTIFICATIONS_REQUEST_COUNT_KEY)) {
                editor.putInt(NOTIFICATIONS_REQUEST_COUNT_KEY, 0);
            }

            if (!prefs.contains(IS_FIRST_RUN_KEY)) editor.putBoolean(IS_FIRST_RUN_KEY, true);
            if (!prefs.contains(IS_BLOCKING_KEY)) editor.putBoolean(IS_BLOCKING_KEY, true);
            editor.apply();
        } else {
            updateNotificationPrefs(context);
        }
    }

    static void updateNotificationPrefs(Context context) {
        if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(context);

        synchronized (AdblockFastApplication.class) {
            NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Editor editor = prefs.edit();

            if (ANDROID_VERSION_NUMBER >= Build.VERSION_CODES.N && notificationManager != null) {
                editor.putBoolean(
                    ARE_NOTIFICATIONS_STILL_ALLOWED_KEY,
                    notificationManager.areNotificationsEnabled()
                );
            } else {
                editor.putBoolean(ARE_NOTIFICATIONS_STILL_ALLOWED_KEY, true);
            }

            editor.apply();
            dumpPrefs();
        }
    }

    static void updateLegacyPrefs(Context context) {
        if (prefs.contains(LEGACY_IS_FIRST_RUN_KEY)) {
            Editor editor = prefs.edit();
            SharedPreferences legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, 0);

            editor.putString(VERSION_NUMBER_KEY, legacyVersionNumber);
            editor.putString(INITIAL_VERSION_NUMBER_KEY, legacyVersionNumber);
            editor.putString(BLOCKING_MODE_KEY, STANDARD_MODE_VALUE);
            editor.putBoolean(IS_FIRST_RUN_KEY, prefs.getBoolean(LEGACY_IS_FIRST_RUN_KEY, true));
            editor
                .putBoolean(IS_BLOCKING_KEY, legacyPrefs.getBoolean(LEGACY_IS_BLOCKING_KEY, true));
            editor.remove(LEGACY_IS_FIRST_RUN_KEY);
            editor.apply();
            legacyPrefs.edit().clear().apply();
        }
    }

    static void dumpPrefs() {
        Map<String, ?> entries = prefs.getAll();

        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            Log.d("AdblockFastApplication", entry.getKey() + ": " + entry.getValue().toString());
        }
    }
}

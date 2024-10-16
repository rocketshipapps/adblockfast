package com.rocketshipapps.adblockfast;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.Map;

import kotlin.Unit;

import org.apache.maven.artifact.versioning.ComparableVersion;

import com.joinmassive.sdk.MassiveClient;
import com.joinmassive.sdk.MassiveNotificationOptions;
import com.joinmassive.sdk.MassiveOptions;
import com.joinmassive.sdk.MassiveServiceType;

import com.onesignal.OneSignal;

import com.wbrawner.plausible.android.Plausible;

public class AdblockFastApplication extends Application {
    public static final String VERSION_NUMBER = BuildConfig.VERSION_NAME;
    public static final int ANDROID_VERSION_NUMBER = Build.VERSION.SDK_INT;
    public static final String VERSION_NUMBER_KEY = "version_number";
    public static final String PREVIOUS_VERSION_NUMBER_KEY = "previous_version_number";
    public static final String INITIAL_VERSION_NUMBER_KEY = "initial_version_number";
    public static final String ANDROID_VERSION_NUMBER_KEY = "android_version_number";
    public static final String BLOCKING_MODE_KEY = "blocking_mode";
    public static final String NOTIFICATIONS_REQUEST_COUNT_KEY = "notifications_request_count";
    public static final String IS_FIRST_RUN_KEY = "is_first_run";
    public static final String IS_BLOCKING_KEY = "is_blocking";
    public static final String ARE_NOTIFICATIONS_ALLOWED_KEY = "are_notifications_allowed";
    public static final String ARE_NOTIFICATIONS_STILL_ALLOWED_KEY =
        "are_notifications_still_allowed";
    public static final String SHOULD_OVERRIDE_BROWSER_DETECTION_KEY =
        "should_override_browser_detection";
    public static final String SHOULD_BUBBLE_WRAP_MODE_KEY = "should_bubble_wrap_mode";
    public static final String SHOULD_PAUSE_NOTIFICATIONS_KEY = "should_pause_notifications";
    public static final String STANDARD_MODE_VALUE = "standard";
    public static final String LUDICROUS_MODE_VALUE = "ludicrous";
    public static final String BLOCKING_UPDATE_ACTION =
        "com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE";
    public static final Intent SAMSUNG_BROWSER_INTENT =
        new Intent().setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING");
    public static String packageName;
    public static SharedPreferences prefs;
    public static Intent blockingUpdateIntent;

    static final String LEGACY_VERSION_NUMBER = "<=2.1.0";
    static final String LEGACY_PREFS_NAME = "adblockfast";
    static final String LEGACY_IS_FIRST_RUN_KEY = "first_run";
    static final String LEGACY_IS_BLOCKING_KEY = "rule_status";
    static final ComparableVersion COMPARABLE_VERSION = new ComparableVersion(VERSION_NUMBER);

    @Override
    public void onCreate() {
        super.onCreate();

        packageName = getApplicationContext().getPackageName();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        blockingUpdateIntent =
            new Intent()
                .setAction(BLOCKING_UPDATE_ACTION)
                .setData(Uri.parse("package:" + packageName));

        handlePrefs(this);
        MassiveClient.Companion.init(BuildConfig.MASSIVE_API_TOKEN, this, (state) -> Unit.INSTANCE);
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID);
    }

    public static void handlePrefs(Context context) {
        synchronized (AdblockFastApplication.class) {
            dumpPrefs();
            updateLegacyPrefs(context);
            dumpPrefs();
            initPrefs();
            dumpPrefs();
        }
    }

    public static void handleNotificationPrefs(Context context) {
        if (AdblockFastApplication.prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        synchronized (AdblockFastApplication.class) {
            NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            SharedPreferences.Editor editor = prefs.edit();

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

    static void initPrefs() {
        String versionNumber = prefs.getString(VERSION_NUMBER_KEY, "0.0.0");

        if (new ComparableVersion(versionNumber).compareTo(COMPARABLE_VERSION) < 0) {
            SharedPreferences.Editor editor = prefs.edit();

            if (prefs.contains(VERSION_NUMBER_KEY)) {
                editor.putString(PREVIOUS_VERSION_NUMBER_KEY, versionNumber);
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

            if (!prefs.contains(NOTIFICATIONS_REQUEST_COUNT_KEY)) {
                editor.putInt(NOTIFICATIONS_REQUEST_COUNT_KEY, 0);
            }

            if (!prefs.contains(IS_FIRST_RUN_KEY)) editor.putBoolean(IS_FIRST_RUN_KEY, true);
            if (!prefs.contains(IS_BLOCKING_KEY)) editor.putBoolean(IS_BLOCKING_KEY, true);
            editor.apply();
        }
    }

    static void updateLegacyPrefs(Context context) {
        if (prefs.contains(LEGACY_IS_FIRST_RUN_KEY)) {
            SharedPreferences.Editor editor = prefs.edit();
            SharedPreferences legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, 0);

            editor.putString(VERSION_NUMBER_KEY, LEGACY_VERSION_NUMBER);
            editor.putString(INITIAL_VERSION_NUMBER_KEY, LEGACY_VERSION_NUMBER);
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
            Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
        }
    }
}

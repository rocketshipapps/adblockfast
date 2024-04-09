package com.rocketshipapps.adblockfast;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.Map;

import kotlin.Unit;

import org.apache.maven.artifact.versioning.ComparableVersion;

import com.massive.sdk.InitCallback;
import com.massive.sdk.MassiveClient;
import com.massive.sdk.MassiveNotificationOptions;
import com.massive.sdk.MassiveOptions;
import com.massive.sdk.MassiveServiceType;
import com.massive.sdk.State;

import com.onesignal.OneSignal;

import com.wbrawner.plausible.android.Plausible;

import com.rocketshipapps.adblockfast.utils.Ruleset;

public class AdblockFastApplication extends Application {
    public static final String VERSION_NUMBER = BuildConfig.VERSION_NAME;
    public static final String VERSION_NUMBER_KEY = "version_number";
    public static final String PREVIOUS_VERSION_NUMBER_KEY = "previous_version_number";
    public static final String INITIAL_VERSION_NUMBER_KEY = "initial_version_number";
    public static final String BLOCKING_MODE_KEY = "blocking_mode";
    public static final String NOTIFICATIONS_REQUEST_COUNT_KEY = "notifications_request_count";
    public static final String IS_FIRST_RUN_KEY = "is_first_run";
    public static final String IS_BLOCKING_KEY = "is_blocking";
    public static final String ARE_NOTIFICATIONS_ALLOWED_KEY = "are_notifications_allowed";
    public static final String SHOULD_OVERRIDE_BROWSER_DETECTION_KEY =
        "should_override_browser_detection";
    public static final String STANDARD_MODE_VALUE = "standard";
    public static final String LUDICROUS_MODE_VALUE = "ludicrous";
    public static final Intent SAMSUNG_BROWSER_INTENT =
        new Intent().setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING");
    public static String packageName;
    public static SharedPreferences prefs;
    public static Intent blockingUpdateIntent;
    public static MassiveClient massiveClient;

    static final String LEGACY_VERSION_NUMBER = "<=2.1.0";
    static final String LEGACY_PREFS_NAME = "adblockfast";
    static final String LEGACY_IS_FIRST_RUN_KEY = "first_run";
    static final String LEGACY_IS_BLOCKING_KEY = "rule_status";

    @Override
    public void onCreate() {
        super.onCreate();

        packageName = getApplicationContext().getPackageName();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        blockingUpdateIntent =
            new Intent()
                .setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE")
                .setData(Uri.parse("package:" + packageName));

        dumpPrefs();
        updateLegacyPrefs(this);
        dumpPrefs();
        initPrefs();
        dumpPrefs();

        if (Ruleset.isUpgraded()) initMassive(this);
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID);
    }

    public static void updateLegacyPrefs(Context context) {
        if (prefs.contains(LEGACY_IS_FIRST_RUN_KEY)) {
            SharedPreferences.Editor editor = prefs.edit();
            SharedPreferences legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, 0);

            editor.putString(VERSION_NUMBER_KEY, LEGACY_VERSION_NUMBER).apply();
            editor.putString(INITIAL_VERSION_NUMBER_KEY, LEGACY_VERSION_NUMBER).apply();
            editor.putString(BLOCKING_MODE_KEY, STANDARD_MODE_VALUE).apply();
            editor
                .putBoolean(IS_FIRST_RUN_KEY, prefs.getBoolean(LEGACY_IS_FIRST_RUN_KEY, true))
                .apply();
            editor
                .putBoolean(IS_BLOCKING_KEY, legacyPrefs.getBoolean(LEGACY_IS_BLOCKING_KEY, true))
                .apply();
            editor.remove(LEGACY_IS_FIRST_RUN_KEY).apply();
            legacyPrefs.edit().clear().apply();
        }
    }

    public static void initPrefs() {
        String versionNumber = prefs.getString(VERSION_NUMBER_KEY, "0.0.0");

        if (
            new ComparableVersion(versionNumber)
                .compareTo(new ComparableVersion(VERSION_NUMBER)) < 0
        ) {
            SharedPreferences.Editor editor = prefs.edit();

            if (prefs.contains(VERSION_NUMBER_KEY)) {
                editor.putString(PREVIOUS_VERSION_NUMBER_KEY, versionNumber).apply();
                Plausible
                    .INSTANCE
                    .event("Update", "/v" + versionNumber + "-to-v" + VERSION_NUMBER, "", null);
            } else {
                Plausible.INSTANCE.event("Install", "/v" + VERSION_NUMBER, "", null);
            }

            editor.putString(VERSION_NUMBER_KEY, VERSION_NUMBER).apply();

            if (!prefs.contains(INITIAL_VERSION_NUMBER_KEY)) {
                editor.putString(INITIAL_VERSION_NUMBER_KEY, VERSION_NUMBER).apply();
            }

            if (!prefs.contains(NOTIFICATIONS_REQUEST_COUNT_KEY)) {
                editor.putInt(NOTIFICATIONS_REQUEST_COUNT_KEY, 0).apply();
            }

            if (!prefs.contains(IS_FIRST_RUN_KEY)) {
                editor.putBoolean(IS_FIRST_RUN_KEY, true).apply();
            }

            if (!prefs.contains(IS_BLOCKING_KEY)) editor.putBoolean(IS_BLOCKING_KEY, true).apply();
        }
    }

    public static void dumpPrefs() {
        Map<String, ?> entries = prefs.getAll();

        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    public static void initMassive(Context context) {
        if (massiveClient == null) {
            MassiveClient.Companion.getInstance(context, (client) -> {
                massiveClient = client;

                if (client.getState() == State.NotInitialized) {
                    client.initAsync(
                        BuildConfig.MASSIVE_API_TOKEN,
                        new MassiveOptions(
                            MassiveServiceType.Foreground,
                            new MassiveNotificationOptions(
                                context.getString(R.string.name),
                                context.getString(R.string.foreground_text),
                                R.drawable.icon
                            )
                        ),
                        new InitCallback() {
                            @Override
                            public void onSuccess() {
                                if (Ruleset.isUpgraded()) client.start();
                                Plausible.INSTANCE.event("Succeed", "/massive", "", null);
                            }

                            @Override
                            public void onFailure(@NonNull String message) {
                                Plausible.INSTANCE.event("Fail", "/massive", "", null);
                            }
                        }
                    );
                }

                return Unit.INSTANCE;
            });
        } else if (massiveClient.getState() != State.NotInitialized) {
            massiveClient.start();
        }
    }

    public static void finalizeMassive() {
        if (massiveClient != null) massiveClient.dispose();
    }
}

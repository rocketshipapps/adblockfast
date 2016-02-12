package com.rocketshipapps.adblockfast.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.rocketshipapps.adblockfast.BuildConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    SharedPreferences sharedPreferences;
    String samsungBrowserVersion = "0";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(BuildConfig.GCM_DEFAULT_SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            sendRegistrationTokenToServer(token);
        } catch (IOException ignore) {}
    }

    private void sendRegistrationTokenToServer(String token) {
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo("com.sec.android.app.sbrowser", 0);
            samsungBrowserVersion = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException ignore) {}

        if (sharedPreferences.getString("RELEASE", "").equals(Build.VERSION.RELEASE) &&
                sharedPreferences.getString("samsungBrowserVersion", "").equals(samsungBrowserVersion)) return;

        List<Pair<String,String>> params = new ArrayList<>();
        params.add(new Pair<>("token", token));
        params.add(new Pair<>("os", "android"));
        params.add(new Pair<>("os_version", Build.VERSION.RELEASE));
        params.add(new Pair<>("device_manufacturer", Build.MANUFACTURER));
        params.add(new Pair<>("device_model", Build.MODEL));
        params.add(new Pair<>("samsung_browser_version", samsungBrowserVersion));

        Pair<String, String> header = new Pair<>("x-application-secret", BuildConfig.APP_SECRET);

        Fuel.post(BuildConfig.HOST + "/installations", params)
            .header(header)
            .responseString(new Handler<String>() {
                @Override
                public void success(@NonNull Request request, @NonNull Response response, String s) {
                    Log.d(TAG, "reponseMessage: " + response.getHttpResponseMessage());
                    Log.d(TAG, "reponseUrl: " + response.getUrl());
                    Log.d(TAG, "response: " + s);

                    sharedPreferences.edit()
                        .putString("RELEASE", Build.VERSION.RELEASE)
                        .putString("samsungBrowserVersion", samsungBrowserVersion)
                        .apply();
                }

                @Override
                public void failure(@NonNull Request request, @NonNull Response response, @NonNull FuelError fuelError) {
                    Log.d(TAG, "fuelError: " + fuelError.getMessage());
                    Log.d(TAG, "reponseMessage: " + response.getHttpResponseMessage());
                    Log.d(TAG, "reponseUrl: " + response.getUrl());
                }
            });
    }
}

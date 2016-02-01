package com.rocketshipapps.adblockfast.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.rocketshipapps.adblockfast.R;
import com.rocketshipapps.adblockfast.utils.Preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            sendRegistrationTokenToServer(token);

            sharedPreferences.edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (IOException e) {
            e.printStackTrace();

            sharedPreferences.edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendRegistrationTokenToServer(String token) {
        //if (sharedPreferences.getBoolean(Preferences.SENT_TOKEN_TO_SERVER, false)) return;

        List<Pair<String,String>> params = new ArrayList<>();
        params.add(new Pair<>("token", token));
        params.add(new Pair<>("os", "android"));

        Pair<String, String> header = new Pair<>("x-application-secret", Preferences.SECRET);

        Fuel.post(Preferences.HOST + "/installations", params)
            .header(header)
            .responseString(new Handler<String>() {
                @Override
                public void success(Request request, Response response, String s) {
                    Log.d(TAG, "reponseMessage: " + response.getHttpResponseMessage());
                    Log.d(TAG, "reponseUrl: " + response.getUrl());
                    Log.d(TAG, "response: " + s);
                }

                @Override
                public void failure(Request request, Response response, FuelError fuelError) {
                    Log.d(TAG, "fuelError: " + fuelError.getMessage());
                    Log.d(TAG, "reponseMessage: " + response.getHttpResponseMessage());
                    Log.d(TAG, "reponseUrl: " + response.getUrl());
                }
            });
    }
}

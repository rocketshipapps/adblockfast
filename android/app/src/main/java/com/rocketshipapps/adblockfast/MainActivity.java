package com.rocketshipapps.adblockfast;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import org.apache.maven.artifact.versioning.ComparableVersion;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import com.wbrawner.plausible.android.Plausible;

import com.rocketshipapps.adblockfast.utils.Ruleset;

public class MainActivity extends AppCompatActivity {
    public static final String VERSION_NUMBER = BuildConfig.VERSION_NAME;
    public static final String VERSION_NUMBER_KEY = "version_number";
    public static final String PREVIOUS_VERSION_NUMBER_KEY = "previous_version_number";
    public static final String INITIAL_VERSION_NUMBER_KEY = "initial_version_number";
    public static final String IS_FIRST_RUN_KEY = "is_first_run";
    public static final String IS_BLOCKING_KEY = "is_blocking";
    static final Intent SAMSUNG_BROWSER_INTENT =
        new Intent().setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING");

    static final String LEGACY_VERSION_NUMBER = "<=2.1.0";
    static final String LEGACY_PREFS_NAME = "adblockfast";
    static final String LEGACY_IS_FIRST_RUN_KEY = "first_run";
    static final String LEGACY_IS_BLOCKING_KEY = "rule_status";
    // TODO: Refactor subscription constants
    static final String RETRIEVED_ACCOUNT_PREF = "retrieved_account";
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1;
    static final int REQUEST_CODE_ACCOUNT_INTENT = 2;

    public static SharedPreferences prefs;
    String packageName;
    Intent blockingUpdateIntent;
    ImageButton logoButton;
    TextView statusText;
    TextView hintText;
    boolean isLogoAnimating = false;
    boolean hasSamsungBrowser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(
            ViewPump.builder().addInterceptor(
                new CalligraphyInterceptor(
                    new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/AvenirNextLTPro-Light.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
                )
            ).build()
        );
        setContentView(R.layout.main_view);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        packageName = getApplicationContext().getPackageName();
        blockingUpdateIntent =
            new Intent()
                .setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE")
                .setData(Uri.parse("package:" + packageName));
        logoButton = findViewById(R.id.logo_button);
        statusText = findViewById(R.id.status_text);
        hintText = findViewById(R.id.hint_text);

        updateLegacyPrefs();
        initPrefs();
        logoButton.setOnClickListener(this::onLogoPressed);
        findViewById(R.id.help_button).setOnClickListener(this::onHelpPressed);
        findViewById(R.id.about_button).setOnClickListener(this::onAboutPressed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        detectSamsungBrowser();

        if (Ruleset.isEnabled()) {
            animateBlocking();
        } else {
            animateUnblocking();
        }

        if (!hasSamsungBrowser) {
            presentHelp(false);
        } else if (prefs.getBoolean(IS_FIRST_RUN_KEY, true)) {
            presentHelp(true);
            prefs.edit().putBoolean(IS_FIRST_RUN_KEY, false).apply();
        } else {
            presentOffer();
        }

        Plausible.INSTANCE.pageView("/", "", null);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
    }

    void updateLegacyPrefs() {
        if (prefs.contains(LEGACY_IS_FIRST_RUN_KEY)) {
            SharedPreferences.Editor editor = prefs.edit();
            SharedPreferences legacyPrefs = this.getSharedPreferences(LEGACY_PREFS_NAME, 0);

            editor.putString(VERSION_NUMBER_KEY, LEGACY_VERSION_NUMBER).apply();
            editor.putString(INITIAL_VERSION_NUMBER_KEY, LEGACY_VERSION_NUMBER).apply();
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

    void initPrefs() {
        String versionNumber = prefs.getString(VERSION_NUMBER_KEY, "0.0.0");

        if (
            new ComparableVersion(versionNumber)
                .compareTo(new ComparableVersion(VERSION_NUMBER)) < 0
        ) {
            SharedPreferences.Editor editor = prefs.edit();

            if (prefs.contains(VERSION_NUMBER_KEY)) {
                editor.putString(PREVIOUS_VERSION_NUMBER_KEY, versionNumber).apply();
            }

            if (!prefs.contains(INITIAL_VERSION_NUMBER_KEY)) {
                editor.putString(INITIAL_VERSION_NUMBER_KEY, VERSION_NUMBER).apply();
            }

            if (!prefs.contains(IS_FIRST_RUN_KEY)) {
                editor.putBoolean(IS_FIRST_RUN_KEY, true).apply();
            }

            if (!prefs.contains(IS_BLOCKING_KEY)) editor.putBoolean(IS_BLOCKING_KEY, true).apply();

            editor.putString(VERSION_NUMBER_KEY, VERSION_NUMBER).apply();
        }
    }

    void detectSamsungBrowser() {
        List<ResolveInfo> list =
            getPackageManager().queryIntentActivities(SAMSUNG_BROWSER_INTENT, 0);

        if (list.size() > 0) hasSamsungBrowser = true;
    }

    public void onLogoPressed(View v) {
        if (!isLogoAnimating) {
            if (Ruleset.isEnabled()) {
                Ruleset.disable();
                animateUnblocking();
            } else {
                Ruleset.enable();
                animateBlocking();
            }

            sendBroadcast(blockingUpdateIntent);
        }
    }

    void onHelpPressed(View v) { presentHelp(true); }

    void onAboutPressed(View v) {
        Dialog dialog = presentDialog(R.layout.about_dialog);

        ((TextView) dialog.findViewById(R.id.version_text))
            .setText(String.format(" %s", VERSION_NUMBER));
        setHtml(dialog.findViewById(R.id.tagline), R.string.tagline, false);
        setHtml(dialog.findViewById(R.id.copyright_text), R.string.copyright_notice, true);

        dialog.findViewById(R.id.dismiss_button).setOnClickListener((w) -> dialog.dismiss());
    }

    void presentOffer() {
        Dialog dialog = presentDialog(R.layout.offer_dialog);

        ((TextView) dialog.findViewById(R.id.summary_text)).setText(R.string.offer_summary);
        setHtml(dialog.findViewById(R.id.details_text), R.string.offer_details, true);
        setHtml(dialog.findViewById(R.id.contact_text), R.string.contact_info, true);

        dialog.findViewById(R.id.accept_button).setOnClickListener((v) -> dialog.dismiss());
        dialog.findViewById(R.id.decline_button).setOnClickListener((v) -> dialog.dismiss());
    }

    void presentHelp(boolean isDismissible) {
        Dialog dialog = presentDialog(R.layout.help_dialog);
        TextView summaryText = dialog.findViewById(R.id.summary_text);
        TextView detailsText = dialog.findViewById(R.id.details_text);
        Button dismissButton = dialog.findViewById(R.id.dismiss_button);

        if (hasSamsungBrowser) {
            summaryText.setText(R.string.settings_summary);
            setHtml(detailsText, R.string.settings_details, false);
            detailsText.setOnClickListener((v) -> startActivity(SAMSUNG_BROWSER_INTENT));
        } else {
            summaryText.setText(R.string.install_summary);
            setHtml(detailsText, R.string.install_details, true);
        }

        setHtml(dialog.findViewById(R.id.contact_text), R.string.contact_info, true);

        if (isDismissible) {
            dismissButton.setOnClickListener((v) -> dialog.dismiss());
        } else {
            dismissButton.setOnClickListener((v) -> onBackPressed());
        }
    }

    Dialog presentDialog(int id) {
        Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false);
        dialog.setContentView(id);
        dialog.show();

        return dialog;
    }

    void setHtml(TextView view, int id, boolean shouldLink) {
        view.setText(Html.fromHtml(getString(id)));

        if (shouldLink) view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    void animateBlocking() {
        animateLogo(new int[] {
            R.drawable.blocked_frame_0,
            R.drawable.blocked_frame_1,
            R.drawable.blocked_frame_2,
            R.drawable.blocked_frame_3,
            R.drawable.blocked_frame_4,
            R.drawable.blocked_frame_5,
            R.drawable.blocked_frame_6,
            R.drawable.blocked_frame_7,
            R.drawable.blocked_frame_8,
            R.drawable.blocked_frame_9,
            R.drawable.blocked_frame_10,
            R.drawable.blocked_frame_11,
            R.drawable.blocked_frame_12,
            R.drawable.blocked_frame_13,
            R.drawable.blocked_frame_14,
            R.drawable.blocked_frame_15,
            R.drawable.unblocked_frame_0,
            R.drawable.unblocked_frame_1,
            R.drawable.unblocked_frame_2,
            R.drawable.unblocked_frame_3,
            R.drawable.unblocked_frame_4,
            R.drawable.unblocked_frame_5,
            R.drawable.unblocked_frame_6,
            R.drawable.unblocked_frame_7,
            R.drawable.unblocked_frame_8,
            R.drawable.unblocked_frame_9,
            R.drawable.unblocked_frame_10,
            R.drawable.unblocked_frame_11,
            R.drawable.unblocked_frame_12,
            R.drawable.unblocked_frame_13,
            R.drawable.unblocked_frame_14,
            R.drawable.unblocked_frame_15,
            R.drawable.blocked_frame_0,
            R.drawable.blocked_frame_1,
            R.drawable.blocked_frame_2,
            R.drawable.blocked_frame_3,
            R.drawable.blocked_frame_4,
            R.drawable.blocked_frame_5,
            R.drawable.blocked_frame_6,
            R.drawable.blocked_frame_7,
            R.drawable.blocked_frame_8,
            R.drawable.blocked_frame_9,
            R.drawable.blocked_frame_10,
            R.drawable.blocked_frame_11,
            R.drawable.blocked_frame_12,
            R.drawable.blocked_frame_13,
            R.drawable.blocked_frame_14,
            R.drawable.blocked_frame_15
        }, R.string.blocked_message, R.string.blocked_hint);
    }

    void animateUnblocking() {
        animateLogo(new int[] {
            R.drawable.unblocked_frame_0,
            R.drawable.unblocked_frame_1,
            R.drawable.unblocked_frame_2,
            R.drawable.unblocked_frame_3,
            R.drawable.unblocked_frame_4,
            R.drawable.unblocked_frame_5,
            R.drawable.unblocked_frame_6,
            R.drawable.unblocked_frame_7,
            R.drawable.unblocked_frame_8,
            R.drawable.unblocked_frame_9,
            R.drawable.unblocked_frame_10,
            R.drawable.unblocked_frame_11,
            R.drawable.unblocked_frame_12,
            R.drawable.unblocked_frame_13,
            R.drawable.unblocked_frame_14,
            R.drawable.unblocked_frame_15,
            R.drawable.blocked_frame_0,
            R.drawable.blocked_frame_1,
            R.drawable.blocked_frame_2,
            R.drawable.blocked_frame_3,
            R.drawable.blocked_frame_4,
            R.drawable.blocked_frame_5,
            R.drawable.blocked_frame_6,
            R.drawable.blocked_frame_7,
            R.drawable.blocked_frame_8,
            R.drawable.blocked_frame_9,
            R.drawable.blocked_frame_10,
            R.drawable.blocked_frame_11,
            R.drawable.blocked_frame_12,
            R.drawable.blocked_frame_13,
            R.drawable.blocked_frame_14,
            R.drawable.blocked_frame_15,
            R.drawable.unblocked_frame_0,
            R.drawable.unblocked_frame_1,
            R.drawable.unblocked_frame_2,
            R.drawable.unblocked_frame_3,
            R.drawable.unblocked_frame_4,
            R.drawable.unblocked_frame_5,
            R.drawable.unblocked_frame_6,
            R.drawable.unblocked_frame_7,
            R.drawable.unblocked_frame_8,
            R.drawable.unblocked_frame_9,
            R.drawable.unblocked_frame_10,
            R.drawable.unblocked_frame_11,
            R.drawable.unblocked_frame_12,
            R.drawable.unblocked_frame_13,
            R.drawable.unblocked_frame_14,
            R.drawable.unblocked_frame_15
        }, R.string.unblocked_message, R.string.unblocked_hint);
    }

    void animateLogo(int[] resources, int status, int hint) {
        double delay = 62.5;
        isLogoAnimating = true;

        for (int i = 0; i < resources.length; i++) {
            if (i == 0) {
                logoButton.setImageResource(resources[i]);
            } else {
                final int I = i;

                new Handler().postDelayed(() -> runOnUiThread(() -> {
                    logoButton.setImageResource(resources[I]);

                    if (I == resources.length - 1) {
                        isLogoAnimating = false;

                        statusText.setText(status);
                        hintText.setText(hint);
                    }
                }), Math.round(i * delay));
            }
        }
    }

    void initPlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();

        if (availability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            availability.makeGooglePlayServicesAvailable(this);
        }
    }

    // TODO: Refactor rest of subscription methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACCOUNT_INTENT) {
            if (data != null) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                if (email != null) {
                    new Thread(() -> {
                        try {
                            URL url = new URL(BuildConfig.SUBSCRIBE_URL);
                            HttpURLConnection req = (HttpURLConnection) url.openConnection();

                            req.setRequestMethod("POST");
                            req.setRequestProperty(
                                "Content-Type", "application/json;charset=UTF-8"
                            );
                            req.setRequestProperty("Accept", "application/json");
                            req.setDoOutput(true);
                            req.setDoInput(true);

                            JSONObject params = new JSONObject();
                            params.put("email", email);

                            DataOutputStream os = new DataOutputStream(req.getOutputStream());
                            os.writeBytes(params.toString());

                            os.flush();
                            os.close();

                            req.disconnect();

                            prefs.edit().putBoolean(RETRIEVED_ACCOUNT_PREF, true).apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

            detectSamsungBrowser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_GET_ACCOUNTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                getAccounts();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }

            getAccounts();
        }
    }

    void checkAccountPermission() {
        if (prefs.getBoolean(RETRIEVED_ACCOUNT_PREF, false)) return;

        if (
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.GET_ACCOUNTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            getAccounts();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.GET_ACCOUNTS
                )
            ) {
                showAccountPermissionAlert();
            } else {
                requestPermissions(
                    new String[] { Manifest.permission.GET_ACCOUNTS },
                    REQUEST_PERMISSION_GET_ACCOUNTS
                );
            }
        }
    }

    void getAccounts() {
        Intent intent =
            AccountPicker.newChooseAccountIntent(
                new AccountPicker
                    .AccountChooserOptions
                    .Builder()
                    .setAllowableAccountsTypes(Collections.singletonList("com.google"))
                    .build()
            );
        startActivityForResult(intent, REQUEST_CODE_ACCOUNT_INTENT);
    }

    void showAccountPermissionAlert() {
        new AlertDialog
            .Builder(this)
            .setTitle("Permission needed")
            .setMessage("Get email address")
            .setPositiveButton(android.R.string.ok, (d, w) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        new String[] { Manifest.permission.GET_ACCOUNTS },
                        REQUEST_PERMISSION_GET_ACCOUNTS
                    );
                }
            })
            .create()
            .show();
    }
}

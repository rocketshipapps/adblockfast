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
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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
import java.util.Map;

import org.json.JSONObject;

import org.apache.maven.artifact.versioning.ComparableVersion;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.calligraphy3.TypefaceUtils;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import com.wbrawner.plausible.android.Plausible;

import com.onesignal.Continue;
import com.onesignal.OneSignal;

import com.rocketshipapps.adblockfast.utils.Ruleset;

public class MainActivity extends AppCompatActivity {
    public static final String VERSION_NUMBER = BuildConfig.VERSION_NAME;
    public static final String VERSION_NUMBER_KEY = "version_number";
    public static final String PREVIOUS_VERSION_NUMBER_KEY = "previous_version_number";
    public static final String INITIAL_VERSION_NUMBER_KEY = "initial_version_number";
    public static final String BLOCKING_MODE_KEY = "blocking_mode";
    public static final String IS_FIRST_RUN_KEY = "is_first_run";
    public static final String IS_BLOCKING_KEY = "is_blocking";
    public static final String STANDARD_MODE_VALUE = "standard";
    public static final String LUDICROUS_MODE_VALUE = "ludicrous";
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
    public static Intent blockingUpdateIntent;
    String packageName;
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

        dumpPrefs();
        updateLegacyPrefs();
        dumpPrefs();
        initPrefs();
        dumpPrefs();
        logoButton.setOnClickListener(this::onLogoPressed);
        findViewById(R.id.help_button).setOnClickListener(this::onHelpPressed);
        findViewById(R.id.about_button).setOnClickListener(this::onAboutPressed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        detectSamsungBrowser();

        if (Ruleset.isEnabled()) {
            animateBlocking(this::onboardUser);
        } else {
            animateUnblocking(this::onboardUser);
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

            if (!prefs.contains(BLOCKING_MODE_KEY)) {
                editor.putString(BLOCKING_MODE_KEY, STANDARD_MODE_VALUE).apply();
            }

            if (!prefs.contains(IS_FIRST_RUN_KEY)) {
                editor.putBoolean(IS_FIRST_RUN_KEY, true).apply();
            }

            if (!prefs.contains(IS_BLOCKING_KEY)) editor.putBoolean(IS_BLOCKING_KEY, true).apply();
            editor.putString(VERSION_NUMBER_KEY, VERSION_NUMBER).apply();

            if (!prefs.contains(PREVIOUS_VERSION_NUMBER_KEY)) {
                Plausible.INSTANCE.event("Install", "/v" + VERSION_NUMBER, "", null);
            } else {
                Plausible.INSTANCE.event(
                    "Update", "/v" + versionNumber + "-to-v" + VERSION_NUMBER, "", null
                );
            }
        }
    }

    void dumpPrefs() {
        Map<String, ?> entries = prefs.getAll();

        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    void detectSamsungBrowser() {
        List<ResolveInfo> list =
            getPackageManager().queryIntentActivities(SAMSUNG_BROWSER_INTENT, 0);

        if (list.size() > 0) {
            hasSamsungBrowser = true;

            Plausible.INSTANCE.event("Hit", "/samsung-browser", "", null);
        } else {
            Plausible.INSTANCE.event("Miss", "/samsung-browser", "", null);
        }
    }

    public void onLogoPressed(View v) {
        if (!isLogoAnimating) {
            if (Ruleset.isEnabled()) {
                Ruleset.disable(this);
                animateUnblocking(null);
                Plausible.INSTANCE.event("Unblock", "/", "", null);
            } else {
                Ruleset.enable(this);
                animateBlocking(null);
                Plausible.INSTANCE.event("Block", "/", "", null);
            }
        }
    }

    void onHelpPressed(View v) {
        presentHelp(null);
    }

    void onAboutPressed(View v) {
        Dialog dialog = presentDialog(R.layout.about_dialog);
        TextView defaultText = dialog.findViewById(R.id.default_text);
        TextView upgradeText = dialog.findViewById(R.id.upgrade_text);
        Typeface bodyFont = TypefaceUtils.load(getAssets(), "fonts/AvenirNextLTPro-Light.otf");
        Typeface emphasisFont = TypefaceUtils.load(getAssets(), "fonts/AvenirNext-Medium.otf");

        ((TextView) dialog.findViewById(R.id.version_text))
            .setText(String.format(" %s", VERSION_NUMBER));
        setHtml(dialog.findViewById(R.id.tag_text), R.string.tagline, false);
        setHtml(defaultText, R.string.default_link, false);
        setHtml(upgradeText, R.string.upgrade_link, false);
        setHtml(dialog.findViewById(R.id.copyright_text), R.string.copyright_notice, true);

        if (Ruleset.isUpgraded()) {
            upgradeText.setTypeface(emphasisFont);
            defaultText.setTypeface(bodyFont);
        }

        defaultText.setOnClickListener((w) -> {
            Ruleset.downgrade(this);
            defaultText.setTypeface(emphasisFont);
            upgradeText.setTypeface(bodyFont);
            Plausible.INSTANCE.event("Default", "/about", "", null);
        });

        upgradeText.setOnClickListener((w) -> {
            Ruleset.upgrade(this);
            upgradeText.setTypeface(emphasisFont);
            defaultText.setTypeface(bodyFont);
            Plausible.INSTANCE.event("Upgrade", "/about", "", null);
        });

        dialog.findViewById(R.id.dismiss_button).setOnClickListener((w) -> {
            dialog.dismiss();
            Plausible.INSTANCE.event("Dismiss", "/about", "", null);
        });

        Plausible.INSTANCE.pageView("/about", "", null);
    }

    void presentModeChoices(Runnable continuationHandler) {
        Dialog dialog = presentDialog(R.layout.mode_dialog);
        Button defaultButton = dialog.findViewById(R.id.default_button);
        Button upgradeButton = dialog.findViewById(R.id.upgrade_button);

        ((TextView) dialog.findViewById(R.id.summary_text)).setText(R.string.mode_summary);
        setHtml(dialog.findViewById(R.id.details_text), R.string.mode_details, true);
        setHtml(dialog.findViewById(R.id.contact_text), R.string.contact_info, true);

        defaultButton.setOnClickListener((v) -> {
            Ruleset.downgrade(this);
            dialog.dismiss();
            if (continuationHandler != null) continuationHandler.run();
            Plausible.INSTANCE.event("Default", "/mode", "", null);
        });

        upgradeButton.setOnClickListener((v) -> {
            Ruleset.upgrade(this);
            dialog.dismiss();
            if (continuationHandler != null) continuationHandler.run();
            Plausible.INSTANCE.event("Upgrade", "/mode", "", null);
        });

        Plausible.INSTANCE.pageView("/mode", "", null);
    }

    void presentHelp(Runnable continuationHandler) {
        Dialog dialog = presentDialog(R.layout.help_dialog);
        TextView summaryText = dialog.findViewById(R.id.summary_text);
        TextView detailsText = dialog.findViewById(R.id.details_text);
        Button dismissButton = dialog.findViewById(R.id.dismiss_button);

        if (hasSamsungBrowser) {
            summaryText.setText(R.string.settings_summary);
            setHtml(detailsText, R.string.settings_details, false);

            detailsText.setOnClickListener((v) -> {
                startActivity(SAMSUNG_BROWSER_INTENT);
                Plausible.INSTANCE.event("Install", "/samsung-browser", "", null);
            });
        } else {
            summaryText.setText(R.string.install_summary);
            setHtml(detailsText, R.string.install_details, true);
        }

        setHtml(dialog.findViewById(R.id.contact_text), R.string.contact_info, true);

        if (continuationHandler != null) {
            if (hasSamsungBrowser) dismissButton.setText(R.string.continue_label);

            dismissButton.setOnClickListener((v) -> {
                dialog.dismiss();
                continuationHandler.run();
                Plausible.INSTANCE.event("Dismiss", "/help", "", null);
            });
        } else {
            dismissButton.setOnClickListener((v) -> {
                dialog.dismiss();
                Plausible.INSTANCE.event("Dismiss", "/help", "", null);
            });
        }

        Plausible.INSTANCE.pageView("/help", "", null);
    }

    void presentNotificationsChoices(Runnable continuationHandler) {
        Dialog dialog = presentDialog(R.layout.notifications_dialog);

        ((TextView) dialog.findViewById(R.id.summary_text)).setText(R.string.notifications_summary);
        setHtml(dialog.findViewById(R.id.details_text), R.string.notifications_details, false);

        dialog.findViewById(R.id.accept_button).setOnClickListener((v) -> {
            dialog.dismiss();
            Plausible.INSTANCE.event("Pre-accept", "/notifications", "", null);

            OneSignal.getNotifications().requestPermission(true, Continue.with((r) -> {
                if (r.isSuccess()) {
                    if (Boolean.TRUE.equals(r.getData())) {
                        Plausible.INSTANCE.event("Accept", "/notifications", "", null);
                    }
                    else {
                        Plausible.INSTANCE.event("Decline", "/notifications", "", null);
                    }
                }

                if (continuationHandler != null) continuationHandler.run();
            }));
        });

        dialog.findViewById(R.id.decline_button).setOnClickListener((v) -> {
            dialog.dismiss();
            if (continuationHandler != null) continuationHandler.run();
            Plausible.INSTANCE.event("Pre-decline", "/notifications", "", null);
        });

        Plausible.INSTANCE.pageView("/notifications", "", null);
    }

    Dialog presentDialog(int id) {
        Dialog dialog = new Dialog(this);
        Window window = dialog.getWindow();

        if (window != null) {
            window.getAttributes().windowAnimations = R.style.Animation;

            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(id);
        dialog.show();

        return dialog;
    }

    void setHtml(TextView view, int id, boolean shouldLink) {
        view.setText(Html.fromHtml(getString(id)));
        if (shouldLink) view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    void animateBlocking(Runnable callback) {
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
        }, R.string.blocked_message, R.string.blocked_hint, callback);
    }

    void animateUnblocking(Runnable callback) {
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
        }, R.string.unblocked_message, R.string.unblocked_hint, callback);
    }

    void animateLogo(int[] resources, int status, int hint, Runnable callback) {
        isLogoAnimating = true;
        double delay = 62.5;

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
                        if (callback != null) callback.run();
                    }
                }), Math.round(i * delay));
            }
        }
    }

    void onboardUser() {
        if (!hasSamsungBrowser) {
            presentHelp(this::onBackPressed);
        } else if (prefs.getBoolean(IS_FIRST_RUN_KEY, true)) {
            presentModeChoices(() ->
                presentHelp(() ->
                    presentNotificationsChoices(() ->
                        prefs.edit().putBoolean(IS_FIRST_RUN_KEY, false).apply()
                    )
                )
            );
            Plausible.INSTANCE.event("Onboard", "/", "", null);
        }
    }

    void initPlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();

        if (availability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            availability.makeGooglePlayServicesAvailable(this);
            Plausible.INSTANCE.event("Miss", "/play-services", "", null);
        } else {
            Plausible.INSTANCE.event("Hit", "/play-services", "", null);
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

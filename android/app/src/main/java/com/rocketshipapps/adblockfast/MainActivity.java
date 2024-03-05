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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import com.massive.sdk.State;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan;
import io.github.inflationx.calligraphy3.TypefaceUtils;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import com.wbrawner.plausible.android.Plausible;

import com.onesignal.Continue;
import com.onesignal.OneSignal;

import com.rocketshipapps.adblockfast.utils.Ruleset;

public class MainActivity extends AppCompatActivity {
    // TODO: Refactor subscription constants
    static final String RETRIEVED_ACCOUNT_PREF = "retrieved_account";
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1;
    static final int REQUEST_CODE_ACCOUNT_INTENT = 2;
    Typeface bodyFont;
    Typeface emphasisFont;
    ImageButton logoButton;
    TextView statusText;
    TextView hintText;
    Dialog dialog;
    boolean isLogoAnimating = false;
    boolean hasSamsungBrowser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(
            ViewPump
                .builder()
                .addInterceptor(
                    new CalligraphyInterceptor(
                        new CalligraphyConfig
                            .Builder()
                            .setDefaultFontPath("fonts/AvenirNextLTPro-Light.otf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        );
        setContentView(R.layout.main_view);

        bodyFont = TypefaceUtils.load(getAssets(), "fonts/AvenirNextLTPro-Light.otf");
        emphasisFont = TypefaceUtils.load(getAssets(), "fonts/AvenirNext-Medium.otf");
        logoButton = findViewById(R.id.logo_button);
        statusText = findViewById(R.id.status_text);
        hintText = findViewById(R.id.hint_text);

        logoButton.setOnClickListener(this::onLogoPressed);
        findViewById(R.id.help_button).setOnClickListener(this::onHelpPressed);
        findViewById(R.id.about_button).setOnClickListener(this::onAboutPressed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPlayServices();
        detectSamsungBrowser();

        if (Ruleset.isEnabled()) {
            animateBlocking(this::onboardUser);
        } else {
            animateUnblocking(this::onboardUser);
        }

        if (Ruleset.isUpgraded()) AdblockFastApplication.massiveClient.start();
        Plausible.INSTANCE.pageView("/", "", null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
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

    void detectSamsungBrowser() {
        List<ResolveInfo> list =
            getPackageManager()
                .queryIntentActivities(AdblockFastApplication.SAMSUNG_BROWSER_INTENT, 0);

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
        Dialog about = presentDialog(R.layout.about_dialog);
        TextView defaultText = about.findViewById(R.id.default_text);
        TextView upgradeText = about.findViewById(R.id.upgrade_text);

        ((TextView) about.findViewById(R.id.version_text))
            .setText(String.format(" %s", AdblockFastApplication.VERSION_NUMBER));
        setHtml(about.findViewById(R.id.tag_text), R.string.tagline, false);
        setHtml(defaultText, R.string.default_link, false);
        setHtml(upgradeText, R.string.upgrade_link, false);
        setHtml(about.findViewById(R.id.copyright_text), R.string.copyright_notice, true);

        if (Ruleset.isUpgraded()) {
            upgradeText.setTypeface(emphasisFont);
            defaultText.setTypeface(bodyFont);
        }

        defaultText.setOnClickListener((w) -> {
            if (AdblockFastApplication.massiveClient.getState() == State.Started) {
                AdblockFastApplication.massiveClient.stop();
            }

            Ruleset.downgrade(this);
            defaultText.setTypeface(emphasisFont);
            upgradeText.setTypeface(bodyFont);
            Plausible.INSTANCE.event("Default", "/about", "", null);
        });

        upgradeText.setOnClickListener((w) -> {
            Ruleset.upgrade(this);
            AdblockFastApplication.massiveClient.start();
            upgradeText.setTypeface(emphasisFont);
            defaultText.setTypeface(bodyFont);
            Plausible.INSTANCE.event("Upgrade", "/about", "", null);
        });

        about.findViewById(R.id.dismiss_button).setOnClickListener((w) -> {
            about.dismiss();
            Plausible.INSTANCE.event("Dismiss", "/about", "", null);
        });

        Plausible.INSTANCE.pageView("/about", "", null);
    }

    void presentModes(Runnable continuationHandler) {
        Dialog modes = presentDialog(R.layout.mode_dialog);
        Button defaultButton = modes.findViewById(R.id.default_button);
        Button upgradeButton = modes.findViewById(R.id.upgrade_button);

        ((TextView) modes.findViewById(R.id.summary_text)).setText(R.string.mode_summary);
        setHtml(modes.findViewById(R.id.details_text), R.string.mode_details, true);

        defaultButton.setOnClickListener((v) -> {
            Ruleset.downgrade(this);
            modes.dismiss();
            Plausible.INSTANCE.event("Default", "/mode", "", null);
            if (continuationHandler != null) continuationHandler.run();
        });

        upgradeButton.setOnClickListener((v) -> {
            Ruleset.upgrade(this);
            AdblockFastApplication.massiveClient.start();
            modes.dismiss();
            Plausible.INSTANCE.event("Upgrade", "/mode", "", null);
            if (continuationHandler != null) continuationHandler.run();
        });

        Plausible.INSTANCE.pageView("/mode", "", null);
    }

    void presentNotificationsOptIn(Runnable continuationHandler) {
        Dialog notificationsOptIn = presentDialog(R.layout.notifications_dialog);
        SharedPreferences.Editor editor = AdblockFastApplication.prefs.edit();

        editor
            .putInt(
                AdblockFastApplication.NOTIFICATIONS_REQUEST_COUNT_KEY,
                AdblockFastApplication
                    .prefs
                    .getInt(AdblockFastApplication.NOTIFICATIONS_REQUEST_COUNT_KEY, 0) + 1
            )
            .apply();
        setHtml(
            notificationsOptIn.findViewById(R.id.request_text),
            R.string.notifications_request, false
        );

        notificationsOptIn.findViewById(R.id.accept_button).setOnClickListener((v) -> {
            notificationsOptIn.dismiss();
            Plausible.INSTANCE.event("Pre-accept", "/notifications", "", null);

            OneSignal.getNotifications().requestPermission(true, Continue.with((r) -> {
                if (r.isSuccess()) {
                    if (Boolean.TRUE.equals(r.getData())) {
                        editor
                            .putBoolean(AdblockFastApplication.ARE_NOTIFICATIONS_ALLOWED_KEY, true)
                            .apply();
                        Plausible.INSTANCE.event("Accept", "/notifications", "", null);
                    } else {
                        editor
                            .putBoolean(AdblockFastApplication.ARE_NOTIFICATIONS_ALLOWED_KEY, false)
                            .apply();
                        Plausible.INSTANCE.event("Decline", "/notifications", "", null);
                    }
                }

                if (continuationHandler != null) continuationHandler.run();
            }));
        });

        notificationsOptIn.findViewById(R.id.decline_button).setOnClickListener((v) -> {
            notificationsOptIn.dismiss();
            Plausible.INSTANCE.event("Pre-decline", "/notifications", "", null);
            if (continuationHandler != null) continuationHandler.run();
        });

        Plausible.INSTANCE.pageView("/notifications", "", null);
    }

    void presentHelp(Runnable continuationHandler) {
        Dialog help = presentDialog(R.layout.help_dialog);
        TextView summaryText = help.findViewById(R.id.summary_text);
        TextView detailsText = help.findViewById(R.id.details_text);
        Button dismissButton = help.findViewById(R.id.dismiss_button);

        if (hasSamsungBrowser) {
            summaryText.setText(R.string.settings_summary);
            setHtml(detailsText, R.string.settings_details, false);

            detailsText.setOnClickListener((v) -> {
                startActivity(AdblockFastApplication.SAMSUNG_BROWSER_INTENT);
                Plausible.INSTANCE.event("Install", "/samsung-browser", "", null);
            });
        } else {
            summaryText.setText(R.string.install_summary);
            setHtml(detailsText, R.string.install_details, true);
        }

        setHtml(help.findViewById(R.id.contact_text), R.string.contact_info, true);

        if (continuationHandler != null) {
            if (hasSamsungBrowser) dismissButton.setText(R.string.continue_label);

            dismissButton.setOnClickListener((v) -> {
                help.dismiss();
                Plausible.INSTANCE.event("Dismiss", "/help", "", null);
                continuationHandler.run();
            });
        } else {
            dismissButton.setOnClickListener((v) -> {
                help.dismiss();
                Plausible.INSTANCE.event("Dismiss", "/help", "", null);
            });
        }

        Plausible.INSTANCE.pageView("/help", "", null);
    }

    Dialog presentDialog(int id) {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();

        dialog = new Dialog(this);
        Window window = dialog.getWindow();

        if (window != null) {
            window.getAttributes().windowAnimations = R.style.Animation;

            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(id);
        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }

    void setHtml(TextView view, int id, boolean shouldLink) {
        SpannableStringBuilder html = new SpannableStringBuilder(Html.fromHtml(getString(id)));
        StyleSpan[] spans = html.getSpans(0, html.length(), StyleSpan.class);

        for (StyleSpan span : spans) {
            if (span.getStyle() == Typeface.BOLD) {
                CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(emphasisFont);
                int start = html.getSpanStart(span);
                int end = html.getSpanEnd(span);

                html.removeSpan(span);
                html.setSpan(typefaceSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        view.setText(html, TextView.BufferType.SPANNABLE);
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

                new Handler().postDelayed(() ->
                    runOnUiThread(() -> {
                        logoButton.setImageResource(resources[I]);

                        if (I == resources.length - 1) {
                            isLogoAnimating = false;

                            statusText.setText(status);
                            hintText.setText(hint);
                            if (callback != null) callback.run();
                        }
                    }
                ), Math.round(i * delay));
            }
        }
    }

    void onboardUser() {
        if (
            AdblockFastApplication.prefs.getBoolean(AdblockFastApplication.IS_FIRST_RUN_KEY, true)
        ) {
            Plausible.INSTANCE.event("Onboard", "/", "", null);

            if (!AdblockFastApplication.prefs.contains(AdblockFastApplication.BLOCKING_MODE_KEY)) {
                presentModes(() ->
                    presentNotificationsOptIn(() -> {
                        if (hasSamsungBrowser) {
                            presentHelp(() ->
                                AdblockFastApplication
                                    .prefs
                                    .edit()
                                    .putBoolean(AdblockFastApplication.IS_FIRST_RUN_KEY, false)
                                    .apply()
                            );
                        } else {
                            presentHelp(this::onBackPressed);
                        }
                    })
                );
            } else if (
                AdblockFastApplication
                    .prefs
                    .getInt(AdblockFastApplication.NOTIFICATIONS_REQUEST_COUNT_KEY, 0) == 0
            ) {
                presentNotificationsOptIn(() -> {
                    if (hasSamsungBrowser) {
                        presentHelp(() ->
                            AdblockFastApplication
                                .prefs
                                .edit()
                                .putBoolean(AdblockFastApplication.IS_FIRST_RUN_KEY, false)
                                .apply()
                        );
                    } else {
                        presentHelp(this::onBackPressed);
                    }
                });
            } else {
                if (hasSamsungBrowser) {
                    presentHelp(() ->
                        AdblockFastApplication
                            .prefs
                            .edit()
                            .putBoolean(AdblockFastApplication.IS_FIRST_RUN_KEY, false)
                            .apply()
                    );
                } else {
                    presentHelp(this::onBackPressed);
                }
            }
        } else if (!hasSamsungBrowser) {
            presentHelp(this::onBackPressed);
        }
    }

    // TODO: Refactor subscription methods
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

                            AdblockFastApplication
                                .prefs
                                .edit()
                                .putBoolean(RETRIEVED_ACCOUNT_PREF, true)
                                .apply();
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
        if (AdblockFastApplication.prefs.getBoolean(RETRIEVED_ACCOUNT_PREF, false)) return;

        if (
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.GET_ACCOUNTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            getAccounts();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                ActivityCompat
                    .shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)
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
        ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), (result) -> {
                    /* TODO: If “result.getResultCode() == Activity.RESULT_OK)”, “result.getData()”
                             contains retrieved account info */
                }
            );
        Intent intent =
            AccountPicker.newChooseAccountIntent(
                new AccountPicker
                    .AccountChooserOptions
                    .Builder()
                    .setAllowableAccountsTypes(Collections.singletonList("com.google"))
                    .build()
            );

        launcher.launch(intent);
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

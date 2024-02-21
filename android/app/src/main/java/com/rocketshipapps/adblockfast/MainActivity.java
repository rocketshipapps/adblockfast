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
import androidx.annotation.Nullable;
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

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import com.wbrawner.plausible.android.Plausible;

import com.rocketshipapps.adblockfast.utils.Ruleset;

public class MainActivity extends AppCompatActivity {
    static final String VERSION_NUMBER = BuildConfig.VERSION_NAME;
    static final Intent SAMSUNG_BROWSER_INTENT =
        new Intent().setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING");
    static final String RETRIEVED_ACCOUNT_PREF = "retrieved_account";
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1;
    static final int REQUEST_CODE_ACCOUNT_INTENT = 2;

    boolean isUiAnimating = false;
    boolean hasSamsungBrowser = false;
    String packageName;
    SharedPreferences preferences;
    ImageButton mainButton;
    TextView statusText;
    TextView hintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageName = getApplicationContext().getPackageName();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(this::onAdBlockPressed);

        statusText = findViewById(R.id.status_text);
        hintText = findViewById(R.id.hint_text);

        findViewById(R.id.help_button).setOnClickListener(this::onHelpPressed);
        findViewById(R.id.about_button).setOnClickListener(this::onAboutPressed);

        if (!Ruleset.exists(this)) {
            Ruleset.enable(this);
            animateBlocking();
        } else if (Ruleset.active(this)) {
            animateBlocking();
        } else {
            animateUnblocking();
        }

        presentOffer();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        if (preferences.getBoolean(RETRIEVED_ACCOUNT_PREF, false)) {
            checkIfHasBlockingBrowser();
        }

        Plausible.INSTANCE.pageView("/", "", null);
    }

    private void checkIfHasBlockingBrowser() {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(SAMSUNG_BROWSER_INTENT, 0);
        if (list.size() > 0) hasSamsungBrowser = true;

        if (!hasSamsungBrowser) {
            presentHelp(false);
        } else if (preferences.getBoolean("first_run", true)) {
            presentHelp(true);
            preferences.edit().putBoolean("first_run", false).apply();
        }
    }

    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            apiAvailability.makeGooglePlayServicesAvailable(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACCOUNT_INTENT) {
            if (data != null) {
                final String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                if (email != null) {
                    new Thread(() -> {
                        try {
                            URL url = new URL(BuildConfig.SUBSCRIBE_URL);
                            HttpURLConnection req = (HttpURLConnection) url.openConnection();

                            req.setRequestMethod("POST");
                            req.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
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

                            preferences.edit().putBoolean(RETRIEVED_ACCOUNT_PREF, true).apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

            checkIfHasBlockingBrowser();
        }
    }

    public void onAdBlockPressed(View v) {
        if (isUiAnimating) return;

        if (Ruleset.active(this)) {
            Ruleset.disable(this);
            animateUnblocking();
        } else {
            Ruleset.enable(this);
            animateBlocking();
        }

        Intent intent = new Intent();
        intent.setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE");
        intent.setData(Uri.parse("package:" + packageName));
        sendBroadcast(intent);
    }

    void onAboutPressed(View v) {
        final Dialog dialog = presentDialog(R.layout.about_dialog);

        ((TextView) dialog.findViewById(R.id.version_text))
            .setText(String.format(" %s", VERSION_NUMBER));
        setHtml(dialog.findViewById(R.id.tagline), R.string.tagline, false);
        setHtml(dialog.findViewById(R.id.copyright_text), R.string.copyright_notice, true);

        dialog.findViewById(R.id.dismiss_button).setOnClickListener((w) -> dialog.dismiss());
    }

    void onHelpPressed(View v) { presentHelp(true); }

    void presentOffer() {
        final Dialog dialog = presentDialog(R.layout.offer_dialog);

        ((TextView) dialog.findViewById(R.id.summary_text)).setText(R.string.offer_summary);
        setHtml(dialog.findViewById(R.id.details_text), R.string.offer_details, true);
        setHtml(dialog.findViewById(R.id.contact_text), R.string.contact_info, true);

        dialog.findViewById(R.id.accept_button).setOnClickListener((v) -> dialog.dismiss());
        dialog.findViewById(R.id.decline_button).setOnClickListener((v) -> dialog.dismiss());
    }

    void presentHelp(boolean isDismissible) {
        final Dialog dialog = presentDialog(R.layout.help_dialog);
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
        final Dialog dialog = new Dialog(this);

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
        animator(new int[] {
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
        animator(new int[] {
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

    void animator(final int[] res, final int resTxtStatus, final int resTxtTap) {
        isUiAnimating = true;

        double delay = 62.5;

        for (int i = 0; i < res.length; i++) {
            if (i == 0) {
                mainButton.setImageResource(res[i]);
            } else {
                Handler handler = new Handler();
                final int finalI = i;
                handler.postDelayed(() -> runOnUiThread(() -> {
                    mainButton.setImageResource(res[finalI]);

                    if (finalI == res.length - 1) {
                        isUiAnimating = false;
                        statusText.setText(resTxtStatus);
                        hintText.setText(resTxtTap);
                    }
                }), Math.round(delay * i));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
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
        if (preferences.getBoolean(RETRIEVED_ACCOUNT_PREF, false)) return;

        if (
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.GET_ACCOUNTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            getAccounts();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
                showAccountPermissionAlert();
            } else {
                requestPermissions(new String[] { Manifest.permission.GET_ACCOUNTS }, REQUEST_PERMISSION_GET_ACCOUNTS);
            }
        }
    }

    void getAccounts() {
        Intent intent =
            AccountPicker.newChooseAccountIntent(
                new AccountPicker.AccountChooserOptions.Builder()
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

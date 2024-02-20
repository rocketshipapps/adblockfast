package com.rocketshipapps.adblockfast;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;

import org.json.JSONObject;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import com.wbrawner.plausible.android.Plausible;

import com.rocketshipapps.adblockfast.utils.Rule;

public class MainActivity extends AppCompatActivity {
    static final String VERSION_NUMBER = BuildConfig.VERSION_NAME;
    static final Intent SAMSUNG_BROWSER_INTENT = new Intent()
        .setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING");
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
        setContentView(R.layout.activity_main);

        mainButton = findViewById(R.id.main_button);
        mainButton.setOnClickListener(this::onAdBlockPressed);

        statusText = findViewById(R.id.status_text);
        hintText = findViewById(R.id.hint_text);

        findViewById(R.id.help_button).setOnClickListener(this::onHelpPressed);
        findViewById(R.id.about_button).setOnClickListener(this::onAboutPressed);

        if (!Rule.exists(this)) {
            Rule.enable(this);
            enableAnimation();
        } else if (Rule.active(this)) {
            enableAnimation();
        } else {
            disableAnimation();
        }
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
            showHelpDialog(false);
        } else if (preferences.getBoolean("first_run", true)) {
            showHelpDialog(true);
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

    private void getAccounts() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[] {"com.google", "com.google.android.legacyimap"}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_ACCOUNT_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACCOUNT_INTENT) {
            if (data != null) {
                final String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                if (email != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    }).start();
                }
            }

            checkIfHasBlockingBrowser();
        }
    }

    public void onAdBlockPressed(View v) {
        if (isUiAnimating) return;

        if (Rule.active(this)) {
            Rule.disable(this);
            disableAnimation();
        } else {
            Rule.enable(this);
            enableAnimation();
        }

        Intent intent = new Intent();
        intent.setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE");
        intent.setData(Uri.parse("package:" + packageName));
        sendBroadcast(intent);
    }

    public void onAboutPressed(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_dialog_about);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ((TextView) dialog.findViewById(R.id.tagline)).setText(Html.fromHtml(getString(R.string.tagline)));

        TextView copyright = dialog.findViewById(R.id.copyright);
        copyright.setText(Html.fromHtml(getString(R.string.copyright)));
        copyright.setMovementMethod(LinkMovementMethod.getInstance());

        dialog.setCancelable(false);
        dialog.show();

        ((TextView) dialog.findViewById(R.id.txt_version)).setText(VERSION_NUMBER);

        dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void onHelpPressed(View v) {
        showHelpDialog(true);
    }

    private void checkAccountPermission() {
        if (preferences.getBoolean(RETRIEVED_ACCOUNT_PREF, false)) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_DENIED) {
            getAccounts();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
                showAccountPermissionAlert();
            } else {
                requestPermissions(new String[] { Manifest.permission.GET_ACCOUNTS }, REQUEST_PERMISSION_GET_ACCOUNTS);
            }
        }
    }

    private void showAccountPermissionAlert() {
        new AlertDialog
            .Builder(this)
            .setTitle("Permission needed")
            .setMessage("Get email address")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION_GET_ACCOUNTS);
                    }
                }
            })
            .create()
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    void showHelpDialog(boolean cancelable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_dialog_help);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false);
        dialog.show();

        TextView summary = dialog.findViewById(R.id.summary);
        TextView details = dialog.findViewById(R.id.details);

        if (hasSamsungBrowser) {
            summary.setText(R.string.settings_summary);
            details.setText(Html.fromHtml(getString(R.string.settings_details)));
            details.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startActivity(SAMSUNG_BROWSER_INTENT);
                }
            });
        } else {
            summary.setText(R.string.install_summary);
            details.setText(Html.fromHtml(getString(R.string.install_details)));
        }
        details.setMovementMethod(LinkMovementMethod.getInstance());
        TextView contact = dialog.findViewById(R.id.contact);
        contact.setText(Html.fromHtml(getString(R.string.contact)));
        contact.setMovementMethod(LinkMovementMethod.getInstance());

        if (cancelable) {
            dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } else {
            dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    void disableAnimation() {
        animator(new int[] {
            R.drawable.blocked_0,
            R.drawable.blocked_1,
            R.drawable.blocked_2,
            R.drawable.blocked_3,
            R.drawable.blocked_4,
            R.drawable.blocked_5,
            R.drawable.blocked_6,
            R.drawable.blocked_7,
            R.drawable.blocked_8,
            R.drawable.blocked_9,
            R.drawable.blocked_10,
            R.drawable.blocked_11,
            R.drawable.blocked_12,
            R.drawable.blocked_13,
            R.drawable.blocked_14,
            R.drawable.blocked_15
        }, R.string.unblocked_message, R.string.unblocked_hint);
    }

    void enableAnimation() {
        animator(new int[] {
            R.drawable.unblocked_0,
            R.drawable.unblocked_1,
            R.drawable.unblocked_2,
            R.drawable.unblocked_3,
            R.drawable.unblocked_4,
            R.drawable.unblocked_5,
            R.drawable.unblocked_6,
            R.drawable.unblocked_7,
            R.drawable.unblocked_8,
            R.drawable.unblocked_9,
            R.drawable.unblocked_10,
            R.drawable.unblocked_11,
            R.drawable.unblocked_12,
            R.drawable.unblocked_13,
            R.drawable.unblocked_14,
            R.drawable.unblocked_15
        }, R.string.blocked_message, R.string.blocked_hint);
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
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainButton.setImageResource(res[finalI]);

                                if (finalI == res.length - 1) {
                                    isUiAnimating = false;
                                    statusText.setText(resTxtStatus);
                                    hintText.setText(resTxtTap);
                                }
                            }
                        });
                    }
                }, Math.round(delay * i));
            }
        }
    }
}

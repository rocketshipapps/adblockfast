package com.rocketshipapps.adblockfast;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rocketshipapps.adblockfast.service.RegistrationIntentService;
import com.rocketshipapps.adblockfast.utils.Rule;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    boolean animating = false;

    ImageButton btnAdblock;
    TextView txtStatus;
    TextView txtTap;

    String packageName;
    String version;

    Tracker tracker;

    SharedPreferences preferences;

    boolean hasBlockingBrowser = false;
    Intent samsungBrowserIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/AvenirNextLTPro-Light.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        packageName = getApplicationContext().getPackageName();
        version = BuildConfig.VERSION_NAME;

        btnAdblock = (ImageButton) findViewById(R.id.btn_adblock);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        txtTap = (TextView) findViewById(R.id.txt_tap);

        if (!Rule.exists(this)) {
            Rule.enable(this);
            enableAnimtaion();
        } else if (Rule.active(this)) {
            enableAnimtaion();
        } else {
            disableAnimtaion();
        }

        AdblockfastApplication application = (AdblockfastApplication) getApplication();
        tracker = application.getDefaultTracker();

        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        samsungBrowserIntent = new Intent();
        samsungBrowserIntent.setAction("com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING");

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();

        tracker.setScreenName("/");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(samsungBrowserIntent, 0);
        if (list.size() > 0) hasBlockingBrowser = true;

        if (preferences.getBoolean("first_run", true) || !hasBlockingBrowser) {
            showHelpDialog(false);
            preferences.edit().putBoolean("first_run", false).apply();
        }

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000)
                        .show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    //region OnClick

    public void onAdBlockPressed(View v) {
        if (animating) return;

        if (Rule.active(this)) {
            Rule.disable(this);
            disableAnimtaion();
        } else {
            Rule.enable(this);
            enableAnimtaion();
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView copyright = (TextView) dialog.findViewById(R.id.copyright);
        copyright.setText(Html.fromHtml(getString(R.string.copyright)));
        copyright.setMovementMethod(LinkMovementMethod.getInstance());

        dialog.show();

        ((TextView) dialog.findViewById(R.id.txt_version)).setText(version);

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

    //endregion

    //region Dialog
    void showHelpDialog(boolean cancelable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_dialog_help);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(cancelable);
        dialog.show();

        TextView summary = (TextView) dialog.findViewById(R.id.summary);
        TextView details = (TextView) dialog.findViewById(R.id.details);

        if (hasBlockingBrowser) {
            summary.setText(R.string.settings_summary);
            details.setText(Html.fromHtml(getString(R.string.settings_details)));
            details.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startActivity(samsungBrowserIntent);
                }
            });
        } else {
            summary.setText(R.string.install_summary);
            details.setText(Html.fromHtml(getString(R.string.install_details)));
        }
        details.setMovementMethod(LinkMovementMethod.getInstance());
        TextView contact = (TextView) dialog.findViewById(R.id.contact);
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
    //endregion

    //region Block Animation

    void disableAnimtaion() {
        animator(new int[]{
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

    void enableAnimtaion() {
        animator(new int[]{
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
        animating = true;

        double delay = 62.5;

        for (int i=0; i<res.length; ++i) {
            if (i==0) {
                btnAdblock.setImageResource(res[i]);
            } else {
                Handler handler = new Handler();
                final int finalI = i;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnAdblock.setImageResource(res[finalI]);

                                if (finalI == res.length-1) {
                                    animating = false;
                                    txtStatus.setText(resTxtStatus);
                                    txtTap.setText(resTxtTap);
                                }
                            }
                        });
                    }
                }, Math.round(delay * i));
            }
        }
    }

    //endregion
}

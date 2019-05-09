package com.rocketshipapps.adblockfast;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PremiumUpgradeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_upgrade);

        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/AvenirNextLTPro-Light.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        ((TextView) findViewById(R.id.txtInstruction1)).setText(Html.fromHtml(getString(R.string.premium_instruction_1)));
        ((TextView) findViewById(R.id.txtInstruction2)).setText(Html.fromHtml(getString(R.string.premium_instruction_2)));
        ((TextView) findViewById(R.id.txtInstruction2)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void onUpgradePressed(View v) {
        setResult(1);
        finish();
    }

    public void onBackPressed(View v) {
        setResult(0);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

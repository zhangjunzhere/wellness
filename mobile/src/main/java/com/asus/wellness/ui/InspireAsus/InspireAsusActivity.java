package com.asus.wellness.ui.InspireAsus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.ui.setting.SettingActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * 定义inspire asus的Activity
 *
 * @author: noah zhang
 * @date: 2015-01-27
 * @remark
 */
public class InspireAsusActivity extends Activity {
    private static final String URL_ASUS_PRIVACY = "http://www.asus.com/us/Terms_of_Use_Notice_Privacy_Policy/Privacy_Policy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inspire_asus);
        getActionBar().hide();

        TextView textView = (TextView) findViewById(R.id.asusPrivacyTextView);
        SpannableString sp = new SpannableString(URL_ASUS_PRIVACY);
        //设置超链接
        sp.setSpan(new URLSpan(URL_ASUS_PRIVACY), 0, URL_ASUS_PRIVACY.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置颜色
        sp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, URL_ASUS_PRIVACY.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(sp);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        View yesButton = findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(false);

                SharedPreferences mSharedPref = getSharedPreferences(SettingActivity.KEY_GA, MODE_PRIVATE);
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putBoolean(SettingActivity.IS_APP_OPT_OUT, true);
                editor.commit();
                InspireAsusActivity.this.finish();
            }
        });

        Utility.trackerScreennView(getApplicationContext(), "Goto InspireAsus");
    }
}

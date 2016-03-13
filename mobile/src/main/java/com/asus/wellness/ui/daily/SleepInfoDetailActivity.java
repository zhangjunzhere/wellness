package com.asus.wellness.ui.daily;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.ui.setting.SettingActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by smile_gao on 2015/12/22.
 */
public class SleepInfoDetailActivity extends Activity  implements View.OnClickListener{
    int[][] info_resid = {
            {R.string.text_sleep_quality_score,R.string.sleepquality_info,R.string.sleepquality_suggestion,R.string.sleep_quality_source,R.string.sleep_quality_source1},
            {R.string.sleeplenght,R.string.sleeplength_info,R.string.sleeplength_suggestion,R.string.sleep_length_source},
            {R.string.deepsleepandlightsleep,R.string.deepsleepandlightsleep_info,R.string.deepsleepandlightsleep_suggestion,R.string.sleep_deeplight_source},
            {R.string.awakenings,R.string.awakenings_info,R.string.awakenings_suggestion,R.string.awakenings_source},
    };
    int[][] tips_resid = {
            { R.string.stick_to_a_sleep_schedule_title,R.string.stick_to_a_sleep_schedule},
            { R.string.exercise_is_great_title,R.string.exercise_is_great },
            { R.string.avoid_cafeine_and_nicotine_title,R.string.avoid_cafeine_and_nicotine},
            { R.string.avoid_alcoholic_drinks_before_bed_title,R.string.avoid_alcoholic_drinks_before_bed },
            { R.string.avoid_large_meals_title,R.string.avoid_large_meals },
            { R.string.if_possible_title, R.string.if_possible },
            { R.string.dont_take_naps_title, R.string.dont_take_naps },
            { R.string.relax_before_bed_title, R.string.relax_before_bed },
            { R.string.take_a_lot_bath_title, R.string.take_a_lot_bath },
            { R.string.have_a_good_sleeping_title,R.string.have_a_good_sleeping},
            { R.string.have_the_right_sunlight_title,R.string.have_the_right_sunlight},
            { R.string.dont_lie_in_bed_title,R.string.dont_lie_in_bed },
            { R.string.see_a_health_title, R.string.see_a_health }
    };
    TextView mTitle ;
    TextView mContent;
    TextView mContentSuggestion;
    TextView mSource;
    TextView mSouceContent;
    TextView mSouceContent1;
    LinearLayout mLLSource1;
    public static String IS_TIPS_KEY = "istips";
    public static String TIPS_KEY_INDEX= "tips_index";
    private boolean isTips= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setContentView(R.layout.sleep_infomation1);
        Intent i = getIntent();
        int key=-1;
        if(i!=null)
        {
            key =  i.getIntExtra("key",-1);
            isTips = i.getBooleanExtra(IS_TIPS_KEY,false);
            Log.i("smile","key: "+key+" "+isTips);

        }
        else
        {
            Log.i("smile","key: empty");
        }
        mTitle = (TextView) findViewById(R.id.info_title);
        mContent=(TextView) findViewById(R.id.info_content);
        mContentSuggestion = (TextView) findViewById(R.id.info_content_suggestion);
        mSource = (TextView) findViewById(R.id.data_source);
        mSouceContent = (TextView) findViewById(R.id.source_content);
        mSouceContent1 = (TextView) findViewById(R.id.source_content1);
        mLLSource1 = (LinearLayout)findViewById(R.id.ll_source1);
        findViewById(R.id.btn_no).setOnClickListener(this);
        findViewById(R.id.btn_yes).setOnClickListener(this);
        if(isTips)
        {
            initTips(key);
            Utility.trackerScreennView(getApplicationContext(), "Goto SleepTipDetail-" + mTitle.getText().toString());
        }
        else
        {
            init(key);
            Utility.trackerScreennView(getApplicationContext(), "Goto SleepInfoDetail-" + mTitle.getText().toString());
        }
    }
    void initTips(int key)
    {
        if(key<0 || key>=tips_resid.length)
        {
            return;
        }
        int[] res = tips_resid[key];
        mTitle.setText(res[0]);
        mContent.setText(res[1]);
        mContentSuggestion.setVisibility(View.GONE);
        mSouceContent.setText(R.string.tips_souce);
        mLLSource1.setVisibility(View.GONE);
    }
    void init(int key)
    {
        if(key<0 || key>=info_resid.length)
        {
            return;
        }
        int[] res = info_resid[key];
        mTitle.setText(res[0]);
        mContent.setText(res[1]);
        mContentSuggestion.setVisibility(View.VISIBLE);
        mContentSuggestion.setText(res[2]);
        mSouceContent.setText(res[3]);
        if(res.length>4)
        {
            mLLSource1.setVisibility(View.VISIBLE);
            mSouceContent1.setText(res[4]);
        }
        else
        {
            mLLSource1.setVisibility(View.GONE);
        }
    }

    private void setActionBar(){
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        int actionbarTitleId=getResources().getIdentifier("action_bar_title", "id", "android");
        TextView abTitle = (TextView)findViewById(actionbarTitleId);
        if(abTitle != null) abTitle.setTextColor(0xff4c4c4c);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        sendBroadcast(new Intent(SettingActivity.FEEDBACK_ACTION));
    }
}

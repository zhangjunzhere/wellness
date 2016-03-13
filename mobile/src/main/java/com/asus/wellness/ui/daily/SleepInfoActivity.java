package com.asus.wellness.ui.daily;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by smile_gao on 2015/12/2.
 */
public class SleepInfoActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setContentView(R.layout.sleep_infomation);

        Utility.trackerScreennView(getApplicationContext(), "Goto SleepInfo");
    }
    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.sleepinformation);
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
}

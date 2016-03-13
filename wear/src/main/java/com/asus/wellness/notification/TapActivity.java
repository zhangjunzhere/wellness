package com.asus.wellness.notification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.microprovider.StepCountInfo;
import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2014/12/25.
 */
public class TapActivity extends Activity {
    private DataLayerManager dataLayerManager = null;
    private long nextGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_tap_layout);

        dataLayerManager =  DataLayerManager.getInstance(this);
        dataLayerManager.connectGoogleApiClient(null);

        long targetGoal = getIntent().getLongExtra(NotificaionHelper.TARGET_GOAL, -1);
        nextGoal = (long) (targetGoal * 1.05);

        changeTextSizeTap();
    }

    private TextView tapToAddGoalTv;
    private void changeTextSizeTap() {
        tapToAddGoalTv = (TextView) findViewById(R.id.tap_to_add_goal);
     //   Utility.adjustTextSize(tapToAddGoalTv,20,0);
       int size =  getResources().getDimensionPixelOffset(R.dimen.tap_to_add_goal_normal);
        Utility.fitFontSizeForView(tapToAddGoalTv,size,Utility.getScreenWidth(this));
//        tapToAddGoalTv.measure(0, 0);
//        if(tapToAddGoalTv.getMeasuredWidth() > 240) {
//            tapToAddGoalTv.setTextSize(getResources().getDimension(R.dimen.tap_to_add_goal_small));
//        } else if(tapToAddGoalTv.getMeasuredWidth() > 220) {
//            tapToAddGoalTv.setTextSize(getResources().getDimension(R.dimen.tap_to_add_goal_normal));
//        } else {
//            tapToAddGoalTv.setTextSize(getResources().getDimension(R.dimen.tap_to_add_goal_large));
//        }
    }

    /**
     *
     * @param view
     */
    public void nextGoalClick(View view) {
        // Send "Next GOAL" To Phone
        dataLayerManager.sendStepGoalDataToPhone(nextGoal);

        // Show in Wellness in ZenWatch
        StepCountInfo.getInstance(this).saveNextStepGoalToDB((int) nextGoal);

        // Start "Next Goal" Activity
        Intent intent = new Intent(this, NextGoalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(NotificaionHelper.TARGET_GOAL, nextGoal);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(dataLayerManager != null) {
//            //dataLayerManager.disConnectGoogleApiClient();
//        }
    }
}

package com.asus.wellness.notification;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.asus.wellness.R;

/**
 * Created by smile_gao on 2014/12/19.
 */
public class ReachGoalActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notification_reach_goal_layout);
        changeTextSizeReachGoal();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificaionHelper.getInstance(this).setHasReachGoalNotification(true);
    }

    private TextView reachGoalTv;
    private void changeTextSizeReachGoal() {
        reachGoalTv = (TextView) findViewById(R.id.reach_goal_tv);
        reachGoalTv.measure(0, 0);
        Log.d("kim_bai", "reach goal width = " + reachGoalTv.getMeasuredWidth());
        if(reachGoalTv.getMeasuredWidth() > 500) {
            reachGoalTv.setTextSize(getResources().getDimension(R.dimen.reach_goal_small));
        } else if(reachGoalTv.getMeasuredWidth() > 220) {
            reachGoalTv.setTextSize(getResources().getDimension(R.dimen.reach_goal_normal));
        } else {
            reachGoalTv.setTextSize(getResources().getDimension(R.dimen.reach_goal_large));
        }
    }
}

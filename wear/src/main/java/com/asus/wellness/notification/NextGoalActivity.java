package com.asus.wellness.notification;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.asus.wellness.R;

/**
 * Created by smile_gao on 2014/12/19.
 */
public class NextGoalActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_next_goal_layout);

        long nextGoal = getIntent().getLongExtra(NotificaionHelper.TARGET_GOAL, -1);

        // Show in "Next GOAL" page
        TextView stepCountTv = (TextView) findViewById(R.id.step_count);
        stepCountTv.setText(String.valueOf(nextGoal));
        NotificaionHelper.getInstance(this).cancelNotification(NotificaionHelper.REACH_GOAL_NOTIFICATION_ID);
    }
}

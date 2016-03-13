package com.asus.wellness.notification;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.StartWorkoutActivity;
import com.asus.wellness.coach.SummeryWorkoutActivity;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.Utility;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class NotificationWorkoutInfoActivity extends Activity {
    private CoachDataModel mCoachDataModel;

    private int[] layoutIds = new int[]{
            R.id.rl_distance,
            R.id.rl_time,
            R.id.rl_calories
    };

    private  List<CoachItem> coachItems = new ArrayList<CoachItem>();
    private Handler mTimerHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pni_coach_activity_workout_notification);

        mCoachDataModel = ((WApplication)getApplication()).getCoachDataModel();

        CoachItem coachItem =   new CoachItem(
                R.drawable.notification_distance,
                R.string.total_quantity,
                "",
                "",
                CoachDataModel.eGoal.QUANTITY
        );
        switch (mCoachDataModel.getType()){
            case RUN:
                coachItem =   new CoachItem(
                        R.drawable.notification_distance,
                        R.string.total_distance,
                        "",
                        mCoachDataModel.getUnit() == CoachDataModel.eUnit.KM ? getString(R.string.distance_unit) : getString(R.string.miles),
                        CoachDataModel.eGoal.DISTANCE
                );
                break;
            case PUSHUP:
                coachItem.iconId = R.drawable.asus_wellness_ic_pushup_g;
                break;
            case SITUP:
                coachItem.iconId = R.drawable.asus_wellness_ic_situp_g ;
                break;
            default:
                break;
        }
        coachItem.value = mCoachDataModel.getCoachValueString();
        coachItems.add(coachItem);

        coachItems.add(
                new CoachItem(
                        R.drawable.notification_timer,
                        R.string.total_time,
                        Utility.formatTime(mCoachDataModel.getTotalTime()),
                        "",
                        CoachDataModel.eGoal.TIME)
        );
        coachItems.add(
                new CoachItem(
                        R.drawable.notification_calories,
                        R.string.calories_burned,
                        String.valueOf(mCoachDataModel.getTotalCalories()),
                        getString(R.string.calories_unit),
                        CoachDataModel.eGoal.COLARIES)
        );

        ViewGroup rl_calories = (ViewGroup) findViewById(R.id.rl_calories);
        switch(mCoachDataModel.getType() ){
            case RUN:
                rl_calories.setVisibility(View.VISIBLE);
                break;
            case PUSHUP:
            case SITUP:
                rl_calories.setVisibility(View.GONE);
                break;
        }

        findViewById(R.id.ll_rootView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String className = SummeryWorkoutActivity.class.getName();
                if(mCoachDataModel.isCoaching()){
                    className = StartWorkoutActivity.class.getName();
                }
                EBCommandUtils.startActivity(this.getClass().getName(), className);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mTimerHandler.removeCallbacks(runnable);
    }

    @Override
    public void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
        updateUI();
        updateTimer();
    }

    @Override
    public void onPause(){
        super.onPause();
        EventBus.getDefault().unregister(this);
        mTimerHandler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long total_time = mCoachDataModel.getHistoryInterval() + (System.currentTimeMillis() - mCoachDataModel.getStartTime())/1000;
            mCoachDataModel.setTotalTime(total_time); //increase 1 second every tick
            mTimerHandler.postDelayed(runnable, 1000);
            updateUI();
        }
    };

    //for evenetbus ,receive command from WorkoutControllerLayout
    public void onEventMainThread(EBCommand ebCommand){
        if(EBCommand.COMMAND_COACH_DATA.equals(ebCommand.command) || EBCommand.COMMAND_COACH_STATE_CHANGED.equals(ebCommand.command)){
           updateUI();
        }
    }

    private void updateTimer(){
        mTimerHandler.removeCallbacks(runnable);
        switch (mCoachDataModel.getState()) {
            case PLAY:
            case RESUME:
                mTimerHandler.post(runnable);
                break;
            case STOP:
            case FINISH:
            case PAUSE:
            default:
                break;
        }
    }

    private void updateUI(){
        coachItems.get(0).value =   mCoachDataModel.getCoachValueString();
        coachItems.get(1).value = Utility.formatTime(mCoachDataModel.getTotalTime());
        coachItems.get(2).value =  String.valueOf(mCoachDataModel.getTotalCalories());

        TextView tv_title = (TextView)findViewById(R.id.tv_title);
        String title  =  getString(R.string.coach);
//        if(mCoachDataModel.achieveTargetValue()){
//            title = getResources().getString(R.string.coach_done);
//        }else{
        switch (mCoachDataModel.getType()){
            case RUN:
                title =getResources().getString(R.string.running);
                break;
            case PUSHUP:
                title =getResources().getString(R.string.pushup);
                break;
            case SITUP:
                title = getResources().getString(R.string.situp);
                break;
        }
//        }
        tv_title.setText(NotificaionHelper.getInstance(this).getCoachStateTitle(title));

        for(int i = 0; i < layoutIds.length; i++ ){
            View layout =  findViewById(layoutIds[i]);
            ImageView iv_icon = (ImageView) layout.findViewById(R.id.iv_icon);
            TextView tv_value = (TextView) layout.findViewById(R.id.tv_value);
            TextView tv_unit = (TextView) layout.findViewById(R.id.tv_unit);
            CoachItem item = coachItems.get(i);
            iv_icon.setImageResource(item.iconId);
            tv_value.setText(item.value);
            tv_unit.setText(item.unit);
        }

        updateTimer();
    }

    private class CoachItem {
        public int iconId;
        public String title;
        public String value;
        public String unit;

        public CoachItem(int iconId, int titleId, String value, String unit, CoachDataModel.eGoal goal){
            this.iconId = iconId;
            this.title = getString(titleId);
            this.value = value;
            this.unit = unit;
        }
    }
}

package com.asus.wellness.coach;

import android.os.Bundle;

import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.Utility;

import java.text.SimpleDateFormat;

import de.greenrobot.event.EventBus;


public class StartWorkoutActivity extends WearableActivity {
    private String TAG = "StartWorkoutActivity";
    public final static String ACTION = "com.asus.wellness.action.startworkoutactivity";
    private CoachDataModel mCoachDataModel;
    private Handler mTimerHandler = new Handler();
    private boolean isResumed;
    private ScrollView mRootView;
    private LinearLayout mControllerPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoachDataModel = ((WApplication)getApplication()).getCoachDataModel();
        setContentView(R.layout.pni_coach_activity_workout_start);
        setAmbientEnabled();
        mRootView = (ScrollView)findViewById(R.id.scrollview_container);
        mControllerPanel = (LinearLayout) findViewById(R.id.ll_controller);
        EventBus.getDefault().register(this);

        LinearLayout ll_calories = (LinearLayout) findViewById(R.id.ll_calories);
        switch(mCoachDataModel.getType() ){
            case RUN:
                ll_calories.setVisibility(View.VISIBLE);
                break;
            case PUSHUP:
            case SITUP:
                ll_calories.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();
        if(mCoachDataModel.achieveTargetValue( ) || mCoachDataModel.getState().equals(CoachDataModel.eState.STOP)){
            //startActivity(new Intent(this, SummeryWorkoutActivity.class));
            finish();
        }else{
			isResumed = true;
            updateUI();
            updateTimer();
        }
    }

    @Override
    protected  void onPause(){
        super.onPause();
        isResumed = false;
        mTimerHandler.removeCallbacks(runnable);
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        isResumed  = false;
        EventBus.getDefault().unregister(this);
        mTimerHandler.removeCallbacks(runnable);
    }

    private void updateUI(){
        calculatorTotalTime();
        TextView tv_timer = (TextView)findViewById(R.id.tv_timer);
        String time = Utility.formatTime(mCoachDataModel.getTotalTime(), isAmbient());
        tv_timer.setText(time);

        TextView tv_calories = (TextView)findViewById(R.id.tv_calories);
        tv_calories.setText(String.valueOf(mCoachDataModel.getTotalCalories()));

        TextView tv_target_distance = (TextView)findViewById(R.id.tv_target_distance);
        tv_target_distance.setText(String.valueOf(mCoachDataModel.getDistanceTargetWithUnitString(this)));

        TextView tv_target = (TextView)findViewById(R.id.tv_target);
        String target = mCoachDataModel.getTargetString(this);
//        if(mCoachDataModel.getGoal() == CoachDataModel.eGoal.NOGOAL){
//            target = "N/A";
//        }
        tv_target.setText(target);


        TextView tv_target_unit = (TextView)findViewById(R.id.tv_target_unit);
        tv_target_unit.setText( mCoachDataModel.getTargetUnitString(this));

       
        TextView tv_percent_value = (TextView)findViewById(R.id.tv_percent);
        tv_percent_value.setText(mCoachDataModel.getPercentString());

        String workoutValue = mCoachDataModel.getCoachValueString();
        TextView tv_workout = (TextView)findViewById(R.id.tv_workout);
        tv_workout.setText(workoutValue);

        ImageView iv_workout_icon = (ImageView) findViewById(R.id.iv_workout_icon);
        int visibility = View.GONE;
        int workoutIconId = R.drawable.asus_wellness_ic_run_b;
        switch (mCoachDataModel.getType()){
            case RUN:
                visibility = View.VISIBLE;
                workoutIconId = R.drawable.asus_wellness_ic_run_b;
                break;
            case SITUP:
                workoutIconId = R.drawable.asus_wellness_ic_situp;
                break;
            case PUSHUP:
                workoutIconId = R.drawable.asus_wellness_ic_pushup;
                break;
        }
        iv_workout_icon.setImageResource(workoutIconId);
        tv_target_distance.setVisibility(visibility);
        TextView tv_tap = (TextView)findViewById(R.id.tv_tap);

        ImageView iv_status = (ImageView)findViewById(R.id.iv_status);

        CoachDataModel coachDataModel = WApplication.getInstance().getCoachDataModel();

        switch (coachDataModel.getState()) {
            case PLAY:
            case RESUME:
                tv_tap.setText(R.string.tap_to_pause);
                iv_status.setImageResource(R.drawable.asus_wellness_ic_coach_play);
                break;
            case PAUSE:
            case STOP:
            case FINISH:
            default:
                tv_tap.setText(R.string.workout_start_action);
                iv_status.setImageResource(R.drawable.asus_wellness_ic_coach_pause);
                break;
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {            
            mTimerHandler.postDelayed(runnable, 1000);
            updateUI();
        }
    };

    private void calculatorTotalTime(){
        long total_time = mCoachDataModel.getHistoryInterval() + (System.currentTimeMillis() - mCoachDataModel.getStartTime())/1000;
//        Log.d(TAG,  "calculatorTotalTime total_time: " + total_time + "getHistoryInterval "
//                + mCoachDataModel.getHistoryInterval()  + "start:" + new SimpleDateFormat("hh:mm:ss").format(mCoachDataModel.getStartTime())
//                + " now:" +  new SimpleDateFormat("hh:mm:ss").format(System.currentTimeMillis()));
        mCoachDataModel.setTotalTime(total_time); //increase 1 second every tick
    }

    //for evenetbus ,receive command from WorkoutControllerLayout
    public void onEventMainThread(EBCommand ebCommand){
        if(EBCommand.COMMAND_COACH_DATA.equals(ebCommand.command) || EBCommand.COMMAND_COACH_STATE_CHANGED.equals(ebCommand.command)) {
            if (mCoachDataModel.achieveTargetValue() || ( mCoachDataModel.getState().equals(CoachDataModel.eState.STOP) &&  EBCommand.COMMAND_COACH_STATE_CHANGED.equals(ebCommand.command))) {
                EBCommandUtils.startActivity(this.getClass().getName(), SummeryWorkoutActivity.class.getName());
                finish();               
            } else if (isResumed) {
                updateUI();
                updateTimer();
            }
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

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        mRootView.setBackgroundResource(R.color.black);
        mRootView.scrollTo(0, 0);
        mControllerPanel.setBackgroundResource(R.color.black);
        findViewById(R.id.tv_clock).setVisibility(View.VISIBLE);
        updateUI();
        mTimerHandler.removeCallbacks(runnable);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateUI();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        mRootView.setBackgroundResource(R.drawable.background_gradient);
        mControllerPanel.setBackgroundResource(R.drawable.pni_asus_app_micro_bg);
        findViewById(R.id.tv_clock).setVisibility(View.GONE);
        updateTimer();
        updateUI();
    }
}

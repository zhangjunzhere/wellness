package com.asus.wellness.sleep;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextClock;
import android.widget.Toast;

import com.asus.wellness.R;

import com.asus.wellness.StartActivity;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.ConfirmStopWorkoutDialog;
import com.asus.wellness.coach.LauncherTipsDialog;
import com.asus.wellness.coach.StartWorkoutActivity;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.EBCommandUtils;


import de.greenrobot.event.EventBus;

public class SleepActivity extends WearableActivity {
    public final static String KEY_REFER_NOTIFICATION = "key_refer_notification";

    private View mRootView;
    private View mTextClock;
    SleepDataModel.eSleep sleepStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pni_sleep_activity);
        mRootView = findViewById(R.id.ll_rootView);
        mTextClock = findViewById(R.id.tv_clock);

        setAmbientEnabled();
        showTips();
        changePage(getSleepFragment(), true);
    }

    //    @Override
    public void changePage(Class<?> fragmentClazz,boolean forced) {
        Fragment fragment = Fragment.instantiate(this, fragmentClazz.getName(), null);
        SleepDataModel.eSleep newStatus = getSleepStatus();
        if(!forced && sleepStatus == newStatus) {
            return;
        }
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SleepDataModel sleepDataModel = SleepDataModel.getInstance();
        if(sleepDataModel.getSleepStatus() == SleepDataModel.eSleep.AWAKE  && !isAmbient()) {
            //EBCommand ebCommand = new EBCommand(SleepActivity.class.getName(), WorkoutDataService.class.getName(), EBCommand.COMMAND_START_ACTIVITY, StartActivity.class.getName());
            //EventBus.getDefault().post(ebCommand);
        }else if(sleepDataModel.getSleepEnabled()){
            EBCommandUtils.showSleepNotification(this.getClass().getName(), true);
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        EBCommandUtils.enterAmbientMode(this.getClass().getName(),true);
        mRootView.setBackgroundResource(R.color.black);
        mTextClock.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        EBCommandUtils.enterAmbientMode(this.getClass().getName(), false);
        mRootView.setBackgroundResource(R.drawable.background_gradient);
        mTextClock.setVisibility(View.GONE);
    }

    public void onEventMainThread(EBCommand ebCommand){
        if(EBCommand.COMMAND_SLEEP_STATE.equals(ebCommand.command)){
            changePage(getSleepFragment(), true);
        }else  if(EBCommand.COMMAND_START_SLEEP.equals(ebCommand.command)){
            boolean sleepEnabled = (boolean) ebCommand.param;
            if(!sleepEnabled){
                SleepDataModel sleepDataModel   = SleepDataModel.getInstance();
                sleepDataModel.setSleepStatus(SleepDataModel.eSleep.FINISH);
                changePage(getSleepFragment(), true);
            }
        }
    }

    private Class<?>  getSleepFragment(){
        Class<?> clazz ;
        sleepStatus = getSleepStatus();
        switch (sleepStatus){
            case START:
                clazz = SleepRecordingFragment.class;
                break;
            default:
                clazz =  SleepSummeryFragment.class;
                break;
        }
        return clazz;
    }

    private SleepDataModel.eSleep getSleepStatus(){
        SleepDataModel sleepDataModel   = SleepDataModel.getInstance();
        return sleepDataModel.getSleepStatus();
    }

    private void showTips(){
//        LauncherTipsDialog dialog = new LauncherTipsDialog();
//        dialog.setTips(getString(R.string.sleep_save_power_tips));
//        dialog.setSplash(true);
//        dialog.show(this.getFragmentManager(), "sleep");
        Toast toast = Toast.makeText(this, getString(R.string.sleep_save_power_tips), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
        toast.show();
    }
}

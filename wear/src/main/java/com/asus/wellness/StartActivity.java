package com.asus.wellness;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;


import com.asus.wellness.Profile.ProfileActivity;
import com.asus.wellness.coach.CoachSetupActivity;
import com.asus.wellness.coach.ConfirmStopWorkoutDialog;
import com.asus.wellness.coach.LauncherTipsDialog;
import com.asus.wellness.service.CollectStepCountService;

import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.StartWorkoutActivity;
import com.asus.wellness.coach.SummeryWorkoutActivity;
import com.asus.wellness.sleep.SleepActivity;
import com.asus.wellness.sleep.SleepDataModel;
import com.asus.wellness.sleep.SleepShortcutActivity;
import com.asus.wellness.system.SystemModel;
import com.asus.wellness.utils.Constant;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.ShortCutUtils;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.utils.WearNameHelper;


public class StartActivity extends Activity {
    private Intent mIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WellnessVersionCode", "WellnessVersionCode: " + getCurrentVersionCode() + " manufacturer:" + WearNameHelper.getWearName()
        + " board: " + Build.BOARD);

        if(!Build.MANUFACTURER.toLowerCase().matches("asus")){
            Toast.makeText(this, getString(R.string.not_support_message), Toast.LENGTH_LONG).show();
            finish();
        }

         //start service to collect step count
        Intent serviceIntent=new Intent(this, CollectStepCountService.class);
        startService(serviceIntent);

//        if(SystemModel.getIntance().isFirstUse())
//        {
//            Intent intent = new Intent(this, ProfileActivity.class);
//            goToActivity(intent);
//            return;
//        }

        boolean launchFromSleep = getIntent().getBooleanExtra(SleepShortcutActivity.KEY_REFERER,false);
        //forward to different activity according to devices
        mIntent = new Intent(this, WellnessMicroAppMain.class);

        Intent voiceIntent = getIntent();
        if(voiceIntent != null ){
            String mimeType = voiceIntent.getType();
            if(mimeType != null && mimeType.contains(Constant.INTEN_ACTION_COACH)) {
                mIntent = new Intent(this, CoachSetupActivity.class);
                mIntent.putExtra(Constant.KEY_MIME_TYPE, mimeType);
            }
        }

        CoachDataModel coachDataModel = WApplication.getInstance().getCoachDataModel() ;
        SleepDataModel sleepDataModel   = SleepDataModel.getInstance();
        LauncherTipsDialog dialog = new LauncherTipsDialog();
        dialog.setOnDismissListener( new ConfirmStopWorkoutDialog.OnDismissListener() {
            @Override
            public void onDismiss(Boolean confirmed) {
                if(confirmed) {
                    goToActivity(mIntent);
                }else {
                    finish();
                }
            }
        });

        if(launchFromSleep){
                if(coachDataModel.isCoaching()){
                    mIntent =  new Intent(this, StartWorkoutActivity.class);
                    dialog.setTips(getString(R.string.workout_coaching_tips ));
                    dialog.show(this.getFragmentManager(), "workout");
                }else{
                    if(!sleepDataModel.getSleepEnabled()){
                        sleepDataModel.setSleepStatus(SleepDataModel.eSleep.START);
                        EBCommandUtils.startSleep(this.getClass().getName(),true);
                    }
                    mIntent =  new Intent(this, SleepActivity.class);
                    goToActivity(mIntent);
                }
        }else { //from main
            if (sleepDataModel.getSleepEnabled()) {
                    mIntent = new Intent(this, SleepActivity.class);
                    dialog.setTips(getString(R.string.sleep_tracking_tips));
                    dialog.show(this.getFragmentManager(), "sleep");
            } else {
                switch (coachDataModel.getState()) {
                    case PLAY:
                    case PAUSE:
                    case RESUME:
                        mIntent = new Intent(this, StartWorkoutActivity.class);
                        break;
                    case STOP:
                        mIntent = new Intent(this, SummeryWorkoutActivity.class);
                        break;
                    default:
                        break;
                }
                goToActivity(mIntent);
            }
        }
    }

    public int getCurrentVersionCode() {
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();

        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return  -1;
        }
    }

    private void goToActivity(Intent intent){
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
        finish();
    }

}

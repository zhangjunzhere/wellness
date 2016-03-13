package com.asus.wellness;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.sleep.SleepShortcutActivity;
import com.asus.wellness.utils.ShortCutUtils;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2015/5/14.
 */
public class WApplication extends Application {
    private static final String TAG = "WApplication";
    private static WApplication  wellnessApplication;
    private static DataHelper dataHelper;
    private CoachDataModel coachDataModel ;
    private boolean mIsZenWatchPni = false;
    private boolean mIsWorn = false;
    private int todaySteps = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WApplication 25 onCreate");
        wellnessApplication = this;
        if(!Utility.isSupportSleepSensor(this))
        {
            Log.i("smile","shortcut: "+SleepShortcutActivity.class.getName());
            ShortCutUtils.disableShortCut(this, SleepShortcutActivity.class.getName());
        }
        if(dataHelper ==null) {
            dataHelper = new DataHelper(this);
            dataHelper.getDaoSession();
        }

        coachDataModel = new CoachDataModel();
        if(Build.MODEL.contains("ASUS ZenWatch 2"))
        {
            mIsZenWatchPni = true;
        }

        todaySteps = StepHelper.getTodaySteps();
    }
    public boolean isZenWatchPni()
    {
        return  mIsZenWatchPni;
    }
    public static DataHelper getDataHelper(){ return dataHelper;}

    public static void setDataHelper(DataHelper dhelper){ dataHelper = dhelper;}

    public static WApplication getInstance(){
        return wellnessApplication;
    }

    public CoachDataModel getCoachDataModel(){return  coachDataModel;}

    public  void startService(Class<?> clazz){
        Intent serviceIntent=new Intent(this, clazz);
        startService(serviceIntent);
    }

    public void setTodaySteps(int steps){
        todaySteps = steps;
    }
    public int getTodaySteps(){return  todaySteps;}

    public boolean getWearWorn(){return  mIsWorn;}
    public void setWearWorn(boolean isWorn){ mIsWorn = isWorn; }

}

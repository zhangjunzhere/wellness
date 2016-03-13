package com.asus.wellness.ga;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asus.sharedata.SleepData;
import com.asus.sharedata.SleepTimeSpan;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.provider.CoachTable;
import com.asus.wellness.sleep.SleepHelper;

import java.util.ArrayList;

/**
 * Created by larrylf_lin on 2015/12/7.
 */
public  class TaskWatcherService extends IntentService{

    protected static final String TAG = "TaskWatcherService";
    private static final int INTERVAL = 1000*60*60*24;
    private TaskWatcherService mContext;
    private final  String mMarkDate = "MarkDate";
    private final  String mNoMarkDate = "NoMarkDate";

    private final  String mMarkGender = "MarkGender";
    private final  int mNoMarkGender = -1;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public TaskWatcherService() {
        super("TaskWatcherService");
        mContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.logv(TAG, "TaskWatcherService : onCreate");

    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    private void setRepeatingAlarm(DataItem dataItem){
        long firstime= SystemClock.elapsedRealtime();
        AlarmManager alarm=(AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(TaskWatcherService.this,AsusAlarmReceiver.class);
        intent.setAction(AsusAlarmReceiver.ACTION_SENDEVENT);
        intent.putExtra(AsusAlarmReceiver.SummerSteps, dataItem.summerSteps);
        intent.putExtra(AsusAlarmReceiver.TotalMillis,dataItem.totalMillis);
        intent.putExtra(AsusAlarmReceiver.SummerRunsfrequency,dataItem.summerRunsfrequency);
        intent.putExtra(AsusAlarmReceiver.SummerPushUpfrequency, dataItem.summerPushUpfrequency);
        intent.putExtra(AsusAlarmReceiver.SummerSitUpfrequency,dataItem.summerSitUpfrequency);
        PendingIntent sender = PendingIntent.getBroadcast(TaskWatcherService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, firstime, INTERVAL, sender);
        Log.i("ga", "Total Steps "+ dataItem.summerSteps + " Total Sleep time: " + dataItem.totalMillis + " Total Runs: " + dataItem.summerRunsfrequency
                + " Total PushUp : " +  dataItem.summerPushUpfrequency + " Total Situp: " + dataItem.summerSitUpfrequency);
    }

    private void cancelRepeatingAlarm(){
        try {
            AlarmManager alarm=(AlarmManager)getSystemService(ALARM_SERVICE);
            Intent intent = new Intent();
            intent.setAction(AsusAlarmReceiver.ACTION_SENDEVENT);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

            alarm.cancel(sender);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
  /*  @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.logv(TAG, "TaskWatcherService : onStartCommand");

        return START_STICKY;
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.logv(TAG, "TaskWatcherService : onDestroy");

        //AnalyticsSettings.unregisterContentObserver(this, mObserver);

        //unregisterReceiver(mBroadcastReceiver);
        //this.unregisterReceiver(mActivityIdleReceiver);
//		cancelRepeatingAlarm();
    }

   /* @Override
    public IBinder onBind(Intent intent) {
        MyLog.logv(TAG, "TaskWatcherService : onBind");
        return null;
    }*/

    @Override
    protected void onHandleIntent(Intent intent) {
        MyLog.logv(TAG, "TaskWatcherService : onHandleIntent");

        if (!isMarkGADataGenderUploaded()){
            int gender = markGADataGenderUploaded();
            GACategory.setSelectGender(gender);
            GAHelper.getInstance(getApplicationContext()).sendEvent(GACategory.CategoryProfile, GACategory.getActionSex(),GACategory.LabelGender,-1);
        }

        if (isGADataUploaded()){
            return;
        }
        try{
            Thread.sleep(5000);
        }catch (Exception e){}

        ParseDataManager parseDataManager = ParseDataManager.getInstance();
        ParseDataManager.Day day = parseDataManager.getDayBefore(System.currentTimeMillis());
        SleepData sleepData = SleepHelper.getSleepQualityByDate(day.startTimeMilli);
        DataItem dataItem = new DataItem();
        if (sleepData != null && sleepData.getDataList().size() > 0) {
            SleepTimeSpan total = sleepData.getTotalSleepTime();
            long totalMillis = total.getTotalMinutes() * 60 * 1000;
            //GAHelper.getInstance(getApplicationContext()).sendTiming(GACategory.CategorySleep, totalMillis, GACategory.getAVGSleepTime(), "SleepTime");
            dataItem.totalMillis = totalMillis;
        }

        ArrayList<ParseDataManager.StepInfo> mArrayStepInfo = parseDataManager.getDayStepInfo(getApplicationContext(), day);
        long summerSteps = 0;
        if (mArrayStepInfo != null) {
            for (ParseDataManager.StepInfo stepInfo : mArrayStepInfo) {
                //time+=(stepInfo.endTime-stepInfo.startTime);
                summerSteps += stepInfo.stepCount;
            }
        }
        dataItem.summerSteps = summerSteps;

        long summerRunsfrequency = 0;
        long summerPushUpfrequency = 0;
        long summerSitUpfrequency = 0;
        ArrayList<ParseDataManager.WorkoutInfo> mArrayRunInfo = parseDataManager.getDayRunInfo(getApplicationContext(), day);
        if (mArrayRunInfo != null) {
            for (ParseDataManager.WorkoutInfo info : mArrayRunInfo) {
                if (info.type == CoachTable.TYPE_RUN) {
                    summerRunsfrequency ++;
                }
            }
        }

        ArrayList<ParseDataManager.WorkoutInfo> mArrayWorkoutInfo = parseDataManager.getDayWorkoutInfo(getApplicationContext(),day.startTimeMilli,day.endTimeMilli);
        if (mArrayWorkoutInfo != null){
            for (ParseDataManager.WorkoutInfo info : mArrayWorkoutInfo) {
                if (info.type == CoachTable.TYPE_PUSHUP) {
                    summerPushUpfrequency ++;
                } else if (info.type == CoachTable.TYPE_SITUP) {
                    summerSitUpfrequency ++;
                }

            }
        }
        dataItem.summerRunsfrequency = summerRunsfrequency;
        dataItem.summerPushUpfrequency = summerPushUpfrequency;
        dataItem.summerSitUpfrequency = summerSitUpfrequency;

        setRepeatingAlarm(dataItem);
        markGADataUploaded();
    }

    private void markGADataUploaded() {
        ParseDataManager parseDataManager = ParseDataManager.getInstance();
        ParseDataManager.Day todayBefore = parseDataManager.getDayBefore(System.currentTimeMillis());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=sp.edit();
        String dayStr = "" + todayBefore.year + todayBefore.month + todayBefore.day;
        Log.i("ga", "mark date uploaded : " + dayStr);
        editor.putString(mMarkDate, dayStr.trim());
        editor.commit();
    }

    private boolean isGADataUploaded() {
        boolean result = false;
        ParseDataManager parseDataManager = ParseDataManager.getInstance();
        ParseDataManager.Day todayBefore = parseDataManager.getDayBefore(System.currentTimeMillis());
        String dayStr = "" + todayBefore.year + todayBefore.month + todayBefore.day;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String markDate = sp.getString(mMarkDate,mNoMarkDate);

        if (markDate.equals(dayStr.trim())){
            result = true;
        }
        else if (markDate.equals(mMarkDate)){
            result = false;
        }
        Log.i("ga", "Date: " + dayStr + " isGADataUploaded: " + result);
        return  result;
    }

    private int markGADataGenderUploaded() {
        ParseDataManager parseDataManager = ParseDataManager.getInstance();
        ParseDataManager.ProfileData profileData = parseDataManager.getProfileData(this);
       int gender =  profileData.gender;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor= sp.edit();
        Log.i("ga", "mark gender : " + gender);
        editor.putInt(mMarkGender, gender);
        editor.commit();
        return  gender;
    }

    private boolean isMarkGADataGenderUploaded() {
        boolean result = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=sp.edit();
        int gender =  sp.getInt(mMarkGender, mNoMarkGender);

        if (gender == -1){
            result = false;
        }else{
            result =  true;
        }
        Log.i("ga", "gender : " + gender + " isMarkGADataGenderUploaded: " + result);
        return result;

    }


}

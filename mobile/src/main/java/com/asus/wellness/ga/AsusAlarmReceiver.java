package com.asus.wellness.ga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by larrylf_lin on 2015/12/7.
 */
public class AsusAlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_SENDEVENT = "asus_sendEvent";
    private static final int MODEL = 1;
    private   String CATEGORY = "User";
    private   String ACTION = "send";
    private   String LABEL = "label";
    private   String TAG = "ga";
    public final static String TotalMillis = "TotalMillis";
    public final static String SummerSteps = "SummerSteps";
    public final static String SummerRunsfrequency = "SummerRunsfrequency";
    public final static String SummerPushUpfrequency = "SummerPushUpfrequency";
    public final static String SummerSitUpfrequency = "SummerSitUpfrequency";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equalsIgnoreCase(ACTION_SENDEVENT)) {
            Log.i(TAG, "asus sent event");

            long totalMillis = intent.getLongExtra(AsusAlarmReceiver.TotalMillis, 0);
            //if (totalMillis != 0){
                GAHelper.getInstance(context).sendTiming(GACategory.CategorySleep, totalMillis, GACategory.getAVGSleepTime(), "SleepTime");
            //}

            long summerSteps = intent.getLongExtra(AsusAlarmReceiver.SummerSteps,0);
            //if (summerSteps != 0){
                GAHelper.getInstance(context).sendEvent(GACategory.CategoryUserSteps, GACategory.getActionAvgStep(), GACategory.LabelSteps, summerSteps);

            //};
            long summerRunsfrequency =  intent.getLongExtra(AsusAlarmReceiver.SummerRunsfrequency,0);;
            long summerPushUpfrequency = intent.getLongExtra(AsusAlarmReceiver.SummerPushUpfrequency,0);
            long summerSitUpfrequency = intent.getLongExtra(AsusAlarmReceiver.SummerSitUpfrequency,0);;

            //if (summerRunsfrequency != 0){
                GAHelper.getInstance(context).sendEvent(GACategory.CategoryWorkoutCoach, GACategory.getActionRun(), GACategory.LabelRun, summerRunsfrequency);
            //}
            //if (summerPushUpfrequency != 0){
                GAHelper.getInstance(context).sendEvent(GACategory.CategoryWorkoutCoach, GACategory.getActionPushUp(), GACategory.LabelPushUp, summerPushUpfrequency);
            //}

            //if (summerSitUpfrequency != 0){
                GAHelper.getInstance(context).sendEvent(GACategory.CategoryWorkoutCoach, GACategory.getActionSitUp(), GACategory.LabelSitup, summerSitUpfrequency);
            //}
            Log.i(TAG, "Total Sleep Time:" + totalMillis + " Total Steps: " + summerSteps +
                    " Total SitUp: " + summerSitUpfrequency + " Total Runs: " + summerRunsfrequency + " Total PushUp: " + summerPushUpfrequency);

        }
    }

}
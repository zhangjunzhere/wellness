package com.asus.wellness.ga;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.asus.wellness.R;
import com.asus.wellness.utils.GAApplication;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Date;

/**
 * Created by larrylf_lin on 2015/12/3.
 */
public class GAHelper {

    public static boolean sEnableTracker = true;// !Build.TYPE.equals("user");
    private static final String DEFAULT_GA_ID = Build.TYPE.equals("user")? "UA-61982112-1": "UA-61982112-2";
    private static String GA_ID = DEFAULT_GA_ID;
    public static final boolean isMonkey = AsusSystemProperties.getBoolean("debug.monkey", false);
    private static final boolean isDebug =AsusSystemProperties.getInt("ro.debuggable", 0) == 1;
    public static final boolean isUser = Build.TYPE.equals("user");
    public static final boolean isWiFiOnly = "wifi-only".equals(AsusSystemProperties.get("ro.carrier"));
    private static Tracker mGaTracker;
    private static GoogleAnalytics mGaInstance;
    private Date beginTime;
    private Context context;

    private static GAHelper _instance = null;

    public GAHelper(Context context) {
        this.context = context;
    }

    public static synchronized GAHelper getInstance(Context context) {
       // EasyTracker.getInstance().setContext(context);
        if (_instance == null) {
            _instance = new GAHelper(context);
            _instance.Init();
        }
        return _instance;
    }

    public Tracker getTracker() {
        return mGaTracker;
    }

    private void Init() {
        // Get the GoogleAnalytics singleton. Note that the SDK uses
        // the application context to avoid leaking the current context.
        if (mGaInstance == null)
            mGaInstance = GoogleAnalytics.getInstance(context);

        // Use the GoogleAnalytics singleton to get a Tracker.
        if (mGaTracker == null)
            mGaTracker = GAApplication.getInstance().getTracker(context);


    }

    /*public void sendException(Exception e) {
        mGaTracker.sendException(context.getClass().getName(), e, false);
    }*/

    String contextName() {
        return context.getClass().getName();
    }

    public void sendEvent(String category, String action, String label,
                          long value) {

        if (!isMonkey && sEnableTracker){
            Log.i("ga","sendEvent action: " + action);
            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
            eventBuilder.setCategory(category);
            eventBuilder.setAction(action);
            eventBuilder.setLabel(label);
            if (value != -1){
                eventBuilder.setValue(value);
            }
            mGaTracker.send(eventBuilder.build());
        }
    }


    public void sendTiming(String category, long loadtime, String name,
                           String label) {
       //mGaTracker.sendTiming(category, loadtime, name, label);
        if (!isMonkey && sEnableTracker){
            mGaTracker.send(new HitBuilders.TimingBuilder()
                    .setCategory(category)
                    .setValue(loadtime)
                    .setVariable(name)
                    .setLabel(label)
                    .build());
        }


    }

   public void setCustomDimension(int index, String value) {
       // mGaTracker.setCustomDimension(index, value);
       // mGaTracker.sendView();
       if (!isMonkey && sEnableTracker){
           mGaTracker.send(new HitBuilders.ScreenViewBuilder()
                   .setCustomDimension(1, "premiumUser")
                   .build());
       }
    }

    public void setCustomMetric(int index, long value) {
        //mGaTracker.setCustomMetric(index, value);
        //mGaTracker.sendView();
        if (!isMonkey && sEnableTracker){
            mGaTracker.send(new HitBuilders.ScreenViewBuilder()
                    .setCustomDimension(1, "premiumUser")
                    .build());
        }
    }

    /*public void sendTransaction(Transaction trans) {
        mGaTracker.sendTransaction(trans);
    }*/

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    private long timeSpan() {
        Date curTime = new Date();
        long span = curTime.getTime() - beginTime.getTime();
        return span;
    }

}

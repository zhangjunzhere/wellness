package com.asus.wellness.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.asus.wellness.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Kim_Bai on 4/20/2015.
 */
public class GAApplication {
    private Tracker mTracker;
    private static GAApplication mGAApplication;
    private GAApplication() {
    }

    public static GAApplication getInstance() {
        if(mGAApplication == null) {
            mGAApplication = new GAApplication();
        }

        return mGAApplication;
    }

    synchronized public Tracker getTracker( Context context) {
        if(mTracker == null) {
            Log.i("ga","user: " +  Build.TYPE.equals("user"));
           /* mTracker =   Build.TYPE.equals("user") ?  GoogleAnalytics.getInstance(context).newTracker(R.xml.app_tracker):
                    GoogleAnalytics.getInstance(context).newTracker(R.xml.app_tracker2);*/
            mTracker =   GoogleAnalytics.getInstance(context).newTracker(R.xml.app_tracker);
            mTracker.enableAdvertisingIdCollection(true);
        }
        return mTracker;
    }
}

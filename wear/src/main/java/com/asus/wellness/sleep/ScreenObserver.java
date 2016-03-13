package com.asus.wellness.sleep;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

public class ScreenObserver {
    private Context mContext;
    private ScreenBroadcastReceiver mScreenReceiver;
    private ScreenStateListener mScreenStateListener;
    private static Method mReflectScreenState;  
    
    public ScreenObserver(Context context) {
        if (context == null){
            Log.i("larry","Sleep getActivity null error.");
        }
        mContext = context;
        mScreenReceiver = new ScreenBroadcastReceiver();
        try {
            mReflectScreenState = PowerManager.class.getMethod("isScreenOn",
                    new Class[] {});
        } catch (NoSuchMethodException nsme) {
        }
    }


    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenStateListener.onScreenOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenStateListener.onScreenOff();
            }
        }
    }


    public void requestScreenStateUpdate(ScreenStateListener listener) {
        mScreenStateListener = listener;
        startScreenBroadcastReceiver();

        firstGetScreenState();
    }


    private void firstGetScreenState() {
        if (mContext == null){
            return;
        }
        PowerManager manager = (PowerManager) mContext
                .getSystemService(Activity.POWER_SERVICE);
        if (isScreenOn(manager)) {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOn();
            }
        } else {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOff();
            }
        }
    }

  
    public void stopScreenStateUpdate() {
        if (mContext != null){
            mContext.unregisterReceiver(mScreenReceiver);
        }

    }


    private void startScreenBroadcastReceiver() {
        if (mContext != null){
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mContext.registerReceiver(mScreenReceiver, filter);
        }

    }


    private static boolean isScreenOn(PowerManager pm) {
        boolean screenState;
        try {
            screenState = (Boolean) mReflectScreenState.invoke(pm);
        } catch (Exception e) {
            screenState = false;
        }
        return screenState;
    }

    public interface ScreenStateListener {
        public void onScreenOn();
        public void onScreenOff();
    }

}

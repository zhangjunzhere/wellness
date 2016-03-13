package com.asus.wellness.ui;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Kim_Bai on 1/5/2015.
 */
public class StepGoalTableObserver extends ContentObserver {
    private Handler mHandler;
    public static int SEND_NEXT_GOAL = 001;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public StepGoalTableObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        sendNextGOAL();
    }

    private void sendNextGOAL() {
        Message msg = Message.obtain();
        msg.what = SEND_NEXT_GOAL;
        mHandler.sendMessage(msg);
    }
}

package com.asus.wellness.notification;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Kim_Bai on 1/5/2015.
 */
public class ProfileTableObserver extends ContentObserver{
    private Handler mHandler;
    public static int PROFILE_CHANGE = 001;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public ProfileTableObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        sendProfileChange();
    }

    private void sendProfileChange() {
        Message msg = Message.obtain();
        msg.what = PROFILE_CHANGE;
        mHandler.sendMessage(msg);
    }
}

package com.asus.commonui.shareactionwidget;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Shared singleton background thread for each process.
 */
public final class BackgroundThread extends HandlerThread {
    private static BackgroundThread sInstance;
    private static Handler sHandler;

    private BackgroundThread() {
        super("asus_commonui.bg", android.os.Process.THREAD_PRIORITY_BACKGROUND);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new BackgroundThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static BackgroundThread get() {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }
}

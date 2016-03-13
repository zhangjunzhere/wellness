
package com.asus.wellness.ga;



//import android.os.SystemProperties;
import android.util.Log;

public class MyLog {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final int LOGV = 0x01;
    private static final int LOGD = 0x02;
    private static final int LOGI = 0x04;
    private static final int LOGW = 0x08;
    private static final int LOGE = 0x10;
    private static int DEBUG;
     
    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    static {
        if (AsusSystemProperties.getInt("ro.debuggable", 0) == 1) {
            DEBUG = LOGE | LOGW | LOGI | LOGD | LOGV;
        } else {
            DEBUG = LOGE | LOGW | LOGI | LOGD;
        }
    }

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------
    public static final boolean onMonkeyTest() {
        return AsusSystemProperties.getBoolean("debug.monkey", false);
    }

    public static final void logv(String tag, String msg) {
        //Log.i("ga","Debug: " + DEBUG + " LOGV: " + LOGV);
        if ((DEBUG & LOGV) != 0) {
            Log.v(tag, msg);
        }
    }

    public static final void logd(String tag, String msg) {
        if ((DEBUG & LOGD) != 0) {
            Log.d(tag, msg);
        }
    }

    public static final void logi(String tag, String msg) {
        if ((DEBUG & LOGI) != 0) {
            Log.d(tag, msg);
        }
    }

    public static final void logw(String tag, String msg) {
        if ((DEBUG & LOGW) != 0) {
            Log.w(tag, msg);
        }
    }

    public static final void loge(String tag, String msg) {
        if ((DEBUG & LOGE) != 0) {
            Log.e(tag, msg);
        }
    }
}

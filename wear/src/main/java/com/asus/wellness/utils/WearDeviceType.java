package com.asus.wellness.utils;

import android.os.Build;

/**
 * Created by smile_gao on 2015/7/1.
 */
public class WearDeviceType {
    public static String MsgHeader = "/wear-device-type";
    public static String Robin ="ASUS ZenWatch";
    public static String Sparrow ="ASUS ZenWatch 2";
    public static Boolean  isRobin()
    {
        return !Build.MODEL.contains(WearDeviceType.Sparrow);
    }
}

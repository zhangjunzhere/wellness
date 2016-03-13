package com.asus.wellness.utils;

/**
 * Created by smile_gao on 2015/7/1.
 */
public class WearDeviceType {
    public static String MsgHeader = "/wear-device-type";
    public static String Robin ="ASUS ZENWATCH";
    public static String Sparrow ="ASUS ZENWATCH 2";
    private String mWatchType = Robin;

    public WearDeviceType(Boolean isrobin)
    {
        mWatchType = isrobin ? Robin : Sparrow;
    }
    public WearDeviceType(String watchtype)
    {
        if(watchtype.equals(Robin) || watchtype.equals(Sparrow))
                 mWatchType = watchtype;
    }
    public String getWatchType()
    {
        return  mWatchType;
    }
    public Boolean isRobin()
    {
        return mWatchType.equals(Robin) ? true: false;
    }
}

package com.asus.sharedata;

/**
 * Created by smile_gao on 2015/10/20.
 */
public class SleepTimeSpan {
    public long hour;
    public long min;
    public SleepTimeSpan(long h, long m)
    {
        hour = h;
        min = m;
    }
    public int getTotalMinutes()
    {
        return (int)(hour*60+min);
    }

    public SleepTimeSpan subTimeSpan(SleepTimeSpan sts)
    {
        int ret = getTotalMinutes() - sts.getTotalMinutes();
        if(ret <=0)
        {
            return new SleepTimeSpan(0,0);
        }
        int hour = ret/60;
        int min = ret%60;
        return new SleepTimeSpan(hour,min);
    }
}

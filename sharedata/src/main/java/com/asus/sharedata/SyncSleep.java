package com.asus.sharedata;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/9/30.
 */
public class SyncSleep {
    private static final String TAG  = "SyncSleep";
    public static final String PATH_MOBILE ="/sleep_notification_phone";
    public static final String PATH_WEAR ="/sleep_notification_phone";
    public static final String SHOW ="show";
    public static final String CANCEL ="cancel";
    public static long SleepType=1001;

    public static final String DATE_FORMAT  = "yyyy-MM-dd";
    public static final String SEPERATOR  =  "," ;

    public  final static int AWAKE = 0;
    public final static int LIGHT = 1;
    public final static int DEEP = 2;

    public static class SleepInfo{
        public long start;
        public long end;
        public int duration_total = 0;
        public int duration_awake = 0;
        public int duration_deep = 0;
        public int duration_light = 0;
        public List<Integer> sleepQuality = new ArrayList<Integer>();
    }

    public static SleepInfo getSleepInfo(String data, long start, long end){
        SleepInfo sleepInfo = new SleepInfo();
        String[] records = data.split(SyncSleep.SEPERATOR);
        if(records.length <= 0){
            Log.e(TAG, "getSleepInfo size <= 0");
            return sleepInfo;
        }
        sleepInfo.start = start;
        sleepInfo.end = end;
        final int ONE_MINUTE_MILLIES = 1*60*1000;
        sleepInfo.duration_total = (int)(end/ONE_MINUTE_MILLIES) -(int)(start/ONE_MINUTE_MILLIES);

        for(String item: records){
            int value = Integer.parseInt(item);
            sleepInfo.sleepQuality.add(value);
            if(value == AWAKE) {
                sleepInfo.duration_awake ++;
            }else if(value == LIGHT) {
                sleepInfo.duration_light ++;
            }else {
                sleepInfo.duration_deep ++;
            }
        }

        sleepInfo.duration_deep =(int) ((float)(sleepInfo.duration_deep)/records.length * sleepInfo.duration_total);
        sleepInfo.duration_light =(int) ((float)(sleepInfo.duration_light)/records.length * sleepInfo.duration_total);
        sleepInfo.duration_awake = sleepInfo.duration_total - sleepInfo.duration_deep - sleepInfo.duration_light;
        return sleepInfo;
    }

}

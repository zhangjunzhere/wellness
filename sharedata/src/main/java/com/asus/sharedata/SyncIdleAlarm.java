package com.asus.sharedata;

/**
 * Created by smile_gao on 2015/8/27.
 */
public class SyncIdleAlarm {
   // public static int DEFAULT_TIME_INTERVAL = 3600000;  // fix viberating always bug.
    public static final String IDLE_ALARM_PATH="/idle_alarm_path";
    public static final String IDLE_ALARM_FROM_WEAR_PATH="/idle_alarm_from_wear_path";


    public static final String KEY_IDLE_ALARM_SWITCH="key_idle_alarm_switch";
    public static final String KEY_IDLE_ALARM_INTERVAL="key_idle_alarm_interval";
    public static final String KEY_IDLE_ALARM_HOUR_OF_DAY_FROM="key_idle_alarm_hour_of_day_from";
    public static final String KEY_IDLE_ALARM_MINUTE_FROM="key_idle_alarm_minute_from";
    public static final String KEY_IDLE_ALARM_HOUR_OF_DAY_TO="key_idle_alarm_hour_of_day_to";
    public static final String KEY_IDLE_ALARM_MINUTE_TO="key_idle_alarm_minute_to";

    public static final int DEFAULT_HOUR_OF_DAY_FROM = 8;
    public static final int DEFAULT_MINUTE_FROM = 0;
    public static final int DEFAULT_HOUR_OF_DAY_TO = 20;
    public static final int DEFAULT_MINUTE_TO = 0;

    public static final int DEFAULT_IDLE_ALARM_INTERVAL=60*60*1000;
    public static final int DEFAULT_IDLE_ALARM_HOUR_OF_DAY_FROM=8;
    public static final int DEFAULT_IDLE_ALARM_MINUTE_FROM=0;
    public static final int DEFAULT_IDLE_ALARM_HOUR_OF_DAY_TO=20;
    public static final int DEFAULT_IDLE_ALARM_MINUTE_TO=0;
}

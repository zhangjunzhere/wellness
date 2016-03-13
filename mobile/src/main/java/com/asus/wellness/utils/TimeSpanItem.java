package com.asus.wellness.utils;

/**
 * Created by smile_gao on 2015/6/25.
 */
public class TimeSpanItem {
    private int hour = 0;
    private int minute=0;
    public TimeSpanItem()
    {
    }
    public TimeSpanItem(int h, int m)
    {
        hour = h;
        minute = m;
    }
    public void addTime(TimeSpanItem ti)
    {
        hour += ti.getHour();
        minute += ti.getMinute();
    }
    public int getTotalInMinutes()
    {
        return  getHour()*60 + getMinute();
    }
    public int getHour()
    {
        return  hour;
    }
    public int getMinute() {
        return minute;
    }
    public  static TimeSpanItem creatTimeItem(long startTime, long endTime)
    {
        int startHour=Integer.valueOf(Utility.getDateTime(startTime, "HH"));
        int startMin=Integer.valueOf(Utility.getDateTime(startTime, "mm"));
        int endHour=Integer.valueOf(Utility.getDateTime(endTime, "HH"));
        int endMin=Integer.valueOf(Utility.getDateTime(endTime, "mm"));
        int totalMinStart=startHour*60+startMin;
        int totalMinEnd=endHour*60+endMin;
        int interval=totalMinEnd-totalMinStart;
        int intervalHour=interval/60;
        int intervalMin=interval%60;

//        String hourString="";
//        if(intervalHour>=1){
//            if(intervalHour==1){
//                hourString=intervalHour+mContext.getString(R.string.daily_info_time_hour);
//            }
//            else{
//                hourString=intervalHour+mContext.getString(R.string.daily_info_time_hours);
//            }
//        }

        String minString="";
        if(intervalMin < 1){
            intervalMin = 1;
        }
        return  new TimeSpanItem(intervalHour,intervalMin);
    }
}

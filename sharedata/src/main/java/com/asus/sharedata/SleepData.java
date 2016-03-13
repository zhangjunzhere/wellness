package com.asus.sharedata;

import android.util.Log;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by smile_gao on 2015/10/14.
 */
public class SleepData {
    private long mStartTime;
    private long mEndTime;
    private long mFirstSleepOnTime;
    private long mLastSleepOnTime;
    private List<Integer> sleepdataList;
    int ONE_MINUTE_MILLIES = 1*60*1000;
    private int mWokeupTimes;
    private long mDeepSleepTimeSpan;
    private long mLightSleepTimeSpan;

    public SleepData(List<Integer> datas,long startTime,long endTime,long firstSleeptime,long lastSleeptime)
    {
        mStartTime = startTime;
        mEndTime = endTime;
        sleepdataList = datas;
        mFirstSleepOnTime = firstSleeptime;
        mLastSleepOnTime = lastSleeptime;
    }
    public void setWokeUpTimes(int times)
    {
        mWokeupTimes = times;
    }

    public void setDeepSleepTimeSpan(long times)
    {
        mDeepSleepTimeSpan = times;
    }

    public void setLightSleepTimeSpan(long times)
    {
        mLightSleepTimeSpan = times;
    }

    public long getStartTime()
    {
        return  mStartTime;
    }
    public  long getEndTime()
    {
        return mEndTime;
    }
    public long getFirstSleepTime() { return  mFirstSleepOnTime;}

    public long getLastSleepTime() { return  mLastSleepOnTime;}


    public List<Integer> getDataList()
    {
        return sleepdataList;
    }

    public SleepTimeSpan getLatency()
    {
        if(mFirstSleepOnTime == 0)
        {
            return  getTimeSpan(mStartTime,mEndTime);
        }
        return  getTimeSpan(mStartTime,mFirstSleepOnTime);
    }
    public SleepTimeSpan getTotalSleepTime()
    {
        return getTimeSpan(mStartTime, mEndTime);
    }
    public SleepTimeSpan getSleepDurationTime()
    {
        return getTimeSpan(mFirstSleepOnTime, mLastSleepOnTime);
    }
    public SleepTimeSpan getTotalDeepSleepTime()
    {
        return getTimeSpan(mDeepSleepTimeSpan/ONE_MINUTE_MILLIES);
    }
    public SleepTimeSpan getTotalLightSleepTime()
    {
          return  getTimeSpan(mLightSleepTimeSpan/ONE_MINUTE_MILLIES);
    }
    public int getWokeupTimes()
    {
        return  mWokeupTimes;
    }
    public int getQuality()
    {
//        NumberFormat nt = NumberFormat.getPercentInstance();
//        nt.setMinimumFractionDigits(0);
//        float t1 =  mEndTime-mStartTime<=0? 0: mDeepSleepTimeSpan/1.0f/(mEndTime-mStartTime);
//        String ret =  nt.format(t1);
//        return ret;
        int sleeplatencyScore = getSleepLatencyScore();
        int sleepDurationScore = getSleepDurationScore();
        int sleepEfficiencyScore= getSleepEfficiencyScore();
        int sleepDeepsleepRatioScore = getDeepSleepRatioScore();
        int nightTimeWakeScore = getNightTimeAwakeningScore();
        int sum = sleeplatencyScore + sleepDurationScore+ sleepEfficiencyScore+ sleepDeepsleepRatioScore+ nightTimeWakeScore;
        sum = sum *4;
        return  sum;
    }
    private int getNightTimeAwakeningScore()
    {
        int times = getWokeupTimes();
        int score = 1;
        if(times>6)
        {
            score =1 ;
        }
        else if(times>=5 && times<=6)
        {
            score =2;
        }
        else if(times>=3 && times<=4)
        {
            score =3;
        }
        else if(times>=1 && times<=2)
        {
            score =4;
        }
        else if(times==0)
        {
            score =5;
        }
        return  score;
    }
    private int getDeepSleepRatioScore()
    {
        float t1 =  mLastSleepOnTime-mFirstSleepOnTime<=0? 0: mDeepSleepTimeSpan/1.0f/(mLastSleepOnTime-mFirstSleepOnTime);
        t1 = t1*100;
        int score = 1;
        if(t1<15)
        {
            score =1 ;
        }
        else  if(t1>=15 && t1<20)
        {
            score = 2 ;
        }
        else  if(t1>=20 && t1<25)
        {
            score = 3 ;
        }
        else  if(t1>=25 && t1<30)
        {
            score = 4 ;
        }
        else  if(t1>=30)
        {
            score = 5 ;
        }
        return  score;
    }
    private int getSleepEfficiencyScore()
    {
        SleepTimeSpan timeSpan = getSleepDurationTime();
        SleepTimeSpan totalTimeSpan = getTotalSleepTime();
        float percent = timeSpan.getTotalMinutes()/1.0f/totalTimeSpan.getTotalMinutes()*100;
        int score =1;
        if(percent<80)
        {
            score =  1;
        }
        else  if(percent>=80 && percent<85)
        {
            score =  2;
        }
        else   if(percent>=85 && percent<90)
        {
            score =  3;
        }
        else   if(percent>=90 && percent<95)
        {
            score =  4;
        }
        else if(percent>=95 && percent<100)
        {
            score =  5;
        }
        return  score;
    }
    private int getSleepDurationScore()
    {
        SleepTimeSpan timeSpan = getSleepDurationTime();
        int score =1;
        if(timeSpan.hour<4 || timeSpan.hour>=11)
        {
            score =  1;
        }
        else  if((timeSpan.hour>=4 && timeSpan.hour<5) || (timeSpan.hour>=10 && timeSpan.hour<11))
        {
            score =  2;
        }
        else  if((timeSpan.hour>=5 && timeSpan.hour<6) || (timeSpan.hour>=9 && timeSpan.hour<10))
        {
            score =  3;
        }
        else  if((timeSpan.hour>=6 && timeSpan.hour<7) || (timeSpan.hour>=8 && timeSpan.hour<9))
        {
            score =  4;
        }
        else  if((timeSpan.hour>=7 && timeSpan.hour<8))
        {
            score =  5;
        }
        return  score;
    }
    private int getSleepLatencyScore()
    {
        SleepTimeSpan timeSpan = getLatency();
        if(timeSpan.hour>0)
        {
            return  1;
        }
        int score =1;
        if(timeSpan.min>42.5)
        {
            score = 1;
        }
        else if(timeSpan.min>28.9 && timeSpan.min<=42.5)
        {
            score = 2;
        }
        else if(timeSpan.min>15.3 && timeSpan.min<=28.9)
        {
            score = 3;
        }
        else if(timeSpan.min>1.7 && timeSpan.min<=15.3)
        {
            score = 4;
        }
        else if(timeSpan.min <1.7)
        {
            score = 5;
        }
        return  score;
    }

    public SleepTimeSpan getTimeSpan(long begin, long end)
    {

        int l =  (int)(end/ONE_MINUTE_MILLIES) - (int)(begin/ONE_MINUTE_MILLIES);
      //  Log.i("smile","getTimeSpan: "+l);
        if(l<=0)
        {
            return  new SleepTimeSpan(0,0);
        }
        return  getTimeSpan(l);
    }
    public SleepTimeSpan getTimeSpan(long timespan)
    {
        int l =  (int)timespan;
        int day=l/(24*60);
        int hour=(l/60-day*24);
        int min=(l-day*24*60-hour*60);
        //  long s=(l/1000-day*24*60*60-hour*60*60-min*60);
        SleepTimeSpan ts = new SleepTimeSpan(hour,min);
        return  ts;
    }

    public long getSleepTimeMins(SleepTimeSpan span){
        return span.hour*60+ span.min;
    }

}

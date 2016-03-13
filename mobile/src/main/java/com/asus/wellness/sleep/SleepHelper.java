package com.asus.wellness.sleep;

import android.net.Uri;
import android.util.Log;

import com.asus.sharedata.SleepData;
import com.asus.sharedata.SyncSleep;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.dbhelper.Sleep;
import com.asus.wellness.dbhelper.SleepDao;
import com.asus.wellness.provider.WellnessProvider;
import com.asus.wellness.sync.DataEventResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/10/14.
 */
public class SleepHelper {

    public  final static int AWAKE = 0;
    public final static int LIGHT = 1;
    public final static int DEEP = 2;
    public static  Uri SLEEP_TABLE_URI = Uri.parse("content://" + WellnessProvider.AUTHORITY + "/" + "sleep");

    public static SleepData getSleepQualityByDate(Long timeMillis){
        final float AWAKE_F = 0.7f;
        final float SLEEP_F = 1.5f;
        long span = 2*60*1000;
        Device device = WApplication.getInstance().getConnectedDevice();
        if(device==null || device.getId()==null)
        {
            Log.i("smile","SleepData device =null or id =null");
            return  null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(SyncSleep.DATE_FORMAT);
        String todaydate = formatter.format(timeMillis);
        SleepDao sleepDao = WApplication.getInstance().getDataHelper().getDaoSession().getSleepDao();
        List<Sleep> sleepList = sleepDao.queryBuilder().where(SleepDao.Properties.Date.eq(todaydate),SleepDao.Properties.DeviceId.eq(device.getId())).orderDesc(SleepDao.Properties.Start).list();
        if(sleepList.size() > 0){

            List<Integer> result = new ArrayList<Integer>();
            Sleep firstSleepItem = sleepList.get(0);
            String[] datastrs = firstSleepItem.getData().split(",");
            if(datastrs==null || datastrs.length==0)
            {
                Log.i("smile","sleep getData == null");
                return null;
            }
            int spanNum = datastrs==null? 1: datastrs.length;
            if(spanNum == 0)
            {
                spanNum = 1;
            }
            long totalTime = firstSleepItem.getEnd() - firstSleepItem.getStart();
            span = totalTime/spanNum;
            long firstSleepOnTime = 0;
            long lastSleepOnTime = 0;
            int wokeuptimes=0;
            boolean isWake=false;
            long deepSleepTime=0;
            long lightSleepTime=0;
            int deepCount=0;
            int lightCount=0;
            boolean isFirstItem = true;
            for(int i = 0; i < datastrs.length; i++ ) {

                int qulitity = AWAKE;
                try {
                    qulitity = Integer.parseInt(datastrs[i]);
                } catch (Exception e) {
                    Log.i("smile", "sleep parseInt exception: " + datastrs[i]);
                    qulitity = AWAKE;
                }
                if (qulitity > 0) {
                    lastSleepOnTime = firstSleepItem.getStart() + span * (i + 1);
                    if (isFirstItem) {
                        firstSleepOnTime = firstSleepItem.getStart() + span * i;
                        isFirstItem = false;
                    }
                }

               if(qulitity ==  DEEP){
                    deepCount++;
                    isWake =false;
                }else if(qulitity ==  LIGHT){
                    lightCount++;
                    isWake =false;
                } else if(qulitity==AWAKE){
                    if(!isFirstItem && !isWake)
                    {
                        wokeuptimes++;
                    }
                    isWake= true;
                }
                // remove last wakeup times
                if(i==datastrs.length-1 && qulitity==AWAKE)
                {
                    if(wokeuptimes>0)
                    {
                        wokeuptimes--;
                    }
                }
                result.add(qulitity);
            }
            Log.i("smile", "sleep start: " + firstSleepOnTime + " last: " + lastSleepOnTime);
            if(lastSleepOnTime> firstSleepItem.getEnd())
            {
                lastSleepOnTime = firstSleepItem.getEnd();
            }
            SleepData sd = new SleepData(result,firstSleepItem.getStart(),firstSleepItem.getEnd(),firstSleepOnTime,lastSleepOnTime);
            sd.setWokeUpTimes(wokeuptimes);

            deepSleepTime = (long)(totalTime*1.0*deepCount/spanNum);
            lightSleepTime = (long)(totalTime*1.0*lightCount/spanNum);
            sd.setDeepSleepTimeSpan(deepSleepTime);
            sd.setLightSleepTimeSpan(lightSleepTime);
            return sd;
        }
        else
        {
            return  null;
        }

    }

    public static DataEventResult writeSleepInfo(List<Sleep> sleepList,Long deviceId)
    {
        SleepDao sleepdao = WApplication.getInstance().getDataHelper().getDaoSession().getSleepDao();
        DataEventResult dataEventResult = new DataEventResult();
        List<Sleep> list = new ArrayList<>();

        for(Sleep sleep : sleepList)
        {
            sleep.setDeviceId(deviceId);
            sleep.setId(null);

            if(!isExist(sleep, sleepdao))  //false : insert or update
            {
                list.add(sleep);
            }

        }
        Log.i("smile","SleepHelper list: "+list.size());
        if(list.size()>0) {
            dataEventResult.sleepUpdate = true;
            try {
                sleepdao.insertInTx(list);
            }catch (Exception e)
            {
                Log.i("smile","sleepdao insert fail");
            }
        }



        return dataEventResult;
    }
    public static boolean isExist(Sleep sleep,SleepDao sleepDao)
    {
        List<Sleep> sleepList =   sleepDao.queryBuilder().where(SleepDao.Properties.DeviceId.eq(sleep.getDeviceId()),
//                SleepDao.Properties.Start.eq(sleep.getStart()),
//                SleepDao.Properties.End.eq(sleep.getEnd()),
                SleepDao.Properties.Date.eq(sleep.getDate())).list();
        for(Sleep temp: sleepList)
        {
            if(temp.getStart() == sleep.getStart() && temp.getEnd()==sleep.getEnd() && temp.getData().equals(sleep.getData()))
            {
                return true;
            }
        }
        if(sleepList ==null || sleepList.size() ==0 )
        {
            return  false;
        }

        return  false;
    }
}

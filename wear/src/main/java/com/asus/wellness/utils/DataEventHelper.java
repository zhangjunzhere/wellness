package com.asus.wellness.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.asus.sharedata.ShareUtils;
import com.asus.sharedata.SyncData;
import com.asus.sharedata.SyncIdleAlarm;
import com.asus.sharedata.SyncProfile;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.sync.SyncService;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.List;

/**
 * Created by smile_gao on 2015/8/12.
 */
public class DataEventHelper {

    public static void processIdleAlarmEvent(Context context, DataEvent event)
    {
        DataMap data=DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sp.edit();
        editor.putBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, data.getBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH));
        editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL, data.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL));
        editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_FROM, data.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_FROM));
        editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_FROM, data.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_FROM));
        editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_TO, data.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_TO));
        editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_TO, data.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_TO));
        editor.commit();

        Log.d("circle","idle switch:"+data.getBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH)+" idle interval:"+data.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL) + ", hF = " + data.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_FROM));

        Boolean open = data.getBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH);
        toggleIdleAlarm(context,open);
    }
    public static void toggleIdleAlarm(Context context,Boolean open)
    {
        if(open){
            Intent intent=new Intent(CollectStepCountService.ACTION_ALARM);
            context.sendBroadcast(intent);
        }
        else{
            Intent intent=new Intent(CollectStepCountService.ACTION_CANCEL_ALARM);
            context.sendBroadcast(intent);
        }
    }
    public static void processSyncDb(Context context,DataLayerManager dataLayerManager)
    {
        Log.i("smile","processSyncDb");
        String currentDBPath = Environment.getDataDirectory() + "//data//" + context.getPackageName() + "//databases//asus_wellness.db";
        File currentDB = new File(currentDBPath);
        if(currentDB==null)
        {
            Log.i("smile","backupDb null");
            return;
        }
        try {
            byte[] content = ShareUtils.getBytesByFile(currentDB);
            String versionCode = ShareUtils.getVersionCode(context);
            dataLayerManager.setSyncDb(content,versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void processSyncEvent(Context context , DataEvent event)
    {
        String devicejson = DataMapItem.fromDataItem(event.getDataItem()).getDataMap().getString(SyncData.STEP_COUNT_DEIVCE_NAME);
        if(TextUtils.isEmpty(devicejson))
        {
            return;
        }
        List<Device> devicelist =  new Gson().fromJson(devicejson,new TypeToken<List<Device>>(){}.getType());
        if(devicelist == null)
        {
            return;
        }
        Log.i("DataEventHelper", "devicelist size: " + devicelist.size());
        String currentDeviceName = WearNameHelper.getWearName();
        for (Device d : devicelist)
        {
            if(d.getName()!= null && d.getName().equals(currentDeviceName))
            {
                //Profile p = Utils.getStandardProfile();
                Long lastStepSyncTime = StepHelper.getLastSyncTime(d);
//                Long lastEcgSyncTime = StepHelper.getLastEcgSyncTime(d);
//                Long lastCoachSyncTime = StepHelper.getLastCoachSyncTime(d);
//                if(lastStepSyncTime == -1 && lastEcgSyncTime == -1 && lastCoachSyncTime ==-1)
//                {
//                    return;
//                }
                Log.i("DateEventHelper","start sync  lastSyncTime: "+lastStepSyncTime+" today step: "+d.getTodaySteps());


                Intent intent = new Intent(context.getApplicationContext(), SyncService.class);
                intent.putExtra(SyncData.Sync_Step_EndTime_Key, lastStepSyncTime);
                intent.putExtra(SyncData.Sync_Today_Step_Key,d.getTodaySteps());
                context.startService(intent);
                return;
            }
        }
        Log.i("DateEventHelper","not find device sync all");
        Intent intent = new Intent(context.getApplicationContext(), SyncService.class);
        intent.putExtra(SyncData.Sync_Step_EndTime_Key, 0L);
//        intent.putExtra(SyncData.Sync_Ecg_MeasureTime_Key, 0L);
//        intent.putExtra(SyncData.Sync_Coach_EndTime_Key, 0L);
        context.startService(intent);

    }
    public static void  processSyncPhoto(Context context , DataEvent event,DataLayerManager dataLayerManager)
    {
        DataMap data=DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        String photouri = data.getString(SyncProfile.KEY_PROFILE_PHOTO_URL);
        Asset asset = data.getAsset(SyncProfile.KEY_PROFILE_PHOTO);
        Bitmap bmp = dataLayerManager.getPhoto(asset);
        if(bmp!=null)
        {
            Log.i("smile", "processSyncPhoto not null");
            byte[] d = ShareUtils.toByteArray(bmp);
            ProfileHelper.updatePhotoData(context,d,photouri);

        }
        else
        {
            Log.i("smile","processSyncPhoto null");
        }


    }
}

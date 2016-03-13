package com.asus.wellness.sync;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asus.sharedata.CoachSyncItem;
import com.asus.sharedata.ShareUtils;
import com.asus.sharedata.SleepSyncItem;
import com.asus.sharedata.SyncData;
import com.asus.sharedata.SyncIdleAlarm;
import com.asus.sharedata.SyncProfile;
import com.asus.wellness.DataLayerManager;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.dbhelper.Coach;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.dbhelper.Location_change;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.Sleep;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.EcgTable;
import com.asus.wellness.provider.StepGoalHelper;
import com.asus.wellness.provider.WellnessProvider;
import com.asus.wellness.sleep.SleepHelper;
import com.asus.wellness.ui.setting.IdleAlarmEvent;
import com.asus.wellness.ui.setting.SettingActivity;
import com.asus.wellness.utils.DeviceHelper;
import com.asus.wellness.utils.EcgHelper;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.LocationHelper;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.utils.Step_count;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.utils.WearDeviceType;
import com.cmcm.common.statistics.CMAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/8/4.
 */
public class DataEventHelper {
    static ExecutorService cachedThreadPool = Executors.newSingleThreadExecutor();


    public static void notifyContentProvider(DataEventResult dataEventResult, Context context) {
        if (dataEventResult.stepUpdate) {
            context.getContentResolver().notifyChange(ActivityStateTable.TABLE_URI, null);
        }
        if (dataEventResult.ecgUpdate) {
            context.getContentResolver().notifyChange(EcgTable.TABLE_URI, null);
        }
        if (dataEventResult.sleepUpdate) {
            context.getContentResolver().notifyChange(SleepHelper.SLEEP_TABLE_URI, null);
        }
    }

    public static DataEventResult processDataEvent(DataEvent event, Context context, Long deviceId, EcgDao ecgDao, DataLayerManager dataLayerManager) {
        return processDataItem(event.getDataItem(), context, deviceId, ecgDao, dataLayerManager);
    }

    public static DataEventResult processDataItem(final DataItem dataItem, final Context context, Long deviceId, EcgDao ecgDao, final DataLayerManager dataLayerManager) {
        HashMap<String, String> msg = new HashMap<>();
        DataEventResult dataEventResult = new DataEventResult();
        if (dataItem.getUri().getPath().startsWith("/ecg")) {
            DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
            if (data.getInt(SyncData.ECG_MEASURE_TYPE) == EcgTable.TYPE_HEARTRATE) {
                // Get tracker.
                Tracker t = GAApplication.getInstance().getTracker(context);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Watch measure")
                        .setAction("Heart rate")
                        .setLabel("")
                        .build());
            }
//                ContentValues cv=new ContentValues();
//                cv.put(EcgTable.COLUMN_MEASURE_TIME, data.getLong(DataLayerManager.ECG_MEASURE_TIME));
//                cv.put(EcgTable.COLUMN_MEASURE_TYPE, data.getInt(DataLayerManager.ECG_MEASURE_TYPE));
//                cv.put(EcgTable.COLUMN_MEASURE_VALUE, data.getInt(DataLayerManager.ECG_MEASURE_VALUE));
//                cv.put(EcgTable.COLUMN_MEASURE_COMMENT, data.getInt(DataLayerManager.ECG_MEASURE_COMMENT));
//                getContentResolver().insert(EcgTable.TABLE_URI, cv);
//            	getContentResolver().insert(EcgTable.TABLE_URI, cv);

            Long measure_time = data.getLong(SyncData.ECG_MEASURE_TIME);
            Long measure_type = (long) (data.getInt(SyncData.ECG_MEASURE_TYPE));
            Long measure_value = (long) data.getInt(SyncData.ECG_MEASURE_VALUE);
            String measure_comment = String.valueOf(data.getInt(SyncData.ECG_MEASURE_COMMENT));
            //smile_gao fix 638563
            String devicejson = data.getString(SyncData.STEP_COUNT_DEIVCE_NAME);
            Log.i("smile", "ecg: " + devicejson);
            Device device = new Gson().fromJson(devicejson, Device.class);
            device = DeviceHelper.getDeviceByName(device);
            deviceId =  device.getId();
            //end smile
            if (deviceId == null) {
                Log.i("smile", "deviceId = null");
                return dataEventResult;
            }
            List<Ecg> ecgList = ecgDao.queryBuilder().where(EcgDao.Properties.DeviceId.eq(deviceId),
                    EcgDao.Properties.Measure_time.eq(measure_time),
                    EcgDao.Properties.Measure_value.eq(measure_value),
                    EcgDao.Properties.Measure_type.eq(measure_type)).list();
            if (ecgList == null || ecgList.size() == 0) {
                Ecg ecg = new Ecg(null, measure_time, measure_value, measure_type, measure_comment, deviceId);
                ecgDao.insert(ecg);
                Utility.updateLastUpdateTime(context, System.currentTimeMillis());
                dataEventResult.ecgUpdate = true;
            }
            msg.put("uptype", "1");
            msg.put("measure_time", String.valueOf(measure_time));
            msg.put("measure_value", String.valueOf(measure_value));
            msg.put("measure_type", String.valueOf(measure_type));
            msg.put("measure_comment", String.valueOf(Integer.parseInt(measure_comment) + 1));
            doCmDeviceEvent(device, CmHelper.MEASURE_MSG_ID, msg);
        } else if (dataItem.getUri().getPath().startsWith("/step_count")) {
            DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
            long startTime = data.getLong(SyncData.STEP_COUNT_START_TIME);
            long endTime = data.getLong(SyncData.STEP_COUNT_END_TIME);
            int step = data.getInt(SyncData.STEP_COUNT_NUMBERS);
            if (endTime < startTime) {
                Log.i("smile", "dataEventResult :endTime< startTime " + endTime + " " + startTime);
                return dataEventResult;
            }
            //smile_gao fix 638563
            String devicejson = data.getString(SyncData.STEP_COUNT_DEIVCE_NAME);

            Device device = new Gson().fromJson(devicejson, Device.class);
            device = DeviceHelper.getDeviceByName(device);
            deviceId =  device.getId();
            Log.i("smile", "datachagne: " + devicejson + " id:  " + deviceId);
            //end smile
            if (deviceId == null) {
                Log.i("smile", "deviceId = null");
                return dataEventResult;
            }
            StepHelper.writeStepCountInfo(startTime, endTime, step, deviceId, context);
            Utility.updateLastUpdateTime(context, System.currentTimeMillis());

            // dataEventResult.stepUpdate = true;
            msg.put("uptype", "1");
            msg.put("activity_starttime", String.valueOf(startTime));
            msg.put("activity_endtime", String.valueOf(endTime));
            msg.put("step_count", String.valueOf(step));
            ParseDataManager parseDataManager = ParseDataManager.getInstance();
            Profile profile = parseDataManager.getStandardProfile();
            String[] info = CmHelper.heightWeightInfo(profile);
            String kcal = String.valueOf(Utility.getWalkCalories(Integer.parseInt(info[0]), step, Integer.parseInt(info[1])));
            msg.put("kcal_count", kcal);
            if (CmHelper.isLocationEnable(context)) {
                Location_change location_change = LocationHelper.getInstance(context).getLocationChangeByTime(startTime);
                if (location_change != null) {
                    msg.put("get_location_time", String.valueOf(location_change.getGet_location_time()));
                    msg.put("latitude", String.valueOf(location_change.getLatitude() * 1000));
                    msg.put("longitude", String.valueOf(location_change.getLongitude() * 1000));
                    msg.put("district", String.valueOf(location_change.getLongitude()));
                }
            }
            doCmDeviceEvent(device,CmHelper.ACTIVITY_MSG_ID,msg);

        }
        //next goal smile_gao add
        else if (dataItem.getUri().getPath().startsWith("/next_goal")) {
            DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
            long nextGoal = data.getLong(SyncProfile.KEY_PROFILE_NEXT_GOAL);
            Log.i("smile", "next goal " + String.valueOf(nextGoal));
            if (nextGoal > 0) {
                StepGoalHelper.getmInstance(context).saveNextStepGoalToDB((int) nextGoal);
                //fix bug -	637737 receive next goal , update to all devices
                dataLayerManager.sendProfileToRobin();
            }
        }
        //watch type
        else if (dataItem.getUri().getPath().startsWith(WearDeviceType.MsgHeader)) {
            DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
            String watchjson = data.getString(SyncData.STEP_COUNT_DEIVCE_NAME, null);
            boolean isPeerConnect = data.getBoolean(SyncData.PEER_CONNECT, false);
            if (watchjson == null) {
                Log.i("smile", "watchjson null");
                return dataEventResult;
            }
            // Log.i("smile", "watchjson " + String.valueOf(watchjson));
            Device connectedWatch = null;
            try {
                Log.i("smile", WearDeviceType.MsgHeader + " watchjson " + watchjson);
                connectedWatch = new Gson().fromJson(watchjson, Device.class);
            } catch (Exception e) {
                Log.i("smile", "WearDeviceType Exception");
            }
            if (connectedWatch != null) {
                connectedWatch = DeviceHelper.addOrUpdateDeivce(connectedWatch);
                Device currDevice = WApplication.getInstance().getConnectedDevice();
                if(currDevice!=null && currDevice.getName() ==  connectedWatch.getName())
                {
                    Log.i("smile", "same current device not update ");
                   return  dataEventResult;
                }
                WApplication.getInstance().setConnectedDevice(connectedWatch);
                WApplication.getInstance().notifiyWatchChange(connectedWatch);
                if (isPeerConnect) {
                    CMAgent.onDeviceConnect(connectedWatch.getBlueaddr(), CmHelper.getDeviceVersion());
                }
            }
        }
        //add sync step path smile_gao 2015/8/12
        else if (dataItem.getUri().getPath().startsWith(SyncData.Sync_Alldata_Path)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Log.i("DataEventHelper", "this is main looper");
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        syncDataItemImpl(dataItem, context, dataLayerManager);
                    }
                });
            } else {
                Log.i("DataEventHelper", "this is not  main looper");
                syncDataItemImpl(dataItem, context, dataLayerManager);
            }

        } else if (dataItem.getUri().getPath().startsWith(SyncProfile.SET_PROFILE_FROM_WEAR_PATH)) {
            ProfileHelper.updateProfileFromWear(context, dataItem);
            dataLayerManager.sendProfileToRobin();// sync other watch
        } else if (dataItem.getUri().getPath().startsWith(SyncIdleAlarm.IDLE_ALARM_FROM_WEAR_PATH)) {

            DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
            boolean defaultSwitch = context.getResources().getBoolean(R.bool.default_idle_alarm_switch);
            boolean alarmon = data.getBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, defaultSwitch);
            Log.i("DataEventHelper", "SyncIdleAlarm KEY_IDLE_ALARM_SWITCH " + alarmon);
            String keyIdleAlarmSwitch = context.getResources().getString(R.string.pref_key_idle_alarm_switch);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(keyIdleAlarmSwitch, alarmon);
            editor.commit();
            //set for SettingActivity checkout remindmetomove
            SharedPreferences mSharedPref = context.getSharedPreferences(SettingActivity.KEY_GA, context.MODE_PRIVATE);
            SharedPreferences.Editor remindEditor = mSharedPref.edit();
            remindEditor.putBoolean(SettingActivity.IS_REMIND_OPT_OUT, !alarmon);
            remindEditor.commit();
            Utility.setRemindMeMoveChecbox(alarmon);
            dataLayerManager.sendIdleAlarmSetting();
            EventBus.getDefault().post(new IdleAlarmEvent());
        }
        else if (dataItem.getUri().getPath().startsWith(SyncData.Sync_Db_Path)){
            syncDb(dataItem,context,dataLayerManager);
        }

        return dataEventResult;
    }
    private static void syncDb(DataItem dataItem, Context context, DataLayerManager dataLayerManager)
    {

        DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
        String remoteDeviceJson = data.get(SyncData.STEP_COUNT_DEIVCE_NAME);
        Device remoteDevice = new Gson().fromJson(remoteDeviceJson, Device.class);
        Log.i("DateEventhelper", "syncDb "+remoteDeviceJson);
        String versionCode = data.getString(SyncData.SYNC_DB_VERSION_CODE,"");
        String deviceName = remoteDevice!=null? remoteDevice.getName():"wear";
        if(versionCode!=null && !versionCode.equals("")){
            deviceName+="_"+versionCode;
        }
        Asset asset = data.getAsset(SyncData.Sync_Data_Key);
        ConnectionResult result = dataLayerManager.getConnectResult();
        if (!result.isSuccess()) {
            Log.i("DateEventhelper", "syncDb ConnectionResult fail");
            EventBus.getDefault().post(new SyncDbEvent());
            return;
        }
        InputStream assertInputStream = dataLayerManager.getAsset(asset);
        if (assertInputStream == null) {
            Log.i("DateEventhelper", "syncDb assertInputStream null");
            EventBus.getDefault().post(new SyncDbEvent());
            return;
        }
        File wearDb = ShareUtils.getBakcupDbFile(deviceName+".db",false,"");
        ShareUtils.writeStreamToFile(assertInputStream,wearDb);
        EventBus.getDefault().post(new SyncDbEvent());
        Log.i("DateEventhelper", "syncDb over");
     }
    private static void doCmDeviceEvent(Device device,String msgid,HashMap<String, String> msg)
    {
        //CmHelper.ACTIVITY_MSG_ID
        if(device!=null && device.getBlueaddr()!=null)
        {
            try {
                CMAgent.onDeviceEvent(device.getBlueaddr(), CmHelper.getDeviceVersion(), msgid , msg);
            }catch (NullPointerException e)
            {
                Log.i("smile","nullpointer: "+msgid);
                e.printStackTrace();
            }
        }
    }

    public static DataEventResult processDataItemList(List<DataItem> dataItems, Context context, Long deviceId, EcgDao ecgDao, DataLayerManager dataLayerManager) {
        DataEventResult dataEventResult = new DataEventResult();
        List<Ecg> ecgList = new ArrayList<>();
        ArrayList<ContentProviderOperation> stepcountOperationList = new ArrayList<>();
        for (int i = 0; i < dataItems.size(); i++) {
            DataItem dataItem = dataItems.get(0);
            if (dataItem.getUri().getPath().startsWith("/ecg")) {
                DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();

                Long measure_time = data.getLong(SyncData.ECG_MEASURE_TIME);
                Long measure_type = (long) (data.getInt(SyncData.ECG_MEASURE_TYPE));
                Long measure_value = (long) data.getInt(SyncData.ECG_MEASURE_VALUE);
                String measure_comment = String.valueOf(data.getInt(SyncData.ECG_MEASURE_COMMENT));
                //smile_gao fix 638563
                String devicejson = data.getString(SyncData.STEP_COUNT_DEIVCE_NAME);
                Log.i("smile", "ecg: " + devicejson);
                Device device = new Gson().fromJson(devicejson, Device.class);
                deviceId = DeviceHelper.getDeviceIdByName(device);
                //end smile
                Ecg ecg = new Ecg(null, measure_time, measure_value, measure_type, measure_comment, deviceId);
                //   ecgDao.insert(ecg);
                ecgList.add(ecg);
                //  Utility.updateLastUpdateTime(context, System.currentTimeMillis());
                dataEventResult.ecgUpdate = true;
            } else if (dataItem.getUri().getPath().startsWith("/step_count")) {
                DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
                long startTime = data.getLong(SyncData.STEP_COUNT_START_TIME);
                long endTime = data.getLong(SyncData.STEP_COUNT_END_TIME);
                int step = data.getInt(SyncData.STEP_COUNT_NUMBERS);
                if (endTime < startTime) {
                    Log.i("smile", "dataEventResult :endTime< startTime " + endTime + " " + startTime);
                    return dataEventResult;
                }
                //smile_gao fix 638563
                String devicejson = data.getString(SyncData.STEP_COUNT_DEIVCE_NAME);
                Device device = new Gson().fromJson(devicejson, Device.class);
                deviceId = DeviceHelper.getDeviceIdByName(device);
                Log.i("smile", "datachagne: " + devicejson + " id:  " + deviceId);
                //end smile
                ContentProviderOperation operation = StepHelper.getInsertStepCountContentProviderOperation(startTime, endTime, step, deviceId, context, -1);
                if (operation != null) {
                    stepcountOperationList.add(operation);
                }
                // StepHelper.writeStepCountInfo(startTime, endTime, step, deviceId, context);
                //  Utility.updateLastUpdateTime(context, System.currentTimeMillis());

                // dataEventResult.stepUpdate = true;
            }
            //next goal smile_gao add
        }
        if (stepcountOperationList.size() > 0) {
            StepHelper.writeStepInfoInTransaction(stepcountOperationList, context);
            Utility.updateLastUpdateTime(context, System.currentTimeMillis());
        }
        if (ecgList.size() > 0) {
            ecgDao.insertInTx(ecgList);
            Utility.updateLastUpdateTime(context, System.currentTimeMillis());
        }
        return dataEventResult;
    }


    public static DataEventResult processSyncDataItemList(List<DataItem> dataItems, Context context, DataLayerManager dataLayerManager) {
        DataEventResult dataEventResult = new DataEventResult();
        ArrayList<ContentProviderOperation> stepcountOperationList = new ArrayList<>();
        for (int i = 0; i < dataItems.size(); i++) {
            DataItem dataItem = dataItems.get(0);
            syncDataItemImpl(dataItem, context, dataLayerManager);
        }

        return dataEventResult;
    }

    public synchronized static void syncDataItemImpl(DataItem dataItem, Context context, DataLayerManager dataLayerManager) {
        Log.i("syncdata", dataItem.getUri().getPath().toString());
        syncAllData(dataItem, dataLayerManager, context);
//        if(dataItem.getUri().getPath().startsWith(SyncData.Sync_Step_Path))
//        {
//            syncStepCount(dataItem,dataLayerManager,context);
//        }
//        else if(dataItem.getUri().getPath().startsWith(SyncData.Sync_Ecg_Path))
//        {
//            syncEcg(dataItem,dataLayerManager,context);
//        }
//        else if(dataItem.getUri().getPath().startsWith(SyncCoach.Sync_Coach_Path))
//        {
//            syncCoach(dataItem,dataLayerManager,context);
//        }
    }

    private static void syncAllData(DataItem dataItem, DataLayerManager dataLayerManager, Context context) {
        DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
        String remoteDeviceJson = data.get(SyncData.STEP_COUNT_DEIVCE_NAME);
        Device remoteDevice = new Gson().fromJson(remoteDeviceJson, Device.class);
        DataEventResult dataEventResult = new DataEventResult();
        if (remoteDevice == null || remoteDevice.getStepsynctime() == null) {
            Log.i("DateEventhelper", "syncEcg remote device null");
            return;
        }
        Device deviceDb = DeviceHelper.isDeviceExist(remoteDevice);
        if (deviceDb == null || deviceDb.getStepsynctime() == null || deviceDb.getStepsynctime() < remoteDevice.getStepsynctime()) {
            if (deviceDb == null) {
                deviceDb = DeviceHelper.getDeviceByName(remoteDevice);
            }
            if (deviceDb.getStepsynctime() == null) {
                deviceDb.setStepsynctime(0L);
            }
            Asset asset = data.getAsset(SyncData.Sync_Data_Key);
            ConnectionResult result = dataLayerManager.getConnectResult();
            if (!result.isSuccess()) {
                Log.i("DateEventhelper", "syncEcg ConnectionResult fail");
                return;
            }
            InputStream assertInputStream = dataLayerManager.getAsset(asset);
            if (assertInputStream == null) {
                Log.i("DateEventhelper", "syncEcg assertInputStream null");
                return;
            }
            String allDatajson = Utility.inputStream2String(assertInputStream);
            if(allDatajson==null)
            {
                Log.i("DateEventhelper", "allDatajson null");
                return;
            }
            String[] syncItems = allDatajson.split(SyncData.Sync_Item_Out_Sepration);
            for (String si : syncItems) {
                if (si.startsWith(SyncData.Sync_Coach_Tag_Key)) {
                    String[] coachitems = si.split(SyncData.Sync_Item_In_Sepration);
                    if (coachitems.length > 1) {
                        syncCoach(coachitems[1], null,context, deviceDb);
                        if(coachitems.length>2)
                        {
                            syncCoach(coachitems[1], coachitems[2], context, deviceDb);
                        }
                    }

                }
                else  if (si.startsWith(SyncData.Sync_Step_Tag_Key)) {
                    String[] stepitems = si.split(SyncData.Sync_Item_In_Sepration);
                    if (stepitems.length > 1) {
                        syncStepCount(stepitems[1], context, deviceDb);
                    }

                }
                else  if (si.startsWith(SyncData.Sync_Ecg_Tag_Key)) {
                    String[] ecgitems = si.split(SyncData.Sync_Item_In_Sepration);
                    if (ecgitems.length > 1) {
                        syncEcg(ecgitems[1],context,deviceDb);
                    }

                }
//                else if(si.startsWith(SyncData.Sync_Sleep_Tag_Key)) {
//                    String[] sleepItems = si.split(SyncData.Sync_Item_In_Sepration);
//                    if (sleepItems.length > 1) {
//                        syncSleepItems(sleepItems[1], context, deviceDb);
//                    }
//                }
                else if(si.startsWith(SyncData.Sync_Sleep_New_Tag_Key)) {
                    String[] sleeps = si.split(SyncData.Sync_Item_In_Sepration);
                    if (sleeps.length > 1) {
                        syncSleep(sleeps[1], context, deviceDb);
                    }
                }
            }

        }
    }

    private static void syncSleep(String data, Context context, Device deviceDb){
        DataEventResult dataEventResult = new DataEventResult();
        String sleepjson = data;
        Log.i("DateEventhelper", "syncSleep sleepJson " + sleepjson);
        List<Sleep> list = new Gson().fromJson(sleepjson, new TypeToken<List<Sleep>>() {
        }.getType());
        Log.i("DateEventhelper", "syncSleep list size " + list.size());
        if(list==null || list.size() ==0)
        {
            return;
        }
        dataEventResult = SleepHelper.writeSleepInfo(list, deviceDb.getId());
        Long lastMeasureTime = list.get(list.size() - 1).getEnd();
        if (deviceDb.getStepsynctime() == null || dataEventResult.sleepUpdate || deviceDb.getStepsynctime() < lastMeasureTime) {
            deviceDb.setStepsynctime(lastMeasureTime);
            // deviceDb.setEcgsynctime(lastMeasureTime);
            WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao().update(deviceDb);
            ;
        }

        if (dataEventResult.sleepUpdate) {
            //  Utility.updateLastUpdateTime(context, System.currentTimeMillis());
            DataEventHelper.notifyContentProvider(dataEventResult, context);
            updateProfileStartTime(context);
        }
    }

    private static void syncEcg(String data, Context context, Device deviceDb) {

        DataEventResult dataEventResult = new DataEventResult();
        String ecgjson = data;
        Log.i("DateEventhelper", "syncEcg ecgjson " + ecgjson);
        List<Ecg> list = new Gson().fromJson(ecgjson, new TypeToken<List<Ecg>>() {
        }.getType());
        Log.i("DateEventhelper", "syncEcg list size " + list.size());
        dataEventResult = EcgHelper.writeEcgInfo(list, deviceDb.getId());
        Long lastMeasureTime = list.get(list.size() - 1).getMeasure_time();
        if (deviceDb.getStepsynctime() == null || dataEventResult.ecgUpdate || deviceDb.getStepsynctime() < lastMeasureTime) {
            deviceDb.setStepsynctime(lastMeasureTime);
            // deviceDb.setEcgsynctime(lastMeasureTime);
            WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao().update(deviceDb);
            ;
        }

        if (dataEventResult.ecgUpdate) {
            //  Utility.updateLastUpdateTime(context, System.currentTimeMillis());
            DataEventHelper.notifyContentProvider(dataEventResult, context);
            updateProfileStartTime(context);
        }
    }

    private static void syncStepCount(String data, Context context, Device deviceDb) {
        ArrayList<ContentProviderOperation> stepcountOperationList = new ArrayList<>();

        String stepjson = data;
        Log.i("DateEventhelper", "syncStepCount stepjson " + stepjson);
        List<Step_count> list = new Gson().fromJson(stepjson, new TypeToken<List<Step_count>>() {
        }.getType());
//        if(list!=null && list.size()>0)
//        {
//            StepHelper.removeAllTodaySteps(deviceDb);
//        }
        for (Step_count s : list) {
            if (s.getStart() == null) {
                continue;
            }
            s.setId(null);
            ContentProviderOperation operation = StepHelper.getInsertStepCountContentProviderOperation(s.getStart(), s.getEnd(), s.getStep_count(), deviceDb.getId(), context, -1);
            if (operation != null) {
                stepcountOperationList.add(operation);
            }
        }
        Long lastStepStartTime = list.get(list.size() - 1).getEnd();

        if ((stepcountOperationList != null && stepcountOperationList.size() > 0) || deviceDb.getStepsynctime() == null || deviceDb.getStepsynctime() < lastStepStartTime) {
            deviceDb.setStepsynctime(lastStepStartTime);
            WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao().update(deviceDb);

        }

        if (stepcountOperationList.size() > 0) {
            StepHelper.writeStepInfoInTransaction(stepcountOperationList, context);
            updateProfileStartTime(context);
        }
    }

    private static void syncCoach(String coachItemsdata,String coachData, Context context, Device deviceDb) {
        ArrayList<ContentProviderOperation> stepcountOperationList = new ArrayList<>();
        String coachitemjson = coachItemsdata;
        String coachJson = coachData;
        if(coachJson!=null)
        {
            Log.i("smile","coachData: "+coachData);
            List<Coach> coachList = new Gson().fromJson(coachJson, new TypeToken<List<Coach>>() {}.getType());
            if(coachList!=null && coachList.size()>0)
            {
                StepHelper.insertCoachs(coachList,deviceDb);
            }
        }

        Log.i("DateEventhelper", "syncCoach coachjson " + coachitemjson);
        List<CoachSyncItem> list = new Gson().fromJson(coachitemjson, new TypeToken<List<CoachSyncItem>>() {
        }.getType());
        for (CoachSyncItem coachSyncItem : list) {
            if (coachSyncItem.getStart() == null) {
                continue;
            }
            ContentProviderOperation operation = StepHelper.getInsertStepCountContentProviderOperation(coachSyncItem.getStart(), coachSyncItem.getEnd(), coachSyncItem.getValue(), deviceDb.getId(), context,coachSyncItem.getType());
            if (operation != null) {
                stepcountOperationList.add(operation);
            }
        }
        Long lastStepStartTime = list.get(list.size() - 1).getEnd();

        if ((stepcountOperationList != null && stepcountOperationList.size() > 0) || deviceDb.getStepsynctime() == null || deviceDb.getStepsynctime() < lastStepStartTime) {
            deviceDb.setStepsynctime(lastStepStartTime);
            WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao().update(deviceDb);

        }

        if (stepcountOperationList.size() > 0) {
            StepHelper.writeStepInfoInTransaction(stepcountOperationList, context);
            updateProfileStartTime(context);
        }
    }

    private static void updateProfileStartTime(Context context) {
        Utility.updateLastUpdateTime(context, System.currentTimeMillis());
        ProfileHelper.updateProfileStartTime();
    }
}

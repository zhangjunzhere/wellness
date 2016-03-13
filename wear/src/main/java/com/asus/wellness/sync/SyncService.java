package com.asus.wellness.sync;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.asus.sharedata.ShareUtils;
import com.asus.sharedata.SyncData;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.dbhelper.Sleep;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.Step_count;
import com.google.android.gms.common.ConnectionResult;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/8/11.
 */
public class SyncService extends IntentService {
    final static String Tag = "SyncService";
   static DataLayerManager dataLayerManager;
   private long mTodaySteps;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
  //  private Long syncstartTime = 0L;
    public SyncService() {
        super("wearSyncService");
        Log.i("smile","syncService ");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("smile", "sync service oncreate ");
        if(dataLayerManager == null)
        {
            Log.i("smile","init  dataLayerManager");
            dataLayerManager = new DataLayerManager(this);

        }



    }
    void runSync(Long syncTime)
    {
        Log.i("smile", "syncStepTime: " + syncTime);
        List<SyncItem> syncList = new ArrayList<>();
      SyncItem stepSyncItem =   getStepSyncItem(syncTime);
      SyncItem ecgSyncItem  =   getEcgSyncItem(syncTime);
      SyncItem coachSyncItem =   getCoachSyncItem(syncTime);
      SyncItem sleepSyncItem = getSleepSyncItem(syncTime);
        if(stepSyncItem!=null)
             syncList.add(stepSyncItem);
        if(ecgSyncItem!=null)
              syncList.add(ecgSyncItem);
        if(coachSyncItem!=null)
              syncList.add(coachSyncItem);
        if(sleepSyncItem!=null)
            syncList.add(sleepSyncItem);
        if(syncList.size()==0)
        {
            Log.i("smile","list size ==0 no need to sync");
            return;
        }
        String data = formateData(syncList);
        long time = getLastItemTime(syncList);
        dataLayerManager.setSyncData(data, time);
    }
    public long getLastItemTime(List<SyncItem> list)
    {
        long time=0;
        for(SyncItem si : list)
        {
           if(time<si.lastItemTime)
           {
               time = si.lastItemTime;
           }
        }
        return  time;
    }
    public String formateData(List<SyncItem> list)
    {
        String ret = "";
        for(SyncItem si : list)
        {
            ret+=si.formatData()+SyncData.Sync_Item_Out_Sepration;
        }
        if(ret.length()>0)
        {
            ret =  ret.substring(0,ret.length()-1);
        }
        return  ret;
    }
    private SyncItem getSleepSyncItem(Long syncSleepTime)
    {
        if(syncSleepTime>=0) {
            List<Sleep> list = StepHelper.getSleepList(syncSleepTime);
            if (list == null || list.size() == 0) {
                Log.i(Tag, "sync sleep size = 0");
                return null;
            }
            Log.i(Tag, "sync sleep size = " + list.size());
            String data = new Gson().toJson(list);
            Long time = list.get(list.size()-1).getEnd();
            return  new SyncItem(SyncData.Sync_Sleep_New_Tag_Key,data,time);
            // dataLayerManager.setSyncCoachData(list);
        }
        return null;
    }
    private SyncItem getCoachSyncItem(Long syncCoachTime)
    {
        if(syncCoachTime>=0) {
            CoachTempItem item = StepHelper.getCoachItemList(syncCoachTime);
            if (item == null || item.coachSyncItemList==null ||item.coachSyncItemList.size() == 0) {
                Log.i(Tag, "sync coach size = 0");
                return null;
            }
            Log.i(Tag, "sync coach size = " + item.coachSyncItemList.size());
            String dataItems = new Gson().toJson(item.coachSyncItemList);
            String coachs = new Gson().toJson(item.coachList);
            List<String> dataList = new ArrayList<>();
            dataList.add(dataItems);
            dataList.add(coachs);
            Long time = item.coachSyncItemList.get(item.coachSyncItemList.size()-1).getEnd();
            return  new SyncItem(SyncData.Sync_Coach_Tag_Key,dataList,time);
           // dataLayerManager.setSyncCoachData(list);
        }
        return null;
    }
    private SyncItem getStepSyncItem(Long syncStepTime)
    {
        if(syncStepTime>=0) {
//            long midTime = ShareUtils.getMidnightMilles(System.currentTimeMillis());
//            if(syncStepTime>=midTime)
//            {
//                int totalstep = StepHelper.getTodaySteps();
//                if(totalstep == mTodaySteps)
//                {
//                    Log.i(Tag, "totalstep  equals "+totalstep+" midTime "+midTime);
//                    return null;
//                }
//                else
//                {
//                    Log.i(Tag, "totalstep not equals "+totalstep+" midTime "+midTime);
//                }
//            }
            List<Step_count> list = StepHelper.getSyncStepCountList(syncStepTime);
            if (list == null || list.size() == 0) {
                Log.i(Tag, "sync step size = 0");
                return null;
            }
            Log.i(Tag, "sync step size = " + list.size());
            String data = new Gson().toJson(list);
            Long time = list.get(list.size()-1).getEnd();
            return  new SyncItem(SyncData.Sync_Step_Tag_Key,data,time);
            //dataLayerManager.setSyncStepData(list);
        }
        return null;
    }

    private SyncItem getEcgSyncItem(Long syncEcgTime)
    {
        if(syncEcgTime>=0) {
            List<Ecg> list = StepHelper.getSyncEcgCountList(syncEcgTime);
            if (list == null || list.size() == 0) {
                Log.i(Tag, "sync ecg size = 0");
                return null;
            }
            Log.i(Tag, "sync ecg size = " + list.size());
            String data = new Gson().toJson(list);
            Long time = list.get(list.size()-1).getMeasure_time();
            return  new SyncItem(SyncData.Sync_Ecg_Tag_Key,data,time);
           // dataLayerManager.setSyncEcgData(list);
        }
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("smile", "onHandleIntent  " + intent.getAction());
      final  Long syncStepTime =intent.getLongExtra(SyncData.Sync_Step_EndTime_Key,-1L);
//      final  Long syncEcgTime =intent.getLongExtra(SyncData.Sync_Ecg_MeasureTime_Key,-1L);
//        final  Long syncCoachTime =intent.getLongExtra(SyncData.Sync_Coach_EndTime_Key,-1L);
        mTodaySteps = intent.getLongExtra(SyncData.Sync_Today_Step_Key,0L);
        if(!dataLayerManager.isConnected()) {
            ConnectionResult cr = dataLayerManager.connectBlocking(500);
//            dataLayerManager.connectGoogleApiClient(new CollectStepCountService.GoogleApiConnectCallback() {
//                @Override
//                public void onConnect() {
//                    runSync(syncStepTime);
//                }
//            });
            if(!cr.isSuccess())
            {
                Log.i("smile","SyncService: "+cr.getErrorCode());
                return;
            }
        }
        runSync(syncStepTime);
    }
    class SyncItem {
        String tag ;
        List<String> data;
        long lastItemTime;
        public SyncItem(String tag, String data,long lastItemTime)
        {
            this.data= new ArrayList<String>();
            this.tag = tag;
            this.data.add(data);
            this.lastItemTime = lastItemTime;
        }
        public SyncItem(String tag, List<String> data,long lastItemTime)
        {
            this.data= new ArrayList<String>();
            this.tag = tag;
            this.data =data;
            this.lastItemTime = lastItemTime;
        }
        public String formatData()
        {
            String ret=tag+SyncData.Sync_Item_In_Sepration;
           for(int i=0;i<data.size();i++){
               ret+=data.get(i);
               if(i!=data.size()-1)
               {
                   ret+=SyncData.Sync_Item_In_Sepration;
               }
           }
           return  ret;
        }
    }


}

package com.asus.wellness.sync;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.asus.wellness.DataLayerManager;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Activity_stateDao;
import com.asus.wellness.dbhelper.DaoSession;
import com.asus.wellness.dbhelper.EcgDao;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by smile_gao on 2015/8/5.
 */
public class SyncService extends IntentService {

    public static String Key_Command = "cmd";
    public static String Command_Photo = "photo";
    public static String Key_Command_Photo_Data = "data";
    public static String Key_Command_Photo_Url = "photoUrl";

    public static String Command_Sync = "sync";

   static DataLayerManager dataLayerManager;
   static ExecutorService cachedThreadPool;
    public SyncService() {
        super("mobileSyncService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("smile", "syncservice oncreate " + Thread.currentThread().getId());

        if(dataLayerManager == null) {
            dataLayerManager = new DataLayerManager(this);
        }
        if(cachedThreadPool == null)
           cachedThreadPool = Executors.newFixedThreadPool(1);

    }
    private synchronized void syncPhoto(final  byte[] data, final String photoUrl)
    {
        if (!dataLayerManager.isConnected()) {
            Log.i("smile","sync service fetchdata dataLayerManager not connected ");

            dataLayerManager.connectGoogleApiClient(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {

                    sendPhoto(data,photoUrl);
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        }
        else
        {
            sendPhoto(data,photoUrl);
        }
    }
    private void sendPhoto(byte[] data,String photoUrl)
    {
        dataLayerManager.sendProfilePhoto(data,photoUrl);
    }
    private synchronized void syncData() {
        if (!dataLayerManager.isConnected()) {
             Log.i("smile","sync service fetchdata dataLayerManager not connected ");
            //SyncdataDao syncdataDao = WApplication.getInstance().getDataHelper().getDaoSession().getSyncdataDao();
            Activity_stateDao activity_stateDao = WApplication.getInstance().getDataHelper().getDaoSession().getActivity_stateDao();
            EcgDao ecgDao = WApplication.getInstance().getDataHelper().getDaoSession().getEcgDao();

            dataLayerManager.connectGoogleApiClient(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {

                    getDataFromGoogleService();
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        }
        else
        {
            getDataFromGoogleService();
        }

    }
    private void getDataFromGoogleService()
    {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                dataLayerManager.getData(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(DataItemBuffer dataItems) {

                        final List<DataItem> events = FreezableUtils.freezeIterable(dataItems);
                        cachedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("smile", "sync service getData onResult  " + events.size() + " thread id: " + Thread.currentThread().getId());
                                processResults(events);
                            }
                        });
                        dataItems.release();

                        //
                    }
                });
            }
        });

    }
    private void processResults(List<DataItem> dataItems)
    {

        if (dataItems.size() != 0) {
//            Long deviceId = WApplication.getInstance().getConnectedDevice().getId();
//            DaoSession daoSession=WApplication.getInstance().getDataHelper().getDaoSession();
//            EcgDao ecgDao = daoSession.getEcgDao();
            DataEventResult dataEventResult = new DataEventResult();
//            dataEventResult = DataEventHelper.processSyncDataItemList(dataItems, SyncService.this, dataLayerManager);
//            for (int i = 0; i < dataItems.size(); i++) {
//                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));
//                 Log.i("smile", "processResults: " + dataMapItem.getUri().toString());
//                if (dataMapItem.getUri().getPath().startsWith("/step_count") ||  dataMapItem.getUri().getPath().startsWith("/ecg") )
//                {
//                    der = DataEventHelper.processDataItem(dataItems.get(i), SyncService.this, deviceId, ecgDao, dataLayerManager);
//                    dataEventResult.setResult(der);;
//                }
//            }
            dataEventResult = DataEventHelper.processSyncDataItemList(dataItems,SyncService.this,dataLayerManager);
            DataEventHelper.notifyContentProvider(dataEventResult,SyncService.this);

        }
      //  dataItems.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String cmd = intent.getStringExtra("cmd");

        if(cmd == null)
        {
            return;
        }
        if(cmd.equals(Command_Photo))
        {
            byte[] data =  intent.getByteArrayExtra(Key_Command_Photo_Data);
            String photoUrl =  intent.getStringExtra(Key_Command_Photo_Url);
            syncPhoto(data,photoUrl);
        }
        else if(cmd.equals(Command_Sync)) {
            syncData();
        }
    }
}

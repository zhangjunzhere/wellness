package com.asus.wellness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.asus.sharedata.SyncSleep;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.dbhelper.DaoSession;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.sleep.SleepTrackingNotification;
import com.asus.wellness.sync.DataEventResult;
import com.asus.wellness.sync.SyncService;
import com.asus.wellness.sync.DataEventHelper;
import com.cmcm.common.statistics.CMAgent;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * Listens to dataItems and RPCs from the local clockwork node.
 */
public class EcgAndStepCountListenerService extends WearableListenerService {
	
    private static final String TAG = "EcgAndStepService";

    public static final String START_DOWNLOAD_WELLNESS_PATH="/start_download_wellness_path";

    final static String ACTION_DATA_CHANGED =
            "com.example.android.wearable.datalayer.ACTION_DATA_CHANGED";
    final static String ACTION_DATA_DELETED =
            "com.example.android.wearable.datalayer.ACTION_DATA_DELETED";
    final static String ACTION_RPC_RECEIVED =
            "com.example.android.wearable.datalayer.ACTION_RPC_RECEIVED";
    final static String EVENT_KEY = "event";

    //smile_gao add for target goal change by wear
    /** Notifies local listeners of dataItem callbacks. */
    private LocalBroadcastManager mBroadcastManager;
   static DataLayerManager dataLayerManager;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SleepTrackingNotification.CLICK_ACTION)) {
                SleepTrackingNotification.getInstance().cancelTrackingNotification(context);
                dataLayerManager.sendMessageToPhone(SyncSleep.PATH_WEAR,SyncSleep.CANCEL);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("smile","EcgAndStepCountListenerService oncreate");
        mBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        if(dataLayerManager == null)
        {
            Log.i("smile","EcgAndStepCountListenerService oncreate dataLayerManager null");
            dataLayerManager=new DataLayerManager(this);
            dataLayerManager.connectGoogleApiClient();
        }
        else if(!dataLayerManager.isConnected())
        {
            Log.i("smile","EcgAndStepCountListenerService oncreate dataLayerManager not connected");
            dataLayerManager.connectGoogleApiClient();
        }

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(SleepTrackingNotification.CLICK_ACTION);
//        registerReceiver(mReceiver, filter);

        //smile gao start sync service
//        Intent serviceIntent=new Intent(getApplicationContext(), SyncService.class);
//        getApplicationContext().startService(serviceIntent);
        //smile gao start sync service
        //testInsertOrUpdate();
    }





    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
     //   Log.d(TAG, "onDataChanged: " + dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        // Broadcast all the events received in this batch.
       DaoSession daoSession=WApplication.getInstance().getDataHelper().getDaoSession();
        EcgDao ecgDao = daoSession.getEcgDao();
        Long deviceId = WApplication.getInstance().getConnectedDevice().getId();
        DataEventResult dataEventResult = new DataEventResult();
        for (DataEvent event : events) {
            Log.d(TAG, "onDataChanged: " + event.getDataItem().getUri().getPath()+" "+event.getType());
            if(event.getType() == DataEvent.TYPE_DELETED)
            {
                continue;
            }
           DataEventResult der = DataEventHelper.processDataEvent(event, this, deviceId, ecgDao, dataLayerManager);
           dataEventResult.setResult(der);

//            else if(event.getDataItem().getUri().getPath().startsWith(DataLayerManager.PROFILE_PATH_REQUEST))
//            {
//                Log.i("smile", DataLayerManager.PROFILE_PATH_REQUEST + " datachange receive ");
//                dataLayerManager.sendProfileToRobin();
//            }
        	
            Intent intent = new Intent();
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                intent.setAction(ACTION_DATA_CHANGED);
                intent.putExtra(EVENT_KEY, event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                intent.setAction(ACTION_DATA_DELETED);
                intent.putExtra(EVENT_KEY, event.getDataItem().getUri());
            }
            mBroadcastManager.sendBroadcast(intent);
        }
        //smile gao add for notify db change
        DataEventHelper.notifyContentProvider(dataEventResult,EcgAndStepCountListenerService.this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onMessageReceived: " + messageEvent);
        }

        // Broadcast any message.
        Intent intent = new Intent(ACTION_RPC_RECEIVED);
        intent.putExtra(EVENT_KEY, messageEvent.toString());
        mBroadcastManager.sendBroadcast(intent);

        // But if we're supposed to start an activity, do so, too.
        if (messageEvent.getPath().equals(START_DOWNLOAD_WELLNESS_PATH)) {
        	Intent downloadIntent=new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.asus.wellness"));
        	downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(downloadIntent);
        } else if(messageEvent.getPath().matches("/check_companion")){
        	dataLayerManager.sendMessageToPhone("/check_companion", "echo");
        }else if(messageEvent.getPath().matches(SyncSleep.PATH_MOBILE)){
            String command  = new String(messageEvent.getData());
            if(SyncSleep.SHOW.equals(command)){
                // TO DO show notification
//                SleepTrackingNotification.getInstance().registerTrackingNotification(this);
//                SleepTrackingNotification.getInstance().showRemoteViewNotification(this);
            }else{
                //cancel notification
                //SleepTrackingNotification.getInstance().ca
//               SleepTrackingNotification.getInstance().cancelTrackingNotification(this);
            }
        }
        //smile add for  wear device type 2015/07/01
//        else if(messageEvent.getPath().matches(WearDeviceType.MsgHeader))
//        {
//            String watchjson = "";
//            Device connectedWatch = null;
//            try
//            {
//                watchjson=new String(messageEvent.getData(),"UTF-8");
//                Log.i("smile", WearDeviceType.MsgHeader + " watchjson " + watchjson);
//                connectedWatch = new Gson().fromJson(watchjson,Device.class);
//            }catch (Exception e)
//            {
//                Log.i("smile","WearDeviceType Exception");
//            }
//            if(connectedWatch!= null) {
//                connectedWatch = DeviceHelper.addOrUpdateDeivce(connectedWatch);
//                WApplication.getInstance().setConnectedDevice(connectedWatch);
//                WApplication.getInstance().notifiyWatchChange(connectedWatch);
//            }
//
//            Log.i("smile", WearDeviceType.MsgHeader + " " + connectedWatch.getName());
//
////            //junzheng++ update database , replace Asus ZenWatch with first robin name
////            final String ROBIN_MODEL = "ASUS ZenWatch";
////            final String model = "ASUS ZenWatch";
////            final String deviceName = "ASUS ZenWatch CA5D";
////            Utility.updateDeviceTable(deviceName,model);
////            //end junzheng++
////            Boolean robin = watchtype.equals(WearDeviceType.Robin) ? true : false;
////            WApplication.getInstance().setIsZenWatchRobin(robin);
////            WApplication.getInstance().notifiyWatchChange(new WearDeviceType(watchtype));
//        }
 //       else if(messageEvent.getPath().matches(DataLayerManager.PROFILE_PATH_REQUEST))
 //       {
 //           Log.i("smile", DataLayerManager.PROFILE_PATH_REQUEST + " receive ");
 //           dataLayerManager.sendProfileToRobin();
 //      }
    }


    @Override
    public void onPeerConnected(Node peer) {
    	Log.d("circle","onpeerconnected:id:"+peer.getId()+" displayname:"+peer.getDisplayName());
    	
    	dataLayerManager.sendProfileToRobin();
    	dataLayerManager.sendIdleAlarmSetting();
    }

    @Override
    public void onPeerDisconnected(Node peer) {
    	Log.d("circle","onpeerdisconnected:id:"+peer.getId()+" displayname:"+peer.getDisplayName());
        String blueAddr= CmHelper.findFromDevice(peer.getDisplayName());
        if(blueAddr!=null)
            CMAgent.onDeviceDisconnect(blueAddr);
    }



	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dataLayerManager.disConnectGoogleApiClient();
//        unregisterReceiver(mReceiver);
	}
	
	
//    private  void testInsertOrUpdate(){
//        //for test
//        long startTime = 1435128932960L;
//        long endTime =  1435128935960L;
//        int step = 300;
//
//        writeStepCountInfo( startTime,  endTime,  step) ;
//
//        startTime = 1435128945960L;
//        endTime =  1435128975960L;
//        step = 60;
//        writeStepCountInfo( startTime,  endTime,  step) ;
//
//
//        // repeated insert
//        writeStepCountInfo( startTime,  endTime,  step) ;
//    }
}

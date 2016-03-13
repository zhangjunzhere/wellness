package com.asus.wellness;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.asus.sharedata.SyncData;
import com.asus.sharedata.SyncIdleAlarm;
import com.asus.sharedata.SyncProfile;
import com.asus.sharedata.SyncSleep;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.sleep.SleepDataModel;
import com.asus.wellness.utils.DataEventHelper;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.utils.AsusLog;
import com.asus.wellness.utils.WearNameHelper;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Listens to dataItems and RPCs from the local clockwork node.
 */
public class ListenerService extends WearableListenerService {
	private String TAG = "ListenerService";
	public static final String START_ACTIVITY_HEART_RATE="/start_activity_heart_rate";
	public static final String START_ACTIVITY_STRESS="/start_activity_stress";

	public static final String CHECK_WATCH_PATH="/check_watch_path";
	//smile_gao
	public static final String CHECK_WATCH_TYPE_PATH="/check_watch_type_path";
	//end smile
	public static final String RETURN_CHECK_WATCH_PATH="/return_check_watch_path";
    public static final String RETRIEVE_WATCH_STEP_PATH="/retrive_watch_step_path";


	public static final int INDEX_ACTIVITY_TYPE=0;
	public static final int INDEX_ACTIVITY_DISTANCE=1;
	public static final int INDEX_ACTIVITY_BIKE_STEP=2;
	

	
  private   DataLayerManager dataLayerManager;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Utility.TAG,  TAG + " WearableListenerService start CollectStepCountService");
		Intent serviceIntent=new Intent(getApplicationContext(), CollectStepCountService.class);
		getApplicationContext().startService(serviceIntent);
    	dataLayerManager =  DataLayerManager.getInstance(this);
		if(!dataLayerManager.isConnected())
		{
			Log.d(Utility.TAG ,"smile" + "dataLayerManager not null ,but not connected");
			dataLayerManager.connectGoogleApiClient(null);
		}
//		else
//		{
//			Log.d("smile", "dataLayerManager not null,Connected");
//		}
//        dataLayerManager.connectGoogleApiClient(new CollectStepCountService.GoogleApiConnectCallback() {
//			@Override
//			public void onConnect() {
//
//			}
//		});
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        // Broadcast all the events received in this batch.
        for (DataEvent event : events) {
			//Log.d("circle","robin datachanged "+event.getDataItem().getUri().toString());
			if(event.getType() == DataEvent.TYPE_DELETED)
			{
				continue;
			}
        	if(event.getDataItem().getUri().getPath().matches(SyncProfile.PROFILE_PATH)){
        		//delete all data


				//fix 644935 reach goal show more than once
//				int stepgoal = data.getInt(KEY_PROFILE_STEP_GOAL,Utility.TARGET_GOAL);
//				List<Profile> listprofile = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
//				if(listprofile!=null && listprofile.size()>0)
//				{
//					if(listprofile.get(0).getNext_step_goal()!=null && listprofile.get(0).getNext_step_goal()!= stepgoal)
//					{
//						CollectStepCountService.mShowStepGoalNotificationAlready = false;
//					}
//				}
//				else if(stepgoal != Utility.TARGET_GOAL)
//				{
//					CollectStepCountService.mShowStepGoalNotificationAlready = false;
//				}
				//ens smile
				DataMap data=DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
				ProfileHelper.updateProfile(this,data, dataLayerManager);


     //smile_gao add for when phone change step goal  then cancel  notificaiton
//                if(NotificaionHelper.getInstance(this).isHasReachGoalNotification()) {
//                    NotificaionHelper.getInstance(this).cancelNotification(NotificaionHelper.REACH_GOAL_NOTIFICATION_ID);
//                }
//


                //end smile
        	}
        	else if(event.getDataItem().getUri().getPath().matches(SyncIdleAlarm.IDLE_ALARM_PATH)){

				DataEventHelper.processIdleAlarmEvent(ListenerService.this,event);
        	}
			//add sync service
			else if(event.getDataItem().getUri().getPath().matches(SyncData.Request_Sync_Data_Path)){
				AsusLog.i(Utility.TAG,"wear RETRIEVE_WATCH_STEP_PATH");
				DataEventHelper.processSyncEvent(ListenerService.this,event);
			}
			else if(event.getDataItem().getUri().getPath().matches(SyncProfile.PROFILE_PHOTO_PATH))
			{
				DataEventHelper.processSyncPhoto(ListenerService.this, event, dataLayerManager);
			}
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
    	Log.d("circle","robin message received:"+messageEvent.getPath());
        //for knock knock
        if(messageEvent.getPath().equals(START_ACTIVITY_HEART_RATE)){
        	Intent intent=new Intent(this, MeasureActivity.class);
        	intent.putExtra(MeasureActivity.KEY_START_HEART_RATE, true);
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intent);
        }
        else if(messageEvent.getPath().equals(START_ACTIVITY_STRESS)){
        	Intent intent=new Intent(this, MeasureActivity.class);
        	intent.putExtra(MeasureActivity.KEY_START_RELAXATION, true);
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intent);
        }
        else if(messageEvent.getPath().equals(CHECK_WATCH_PATH)){
        	dataLayerManager.returnManufacturor(Build.MANUFACTURER);
        }
		//smile_gao check watch type
		else if(messageEvent.getPath().equals(CHECK_WATCH_TYPE_PATH))
		{
			AsusLog.i("smile","wear CHECK_WATCH_TYPE_PATH");
			dataLayerManager.sendWatchTypeToPhone(false);
		}
        else if(messageEvent.getPath().equals(SyncSleep.PATH_WEAR))
        {
            AsusLog.i(Utility.TAG, "wear RETRIEVE_WATCH_STEP_PATH");
			SleepDataModel.getInstance().setSleepStatus(SleepDataModel.eSleep.FINISH);
			EBCommand ebCommand = new EBCommand(this.getClass().getName(), CollectStepCountService.class.getName(),EBCommand.COMMAND_START_SLEEP,false);
			EventBus.getDefault().post(ebCommand);
        }
		else if(messageEvent.getPath().equals(SyncData.Request_Sync_Db_Path))
		{
			if(messageEvent.getData() !=null )
			{
				String deviename = new String(messageEvent.getData());
				String currentDeviceName = WearNameHelper.getWearName();
				Log.i("smile","devicename: "+deviename+"  curr devicename: "+ currentDeviceName);
				if(deviename.equals(currentDeviceName))
				{
					DataEventHelper.processSyncDb(ListenerService.this,dataLayerManager);
				}
			}

		}
    }

    @Override
    public void onPeerConnected(Node peer) {
    	Log.d("circle","wear peer connected");
		//for CM data
		dataLayerManager.sendWatchTypeToPhone(true);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
    	Log.d("circle","wear peer disconnected");
    }
    
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(Utility.TAG ,TAG+ " onDestroy ");
		super.onDestroy();
		//dataLayerManager.disConnectGoogleApiClient();
	}
}

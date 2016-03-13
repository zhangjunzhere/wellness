package com.asus.wellness.datalayer;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.asus.sharedata.CoachSyncItem;
import com.asus.sharedata.ShareUtils;
import com.asus.sharedata.SyncCoach;
import com.asus.sharedata.SyncData;
import com.asus.sharedata.SyncIdleAlarm;
import com.asus.sharedata.SyncProfile;
import com.asus.wellness.ListenerService;
import com.asus.wellness.StepCountManager;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.service.CollectStepCountService.GoogleApiConnectCallback;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.Step_count;
import com.asus.wellness.utils.AsusLog;
import com.asus.wellness.utils.Device;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.utils.WearDeviceType;
import com.asus.wellness.utils.WearNameHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.gson.Gson;

public class DataLayerManager implements ConnectionCallbacks, OnConnectionFailedListener{
	
	private GoogleApiClient mGoogleApiClient;
	private static final String TAG="[Wellness]DataLayerManager";
	

	public static final String START_DOWNLOAD_WELLNESS_PATH="/start_download_wellness_path";

	private List<GoogleApiConnectCallback> mCallbackList= new ArrayList<GoogleApiConnectCallback>();

	private static DataLayerManager s_dataLayerManager = null;
	public static DataLayerManager getInstance(Context context){
		if(s_dataLayerManager == null){
			s_dataLayerManager = new DataLayerManager(context.getApplicationContext());
		}
		return s_dataLayerManager;
	}
	
	public DataLayerManager(Context context){
		if(StepCountManager.USE_FITNESS) {
			mGoogleApiClient = new GoogleApiClient.Builder(context)
					.addApi(Wearable.API)
					.addApi(Fitness.HISTORY_API)
					.addApi(Fitness.RECORDING_API)
					.useDefaultAccount()
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		}
		else
		{
			mGoogleApiClient = new GoogleApiClient.Builder(context)
					.addApi(Wearable.API)
					.useDefaultAccount()
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		}
	}

	public GoogleApiClient getGoogleApiClient(){return mGoogleApiClient;}

	public void addListener(MessageListener messageListener){
		Wearable.MessageApi.addListener(mGoogleApiClient, messageListener);
	}
	
	public void removeListener(MessageListener messageListener){
		Wearable.MessageApi.removeListener(mGoogleApiClient, messageListener);
	}
	
	public void connectGoogleApiClient(GoogleApiConnectCallback cb) {
		Log.d(Utility.TAG ,TAG +  "mGoogleApiClient  isConnected " + mGoogleApiClient.isConnected());
		if(mGoogleApiClient.isConnected())
		{
			if(cb!=null)
				cb.onConnect();
			return;
		}
		//if (!mGoogleApiClient.isConnected()){
			mGoogleApiClient.connect();
		//}
		if(cb != null){
			if(mGoogleApiClient.isConnected()){
				cb.onConnect();
			}else{
				mCallbackList.add(cb);
			}
		}
	}

	public void reConnectGoogleApiClient() {
	    Log.d(Utility.TAG, TAG + "mGoogleApiClient  reConnected " );
//		mGoogleApiClient.reconnect();
//		mGoogleApiClient.disconnect();
//		mGoogleApiClient.connect();
	}

	public ConnectionResult connectBlocking(int timeout)
	{
		return   mGoogleApiClient.blockingConnect(timeout, TimeUnit.MILLISECONDS);
	}
	
	public void disConnectGoogleApiClient(){
		Log.i("circle","GoogleApiClient disconnect . ");
		mGoogleApiClient.disconnect();
		mCallbackList.clear();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		Log.d("circle","GoogleApiClient connect failed. "+result.getErrorCode());
//		if (result.hasResolution()) {
//            try {
//                result.startResolutionForResult((Activity) this.getApplicationContext(), REQUEST_RESOLVE_ERROR);
//            } catch (IntentSender.SendIntentException e) {
//                // There was an error with the resolution intent. Try again.
//                mGoogleApiClient.connect();
//            }
//        }
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		for(GoogleApiConnectCallback connectCallback : mCallbackList){
			connectCallback.onConnect();
		}
		Log.d("circle","GoogleApiClient connected. mCallbackList.size = " + mCallbackList.size() );
		//smile_gao add send robin or sparrow to phone move to ListernService
		//sendWatchTypeToPhone();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		Log.d("circle","GoogleApiClient connect suspend.");
	}

	//smile_gao send step goal to phone 2014/12/25
    public void sendStepGoalDataToPhone(final long nextGoal){
        Log.d("circle","send GoalData ToPhone1:"+nextGoal);
        PutDataMapRequest bundleDataItem = PutDataMapRequest.createWithAutoAppendedId("/next_goal");
        bundleDataItem.getDataMap().putLong(SyncProfile.KEY_PROFILE_NEXT_GOAL, nextGoal);
        PutDataRequest request = bundleDataItem.asPutDataRequest();

        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }
    //end smile


	public void sendStepCountDataToPhone(final long startTime, final long endTime, final int stepNumbers){
		Log.d("circle", "sendStepCountDataToPhone:" + startTime + " " + endTime + " " + stepNumbers);
		PutDataMapRequest bundleDataItem = PutDataMapRequest.createWithAutoAppendedId("/step_count");
        bundleDataItem.getDataMap().putLong(SyncData.STEP_COUNT_START_TIME, startTime);
        bundleDataItem.getDataMap().putLong(SyncData.STEP_COUNT_END_TIME, endTime);
        bundleDataItem.getDataMap().putInt(SyncData.STEP_COUNT_NUMBERS, stepNumbers);
		//fix bug 638563 smile_gao
		bundleDataItem.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME,  new Gson().toJson(WearNameHelper.getDevice()));
		//end smile
        PutDataRequest request = bundleDataItem.asPutDataRequest();

        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
	}
	
	public void sendEcgDataToPhone(final long measureTime, final int measureType, final int measureValue, final int commentIndex){
		Log.d("circle","sendEcgDataToPhone:"+measureTime+" "+measureType+" "+measureValue);
		PutDataMapRequest bundleDataItem = PutDataMapRequest.createWithAutoAppendedId("/ecg");
        bundleDataItem.getDataMap().putLong(SyncData.ECG_MEASURE_TIME, measureTime);
        bundleDataItem.getDataMap().putInt(SyncData.ECG_MEASURE_TYPE, measureType);
        bundleDataItem.getDataMap().putInt(SyncData.ECG_MEASURE_VALUE, measureValue);
        bundleDataItem.getDataMap().putInt(SyncData.ECG_MEASURE_COMMENT, commentIndex);
		//fix bug 638563 smile_gao
		bundleDataItem.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME, new Gson().toJson(WearNameHelper.getDevice()));
		//end smile
        PutDataRequest request = bundleDataItem.asPutDataRequest();

        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
	}
	//smile send wear type  robin  or sparrow  2015.07.01
	public void sendWatchTypeToPhone(boolean isPeerConnect)
	{

	//	Device watch = new Device(null,WearNameHelper.getWearName(),WearNameHelper.getBlueAddr(),WearDeviceType.isRobin(),null);
		String watchjson = new Gson().toJson(WearNameHelper.getOriginDevice());
		Log.i("smile","watch json: "+watchjson);
		PutDataMapRequest bundleDataItem = PutDataMapRequest.createWithAutoAppendedId(WearDeviceType.MsgHeader);
		bundleDataItem.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME, watchjson);
		bundleDataItem.getDataMap().putBoolean(SyncData.PEER_CONNECT, isPeerConnect);
		PutDataRequest request = bundleDataItem.asPutDataRequest();

		if (!mGoogleApiClient.isConnected()) {
			Log.i("smile","google not connected");
			return;
		}
		PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);

	}
	public void sendIdleAlram(boolean alarmon)
	{
		PutDataMapRequest bundleDataItem = PutDataMapRequest.create(SyncIdleAlarm.IDLE_ALARM_FROM_WEAR_PATH);
		bundleDataItem.getDataMap().putBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, alarmon);
		bundleDataItem.getDataMap().putLong("time",System.currentTimeMillis());
		PutDataRequest request = bundleDataItem.asPutDataRequest();
		if (!mGoogleApiClient.isConnected()) {
			Log.i("smile","sendIdleAlram google not connected");
			return;
		}
		PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
		result.setResultCallback(new ResultCallback<DataItemResult>() {
			@Override
			public void onResult(DataItemResult dataItemResult) {
				Log.i("smile","sendIdleAlram dataItemResult isSuccess: "+dataItemResult.getStatus().isSuccess());
			}
		});
	}
	public void sendProfileToPhone(){
		Profile profile = ProfileHelper.getStandardProfile();
		if(profile!=null){
			String watchjson = new Gson().toJson(WearNameHelper.getOriginDevice());
			PutDataMapRequest bundleDataItem = PutDataMapRequest.create(SyncProfile.SET_PROFILE_FROM_WEAR_PATH);
			//bundleDataItem.getDataMap().putString(KEY_PROFILE_NAME, profile.name);
			//bundleDataItem.getDataMap().putString(KEY_PROFILE_PHOTO, profile.photo_path);
			//bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_AGE, profile.getAge());
			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_GENDER, profile.getGender());
			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_HEIGHT, profile.getHeight());
			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_HEIGHT_UNIT, profile.getHeight_unit());
			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_WEIGHT, profile.getWeight());
			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_WEIGHT_UNIT, profile.getWeight_unit());
			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_DISTANCE_UNIT, profile.getDistance_unit());
			int stepGoal=profile.getStep_goal() == null ? Utility.TARGET_GOAL : profile.getStep_goal();

			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_STEP_GOAL, stepGoal);

			bundleDataItem.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME, watchjson);
			Log.d("circle","sendProfileToRobin:data:"
					+profile.getAge()+" "
					+profile.getGender()+" "
					+profile.getHeight()+" "
					+profile.getHeight_unit()+" "
					+profile.getWeight()+" "
					+profile.getWeight_unit()+" "
					+ profile.getDistance_unit()+" "
					+stepGoal);
			PutDataRequest request = bundleDataItem.asPutDataRequest();

			if (!mGoogleApiClient.isConnected()) {
				return;
			}

			PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
		}
	}


	//smile request profile udpate  2015.07.01
	public void sendRequestProfile()
	{
		AsusLog.i("smile", "sendRequestProfile ");
		//sendMessageToPhone(ListenerService.PROFILE_PATH_REQUEST , ListenerService.PROFILE_PATH_REQUEST);
	}
	//end smile



	public void sendMessageToPhone(String path, String message){
		byte[] messageByte = null;
		try {
			messageByte=message.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Wearable.MessageApi.sendMessage(
                mGoogleApiClient, "fakenode", path, messageByte).setResultCallback(
                new ResultCallback<SendMessageResult>() {
                    @Override
                    public void onResult(SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(Utility.TAG, TAG + "Failed to connect to Google Api Client with status: "+ sendMessageResult.getStatus());
                        }
                    }
                }
        );
	}
	
	public void returnManufacturor(String message){
		byte[] messageByte = null;
		try {
			messageByte=message.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Wearable.MessageApi.sendMessage(
                mGoogleApiClient, "fakenode", ListenerService.RETURN_CHECK_WATCH_PATH, messageByte).setResultCallback(
                new ResultCallback<SendMessageResult>() {
                    @Override
                    public void onResult(SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(Utility.TAG, TAG + "Failed to connect to Google Api Client with status: " + sendMessageResult.getStatus());
                        }
                    }
                }
        );
	}

	public  void getData(ResultCallback<DataItemBuffer> callback)
	{
		PendingResult<DataItemBuffer> results =  Wearable.DataApi.getDataItems(mGoogleApiClient);
		results.setResultCallback(callback);
//		PendingResult<DataItemResult> item = Wearable.DataApi.getDataItem(mGoogleApiClient,PutDataMapRequest.createWithAutoAppendedId(ListenerService.PROFILE_PATH).getUri());
//		item.setResultCallback();
	}
	public boolean isConnected()
	{
		return mGoogleApiClient.isConnected();
	}
	public  void setSyncStepData(final List<Step_count> stepCountList)
	{
		if(stepCountList.size()<=0)
		{
			return;
		}
		Long lastStepCountTime = stepCountList.get(stepCountList.size()-1).getEnd();//change to getend
		String steps = new Gson().toJson(stepCountList);
		Device device = WearNameHelper.getDevice();
		if(device.getStepsynctime()==null || device.getStepsynctime()< lastStepCountTime)
		{
			device.setStepsynctime(lastStepCountTime);
		}
		Log.i("smile","setSyncData steps "+steps);
//		device.setStepsynctime(lastStepCountTime);
		Asset asset = Asset.createFromBytes(steps.getBytes());
//		PutDataMapRequest dataMap = PutDataMapRequest.create(SyncData.Sync_Step_Path);
//		dataMap.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME, new Gson().toJson(device));
//		dataMap.getDataMap().putAsset(SyncData.Sync_Step_Data_Key, asset);
//		dataMap.getDataMap().putLong("time", System.currentTimeMillis());
//		PutDataRequest request = dataMap.asPutDataRequest();
//		if (!mGoogleApiClient.isConnected()) {
//			Log.i("setSyncData","setSyncECgData googleclient not connected");
//			return;
//		}
//		Wearable.DataApi.putDataItem(mGoogleApiClient,request).setResultCallback(new ResultCallback<DataItemResult>() {
//			@Override
//			public void onResult(DataItemResult dataItemResult) {
//				Log.i("setSyncData","result: "+dataItemResult.getStatus().isSuccess());
//			}
//		});;
//		ProfileHelper.updateLastSyncStepTime(lastStepCountTime);
//		Log.i("smile","setSyncData over");
		doSync(SyncData.Sync_Step_Path,asset,"setSyncStepData",device, SyncData.Sync_Step_Data_Key);
		ProfileHelper.updateLastSyncStepTime(lastStepCountTime);

	}
	public void setSyncDb(byte[] data,String versioncode)
	{
		Device device = WearNameHelper.getDevice();
//		if(device.getStepsynctime()==null || device.getStepsynctime()< lastItemTime)
//		{
//			device.setStepsynctime(lastItemTime);
//		}
		Log.i("smile", "setSyncDb  versioncode:"+versioncode);//+ data
		Asset asset = Asset.createFromBytes(data);
		doSyncDb(SyncData.Sync_Db_Path, asset, "setSyncDb", device, SyncData.Sync_Data_Key, versioncode);
	//	ProfileHelper.updateLastSyncStepTime(lastItemTime);
	}

	public void setSyncData(String data,long lastItemTime)
	{
		Device device = WearNameHelper.getDevice();
		if(device.getStepsynctime()==null || device.getStepsynctime()< lastItemTime)
		{
			device.setStepsynctime(lastItemTime);
		}
		Log.i("smile", "setSyncData  " + data);
		Asset asset = Asset.createFromBytes(data.getBytes());
		doSync(SyncData.Sync_Alldata_Path, asset, "setSyncData", device, SyncData.Sync_Data_Key);
		ProfileHelper.updateLastSyncStepTime(lastItemTime);
	}
	public  void setSyncCoachData(final List<CoachSyncItem> coachSyncItemList)
	{
		if(coachSyncItemList.size()<=0)
		{
			return;
		}
		Long lastCoachItemEndTime = coachSyncItemList.get(coachSyncItemList.size()-1).end;//change to getend
		String coachitems = new Gson().toJson(coachSyncItemList);
		Device device = WearNameHelper.getDevice();
		if(device.getStepsynctime()==null || device.getStepsynctime()< lastCoachItemEndTime)
		{
			device.setStepsynctime(lastCoachItemEndTime);
		}
		Log.i("smile", "setSyncCoachData  " + coachitems);
	//	device.setStepsynctime(lastCoachItemEndTime);
		Asset asset = Asset.createFromBytes(coachitems.getBytes());
		doSync(SyncCoach.Sync_Coach_Path, asset, "setSyncCoachData", device, SyncData.Sync_Data_Key);
		ProfileHelper.updateLastSyncStepTime(lastCoachItemEndTime);
	}
	private void doSyncDb(String path,Asset asset,final  String tag,Device device,String dataKey, String versionCode)
	{
		PutDataMapRequest dataMap = PutDataMapRequest.create(path);
		dataMap.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME, new Gson().toJson(device));
		dataMap.getDataMap().putAsset(dataKey, asset);
		dataMap.getDataMap().putLong("time", System.currentTimeMillis());
		dataMap.getDataMap().putString(SyncData.SYNC_DB_VERSION_CODE, versionCode);
		PutDataRequest request = dataMap.asPutDataRequest();
		if (!mGoogleApiClient.isConnected()) {
			Log.i(tag,tag+" googleclient not connected");
			return;
		}
		Wearable.DataApi.putDataItem(mGoogleApiClient,request).setResultCallback(new ResultCallback<DataItemResult>() {
			@Override
			public void onResult(DataItemResult dataItemResult) {
				Log.i(tag, "result: " + dataItemResult.getStatus().isSuccess());
			}
		});

		Log.i("smile", tag + " over");
	}
	private void doSync(String path,Asset asset,final  String tag,Device device,String dataKey)
	{
		PutDataMapRequest dataMap = PutDataMapRequest.create(path);
		dataMap.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME, new Gson().toJson(device));
		dataMap.getDataMap().putAsset(dataKey, asset);
		dataMap.getDataMap().putLong("time", System.currentTimeMillis());
		PutDataRequest request = dataMap.asPutDataRequest();
		if (!mGoogleApiClient.isConnected()) {
			Log.i(tag,tag+" googleclient not connected");
			return;
		}
		Wearable.DataApi.putDataItem(mGoogleApiClient,request).setResultCallback(new ResultCallback<DataItemResult>() {
			@Override
			public void onResult(DataItemResult dataItemResult) {
				Log.i(tag, "result: " + dataItemResult.getStatus().isSuccess());
			}
		});

		Log.i("smile", tag + " over");
	}
	public  void setSyncEcgData(final List<Ecg> ecgList)
	{
		if(ecgList.size()<=0)
		{
			return;
		}
		Long lastEcgTime = ecgList.get(ecgList.size()-1).getMeasure_time();
		String ecgs = new Gson().toJson(ecgList);
		Log.i("smile","setSyncEcgData ecgs "+ecgs);
		Device device = WearNameHelper.getDevice();
		if(device.getStepsynctime()==null || device.getStepsynctime()< lastEcgTime)
		{
			device.setStepsynctime(lastEcgTime);
		}


		Asset asset = Asset.createFromBytes(ecgs.getBytes());

		doSync(SyncData.Sync_Ecg_Path,asset,"setSyncEcgData",device,SyncData.Sync_Ecg_Data_Key);
		ProfileHelper.updateLastSyncStepTime(lastEcgTime);

	}
	public Bitmap getPhoto(Asset asset)
	{
		if (null != asset) {
			if (!mGoogleApiClient.isConnected()) {

			  ConnectionResult cr=	mGoogleApiClient.blockingConnect(200, TimeUnit.MILLISECONDS);
				if(!cr.isSuccess()) {
					Log.e(Utility.TAG, TAG + "Failed to set notification background"
							+ " - Client disconnected from Google Play Services");
					return  null;
				}


			}
			try {
				InputStream inputStream =
						Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await().getInputStream();
				if (inputStream != null) {
					Bitmap profilePic = BitmapFactory.decodeStream(inputStream);
					return profilePic;
				}
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return  null;
	}

	
}

package com.asus.wellness;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.asus.sharedata.ShareUtils;
import com.asus.sharedata.SyncData;
import com.asus.sharedata.SyncIdleAlarm;
import com.asus.sharedata.SyncProfile;
import com.asus.wellness.ParseDataManager.ProfileData;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.provider.StepGoalTable;
import com.asus.wellness.ui.setting.SettingIdleAlarmActivity;
import com.asus.wellness.ui.setting.SettingStepGoalActivity;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataLayerManager implements ConnectionCallbacks, OnConnectionFailedListener{
	
	private GoogleApiClient mGoogleApiClient;
	private Context mContext;
	private static final String TAG="DataLayerManager";

	public static final String CHECK_WATCH_PATH="/check_watch_path";
    public static final String RETRIEVE_WATCH_STEP_PATH="/retrive_watch_step_path";
	//smile_gao
	public static final String CHECK_WATCH_TYPE_PATH="/check_watch_type_path";
	//end smile
	public static final String RETURN_CHECK_WATCH_PATH="/return_check_watch_path";

	private boolean mIsChecktAsusWatch=false;

	private boolean mIsChecktAsusWatchType = false;
	
	private MessageListener mMessageListener;
	private ConnectionCallbacks mCallback;
	ExecutorService cachedThreadPool;
	public DataLayerManager(Context context){
		mGoogleApiClient = new GoogleApiClient.Builder(context)
			.addApi(Wearable.API)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
		mContext=context;
		cachedThreadPool = Executors.newFixedThreadPool(1);
	}
	
	public void connectGoogleApiClient(){
		mGoogleApiClient.connect();
	}
	public void connectGoogleApiClient(ConnectionCallbacks cb){
		mGoogleApiClient.connect();
		mCallback=cb;
	}
	
	public void connectGoogleApiClientForCheckAsusWatch(boolean isCheck,boolean isCheckType, MessageListener listener){
		mIsChecktAsusWatch=isCheck;
		mMessageListener=listener;
		mIsChecktAsusWatchType = isCheckType;
		mGoogleApiClient.connect();
	}
	
	public void disConnectGoogleApiClient(){

		mGoogleApiClient.disconnect();
	}
	
	public void addListener(MessageListener messageListener){
		Wearable.MessageApi.addListener(mGoogleApiClient, messageListener);
	}
	
	public void removeListener(MessageListener messageListene){
		 Wearable.MessageApi.removeListener(mGoogleApiClient, messageListene).setResultCallback(new ResultCallback<Status>() {

			 @Override
			 public void onResult(Status arg0) {
				 // TODO Auto-generated method stub
				 Log.e("circle", "disconnect googel api");
				 disConnectGoogleApiClient();
			 }

		 });
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		Log.d("circle","GoogleApiClient connect failed."+result.getErrorCode());
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
		Log.d("circle","GoogleApiClient connected.");
		if(mCallback!=null)
		{
			mCallback.onConnected(arg0);
		}
		if(mIsChecktAsusWatch){
			checkIsAsusWatch();
			if(mIsChecktAsusWatchType)
					checkWatchType();
			if(mMessageListener!=null){
				addListener(mMessageListener);
			}

		}
	}
	public  void getData(ResultCallback<DataItemBuffer> callback)
	{
		PendingResult<DataItemBuffer> results =  Wearable.DataApi.getDataItems(mGoogleApiClient);
		results.setResultCallback(callback);
	}
	public void putData(DataMapItem source)
	{
		PutDataMapRequest bundleDataItem = PutDataMapRequest.createFromDataMapItem(source);
		PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, bundleDataItem.asPutDataRequest());
	}
	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		Log.d("circle","GoogleApiClient connect suspend.");
	}
	
	public void sendProfileToRobin(){
		ProfileData profile= ParseDataManager.getInstance().getProfileData(mContext);
		if(profile!=null){
			PutDataMapRequest bundleDataItem = PutDataMapRequest.create(SyncProfile.PROFILE_PATH);
			bundleDataItem.getDataMap().putString(SyncProfile.KEY_PROFILE_NAME, profile.name);
			if(profile.photo_path!=null && profile.photo_path!="")
			{
				bundleDataItem.getDataMap().putString(SyncProfile.KEY_PROFILE_PHOTO_URL, profile.photo_path);
				Bitmap bitmap = Utility.getPhotoBitmap(mContext, profile.photo_path);
				if(bitmap!=null) {
					byte[] bmpdata = ShareUtils.toByteArray(bitmap);
					wrapProfilePhotoData(bundleDataItem, bmpdata);
				}
			}
			int age = Utility.calcAge(profile.birthday);
			if(age>0) {
				bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_AGE, profile.age);
			}
			bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_GENDER, profile.gender);
	        bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_HEIGHT, profile.height);
	        bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_HEIGHT_UNIT, profile.heightUnit);
	        bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_WEIGHT, profile.weight);
	        bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_WEIGHT_UNIT, profile.weightUnit);
	        bundleDataItem.getDataMap().putLong(SyncProfile.KEY_PROFILE_START_TIME, profile.start_time);
          //  bundleDataItem.getDataMap().putLong(KEY_PROFILE_BIRTHDAY, profile.birthday);
	    	Cursor stepGoalCursor = Utility.getStepGoalCursor(mContext);
	    	int stepGoal=SettingStepGoalActivity.DEFAULT_STEP_GOAL;
	    	if(stepGoalCursor.moveToFirst()){
	    		stepGoal=stepGoalCursor.getInt(stepGoalCursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
	    	}
	    	stepGoalCursor.close();
	        bundleDataItem.getDataMap().putInt(SyncProfile.KEY_PROFILE_STEP_GOAL, stepGoal);
			Log.d("circle","sendProfileToRobin:data:"
					+profile.name+" "
					+profile.photo_path+" "
					+profile.age+" "
					+profile.gender+" "
					+profile.height+" "
					+profile.heightUnit+" "
					+profile.weight+" "
					+profile.weightUnit+" "
					+profile.start_time+" "
                    +profile.birthday+" "
					+stepGoal);
	        PutDataRequest request = bundleDataItem.asPutDataRequest();

	        if (!mGoogleApiClient.isConnected()) {
				Log.i("smile","mGoogleApiClient not connected");
	            return;
	        }

	        PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
		}
	}
	
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
                            Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                    + sendMessageResult.getStatus());
                        }
                    }
                }
        );
	}
	
	public void isConnected(ResultCallback<GetConnectedNodesResult> rc){
		PendingResult<GetConnectedNodesResult> pr=Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
		pr.setResultCallback(rc);
	}
	
	public void sendIdleAlarmSetting(){
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(mContext);


		String keyIdleAlarmSwitch=mContext.getString(R.string.pref_key_idle_alarm_switch);
		boolean defaultSwitch=mContext.getResources().getBoolean(R.bool.default_idle_alarm_switch);

		String keyIdleAlarmInterval=mContext.getString(R.string.pref_key_idle_alarm_options_interval);

        Log.d("circle","send idle alarm setting, TIME_INTERVAL = " + sp.getInt(keyIdleAlarmInterval, SettingIdleAlarmActivity.DEFAULT_TIME_INTERVAL) + ", DEFAULT_TIME_INTERVAL = " + SettingIdleAlarmActivity.DEFAULT_TIME_INTERVAL);

        PutDataMapRequest bundleDataItem = PutDataMapRequest.create(SyncIdleAlarm.IDLE_ALARM_PATH);
        bundleDataItem.getDataMap().putBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, sp.getBoolean(keyIdleAlarmSwitch, defaultSwitch));
		bundleDataItem.getDataMap().putInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL, sp.getInt(keyIdleAlarmInterval, SettingIdleAlarmActivity.DEFAULT_TIME_INTERVAL));
        bundleDataItem.getDataMap().putInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_FROM, sp.getInt(Utility.HOUR_OF_DAY_FROM, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_FROM));
        bundleDataItem.getDataMap().putInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_FROM, sp.getInt(Utility.MINUTE_FROM, SyncIdleAlarm.DEFAULT_MINUTE_FROM));
        bundleDataItem.getDataMap().putInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_TO, sp.getInt(Utility.HOUR_OF_DAY_TO, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_TO));
        bundleDataItem.getDataMap().putInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_TO, sp.getInt(Utility.MINUTE_TO, SyncIdleAlarm.DEFAULT_MINUTE_TO));

        PutDataRequest request = bundleDataItem.asPutDataRequest();

        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
	}
	
	public void checkIsAsusWatch(){
		Log.i("smile","checkIsAsusWatch");
		Wearable.MessageApi.sendMessage(
                mGoogleApiClient, "fakenode", CHECK_WATCH_PATH, null).setResultCallback(
                new ResultCallback<SendMessageResult>() {
                    @Override
                    public void onResult(SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                    + sendMessageResult.getStatus());
                        }
                    }
                }
        );
	}
	//smile_gao 2015/07/01
	public void checkWatchType(){
		Log.i("smile","checkWatchType");
		Wearable.MessageApi.sendMessage(
				mGoogleApiClient, "fakenode", CHECK_WATCH_TYPE_PATH, null).setResultCallback(
				new ResultCallback<SendMessageResult>() {
					@Override
					public void onResult(SendMessageResult sendMessageResult) {
						if (!sendMessageResult.getStatus().isSuccess()) {
							Log.e(TAG, "Failed to connect to Google Api Client with status: "
									+ sendMessageResult.getStatus());
						}
					}
				}
		);
	}
	//smile_gao 2016/01/05 sync wear db
	public boolean syncWearDb(){

	   String devicename = null;
		if(WApplication.getInstance().getConnectedDevice()!=null )
		{
			devicename = WApplication.getInstance().getConnectedDevice().getName();
		}
		if(devicename==null)
		{
			Log.i("smile","syncWearDb blueAddr null");
			return  false;
		}
		final  String realdevicename = devicename;
		Log.i("smile","syncWearDb "+devicename);
		cachedThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
				List<Node> list = nodes.getNodes();
				for (Node node : nodes.getNodes()) {
					Log.i("smile","syncWearDb id: "+node.getId()+" name: "+node.getDisplayName());
					Wearable.MessageApi.sendMessage(
							mGoogleApiClient, node.getId(), SyncData.Request_Sync_Db_Path, realdevicename.getBytes()).setResultCallback(
							new ResultCallback<SendMessageResult>() {
								@Override
								public void onResult(SendMessageResult sendMessageResult) {
									if (!sendMessageResult.getStatus().isSuccess()) {
										Log.e(TAG, "Failed Google Api Client with "+sendMessageResult.getStatus());
									}
								}
							});

				}
			}
		});



		return  true;
	}
//    public void retrieveStepFromWatch(){
//        Log.i("smile","retrieveStepFromWatch");
//        Wearable.MessageApi.sendMessage(
//                mGoogleApiClient, "fakenode", RETRIEVE_WATCH_STEP_PATH, null).setResultCallback(
//                new ResultCallback<SendMessageResult>() {
//                    @Override
//                    public void onResult(SendMessageResult sendMessageResult) {
//                        if (!sendMessageResult.getStatus().isSuccess()) {
//                            Log.e(TAG, "Failed to connect to Google Api Client with status: "
//                                    + sendMessageResult.getStatus());
//                        }
//                    }
//                }
//        );
//    }
	public boolean isConnected()
	{
		return  mGoogleApiClient.isConnected();
	}
	public ConnectionResult getConnectResult()
	{
		return  mGoogleApiClient.blockingConnect(200, TimeUnit.MILLISECONDS);
	}
	public InputStream getAsset(Asset asset)
	{
		return Wearable.DataApi.getFdForAsset(mGoogleApiClient,asset).await().getInputStream();
	}

	public void requestSyncData(){


		List<Device> deviceList = WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao().loadAll();
//		if(deviceList == null || deviceList.size() ==0 ) {
//			return;
//		}
		for (Device d: deviceList ) {
			long todaysteps = StepHelper.getTodayTotalStepsByDevice(d);
			d.setTodaySteps(todaysteps);
		}
		PutDataMapRequest bundleDataItem = PutDataMapRequest.create(SyncData.Request_Sync_Data_Path);
		long time = System.currentTimeMillis();
		String devicejson = new Gson().toJson(deviceList);//,new TypeToken<List<Device>>(){}.getType()
		Log.i("smile","jsontime: "+(System.currentTimeMillis()-time)+" devicejson: "+devicejson);
		bundleDataItem.getDataMap().putString(SyncData.STEP_COUNT_DEIVCE_NAME, devicejson);
		bundleDataItem.getDataMap().putLong("time",System.currentTimeMillis());
		PutDataRequest request = bundleDataItem.asPutDataRequest();

			if (!mGoogleApiClient.isConnected()) {
				return;
			}
		PendingResult<DataItemResult> result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);

	}
	public  void sendProfilePhoto(Bitmap bmp,String photoUrl){
		if(bmp==null || TextUtils.isEmpty(photoUrl))
		{
			return;
		}
		byte[] datas = ShareUtils.toByteArray(bmp);
		sendProfilePhoto(datas,photoUrl);
	}
	public void sendProfilePhoto(byte[] data,String photoUrl)
	{
		Log.i("smile", "sendProfilePhoto "+photoUrl);

		PutDataMapRequest dataMap = PutDataMapRequest.create(SyncProfile.PROFILE_PHOTO_PATH);
		if(photoUrl!=null && photoUrl!="")
		{
			dataMap.getDataMap().putString(SyncProfile.KEY_PROFILE_PHOTO_URL, photoUrl);
		}
		wrapProfilePhotoData(dataMap,data);
		dataMap.getDataMap().putLong("time", System.currentTimeMillis());
		PutDataRequest request = dataMap.asPutDataRequest();
		if (!mGoogleApiClient.isConnected()) {
			Log.i("sendProfilePhoto","setSyncECgData googleclient not connected");
			return;
		}
		Wearable.DataApi.putDataItem(mGoogleApiClient,request).setResultCallback(new ResultCallback<DataItemResult>() {
			@Override
			public void onResult(DataItemResult dataItemResult) {
				Log.i("sendProfilePhoto", "result: " + dataItemResult.getStatus().isSuccess());
			}
		});;
	}
	private  void wrapProfilePhotoData(PutDataMapRequest dataMap,byte[] data)
	{
		Asset asset = Asset.createFromBytes(data);
		dataMap.getDataMap().putAsset(SyncProfile.KEY_PROFILE_PHOTO, asset);
	}

}

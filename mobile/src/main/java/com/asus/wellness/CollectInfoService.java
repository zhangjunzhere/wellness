package com.asus.wellness;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.asus.wellness.WellnessLocationManager.LatLongCallback;
import com.asus.wellness.detectactivity.DetectActivityCallback;
import com.asus.wellness.detectactivity.DetectActivityManager;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.LocationChangeTable;
import com.asus.wellness.utils.LocationHelper;
import com.asus.wellness.utils.Utility;
import com.cmcm.common.statistics.CMAgent;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class CollectInfoService extends Service{
	DataLayerManager dataLayerManager;
	int i=3;//debug
	WellnessLocationManager wlm;
	
	public static final int METER_ERROR_THRESHOLD=300;//300 meter
	public static final int SPEED_ERROR_THRESHOLD=12;//12KM/hr
    public static final long REQUEST_LOCATION_TIMEOUT = 60*1000; // 1 min
	public static boolean enableLocationFunction=true;
	DetectActivityManager r;
	ExecutorService cachedThreadPool;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("circle", "collectinfoservice start");
		CMAgent.onServiceActive();
		cachedThreadPool = Executors.newCachedThreadPool();
	//	Log.d("circle","collectinfoservice start cachedThreadPool");
		dataLayerManager=new DataLayerManager(this);
		dataLayerManager.connectGoogleApiClient();
		
		if(Utility.DEBUG){
			WellnessLocationManager testWlm=new WellnessLocationManager(CollectInfoService.this, null);	
			//testWlm.startTrackingDistance();
		}
		
		wlm=new WellnessLocationManager(CollectInfoService.this, new LatLongCallback(){

			@Override
			public void onLocationChange(double lat, double lon, long time) {
				// TODO Auto-generated method stub
				Log.i("circle", "get district callback:" + lat + " " + lon);
				//new Thread(new GetDistrictRunnable(lat, lon, time)).start();
				cachedThreadPool.execute(new GetDistrictRunnable(lat, lon, time));
			}
			
		});
		
		r=new DetectActivityManager(this, new DetectActivityCallback(){
			
			public long mLastTime=0;
		//	static final long DELAY_GET_DISTRICT_TIME=5*60*1000;//5mins
		//	private long mLastGetDistrictTime=System.currentTimeMillis();

			@Override
			public void onStartDriving(long time) {
				// TODO Auto-generated method stub
//				TextView text=(TextView)findViewById(R.id.test1text);
//				text.setText("start drive");
			/*	Log.d("circle","debugstatus:start driving");
				wlm.startTrackingDistance();
				mLastTime=time;*/
			}

			@Override
			public void onEndDriving(long time) {
				// TODO Auto-generated method stub
//				MainWellness.this.runOnUiThread(new Runnable(){
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						TextView text=(TextView)findViewById(R.id.test1text);
//						text.setText("end drive");
//					}
//					
//				});
				/*long diff=time-mLastTime;
				int distance=wlm.endTrackingDistance();
				if(diff>60*1000){
					float speed=((float)distance/1000)/((float)diff/1000/60/60);
					if(!(distance<=METER_ERROR_THRESHOLD && speed<=SPEED_ERROR_THRESHOLD)){
						ContentValues cv=new ContentValues();
						cv.put(ActivityStateTable.COLUMN_START, mLastTime);
						cv.put(ActivityStateTable.COLUMN_END, time);
						cv.put(ActivityStateTable.COLUMN_TYPE, ActivityStateTable.TYPE_DRIVE);
						cv.put(ActivityStateTable.COLUMN_DISTANCE, distance);
						getContentResolver().insert(ActivityStateTable.TABLE_URI, cv);		
					}
				}
				Utility.updateLastUpdateTime(CollectInfoService.this, System.currentTimeMillis());
				Log.d("circle","debugstatus:end driving");*/
			}

			@Override
			public void onStartBicycle(long time) {
				// TODO Auto-generated method stub
//				TextView text=(TextView)findViewById(R.id.test1text);
//				text.setText("start bike");
				/*wlm.startTrackingDistance();
				mLastTime=time;
				Log.d("circle","debugstatus:start bike");*/
			}

			@Override
			public void onEndBicycle(long time) {
				// TODO Auto-generated method stub
//				MainWellness.this.runOnUiThread(new Runnable(){
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						TextView text=(TextView)findViewById(R.id.test1text);
//						text.setText("end bike");
//					}
//					
//				});
				/*long diff=time-mLastTime;
				int distance=wlm.endTrackingDistance();
				if(diff>60*1000){
					float speed=((float)distance/1000)/((float)diff/1000/60/60);
					if(!(distance<=METER_ERROR_THRESHOLD && speed<=SPEED_ERROR_THRESHOLD)){
						ContentValues cv=new ContentValues();
						cv.put(ActivityStateTable.COLUMN_START, mLastTime);
						cv.put(ActivityStateTable.COLUMN_END, time);
						cv.put(ActivityStateTable.COLUMN_TYPE, ActivityStateTable.TYPE_BIKE);
						cv.put(ActivityStateTable.COLUMN_DISTANCE, distance);
						getContentResolver().insert(ActivityStateTable.TABLE_URI, cv);		
					}
				}
				Utility.updateLastUpdateTime(CollectInfoService.this, System.currentTimeMillis());
				Log.d("circle","debugstatus:end bike");*/
			}

			@Override
			public void onActivityTypeReceive(String name, int nowStage) {
				// TODO Auto-generated method stub
				if(enableLocationFunction){
                    Log.d("circle","Not still.Get district:" + name);
					//if(!name.matches("still")){
					//	long nowTime=System.currentTimeMillis();
					//	Log.d("circle","Not still.Get district:"+(nowTime-mLastGetDistrictTime));
					//	if(nowTime-mLastGetDistrictTime>=DELAY_GET_DISTRICT_TIME){
                    //mLastGetDistrictTime=nowTime;
                        wlm.startTrackingDistrict();
                        new Handler().postDelayed(new Runnable(){

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                Log.i("circle","timeout:remove district update.");
                                wlm.removeDistrictUpdate();
                            }

                        }, REQUEST_LOCATION_TIMEOUT);
					//	}
				//	}
				}
				Calendar cal=Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				Log.i("circle","debugstatus:"+name+" "+cal.get(Calendar.YEAR)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.DATE)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
			}

			@Override
			public void onStartWalk(long time) {
				// TODO Auto-generated method stub
//				TextView text=(TextView)findViewById(R.id.test1text);
//				text.setText("start walk");
				mLastTime=time;
			}

			@Override
			public void onEndWalk(long time) {
				// TODO Auto-generated method stub
//				MainWellness.this.runOnUiThread(new Runnable(){
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						TextView text=(TextView)findViewById(R.id.test1text);
//						text.setText("end walk");
//					}
//					
//				});
				ContentValues cv=new ContentValues();
				cv.put(ActivityStateTable.COLUMN_START, mLastTime);
				cv.put(ActivityStateTable.COLUMN_END, time);
				cv.put(ActivityStateTable.COLUMN_TYPE, ActivityStateTable.TYPE_WALK);
				getContentResolver().insert(ActivityStateTable.TABLE_URI, cv);
			}

			@Override
			public void onStartMoving(long time) {
				// TODO Auto-generated method stub
				/*Log.d("circle","debugstatus:start moving");
				wlm.startTrackingDistance();
				mLastTime=time;*/
			}

			@Override
			public void onGetLastLocation(Location location) {
				if(location == null)
				{
					return;
				}
				Log.i("smile","onGetLastLocation "+location);
				cachedThreadPool.execute(new GetDistrictRunnable(location.getLatitude(), location.getLongitude(), System.currentTimeMillis()));
			}

		});
		r.startTrackingActivity();
		if(enableLocationFunction){
			registerNetworkListener();	
		}
	}
	
	private void registerNetworkListener(){
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkReceiver, intentFilter);
	}
	
	private BroadcastReceiver networkReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
		    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		    if ( activeNetInfo != null ){
		    	new LoadUnknownLocationTask(context).execute();
		    }
		}
		
	};
	
	private class GetDistrictRunnable implements Runnable{
		
		private double mLatitude=0;
		private double mLongitude=0;
		private long mGetLocationTime=0;
		
		public GetDistrictRunnable(double lat, double lon, long time){
			mLatitude=lat;
			mLongitude=lon;
			mGetLocationTime=time;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String district=wlm.getDistrict(mLatitude, mLongitude);
			Log.d("circle","get district and save into database:"+district);
			if(!district.matches(WellnessLocationManager.CANT_DETECT_DISTRICT)){

				ContentValues cv=new ContentValues();
				cv.put(LocationChangeTable.COLUMN_LOCATION_LATITUDE, mLatitude);
				cv.put(LocationChangeTable.COLUMN_LOCATION_LONGITUDE, mLongitude);
				cv.put(LocationChangeTable.COLUMN_GET_LOCATION_TIME, mGetLocationTime);
				cv.put(LocationChangeTable.COLUMN_DISTRICT, district);
				Uri uri = getContentResolver().insert(LocationChangeTable.TABLE_URI, cv);
			//	int id = Integer.valueOf(uri.getLastPathSegment());
			//	LocationHelper.getInstance(CollectInfoService.this).addLocationData(LocationHelper.getInstance(CollectInfoService.this).new LocationVal(id,mGetLocationTime, district));

            }
		}
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		r.stopTrackingActivity();
		dataLayerManager.disConnectGoogleApiClient();
		wlm.removeDistrictUpdate();
		unregisterReceiver(networkReceiver);
		Log.d("circle","collectinfoservice end");
	}
}

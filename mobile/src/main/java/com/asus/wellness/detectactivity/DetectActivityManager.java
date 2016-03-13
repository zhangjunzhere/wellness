package com.asus.wellness.detectactivity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.asus.wellness.utils.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;

public class DetectActivityManager extends BroadcastReceiver implements ConnectionCallbacks, OnConnectionFailedListener{

	private Context mContext;
	private GoogleApiClient mGoogleApiClient;
	private ActivityRecognition mActivityRecognitionClient;
    private PendingIntent mActivityRecognitionPendingIntent;
    private int mLastActivityType=DetectedActivity.UNKNOWN;
    private int mNowStage=DetectedActivity.UNKNOWN;
    private DetectActivityCallback mDetectActivityCallback;
    private Timer mCheckStopTimer=new Timer();
    private static final int CHECK_STOP_DELAY=3*60*1000;//3mins
    private long mStartBicycleTime=0;
    
    public static final int DetectedActivity_MOVING=111;
    int bikeNum=0, carNum=0;
    int stillNum=0, walkNum=0, footNum=0;
    boolean isRemoveUpdate=false;
    
    public static final String ACTION_SEND_ACTIVITY_TYPE="com.asus.wellness.activitystate";
    public static final String KEY_ACTIVITY_TYPE="activity_type";
	
	public DetectActivityManager(Context context, DetectActivityCallback detectActivityCallback){
		mContext=context;
		mDetectActivityCallback=detectActivityCallback;
	}
	
//	public ActivityRecognition getActivityRecognitionClient(){
//		return mActivityRecognitionClient;
//	}
	
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
//            GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) mContext, 0).show();
        	Toast.makeText(mContext, "google play service connect error", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
        Log.d("circle","requestActivityUpdates" + isRemoveUpdate);
        if(mGoogleApiClient == null)
            return;

		Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		Log.i("smile", "location: onConnected");
		if(location!=null && mDetectActivityCallback!=null)
		{
			mDetectActivityCallback.onGetLastLocation(location);
		}
        if (isRemoveUpdate) {
            if (mGoogleApiClient.isConnected()) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, createRequestPendingIntent());
                getRequestPendingIntent().cancel();
            }
        } else {
			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
					30000,
					createRequestPendingIntent());
        }
        mGoogleApiClient.disconnect();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

    
    private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
        if (null != getRequestPendingIntent()) {

            // Return the existing intent
            return mActivityRecognitionPendingIntent;

        // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService
            Intent intent = new Intent(mContext, ActivityRecognitionIntentService.class);

            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
            PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            setRequestPendingIntent(pendingIntent);
            return pendingIntent;
        }

    }
    
    /**
     * Sets the PendingIntent used to make activity recognition update requests
     * @param intent The PendingIntent
     */
    public void setRequestPendingIntent(PendingIntent intent) {
        mActivityRecognitionPendingIntent = intent;
    }
    
    /**
     * Returns the current PendingIntent to the caller.
     *
     * @return The PendingIntent used to request activity recognition updates
     */
    public PendingIntent getRequestPendingIntent() {
        return mActivityRecognitionPendingIntent;
    }

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if(arg1.getAction().matches(ACTION_SEND_ACTIVITY_TYPE)){
			int activityType=arg1.getIntExtra(KEY_ACTIVITY_TYPE, -4117);
			if(mDetectActivityCallback!=null){
				mDetectActivityCallback.onActivityTypeReceive(this.getNameFromType(activityType), mNowStage);
			}
			if(isActivityChange(activityType)){
				switch(activityType){
					case DetectedActivity.IN_VEHICLE:
//						mCheckStopTimer.cancel();
//						if(mNowStage==DetectedActivity.UNKNOWN){
//							mDetectActivityCallback.onStartDriving(System.currentTimeMillis());
//						}
//						else if(mNowStage==DetectedActivity.ON_BICYCLE){
//							mDetectActivityCallback.onStartDriving(mStartBicycleTime);
//						}
//						mNowStage=DetectedActivity.IN_VEHICLE;
////						if(mNowStage!=DetectedActivity.IN_VEHICLE){
////							//bicycle->vehicle
////							if(mNowStage==DetectedActivity.ON_BICYCLE){
//////								mDetectActivityCallback.onEndBicycle(System.currentTimeMillis());	
////								mDetectActivityCallback.onStartDriving(mStartBicycleTime);
////							}
////							else{
////								mDetectActivityCallback.onStartDriving(System.currentTimeMillis());	
////							}
////							mNowStage=DetectedActivity.IN_VEHICLE;
////						}
//						break;
					case DetectedActivity.ON_BICYCLE:
						mCheckStopTimer.cancel();
						if(mNowStage!=DetectedActivity_MOVING){
							mNowStage=DetectedActivity_MOVING;
							mDetectActivityCallback.onStartMoving(System.currentTimeMillis());	
						}
//						if(mNowStage==DetectedActivity.UNKNOWN){
//							mStartBicycleTime=System.currentTimeMillis();
//							mDetectActivityCallback.onStartBicycle(mStartBicycleTime);
//						}
//						else if(mNowStage==DetectedActivity.IN_VEHICLE){
//							break;
//						}
//						mNowStage=DetectedActivity.ON_BICYCLE;
////						if(mNowStage!=DetectedActivity.ON_BICYCLE){
////							//vehicle->bicycle
////							if(mNowStage==DetectedActivity.IN_VEHICLE){
//////								mDetectActivityCallback.onEndDriving(System.currentTimeMillis());
////								return;
////							}
////							mNowStage=DetectedActivity.ON_BICYCLE;
////							mStartBicycleTime=System.currentTimeMillis();
////							mDetectActivityCallback.onStartBicycle(mStartBicycleTime);	
////						}
						
						break;
						//only for test
//					case DetectedActivity.ON_FOOT:
//						mCheckStopTimer.cancel();
//						if(mNowStage!=DetectedActivity.ON_FOOT){
//							mNowStage=DetectedActivity.ON_FOOT;
//							mDetectActivityCallback.onStartWalk(System.currentTimeMillis());	
//						}
//						break;
					default:
						if(mNowStage==DetectedActivity_MOVING){
							mCheckStopTimer=new Timer();
							mCheckStopTimer.schedule(new TimerTask(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									if(mDetectActivityCallback!=null){
//										switch(mNowStage){
//											case DetectedActivity.IN_VEHICLE:
//												mDetectActivityCallback.onEndDriving(System.currentTimeMillis()-CHECK_STOP_DELAY);
//												break;
//											case DetectedActivity.ON_BICYCLE:
//												mDetectActivityCallback.onEndBicycle(System.currentTimeMillis()-CHECK_STOP_DELAY);
//												break;
//												//only for test
////											case DetectedActivity.ON_FOOT:
////												mDetectActivityCallback.onEndWalk(System.currentTimeMillis()-CHECK_STOP_DELAY);
////												break;
//										}
										if(mNowStage==DetectedActivity_MOVING){
											if(carNum>=bikeNum){
												mDetectActivityCallback.onEndDriving(System.currentTimeMillis()-CHECK_STOP_DELAY);
											}
											else{
												mDetectActivityCallback.onEndBicycle(System.currentTimeMillis()-CHECK_STOP_DELAY);
											}
										}
									}
									carNum=0;
									bikeNum=0;
									walkNum=0;
									stillNum=0;
									footNum=0;
									mNowStage=DetectedActivity.UNKNOWN;
								}
								
							}, CHECK_STOP_DELAY);
						}
						break;
				}
			}
			if(mNowStage==DetectedActivity_MOVING){
				switch(activityType){
				case DetectedActivity.IN_VEHICLE:
					carNum++;
					break;
				case DetectedActivity.ON_BICYCLE:
					bikeNum++;
					break;
				case DetectedActivity.WALKING:
					walkNum++;
					break;
				case DetectedActivity.ON_FOOT:
					footNum++;
					break;
				case DetectedActivity.STILL:
					stillNum++;
					break;
				}
				if(Utility.DEBUG){
					Toast.makeText(mContext, "carNum:"+carNum+" bikeNum:"+bikeNum+" walkNum:"+walkNum+" footNum:"+footNum+" stillNum:"+stillNum, Toast.LENGTH_SHORT).show();	
				}
			}
			//bicycle stage:
			mLastActivityType=activityType;
		}
	}
	
	public boolean isActivityChange(int currentType){
		int lastActivityType=DetectedActivity.UNKNOWN;
		switch(mLastActivityType){
			case DetectedActivity.ON_BICYCLE:
//				lastActivityType=DetectedActivity.ON_BICYCLE;
//				break;
			case DetectedActivity.IN_VEHICLE:
				lastActivityType=DetectedActivity_MOVING;
				break;
		}
		
		int mCurrentType=DetectedActivity.UNKNOWN;
		switch(currentType){
			case DetectedActivity.ON_BICYCLE:
//				mCurrentType=DetectedActivity.ON_BICYCLE;
//				break;
			case DetectedActivity.IN_VEHICLE:
				mCurrentType=DetectedActivity_MOVING;
				break;
		}
		
		if(lastActivityType!=mCurrentType){
			return true;
		}
		else{
			return false;
		}
	}
	
    private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.RUNNING:
            	return "running";
            case DetectedActivity.WALKING:
            	return "walking";
        }
        return "unknown";
    }
    
    public void startTrackingActivity(){
    	isRemoveUpdate=false;
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(ACTION_SEND_ACTIVITY_TYPE);
		mContext.registerReceiver(this, intentFilter);
		Log.d("circle", "1. startTrackingActivity: " + servicesConnected());
        if (servicesConnected()) {
			mGoogleApiClient =
                    new GoogleApiClient.Builder(mContext).addApi(ActivityRecognition.API)
							.addApi(LocationServices.API)
							.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();
			mGoogleApiClient.connect();
        }
    }
    
    public void stopTrackingActivity(){
    	isRemoveUpdate=true;
    	mContext.unregisterReceiver(this);	
		
		//activity client remove
		if (servicesConnected()) {
			if(mGoogleApiClient!=null && mGoogleApiClient.isConnected())
									mGoogleApiClient.disconnect();
		}
    }
}

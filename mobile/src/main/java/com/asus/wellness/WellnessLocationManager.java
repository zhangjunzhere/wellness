package com.asus.wellness;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.asus.wellness.utils.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class WellnessLocationManager{
	
	private LocationManager mLocationMgr;
	private double mDistance=0;
	private double lastLatitude=0;
	private double lastLongitude=0;
	private boolean mMoving=false;
	private static final int ACCURACY_TO_READ_GPS=50;
	private static final int ACCURACY_TO_READ_OTHER=150;
	public static final String DEFAULT_DISTRICT="UNKNOWN";
	public static final String OFF_POSITIONING="offpositioning";
	public static final String CANT_DETECT_DISTRICT="CANT_DETECT";
	Geocoder geoCoder;
	private Context mContext;
	public LatLongCallback mLatLongCallback;
	
	public interface LatLongCallback{
		public void onLocationChange(double lat, double lon, long time);
	}
	
	public WellnessLocationManager(Context context, LatLongCallback llCallback){
		mLocationMgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		geoCoder=new Geocoder(context);
		mContext=context;
		mLatLongCallback=llCallback;
//		//get district
//		try {
//			List<Address> lAddress=geoCoder.getFromLocation( 24.238250, 120.562463, 10);
//			for(int i=0;i<lAddress.size();i++){
//				Address address=lAddress.get(i);
//				if(address.getLocality()!=null){
//					Log.d("circle","test get local:"+address.getLocality());
//					mDistrict=address.getLocality();
//				}
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public void listAllProvider(){
		List<String> l=mLocationMgr.getAllProviders();
		for(String provider:l){
			Log.d("circle",provider);
		}
	}

/*	public void startTrackingDistance(){
		mMoving=true;//Indicating moving stage.
		mDistance=0;
		lastLatitude=0;
		lastLongitude=0;
		Log.i("circle","start tracking distance:moving start");

        if (mLocationMgr.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
            mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 15, mLocationListener);
        } else if (mLocationMgr.getProvider(LocationManager.GPS_PROVIDER) != null) {
            mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 15, mLocationListener);
        }
	}*/
	
	/*public int endTrackingDistance(){
		mLocationMgr.removeUpdates(mLocationListener);
		mMoving=false;//Indicating non-moving stage.
		Log.i("circle","end tracking distance:moving end:distance:"+mDistance);
		return (int) mDistance;
	}*/
	
	public void startTrackingDistrict(){
		if(!mMoving){
			Log.i("circle","start tracking district:not moving");
			try {
				if (mLocationMgr.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
//	        	mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 50, mDistrictListener);
					mLocationMgr.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mDistrictListener, null);
				} /*else if (mLocationMgr.getProvider(LocationManager.GPS_PROVIDER) != null) {
//	        	mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 50, mDistrictListener);
                mLocationMgr.requestSingleUpdate(LocationManager.GPS_PROVIDER, mDistrictListener, null);
            }*/
			}catch (Exception e)
			{

				e.printStackTrace();
			}
		}
	}

	public void removeDistrictUpdate(){
		mLocationMgr.removeUpdates(mDistrictListener);
	}
	
	public double getLastLatitude(){
		return lastLatitude;
	}
	
	public double getLastLongitude(){
		return lastLongitude;
	}
	
	public int getDistance(){
		return (int) mDistance;
	}
	
	public String getDistrict(double latitude, double longitude){
		//get district
		String district=CANT_DETECT_DISTRICT;
		try {
			List<Address> lAddress=geoCoder.getFromLocation( latitude, longitude, 10);
			for(int i=0;i<lAddress.size();i++){
				Address address=lAddress.get(i);
				if(address!=null){
					if(address.getLocality()!=null){
						district=address.getLocality();
						break;
					}	
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return DEFAULT_DISTRICT;
		}
		return district;
	}
	
	public LocationListener mDistrictListener=new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
//			if(location.hasAccuracy()){
//				if((location.getProvider().matches(LocationManager.GPS_PROVIDER)&&location.getAccuracy()<ACCURACY_TO_READ_GPS)
//						||(!location.getProvider().matches(LocationManager.GPS_PROVIDER)&&location.getAccuracy()<ACCURACY_TO_READ_OTHER)){
					if(mLatLongCallback!=null){
						mLatLongCallback.onLocationChange(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
					}
//				}
//			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Log.d("circle","remove mDistrictListener:"+provider);
			mLocationMgr.removeUpdates(this);
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public LocationListener mLocationListener=new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			if(Utility.DEBUG){
				String message="provider:"+location.getProvider()+" hasaccuracy:"+location.hasAccuracy()+" accuracy:"+location.getAccuracy()+" lat:"+location.getLatitude()+" long:"+location.getLongitude()+" speed:"+location.getSpeed();
				Log.d("circle",message);
				Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
			}
			if(location.hasAccuracy()){
				if(location.getProvider().matches(LocationManager.NETWORK_PROVIDER)&&location.getAccuracy()<ACCURACY_TO_READ_OTHER){
					float [] result = new float[3];
					if(lastLatitude==0||lastLongitude==0){
						lastLatitude=location.getLatitude();
						lastLongitude=location.getLongitude();
					}
					Location.distanceBetween(lastLatitude, lastLongitude, location.getLatitude(), location.getLongitude(), result);
					lastLatitude=location.getLatitude();
					lastLongitude=location.getLongitude();
					mDistance+=result[0];
				}
			}
			
			if(mLatLongCallback!=null){
				mLatLongCallback.onLocationChange(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Log.d("circle","remove mLocationListener:"+provider);
			mLocationMgr.removeUpdates(this);
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	};
}

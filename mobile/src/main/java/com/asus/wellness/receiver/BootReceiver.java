package com.asus.wellness.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asus.wellness.CollectInfoService;
import com.asus.wellness.R;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.ui.setting.SettingActivity;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().matches(Intent.ACTION_BOOT_COMPLETED)){
			Cursor cursor=context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
			if(cursor != null && cursor.moveToFirst()){
				SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
				boolean isLocationEnable=SettingActivity.checkLocationServiceEnable(context);
				boolean isActivityTrackingEnable=sp.getBoolean(context.getString(R.string.pref_key_location), context.getResources().getBoolean(R.bool.default_location));
				if(isLocationEnable && isActivityTrackingEnable){
					Log.d("circle","boot to start service");
					Intent serviceIntent=new Intent(context, CollectInfoService.class);
					context.startService(serviceIntent);	
				}
                cursor.close();
			}

		}
		else if(intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)){
			Intent serviceIntent=new Intent(context, CollectInfoService.class);
			SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
			boolean isLocationEnable=SettingActivity.checkLocationServiceEnable(context);
			boolean isActivityTrackingEnable=sp.getBoolean(context.getString(R.string.pref_key_location), context.getResources().getBoolean(R.bool.default_location));
			if(isLocationEnable && isActivityTrackingEnable){
				context.startService(serviceIntent);
			}
			else{
				context.stopService(serviceIntent);
			}
		}
	}

}

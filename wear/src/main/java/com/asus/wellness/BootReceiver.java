package com.asus.wellness;

import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().matches(Intent.ACTION_BOOT_COMPLETED)){	
			//start service to collect step count
			Intent serviceIntent=new Intent(context, CollectStepCountService.class);
			context.startService(serviceIntent);
			
			if(BuildConfig.DEBUG){
				Log.d(Utility.TAG,"boot complete:set KEY_LAST_STEP_NUMBERS to zero");
			}
		}
	}

}

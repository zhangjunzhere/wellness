package com.asus.wellness.knockknock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KnockKnockHeartRateReceiver extends BroadcastReceiver {
	public static final String START_ACTIVITY_HEART_RATE="/start_activity_heart_rate";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("circle","Knock Knock heart rate");
//		DataLayerManager dataLayerManager=new DataLayerManager(context);
//		dataLayerManager.connectGoogleApiClient();
//		dataLayerManager.sendMessageToPhone(START_ACTIVITY_HEART_RATE, "");
	}

}

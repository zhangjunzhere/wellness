package com.asus.wellness.knockknock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KnockKnockStressReceiver extends BroadcastReceiver {
	public static final String START_ACTIVITY_STRESS="/start_activity_stress";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("circle","Knock Knock stress");
//		DataLayerManager dataLayerManager=new DataLayerManager(context);
//		dataLayerManager.connectGoogleApiClient();
//		dataLayerManager.sendMessageToPhone(START_ACTIVITY_STRESS, "");
	}

}

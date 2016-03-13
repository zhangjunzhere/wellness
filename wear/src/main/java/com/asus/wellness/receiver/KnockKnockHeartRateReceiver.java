package com.asus.wellness.receiver;

import com.asus.wellness.MeasureActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KnockKnockHeartRateReceiver extends BroadcastReceiver {
	public static final String START_ACTIVITY_HEART_RATE="/start_activity_heart_rate";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("circle","wearable Knock Knock heart rate");
    	Intent intentStartHeartRate=new Intent(context, MeasureActivity.class);
    	intentStartHeartRate.putExtra(MeasureActivity.KEY_START_HEART_RATE, true);
    	intentStartHeartRate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(intentStartHeartRate);
	}

}

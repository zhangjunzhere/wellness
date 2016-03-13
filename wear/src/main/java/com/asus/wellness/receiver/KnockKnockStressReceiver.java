package com.asus.wellness.receiver;

import com.asus.wellness.MeasureActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KnockKnockStressReceiver extends BroadcastReceiver {
	public static final String START_ACTIVITY_STRESS="/start_activity_stress";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("circle","wearable Knock Knock stress");
    	Intent intentStress=new Intent(context, MeasureActivity.class);
    	intentStress.putExtra(MeasureActivity.KEY_START_RELAXATION, true);
    	intentStress.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(intentStress);
	}

}

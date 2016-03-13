package com.asus.wellness.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.asus.wellness.service.CollectStepCountService;

/**
 * Created by smile_gao on 2015/8/24.
 */
public class PackageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("smile packageReceiver 25",intent.getAction().toString());
        if(intent.getAction().matches(Intent.ACTION_PACKAGE_REPLACED)){
            Intent serviceIntent=new Intent(context, CollectStepCountService.class);
            context.startService(serviceIntent);
        }
    }
}

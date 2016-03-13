package com.asus.wellness.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.utils.WearDeviceType;
import com.cmcm.common.statistics.CMAgent;

import de.greenrobot.event.EventBus;

public class BaseActivity extends FragmentActivity {

	public boolean isSw800=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if(getResources().getConfiguration().smallestScreenWidthDp<800){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			isSw800=false;
		}
		else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			isSw800=true;
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
		CMAgent.onPageStart(getPageName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
		CMAgent.onPageEnd(getPageName());
    }

    public void onEvent(Device device ){}

	public  String getPageName(){
		return  BaseActivity.class.getSimpleName();
	}

}

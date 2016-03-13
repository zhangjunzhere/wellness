package com.asus.wellness;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.ui.setting.SettingActivity;
import com.asus.wellness.utils.DeviceHelper;
import com.asus.wellness.utils.LocationHelper;
import com.asus.wellness.utils.MySharePreferences;
import com.asus.wellness.utils.WearDeviceType;
import com.cmcm.common.statistics.CMAgent;
import com.cmcm.common.statistics.CMEnvironment;
import com.cmcm.common.statistics.ReportLevel;

import de.greenrobot.event.EventBus;


/**
 * Created by smile_gao on 2015/5/14.
 */
public class WApplication extends Application {
    private static final String TAG = "WApplication";
    private static WApplication  wellnessApplication;

    private DataHelper dataHelper;
    private Device  mConnectedDevice ;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        wellnessApplication = this;
        dataHelper=  DataHelper.getInstance(this);

        mConnectedDevice = DeviceHelper.getDefaultDevice();
        initLocation();

        CMAgent.initialize(new CMEnvironment() {
            @Override
            public Context getAppContext() {
                return WApplication.this;
            }

            @Override
            public boolean isDebugMode() {
                return false;
            }

            @Override
            public ReportLevel getReportLevel() {
                boolean inSpire = getSharedPreferences(SettingActivity.KEY_GA, MODE_PRIVATE).getBoolean(SettingActivity.IS_APP_OPT_OUT, true);
                if (inSpire)
                    return CmHelper.UPLOAD_ALL;
                else
                    return CmHelper.UPLOAD_NONE;
            }
        });
        CMAgent.onServiceActive();

       // EventBus.getDefault().register(this);
    }
    private  void initLocation()
    {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                LocationHelper.init(wellnessApplication);
                Log.i("smile","init location");
                return null;
            }
        };
        task.execute();
    }
    public DataHelper getDataHelper(){ return dataHelper;}

//    public void notifiyWatchChange()
//    {
//        EventBus.getDefault().postSticky(new WearDeviceType(isZenWatchRobin()));
//    }

    public void notifiyWatchChange(Device device)
    {
        EventBus.getDefault().postSticky(device);
    }


    public static WApplication getInstance(){
        return wellnessApplication;
    }

    public Device getConnectedDevice(){return  mConnectedDevice;}
    public void setConnectedDevice(Device device){mConnectedDevice = device;}

}

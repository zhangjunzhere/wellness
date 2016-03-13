package com.asus.wellness.utils;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by smile_gao on 2015/7/8.
 */
public class WearNameHelper {

    private  static String mWearName = null;
    private  static String mBlueAddr = null;
    private static Device mDevice = new Device(null,WearNameHelper.getWearName(),WearNameHelper.getBlueAddr(),WearDeviceType.isRobin(),null,null,null);
    private static  Device mOriginDevice = new Device(null,WearNameHelper.getWearName(),WearNameHelper.getBlueAddr(),WearDeviceType.isRobin(),null,null,null);
    public static Device getDevice()
    {
        return mDevice;
    }
    public static Device getOriginDevice()
    {
        return mOriginDevice;
    }
    public static String getWearName()
    {
        if(mWearName != null)
        {
            return  mWearName;
        }
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        String wearName = btAdapter.getName();

        // Reconstitute the pairing device name from the model and the last 4 digits of the bluetooth MAC
        if(wearName == null)
        {
            wearName = "no bluetooth";
        }
        mWearName = wearName;
        return mWearName;
    }
    public static String getBlueAddr()
    {
        if(mBlueAddr != null)
        {
            return  mBlueAddr;
        }
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        String wearName =btAdapter.getAddress();
        if(wearName == null)
        {
            wearName = "no bluetooth";
        }
        mBlueAddr = wearName;
        return mBlueAddr;
    }
}

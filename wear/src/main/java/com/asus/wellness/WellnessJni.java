package com.asus.wellness;


import android.util.Log;

public class WellnessJni {
	public static final String TAG = "AsusWellnessJNI";
	public static final String PKG = "com.asus.wellness";
//	private SharedPreferences prefs;
//	private SharedPreferences.Editor editor;
	
	static {
		System.loadLibrary("wellness_jni");
	}
	native static boolean adjWellness(int wellness_en);
	native static int adjWellnessGet(int wellness_en);

	public WellnessJni() {

	}

    public int getWellness() {
		int ret = 0;

		ret = adjWellnessGet(0);
		if (ret < 0)
			Log.e(TAG, "Error: ioctl failed, please check your software version");
		else
		   	Log.d(TAG, "Set wellness sucessfully");

    	return ret;
    }

    public void setWellness(int val) {
    	if (!adjWellness(val)) {
    		Log.e(TAG, "Error: ioctl failed, please check your software version");
    	}

    	Log.d(TAG, "Set wellness sucessfully");
    }
}
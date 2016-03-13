package com.asus.wellness.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import java.text.DecimalFormat;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.StepCountManager;
import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.sleep.SleepActionFragment;
import com.asus.wellness.view.MyTextView;

public class Utility {
    public static final int TARGET_GOAL = 7000;
	public static final boolean DEBUG=false;
	public static final String TAG="WellnessWearDebug";
	public static int getRelaxLevelDrawableId(int relaxValue){
		int [] drawableId=new int[]
				{R.drawable.asus_wellness_ic_energy_expression1
				, R.drawable.asus_wellness_ic_energy_expression2
				, R.drawable.asus_wellness_ic_energy_expression3
				, R.drawable.asus_wellness_ic_energy_expression4
				, R.drawable.asus_wellness_ic_energy_expression5};
		return drawableId[getFiveLevel(relaxValue)];
	}
	
	public static int getStressLevelDrawableId(int stressValue){
		int [] drawableId=new int[]
				{R.drawable.asus_wellness_ic_stress1
				, R.drawable.asus_wellness_ic_stress2
				, R.drawable.asus_wellness_ic_stress3
				, R.drawable.asus_wellness_ic_stress4
				, R.drawable.asus_wellness_ic_stress5};
		return drawableId[getFiveLevel(stressValue)];
	}
	
	public static int getFiveLevel(int value){
		if(value>=0 && value<20){
			return 0;
		}
		else if(value>=20 && value<40){
			return 1;
		}
		else if(value>=40 && value<60){
			return 2;
		}
		else if(value>=60 && value<80){
			return 3;
		}
		else if(value>=80 && value<=100){
			return 4;
		}
		return 2;
	}
	
	public static String getRelaxLevelString(Context context, int value, int index){
		int level=getFiveLevel(value);
		Random random=new Random();
		String [] text = null;
		switch(level){
			case 0:
			case 1:
				text=context.getResources().getStringArray(R.array.relax_level_1_2); 
				break;
			case 2:
				text=context.getResources().getStringArray(R.array.relax_level_3);
				break;
			case 3:
			case 4:
				text=context.getResources().getStringArray(R.array.relax_level_4_5);
				break;
		}
		return text[index];
	}
	
	public static int getRelaxLevelStringIndex(Context context, int value){
		int level=getFiveLevel(value);
		Random random=new Random();
		String [] text = null;
		switch(level){
			case 0:
			case 1:
				text=context.getResources().getStringArray(R.array.relax_level_1_2); 
				break;
			case 2:
				text=context.getResources().getStringArray(R.array.relax_level_3);
				break;
			case 3:
			case 4:
				text=context.getResources().getStringArray(R.array.relax_level_4_5);
				break;
		}
		return random.nextInt(text.length);
	}
	
	public static String getStressLevelString(Context context, int value){
		int level=getFiveLevel(value);
		Random random=new Random();
		String [] text = null;
		switch(level){
			case 0:
			case 1:
				text=context.getResources().getStringArray(R.array.stress_level_1_2); 
				break;
			case 2:
				text=context.getResources().getStringArray(R.array.stress_level_3);
				break;
			case 3:
			case 4:
				text=context.getResources().getStringArray(R.array.stress_level_4_5);
				break;
		}
		return text[random.nextInt(text.length)];
	}
	
	public static String getIntensityLevel(Context context, int hrBpm, int index){
		return context.getResources().getStringArray(R.array.heart_rate_level)[index];
	}
	
	public static int getIntensityLevelIndex(Context context, int hrBpm){
		Cursor cursor=context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
		int intensity=-1;
		int age=ProfileTable.DEFAULT_AGE;
		if(cursor.moveToFirst()){
			age=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_AGE));
		}
		cursor.close();
		float density=(float)hrBpm/(220-age);
		
		if(density>=0 && density<0.6){
			intensity =0;
		}
		else if(density>=0.6 && density<0.7){
			intensity = 1;
		}
		else if(density>=0.7 && density<0.8){
			intensity = 2;
		}
		else if(density>=0.8 && density<0.9){
			intensity =3;
		}
		else if(density>=0.9){
			intensity = 4;
		}	
		return intensity;
	}
	
	public static String getIntensityLevelIndex(Context context, int hrBpm, int index){
		Cursor cursor=context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
		String intensity="error";
		int age=ProfileTable.DEFAULT_AGE;
		if(cursor.moveToFirst()){
			age=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_AGE));
		}
		cursor.close();
		float density=(float)hrBpm/(220-age);
		
		if(density>=0 && density<0.6){
			intensity = context.getResources().getStringArray(R.array.heart_rate_level)[0];
		}
		else if(density>=0.6 && density<0.7){
			intensity = context.getResources().getStringArray(R.array.heart_rate_level)[1];
		}
		else if(density>=0.7 && density<0.8){
			intensity = context.getResources().getStringArray(R.array.heart_rate_level)[2];
		}
		else if(density>=0.8 && density<0.9){
			intensity = context.getResources().getStringArray(R.array.heart_rate_level)[3];
		}
		else if(density>=0.9){
			intensity = context.getResources().getStringArray(R.array.heart_rate_level)[4];
		}	
		return intensity;
	}
	
	public static String getDateTime(long milli, String template) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				template, Locale.getDefault());
		Date date = new Date();
		date.setTime(milli);
		return dateFormat.format(date);
	}
	



	public static float cmToFt(float cm) {
		return (0.0328083f * cm);
	}

	public static float ftToInch(float ft) {
		return 12 * ft;
	}


	public static float InchToFt(float inch){
		return inch/12;
	}
	
	public static float ftToCm(float ft){
		return (30.48f*ft);
	}
	public static float kgToLbs(float kg) {
		return (kg * 2.20462262f);
	}
	public static float LbsToKg(float lbs){
		return (lbs*0.45359237f);
	}
	
	public static String commaNumber(long value){
		DecimalFormat formatter = new DecimalFormat("#,###,###");
		String formattedString = formatter.format(value);
		return formattedString;
	}

	//smile
	public static void printDp(Activity context)
	{
		DisplayMetrics metric = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;
		int height = metric.heightPixels;
		float density = metric.density;
		int densityDpi = metric.densityDpi;
		int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;
		Log.i("emily", "w = " + width + ", h = " + height + ", density = " + density + ", densityDpi = " + densityDpi + ", smallestScreenWidthDp  = " + smallestScreenWidthDp);

	}
	public static int getScreenWidth(Activity context)
	{
		DisplayMetrics metric = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;
		return  width;
	}

	public static int getScreenHeight(Activity context)
	{
		DisplayMetrics metric = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int height = metric.heightPixels;
		return  height;
	}



	public static int measureText(Context context ,String text, float textSizeDip){
		Paint paint = new Paint();
		paint.setTextSize(textSizeDip);
		float width = paint.measureText(text);
		return dip2px(context,width);
	}

	public static int measureText(Context context ,String text, int textSize){
		float densityMultiplier = context.getResources().getDisplayMetrics().density;
		final float scaledPx = textSize * densityMultiplier;
		Paint paint = new Paint();
		paint.setTextSize(scaledPx);
		float width = paint.measureText(text);
		return dip2px(context,width);
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static String formatTime(long seconds, boolean ignoreSecond){
		long m = seconds/60%60;
		long h = seconds/3600;
		if(ignoreSecond){
			return  String.format("%02d:%02d",h,m);
		}else {
			return formatTime(seconds);
		}
	}

	public static String formatTime(long seconds){
		long s = seconds%60;
		long m = seconds/60%60;
		long h = seconds/3600;

		return String.format("%02d:%02d:%02d",h,m,s);
	}

	public static String[] getModelArray(String format, int max){
		String arr[] = new String[max];
		for(int i =0; i < max; i++){
			arr[i] = String.format(format,i);
		}
		return arr;
	}


	public static void fitFontSizeForView(TextView txtView , int fontsize, int maxwidth)
	{
		if(txtView==null)
		{
			return  ;
		}
		if(txtView instanceof MyTextView)
		{
			Log.i("smile","MyTextView ");
			return;
		}
		float size =  txtView.getTextSize();
		if(size<fontsize)
		{
			size = fontsize;
		}
		int measureWidth = txtView.getWidth();
		if(measureWidth<=0)
		{
			txtView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			measureWidth = txtView.getMeasuredWidth();
		}

		Log.i("smile","maxwidth: "+maxwidth+" measureWidth: "+measureWidth+" fontsize "+fontsize);
		if(measureWidth>= maxwidth)
		{
			size = size* maxwidth/measureWidth;
			size-=0.3;
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
			txtView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			measureWidth = txtView.getMeasuredWidth();
		}
		Log.i("smile","maxwidth: "+maxwidth+" measureWidth: "+measureWidth+" fontsize "+fontsize);
		while (measureWidth>=maxwidth)
		{
    		size= size -1;
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
			txtView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			measureWidth = txtView.getMeasuredWidth();
			Log.i("smile","size: "+size);
		}
    }
	public static void  adjustTextSizePx(final TextView  tv, final float textSizePx, final float margin){
		Activity context = (Activity)tv.getContext();
		int tv_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int tv_height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		tv.measure(tv_width, tv_height);

		DisplayMetrics metric = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metric);

		int width = Math.min(tv.getMeasuredWidth(), (metric.widthPixels - dip2px(context,margin)));

		Paint paint = new Paint();
		paint.setTextSize(textSizePx);
		String text = tv.getText().toString();
		final float stepsWidth=paint.measureText(text);
		float ratio = 1.0f;
		if(width < stepsWidth){
			ratio = width/stepsWidth;
		}
	//	int dp = px2dip(context,textSizePx * ratio);
	//	Log.i("smile","adjustTextSizePx "+textSizePx+" ratio:"+ratio+" tvwidth: "+tv.getMeasuredWidth()+" width "+width+" size dp:  "+dp);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx * ratio);
	}
    public static void  adjustTextSize(final TextView  tv, final float origSizeDp, final float margin){
		Activity context = (Activity)tv.getContext();
		int textSizePx = dip2px(context,origSizeDp);
		adjustTextSizePx(tv,textSizePx,margin);
	}


	public static void  adjustTextSize2(final TextView  tv, final float origSize, final float maxWidth){
		Activity context = (Activity)tv.getContext();
		int textSizePx = dip2px(context, origSize);

		int tv_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int tv_height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		tv.measure(tv_width, tv_height);

		int width = Math.min(tv.getMeasuredWidth(), dip2px(context,maxWidth));
		Paint paint = new Paint();
		paint.setTextSize(textSizePx);
		String text = tv.getText().toString();
		final float stepsWidth=paint.measureText(text);
		float ratio = 1.0f;
		if(width < stepsWidth){
			ratio = width/stepsWidth;
		}

		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx * ratio);
	}

	public static  void  startSingleActivity(Context context, Class<?>  classActivity){
		if(context== null)
		{
			return;
		}
		Intent intent =   new Intent(context, classActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		if(context!=null)
			context.startActivity(intent);
	}
	public static boolean isSupportAsusEcg(Context context)
	{
		Log.i("smile","isSupportAsusEcg: "+WearDeviceType.isRobin());
		return  WearDeviceType.isRobin();
//		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//		for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
//			//Log.i("smile","type: "+sensor.getType()+" stringtype: "+sensor.getStringType()+" name: "+sensor.getName());
//			if(sensor.getStringType().equals("com.asus.android_wear.mood"))
//			{
//				return true;
//			}
//		}
//		return  false;
	}
	public static boolean isSupportSleepSensor(Context context)
	{
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//		for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
//			Log.i("smile","sensor: "+sensor.getName()+" "+sensor.getStringType()+" "+sensor.getVendor()+" "+sensor.getType());
//		}
		for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
			if(sensor.getType() == StepCountManager.PNI_TYPE_SLEEP_ACTIVITY){
				return  true;
			}
		}
		return false;
	}

	public static long getMidnightMilles(long now){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		cal.add(Calendar.HOUR_OF_DAY, -cal.get(Calendar.HOUR_OF_DAY));
		return getHourMilles(cal.getTimeInMillis());
	}

	public static long getHourMilles(long now){
		long hour = now/StepCountManager.ONE_HOUR_MILLIES * StepCountManager.ONE_HOUR_MILLIES;
		SimpleDateFormat dateFormat =  new SimpleDateFormat(StepCountManager.DATE_FORMAT);
		Log.d(TAG, "getHourMilles : " + dateFormat.format(hour) + " now " + dateFormat.format(now));
		return hour;
	}
	public static boolean isPowerConnected(Context context)
	{
		Intent batteryIntent = context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		if(batteryIntent !=null )
		{
			int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
			Log.i("smile","isPowerConnected "+status);
			if(status == BatteryManager.BATTERY_STATUS_CHARGING|| status == BatteryManager.BATTERY_STATUS_FULL)
			{
				return  true;
			}
		}
		return  false;
	}
	public static boolean isUsbConnected(Context context) {
		Intent intent = context.registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
		boolean ret = intent.getExtras().getBoolean("connected");
		Log.i("smile","isUsbConnected "+ret);
		return  ret;
	}
}

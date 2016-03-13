package com.asus.wellness;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.ecg.EcgCallback;
import com.asus.ecg.EcgManager;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.microprovider.EcgTable;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.view.CircleProgressBar;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.MessageEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MeasureActivity extends Activity implements MessageListener, SensorEventListener{
	private final String TAG = "MeasureActivity";
	private EcgManager mEcgManager;
	private int measureType=TYPE_RELAX;
	private CountDownTimer countDownTimer, noTouchTimer, noValueTimeOutTimer;
	//private int HEARTRATE_MEASURE_TIMEOUT=Utility.DEBUG?(1000*1000):(20*1000);//20 seconds
	//private int STRESS_BODY_MEASURE_TIMEOUT=Utility.DEBUG?(1000*1000):(35*1000);//35 seconds
	//private int NO_VALUE_TIMEOUT=15*1000;//15 seconds.
	private Handler repeatAnimationHandler=new Handler();
	
	public static final int TYPE_RELAX=0;
	public static final int TYPE_STRESS=1;
	public static final int TYPE_HEARTRATE=2;
	public static final String KEY_START_HEART_RATE="key_start_heart_rate";
	public static final String KEY_START_RELAXATION="key_start_relaxation";
	public static final String ACTION_MEASURE_INFO="action_measure_info";
	public static final String PREF_KEY_FIRST_MEASURE="pref_key_first_measure";
	
	DataLayerManager ecgDataLayer;
	public static final String START_DOWNLOAD_WELLNESS_FROM_PHONE="start_download_wellness_from_home";
	
	private CircleProgressBar mCircleProgressBar;

    private int mHeartRateValue = 0;
    private int mEnergyValue = 0;

    private int rrCount = 0;
    private List<Integer> mLayoutIds;

    enum STATE {
        IDLE, TOUCHED, MEASURING
    }
    private STATE mState = STATE.IDLE;

    SensorManager sensorManager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //smile_gao add for ioctl wellness,before register, setWellness =1 2015.01.19
            String returnStr = getProcWellNessVal();
	        Log.d("circle","returnStr:"+returnStr);
        if(returnStr != "") {
            int nodeval = Integer.valueOf(returnStr);
            if ( nodeval >= 0 && nodeval < 2) {
                registerHeartRateSensor();
            } else if (nodeval >= 2) {
                WellnessJni wellnessjni = new WellnessJni();
                Log.d("circle", "smile oncreate 1:" + String.valueOf(wellnessjni.getWellness()));
                if (wellnessjni.getWellness() == 0) {
                    wellnessjni.setWellness(1);
                }
                Log.d("circle", "smile oncreate 2:" + String.valueOf(wellnessjni.getWellness()));
                registerHeartRateSensor();
            }
        }
        //end smile //smile_gao add for ioctl wellness 2015.01.19

		setContentView(R.layout.activity_measure_layout);

		setTimer(5000);

		ecgDataLayer= DataLayerManager.getInstance(this);
		ecgDataLayer.connectGoogleApiClient(null);
		
		Intent intent=getIntent();
		if(intent.getAction()!=null && intent.getAction().matches("vnd.google.fitness.VIEW")){
			if(intent.getType().matches("vnd.google.fitness.data_type/com.google.heart_rate.bpm")){
				startMeasureHeartRate(null);	
			}
		}
		else{
			if(intent.hasExtra(KEY_START_HEART_RATE)){
				if(intent.getBooleanExtra(KEY_START_HEART_RATE, false)){
					startMeasureHeartRate(null);
				}
			}
			else if(intent.hasExtra(KEY_START_RELAXATION)){
				if(intent.getBooleanExtra(KEY_START_RELAXATION, false)){
					startMeasureBody(null);
				}
			}	
		}
	}
	private String getProcWellNessVal()
    {
        String returnStr = "";
        try {
            String Writecmd = "proc/wellness";

            String[] cmd = {"system/bin/cat", "", Writecmd};

            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                returnStr += line;
            }
        }catch (IOException e)
        {

        }
        return  returnStr;
    }
	private void setTimer(long timeInMilli){
		noTouchTimer=new CountDownTimer(timeInMilli, 1000) {
			@Override
			public void onFinish() {
				finish();
			}
			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub
			}
		}.start();
	}
	
	public void startMeasureBody(View view){
		measureType=TYPE_RELAX;
		readyToStartMeasure();
	}
	
	public void startMeasureStress(View view){
		measureType=TYPE_STRESS;
		readyToStartMeasure();
	}
	
	public void startMeasureHeartRate(View view){
		measureType=TYPE_HEARTRATE;
		readyToStartMeasure();
	}
	
	public void registerHeartRateSensor(){//for turning on neurosky sensor.
		sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void unRegisterHeartRateSensor(){//for turning off neurosky sensor.
		sensorManager.unregisterListener(this);
	}
	
	public void readyToStartMeasure(){
		noTouchTimer.cancel();
		setTimer(10*1000);
		mEcgManager=new EcgManager(this, new EcgCallback(){
			@Override
			public void onTouchSensor() {
//				mEcgManager.connectEcgSensor();
				noTouchTimer.cancel();
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				repeatAnimationHandler.removeCallbacks(repeatAnimationRunnable);

                showLayout(R.id.ecg_hr_measure_layout);

                findViewById(R.id.ecg_hr_data_container).setVisibility(View.INVISIBLE);
                findViewById(R.id.ecg_relax_container).setVisibility(View.INVISIBLE);
                if (measureType == TYPE_RELAX || measureType == TYPE_STRESS){
                	findViewById(R.id.ecg_relax_startup_container).setVisibility(View.VISIBLE);
                }else{
                	findViewById(R.id.ecg_hr_startup_container).setVisibility(View.VISIBLE);
                }
                

                mState = STATE.TOUCHED;
                rrCount = 0;

                if(measureType == TYPE_HEARTRATE) {
                    //Animation anim = AnimationUtils.loadAnimation(MeasureActivity.this, R.anim.loading_anim);
                    //anim.setInterpolator(new LinearInterpolator());
                    //findViewById(R.id.ecg_hr_startup_circle).startAnimation(anim);
                    Animation anim = AnimationUtils.loadAnimation(MeasureActivity.this, R.anim.heart_beat_anim);
                    findViewById(R.id.ecg_hr_startup_heart).startAnimation(anim);
                }else if (measureType == TYPE_RELAX || measureType == TYPE_STRESS){
                	mCircleProgressBar = (CircleProgressBar) findViewById(R.id.ecg_relax_startup_circle); 
                	mCircleProgressBar.startProgress();
                }
			}

			@Override
            public void onDetachSensor() {
                mState = STATE.IDLE;
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                if (noValueTimeOutTimer != null) {
                    noValueTimeOutTimer.cancel();
                }
                // mEcgManager.closeConnection();
                gotoTutorial();
            }

			@Override
            public void onMeasureing(int type, int value) {
                if (EcgManager.DEBUG) {
                    try {
                        if(mState == STATE.TOUCHED) {
                            showLayout(R.id.ecg_debug_layout);
                            mState = STATE.MEASURING;
                        }
                        switch (type) {
                            case EcgManager.MSG_ASUS_HEARTRATE_MEAN:
                                TextView mean1Text = (TextView)findViewById(R.id.debug_asus_mean_info);
                                mean1Text.setVisibility(View.VISIBLE);
                                mean1Text.setText("Mean : " + value);
                                break;
                            case EcgManager.MSG_ASUS_HEARTRATE_MEDIAN:
                                TextView medianText = (TextView)findViewById(R.id.debug_asus_median_info);
                                medianText.setVisibility(View.VISIBLE);
                                medianText.setText("Median : " + value);
                                break;
                            case EcgManager.MSG_ASUS_HEARTRATE_MODE:
                                TextView modeText = (TextView)findViewById(R.id.debug_asus_mode_info);
                                modeText.setVisibility(View.VISIBLE);
                                modeText.setText("Mode : " + value);
                                break;
                            case EcgManager.MSG_ASUS_HEARTRATE:
                                mHeartRateValue = value;
                                TextView asushrText = (TextView)findViewById(R.id.debug_asus_hr_info);
                                asushrText.setVisibility(View.VISIBLE);
                                asushrText.setText("ASUS HR : " + value);
                                break;
                            case EcgManager.MSG_ECG_RRINTERVAL:
                                TextView rrhrText = (TextView)findViewById(R.id.debug_rrhr_info);
                                rrhrText.setVisibility(View.VISIBLE);
                                rrhrText.setText("R-R HR : " + value);
                                break;
                            case EcgManager.MSG_ECG_HEARTRATE:
                                TextView hrText = (TextView)findViewById(R.id.debug_hr_info);
                                hrText.setVisibility(View.VISIBLE);
                                hrText.setText("NS SDK HR : " + value);
                                break;
                            case EcgManager.MSG_ECG_MOOD:
                                mEnergyValue = value;
                                //TextView stressText = (TextView)findViewById(R.id.debug_mood_info);
                                //stressText.setVisibility(View.VISIBLE);
                                //stressText.setText("Mood:" + value);
                                break;
                        }
                    } catch (Exception e) {
                    }
                    return;
                }
                try {
                    if (mState == STATE.TOUCHED) {
                        if(measureType == TYPE_STRESS || measureType == TYPE_RELAX) {
                            findViewById(R.id.ecg_hr_startup_container).setVisibility(View.INVISIBLE);
                            findViewById(R.id.ecg_hr_data_container).setVisibility(View.INVISIBLE);
                            findViewById(R.id.ecg_relax_container).setVisibility(View.INVISIBLE);
                            mState = STATE.MEASURING;
                        }else if(measureType == TYPE_HEARTRATE && type == EcgManager.MSG_ASUS_HEARTRATE) {
                            findViewById(R.id.ecg_hr_startup_container).setVisibility(View.INVISIBLE);
                            findViewById(R.id.ecg_relax_container).setVisibility(View.INVISIBLE);
                            findViewById(R.id.ecg_hr_data_container).setVisibility(View.VISIBLE);
                            mState = STATE.MEASURING;
                        }
                        rrCount = 0;
                    }
                    switch (type) {
                        case EcgManager.MSG_ECG_RRINTERVAL:
                            rrCount++;
                            if(measureType == TYPE_STRESS || measureType == TYPE_RELAX) {
                            	mCircleProgressBar.increaseRRCount();
                            }
                            break;
                        case EcgManager.MSG_ASUS_HEARTRATE:
                            mHeartRateValue = value;
                            if(measureType != TYPE_HEARTRATE) break;
                            TextView asushrText = (TextView)findViewById(R.id.asus_heart_rate_info);
                            asushrText.setVisibility(View.VISIBLE);
                            asushrText.setText(value+"");
                            break;
                        case EcgManager.MSG_ECG_MOOD:
                            mEnergyValue = value;
                            break;
                    }
                } catch (Exception e) {
                }
            }

			@Override
			public void onGetHeartRate(int value) {
				if(measureType==TYPE_HEARTRATE){
					if(noValueTimeOutTimer!=null){
						noValueTimeOutTimer.cancel();	
					}
					if(countDownTimer != null) {
					    countDownTimer.cancel();
					}
					finishMeasure(value);
				}
			}

			@Override
			public void onGetEnergy(int value) {
				if(measureType==TYPE_STRESS || measureType==TYPE_RELAX){
					if(noValueTimeOutTimer!=null){
						noValueTimeOutTimer.cancel();	
					}
					if(countDownTimer != null) {
					    countDownTimer.cancel();
					}
					finishMeasure(value);
				}
			}

            @Override
            public void onNoUpdateTimeout() {
                if (measureType == TYPE_STRESS || measureType == TYPE_RELAX) {
                    //finishMeasure(mEnergyValue);
                	gotoTutorial();
                } else if (measureType == TYPE_HEARTRATE) {
                    finishMeasure(mHeartRateValue);
                }
            }

            @Override
            public void onNoFisrtRRTimeout() {
                finish();
            }
		});

        if (EcgManager.DEBUG) {
            mEcgManager.setMeasureTimeStage(EcgManager.MEASURE_TIME_LONG);
        } else {
            if (measureType == TYPE_STRESS || measureType == TYPE_RELAX) {
                mEcgManager.setMeasureTimeStage(EcgManager.MEASURE_TIME_NORMAL);
            } else if (measureType == TYPE_HEARTRATE) {
                mEcgManager.setMeasureTimeStage(EcgManager.MEASURE_TIME_SHORT);
            }
        }
        mEcgManager.connectEcgSensor();
        gotoTutorial();
	}

    private void gotoTutorial() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("circle","start notouch timer");
        		noTouchTimer.start();
                showLayout(R.id.measure_tutorial_layout);
                setAnimationResources();
            }
        });
    }

	private Runnable repeatAnimationRunnable=new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			setAnimationResources();
		}
	};
	
	private void setAnimationResources(){
		ImageView hand_image=(ImageView)findViewById(R.id.hand_translation);
        if(hand_image == null){
           Log.e(Utility.TAG, TAG + "setAnimationResources setContentView disorder error usage" );
           return;
        }
		hand_image.setImageResource(R.drawable.asus_wellness_r_hand05);
		ImageView imageAnimation=(ImageView)findViewById(R.id.animation_drawable);
		imageAnimation.setImageResource(R.drawable.asus_wellness_l_hand02);
		
		AnimationDrawable animationDrawable=new AnimationDrawable();
		animationDrawable.addFrame(getResources().getDrawable(R.drawable.asus_wellness_l_hand01), 300);
		animationDrawable.addFrame(getResources().getDrawable(R.drawable.asus_wellness_l_hand02), 300);
		animationDrawable.addFrame(getResources().getDrawable(R.drawable.asus_wellness_l_hand01), 300);
		animationDrawable.addFrame(getResources().getDrawable(R.drawable.asus_wellness_l_hand02), 300);	
		imageAnimation.setImageDrawable(animationDrawable);
		
		TranslateAnimation trans=new TranslateAnimation(0, 0, 150, 0);
		trans.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				ImageView imageAnimation=(ImageView)findViewById(R.id.animation_drawable);
				imageAnimation.setImageResource(R.drawable.asus_wellness_l_hand03);
				
				repeatAnimationHandler.postDelayed(repeatAnimationRunnable, 2500);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		trans.setDuration(1500);
		hand_image.startAnimation(trans);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		repeatAnimationHandler.removeCallbacks(repeatAnimationRunnable);
		if(ecgDataLayer!=null){
			//ecgDataLayer.disConnectGoogleApiClient();
		}
		if(mEcgManager!=null){
			mEcgManager.closeConnection();	
		}
        if (countDownTimer != null){
            countDownTimer.cancel();
        }

		if(this.sensorManager!=null){
            //smile gao add for ioctl wellness, before unRegisterHeartRateSensor setWellness=0
            String returnStr = getProcWellNessVal();
            Log.d("circle","returnStr:"+returnStr);
            if(returnStr != "") {
                int nodeval = Integer.valueOf(returnStr);
               if (nodeval >= 2) {
                    WellnessJni wellnessjni = new WellnessJni();
                    Log.d("circle", "smile ondestroy 1:" + String.valueOf(wellnessjni.getWellness()));
                    if (wellnessjni.getWellness() != 0) {
                        wellnessjni.setWellness(0);
                    }
                    Log.d("circle", "smile ondestroy 2:" + String.valueOf(wellnessjni.getWellness()));

                }
            }
            //end smile
			unRegisterHeartRateSensor();	
		}
	}
	
	private void finishMeasure(final int value){
        mState = STATE.IDLE;
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				showSuggestNotification();
				int index=-1;
				String comment="";
				switch(measureType){
					case TYPE_RELAX:
						showLayout(R.id.measure_result_relax_layout);
						TextView relaxText=(TextView)findViewById(R.id.measure_result_relax_value);
						relaxText.setText(String.valueOf(value));

						ImageView relaxImage=(ImageView)findViewById(R.id.measure_relax_icon);
						relaxImage.setImageResource(Utility.getRelaxLevelDrawableId(value));
						
						TextView relaxCommentText=(TextView)findViewById(R.id.relax_measure_comment_text);
						index=Utility.getRelaxLevelStringIndex(MeasureActivity.this, value);
						comment=Utility.getRelaxLevelString(MeasureActivity.this, value, index);
						relaxCommentText.setText(comment);
						break;
					case TYPE_STRESS:
                        showLayout(R.id.measure_result_stress_layout);
						TextView stressText=(TextView)findViewById(R.id.measure_result_stress_value);
						stressText.setText(String.valueOf(value));

						ImageView stressImage=(ImageView)findViewById(R.id.measure_stress_icon);
						stressImage.setImageResource(Utility.getStressLevelDrawableId(value));
						
						TextView stressCommentText=(TextView)findViewById(R.id.stress_measure_comment_text);
						comment=Utility.getStressLevelString(MeasureActivity.this, value);
						stressCommentText.setText(comment);
						break;
					case TYPE_HEARTRATE:
                        showLayout(R.id.measure_result_heart_rate_layout);
						TextView hrText=(TextView)findViewById(R.id.measure_result_heart_rate_value);
						hrText.setText(String.valueOf(value));
						
						TextView hrCommentText=(TextView)findViewById(R.id.measure_comment_text);
						index=Utility.getIntensityLevelIndex(MeasureActivity.this, value);
						comment=Utility.getIntensityLevel(MeasureActivity.this, value, index);
						hrCommentText.setText(comment);
						break;
				}
				getContentResolver().delete(EcgTable.TABLE_URI, null, null);
				ContentValues cv=new ContentValues();
                long measuretime = System.currentTimeMillis();
				cv.put(EcgTable.COLUMN_MEASURE_TIME,measuretime );
				cv.put(EcgTable.COLUMN_MEASURE_TYPE, measureType);
				cv.put(EcgTable.COLUMN_MEASURE_VALUE, value);
				getContentResolver().insert(EcgTable.TABLE_URI, cv);
				ecgDataLayer.sendEcgDataToPhone(measuretime, measureType, value, index);
			
				//send info to watch manager;
				Intent intent=new Intent(ACTION_MEASURE_INFO);
				switch(measureType){
					case TYPE_RELAX:
						intent.putExtra("relax", value);
						break;
					case TYPE_STRESS:
						intent.putExtra("stress", value);
						break;
					case TYPE_HEARTRATE:
						intent.putExtra("heart_rate", value);
						break;
				}
				sendBroadcast(intent);
				
				if(mEcgManager!=null){
					mEcgManager.closeConnection();	
				}
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				checkCompanionIsInstalled();
			}
			
		});
	}
    
    Timer timer;
    
    private void checkCompanionIsInstalled(){
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
    	if(sp.getBoolean(PREF_KEY_FIRST_MEASURE, true)){
        	timer=new Timer();
        	timer.schedule(new TimerTask(){

    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				showSuggestNotification();
    			}
        		
        	}, 5000);
        	ecgDataLayer.addListener(this);
        	ecgDataLayer.sendMessageToPhone("/check_companion", "echo");
    	}
    }
    
    private void showSuggestNotification(){
    	int notificationId = 001;
    	
    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor=sp.edit();
		editor.putBoolean(PREF_KEY_FIRST_MEASURE, false);
		editor.commit();
		
		Intent startIntent = new Intent(this, CollectStepCountService.class);
		startIntent.setAction(START_DOWNLOAD_WELLNESS_FROM_PHONE);
		PendingIntent startPhoneGooglePlayPendingIntent =        
				PendingIntent.getService(this, 0, startIntent, 0);
		
		NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(this)
//    		.addAction(R.drawable.watch_notification_wellness_128x128, getString(R.string.download_on_phone_message), startPhoneGooglePlayPendingIntent)
		.setSmallIcon(R.mipmap.asus_icon_app_wellness)
		.setContentTitle(getString(R.string.suggest_wellness_title))
		.setContentText(getString(R.string.suggest_wellness_message));
		
		NotificationManagerCompat notificationManager=NotificationManagerCompat.from(this);
		notificationManager.notify(notificationId, notificationBuilder.build());
    }

	@Override
	public void onMessageReceived(MessageEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getPath().matches("/check_companion")){
			timer.cancel();	
			
	    	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
			Editor editor=sp.edit();
			editor.putBoolean(PREF_KEY_FIRST_MEASURE, false);
			editor.commit();
			
			ecgDataLayer.removeListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		Log.d("circle","event:"+event.values[0]);
	}

    private void showLayout(int layoutId){
        if(mLayoutIds ==null){
            mLayoutIds = new ArrayList<Integer>();
            mLayoutIds.add(R.id.measure_options_layout);
            mLayoutIds.add(R.id.ecg_hr_measure_layout);
            mLayoutIds.add(R.id.ecg_debug_layout);
            mLayoutIds.add(R.id.measure_tutorial_layout);
            mLayoutIds.add(R.id.measure_result_relax_layout);
            mLayoutIds.add(R.id.measure_result_stress_layout);
            mLayoutIds.add(R.id.measure_result_heart_rate_layout);
        }

        for(int resId: mLayoutIds){
            int visibility = View.GONE;
            if(resId == layoutId){
                visibility = View.VISIBLE;
            }
            findViewById(resId).setVisibility(visibility);
        }
    }
}

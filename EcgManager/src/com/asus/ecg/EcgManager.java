package com.asus.ecg;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.neurosky.thinkgear.TGDevice;

public class EcgManager {
	static {
		System.loadLibrary("NSUART");
	}

    public static final boolean DEBUG = false;
    public static final boolean LOGENABLE = false;

	private native void SerialJNI_close();
	private native FileDescriptor SerialJNI_open(String dev);
	private TGDevice tgDevice;
	private Context mContext;
	private EcgCallback mEcgCallback=null;
	private int mStress=0;
	private int mStableHeartRate=0;
	private static final String TAG="EcgManager";

    public static final int MSG_ECG_HEARTRATE = TGDevice.MSG_ECG_HEARTRATE;
    public static final int MSG_ECG_MOOD = TGDevice.MSG_ECG_MOOD;
    public static final int MSG_ECG_RRINTERVAL = TGDevice.MSG_ECG_RRINTERVAL;
    public static final int MSG_ASUS_HEARTRATE_MEAN = 996;
    public static final int MSG_ASUS_HEARTRATE_MEDIAN = 997;
    public static final int MSG_ASUS_HEARTRATE_MODE = 998;
    public static final int MSG_GOOGLEFIT_HEARTRATE = 999;
    public static final int MSG_ASUS_HEARTRATE = 1000;

    private final long NO_FIRST_RR_TIMEOUT = 14000; //7000;  //ms
    private final long NO_RR_TIMEOUT = 2000;  //ms
    //private final long NO_TOUCH_TIMEOUT = 500;  //ms

    public static final int MEASURE_TIME_SHORT = 0;
    public static final int MEASURE_TIME_NORMAL = 1;
    public static final int MEASURE_TIME_LONG = 2;

    public static final int RR_NUM_FOR_RELAX = 30;

    private int MAX_COUNT_HR = 4;
    private long mStartTime = 0;

    private AsusEcgProcessor mAsusEcgProcessor = null;
    private Timer noUpdateTimer = null;

    //for log
    private int mRR = 0;
    private int mRRHR = 0;
    private int mSDKHR = 0;
    private int mMean = 0;
    private int mMedian = 0;
    private int mMode = 0;
    private int mAsusHR = 0;

	public EcgManager(Context context, EcgCallback callback){
		mContext=context;
		mEcgCallback=callback;
	}
	
	/**
	 * Before using ECG sensor, it must connect ecg sensor by calling this method first.
	 */
	public void connectEcgSensor(){
		String dev = "/dev/felica";
		FileDescriptor serialPort = SerialJNI_open( dev );
		FileInputStream iStream = new FileInputStream( serialPort );
		FileOutputStream oStream = new FileOutputStream( serialPort );
		tgDevice = new TGDevice( iStream, oStream, handler);
		tgDevice.enableLogCat(false);
		tgDevice.connectStream( true );

        mAsusEcgProcessor = new AsusEcgProcessor();
        if (LOGENABLE) {
            EcgLogHelper.prepareLogFile();
        }
	}

    public void setMeasureTimeStage(int timeStage) {
        switch (timeStage) {
            case MEASURE_TIME_SHORT:
                MAX_COUNT_HR = 4;  // 7 + 4*3 = 19 RR
                break;
            case MEASURE_TIME_NORMAL:
                MAX_COUNT_HR = 8;  // 7 + 4*7 = 35 RR
                break;
            case MEASURE_TIME_LONG:
                MAX_COUNT_HR = 12;  // 7 + 4*11 = 51 RR
                break;
        }
    }

	private Handler handler=new Handler(){
		
		private int mLastTouchSignal=0;//200:touch sensor
		int countHeartRate=0;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
				case TGDevice.MSG_STATE_CHANGE:
					switch(msg.arg1){
					case TGDevice.STATE_CONNECTED:
						tgDevice.start();
						break;
					case TGDevice.STATE_DISCONNECTED:
						break;
					case TGDevice.STATE_CONNECTING:
						break;
					}
					break;
				case TGDevice.MSG_POOR_SIGNAL:
                    Log.d(TAG, "signal: " + msg.arg1 + ", last signal: " + mLastTouchSignal);
                    if (msg.arg1 != mLastTouchSignal) {
                        if (mLastTouchSignal == 200) {
                            mLastTouchSignal = msg.arg1;
                            if (mEcgCallback != null) {
                                if (noUpdateTimer != null) {
                                    noUpdateTimer.cancel();
                                    noUpdateTimer = null;
                                }
                                if (mStableHeartRate == 0) {
                                    mEcgCallback.onDetachSensor();
                                    break;
                                }
                                /*
                                noUpdateTimer = new Timer();
                                noUpdateTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (mEcgCallback != null) {
                                            mEcgCallback.onNoUpdateTimeout();
                                        }
                                    }
                                }, NO_TOUCH_TIMEOUT);
                                */
                                mEcgCallback.onNoUpdateTimeout();
                            }
                        } else if (mLastTouchSignal == 0) {
                            mLastTouchSignal = msg.arg1;
                            if (mEcgCallback != null) {
                                countHeartRate = 0;
                                mStableHeartRate = 0;
                                mStartTime = System.currentTimeMillis();
                                if (mAsusEcgProcessor != null) {
                                    mAsusEcgProcessor.reset();
                                }
                                if (noUpdateTimer != null) {
                                    noUpdateTimer.cancel();
                                    noUpdateTimer = null;
                                }
                                noUpdateTimer = new Timer();
                                noUpdateTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (mEcgCallback != null) {
                                            //mEcgCallback.onDetachSensor();
                                            mEcgCallback.onNoFisrtRRTimeout();
                                        }
                                    }
                                }, NO_FIRST_RR_TIMEOUT);
                                mRR = 0;
                                mRRHR = 0;
                                mSDKHR = 0;
                                mMean = 0;
                                mMedian = 0;
                                mMode = 0;
                                mAsusHR = 0;
                                mEcgCallback.onTouchSensor();
                            }
                        }
                    }
                    mLastTouchSignal = msg.arg1;
                    break;
				case TGDevice.MSG_MODEL_IDENTIFIED:
					Log.d(TAG,"MSG_MODEL_IDENTIFIED");
					break;
				case TGDevice.MSG_ECG_HEARTRATE:
					//Log.d(TAG, "heart rate:"+msg.arg1+" "+countHeartRate);
                    if (LOGENABLE) mSDKHR = msg.arg1;
                    if (mLastTouchSignal == 200) {
                        if (mEcgCallback != null) {
                            mEcgCallback.onMeasureing(MSG_ECG_HEARTRATE, msg.arg1);
                        }
                        countHeartRate++;
                        if (countHeartRate == MAX_COUNT_HR) {
                            if (mAsusEcgProcessor != null && mAsusEcgProcessor.getHeartRate() != 0) {
                                mStableHeartRate = mAsusEcgProcessor.getHeartRate();
                            } else {
                                mStableHeartRate = msg.arg1;
                            }
                            mEcgCallback.onGetHeartRate(mStableHeartRate);
                        }
                    }
					break;
				case TGDevice.MSG_ECG_MOOD:
					Log.d(TAG, "MSG_ECG_MOOD:"+msg.arg1);
					if(mLastTouchSignal==200){
						if(mEcgCallback!=null){
							mEcgCallback.onMeasureing(MSG_ECG_MOOD, msg.arg1);	
						}
						mStress=msg.arg1;
						mEcgCallback.onGetEnergy((100-mStress));
					}
					break;
                case TGDevice.MSG_ECG_RRINTERVAL:
                	//Log.d(TAG,"MSG_ECG_RRINTERVAL:"+msg.arg1+" fast heart rate:"+(int)(60000 / msg.arg1)+" "+countHeartRate);
                    if (noUpdateTimer != null) {
                        noUpdateTimer.cancel();
                        noUpdateTimer = null;
                    }
                    if (mAsusEcgProcessor != null) {
                        mAsusEcgProcessor.inputRR(msg.arg1);
                    }
                    if (LOGENABLE) {
                        mRR = msg.arg1;
                        mRRHR = (int)(60000 / msg.arg1);
                        mMean = mAsusEcgProcessor.getHeartRateMean();
                        mMedian = mAsusEcgProcessor.getHeartRateMedian1();
                        mMode = mAsusEcgProcessor.getHeartRateMode();
                        mAsusHR = mAsusEcgProcessor.getHeartRate();
                        String rrData = "sample=" + countHeartRate + " timeMS="
                                + (System.currentTimeMillis() - mStartTime) + " RR=" + mRR
                                + " RRHR=" + mRRHR + " SDKHR=" + mSDKHR + " Mean=" + mMean + " Median="
                                + mMedian + " Mode=" + mMode + " ASUSHR=" + mAsusHR;
                        EcgLogHelper.recordStringData(rrData);
                    }
                    if (mEcgCallback != null) {
                        if (DEBUG) {
                            if (mAsusEcgProcessor.getHeartRateMean() != 0) {
                                mEcgCallback.onMeasureing(MSG_ASUS_HEARTRATE_MEAN, mAsusEcgProcessor.getHeartRateMean());
                            }
                            if (mAsusEcgProcessor.getHeartRateMedian1() != 0) {
                                mEcgCallback.onMeasureing(MSG_ASUS_HEARTRATE_MEDIAN, mAsusEcgProcessor.getHeartRateMedian1());
                            }
                            if (mAsusEcgProcessor.getHeartRateMode() != 0) {
                                mEcgCallback.onMeasureing(MSG_ASUS_HEARTRATE_MODE, mAsusEcgProcessor.getHeartRateMode());
                            }
                        }
                        if (mAsusEcgProcessor.getHeartRate() != 0) {
                            mEcgCallback.onMeasureing(MSG_ASUS_HEARTRATE, mAsusEcgProcessor.getHeartRate());
                            mStableHeartRate = mAsusEcgProcessor.getHeartRate();
                        }
                        mEcgCallback.onMeasureing(MSG_ECG_RRINTERVAL, (int)(60000 / msg.arg1));
                    }
                    noUpdateTimer = new Timer();
                    noUpdateTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (mEcgCallback != null) {
                                if (mStableHeartRate == 0) {
                                    mLastTouchSignal = 0;
                                    mEcgCallback.onDetachSensor();
                                } else {
                                    mEcgCallback.onNoUpdateTimeout();
                                }
                            }
                        }
                    }, NO_RR_TIMEOUT);
                    break;
			}
		}
		
	};
	
	public int getStableHeartRate(){
		return mStableHeartRate;
	}
	
	/**
	 * Get relaxation value
	 * @return A "Relax" level ranging from 1 to 100. Low values tend to indicate stress or fatigue or "tense/wired" Mood and High values tend to indicate being in a relaxed/calm state. 
	 */
	public int getRelaxation(){
		return 100-mStress;
	}
	
	/**
	 * Get stress value
	 * @return A "Stress" level ranging from 1 to 100. High values tend to indicate stress or fatigue or "tense/wired" Mood and Low values tend to indicate being in a relaxed/calm state.
	 */
	public int getStress(){
		return mStress;
	}
	
	/**
	 * When you are no longer using ECG sensor, you must close connection by calling the method
	 */
	public void closeConnection(){
        tgDevice.close();
        SerialJNI_close();
        if (noUpdateTimer != null) {
            noUpdateTimer.cancel();
            noUpdateTimer = null;
        }
        if (LOGENABLE) {
            EcgLogHelper.releaseLogFile();
        }
	}
}

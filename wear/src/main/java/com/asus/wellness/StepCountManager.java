package com.asus.wellness;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.asus.sharedata.ShareUtils;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.dbhelper.Step_count;
import com.asus.wellness.dbhelper.Step_countDao;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.utils.EBCommand;

import com.asus.wellness.utils.Utility;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.gson.Gson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.ContactsContract;
import android.util.Log;

import de.greenrobot.event.EventBus;

public class StepCountManager implements SensorEventListener {
    private final String TAG = "StepCountManager";
    public static boolean USE_FITNESS = false;
    public static final long ONE_MINUTE_MILLIES = 1 * 10 * 1000;//1 min

    public static final int PNI_TYPE_ACTIVITY = 65637;
    public static final int PNI_TYPE_FITNESS_ACTIVITY = 65639;
    public static final int PNI_TYPE_SLEEP_ACTIVITY = 65640;

    public static final int SAMPLING_PERIOD_US  =1000000; //at least 1  senconds sample
    public static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    public static final long  ONE_HOUR_MILLIES= 1*60*60*1000;
    public static final long  ONE_DAY_MILLIES= 24*60*60*1000;

    private Context mContext;
    private SensorManager sensorManager;
    private StepCountCallback mStepCountCallback = null;
    private DataLayerManager mDataLayerManager  = null;

    private Sensor mStepCountSensor ;
    private Sensor mFitnessActivitySensor;
    private Sensor mSleepSensor;
    private Sensor mActivitySensor;

    private DataPoint mDailyTotalDataPoint;
    SimpleDateFormat sf = new SimpleDateFormat(DATE_FORMAT);

    private static StepCountManager s_stepCountManager = null;
    ExecutorService cachedThreadPool;
    private List<DataPoint> mDataPoints = new ArrayList<DataPoint>();

    public static StepCountManager getInstance(Context context, StepCountCallback callback){
        if(s_stepCountManager == null){
            s_stepCountManager = new StepCountManager(context, callback);
        }
        return s_stepCountManager;
    }

    private  StepCountManager(Context context, StepCountCallback callback) {
        cachedThreadPool = Executors.newCachedThreadPool();
        mContext = context;
        sensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        mStepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mFitnessActivitySensor = sensorManager.getDefaultSensor(PNI_TYPE_FITNESS_ACTIVITY);

        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            switch(sensor.getType()){
                case PNI_TYPE_SLEEP_ACTIVITY:
                    mSleepSensor = sensor;
                    break;
                case PNI_TYPE_ACTIVITY:
                    mActivitySensor = sensor;
                    break;
                default:
                    break;
            }
        }
        mStepCountCallback = callback;
    }

    public boolean registerFitnessSensor(){
        unRegisterFitnessSensor();
        int[] activityMapping = {100000,200000};
        int samplingPeriodUs;
        CoachDataModel coachDataModel = WApplication.getInstance().getCoachDataModel();
        switch(coachDataModel.getType()){
            case PUSHUP:
                samplingPeriodUs = activityMapping[0];
                return sensorManager.registerListener(this, mFitnessActivitySensor , samplingPeriodUs);
            case SITUP:
                samplingPeriodUs = activityMapping[1];
                return sensorManager.registerListener(this, mFitnessActivitySensor , samplingPeriodUs);
            default:
                break;
        }
        return false;
    }

    public void unRegisterFitnessSensor(){
        sensorManager.unregisterListener(this, mFitnessActivitySensor);
    }

    public void registerStepCountSensor() {
        unRegisterStepCountSensor();
        sensorManager.registerListener(this, mStepCountSensor, SAMPLING_PERIOD_US);
        sensorManager.registerListener(this, mActivitySensor, SAMPLING_PERIOD_US);
    }

    public void unRegisterStepCountSensor() {
        Log.i("wellness", "Stepcount un registerStepCountSensor");
        sensorManager.unregisterListener(this, mStepCountSensor);
        sensorManager.unregisterListener(this, mActivitySensor);
    }

    public void enableSleepSensor(boolean enable) {
        sensorManager.unregisterListener(this, mSleepSensor);
        if(enable) {
            sensorManager.registerListener(this, mSleepSensor, 60000, 0);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    public void setDataLayerManager(DataLayerManager dataLayerManager){
        mDataLayerManager =dataLayerManager;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        CoachDataModel coachDataModel = WApplication.getInstance().getCoachDataModel();
        // TODO Auto-generated method stub
        if (event.sensor.getType() == PNI_TYPE_SLEEP_ACTIVITY) {
            Log.d(Utility.TAG, TAG + "sleep activity event " + event.values[0]);
            EBCommand cmdmsg = new EBCommand(StepCountManager.class.getName(), WorkoutDataService.class.getName(), EBCommand.COMMAND_SLEEP_VALUE, (long) event.values[0]);
            EventBus.getDefault().post(cmdmsg);
//        }else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER   ){
//            //SensorEventLogger.getInstance().log(event);
        }else if(event.sensor.getType() == PNI_TYPE_ACTIVITY  ){
            int currentActivity = (int)event.values[0];
            final int NOT_WORN = 5;
            WApplication.getInstance().setWearWorn(currentActivity != NOT_WORN);
            Log.d(Utility.TAG, TAG + " PNI_TYPE_ACTIVITY " + currentActivity + " isWorn " + WApplication.getInstance().getWearWorn() );
        }else if(event.sensor.getType() == PNI_TYPE_FITNESS_ACTIVITY){
            if(coachDataModel.isCoaching() ) {
                long currentFitnessActivityCount = (long) event.values[1] + 256 * (long) event.values[2];
                EBCommand cmdmsg = new EBCommand(StepCountManager.class.getName(), WorkoutDataService.class.getName(), EBCommand.COMMAND_FITNESS_ACTIVITY, currentFitnessActivityCount);
                EventBus.getDefault().post(cmdmsg);
            }
        } else  if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d(TAG, "onSensorChanged event  raw data" + event.values[0] + " ") ;
            if(coachDataModel.isCoaching() && coachDataModel.getType().equals(CoachDataModel.eType.RUN) ){
                EBCommand cmdmsg = new EBCommand(StepCountManager.class.getName(), WorkoutDataService.class.getName(),EBCommand.COMMAND_FITNESS_ACTIVITY,(long)event.values[0]);
                EventBus.getDefault().post(cmdmsg);
            }

            if(USE_FITNESS){
                retriveDailyTotalStep();
            }else{
                InsertOrReplaceHourSteps(System.currentTimeMillis(), (long)event.values[0]);
            }
        }
    }

    private void  InsertOrReplaceHourSteps(long now, long sensor_value){
        Step_countDao step_countDao = WApplication.getInstance().getDataHelper().getDaoSession().getStep_countDao();
        long time = ShareUtils.getMidnightMilles(now);
        List<Step_count> step_counts = step_countDao.queryBuilder().orderDesc(Step_countDao.Properties.Id).limit(1).list();
        Step_count lastStepCount = step_counts.size() > 0 ? step_counts.get(0) : null ;

        if( lastStepCount == null || !isSameDay(System.currentTimeMillis(),lastStepCount.getEnd()) ) {
            lastStepCount = new Step_count(null, now, now, 0L , sensor_value);
        }else{
            Long oldOutput = lastStepCount.getSensor_value();
            long startHourMilles  = ShareUtils.getHourMilles(now);
            long lastHourMillies =  ShareUtils.getHourMilles(lastStepCount.getEnd());

            if(oldOutput ==null || oldOutput > sensor_value){
                lastStepCount.setSensor_value(sensor_value);
            }else if(isSameHour(startHourMilles,lastHourMillies)){
                long steps = sensor_value - oldOutput + lastStepCount.getStep_count();
                lastStepCount.setStep_count(steps);
            }else{
                long steps = sensor_value - oldOutput;
                lastStepCount = new Step_count(null,now,now,steps,sensor_value);
            }
        }

        lastStepCount.setEnd(now);
        lastStepCount.setSensor_value(sensor_value);
       long id =  step_countDao.insertOrReplace(lastStepCount);

        long totalStep = StepHelper.getTodaySteps();
        mStepCountCallback.onWalking(totalStep);
        Log.d(TAG, "InsertOrReplaceHourSteps sensorvalue:" + lastStepCount.getSensor_value() + " step_count " + lastStepCount.getStep_count()+" start: "+lastStepCount.getStart()+" end: "+lastStepCount.getEnd());

    }

    public void onDailyTotalResult(DailyTotalResult dailyTotalResult) {
        if (!dailyTotalResult.getStatus().isSuccess()) {
            Log.d(Utility.TAG, TAG +  "HistoryApi readDailyTotal failure ");
            return;
        }
        DataSet totalSet = dailyTotalResult.getTotal();
        if (totalSet.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e(Utility.TAG, TAG + "HistoryApi readDailyTotal: totalSet.isEmpty ");
            USE_FITNESS = false;
//           mDataLayerManager.reConnectGoogleApiClient();
        }else {
            DataPoint dataPoint = totalSet.getDataPoints().get(0);
            int totalStep = dataPoint.getValue(Field.FIELD_STEPS).asInt();
            long start =  dataPoint.getStartTime(TimeUnit.MILLISECONDS);
            long end =  dataPoint.getEndTime(TimeUnit.MILLISECONDS);

            mDailyTotalDataPoint = dataPoint;
          //  InsertOrReplaceHourSteps(start, end, totalStep);
            SimpleDateFormat sf = new SimpleDateFormat(DATE_FORMAT);
            Log.d(TAG, TAG + "HistoryApi readDailyTotal: " + totalStep + " start: " + sf.format(start) + " end: " + sf.format(end));
            mStepCountCallback.onWalking(totalStep);
            //save step into db when idle 1 min

        }
    }


    public  static boolean isSameDay(long timestamp1, long timestamp2){
       return  timestamp1/ONE_DAY_MILLIES == timestamp2/ONE_DAY_MILLIES ;
    }

    private boolean isSameHour(long timestamp1, long timestamp2){
        return  (timestamp1/ONE_HOUR_MILLIES == timestamp2/ONE_HOUR_MILLIES) && isSameDay(timestamp1,timestamp2) ;
    }


    //use fitness api to retrieve data
    public void retriveDailyTotalStep() {
        Log.i(Utility.TAG, TAG + "retriveDailyTotalStep start");
        if(USE_FITNESS) {
            if (mDataLayerManager.isConnected()) {
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(Utility.TAG, TAG + "retriveDailyTotalStep run");
                        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mDataLayerManager.getGoogleApiClient(), DataType.TYPE_STEP_COUNT_DELTA);
                        DailyTotalResult dailyTotalResult = result.await(30, TimeUnit.SECONDS);
                        onDailyTotalResult(dailyTotalResult);
                    }
                });
            }else {
                Log.e(Utility.TAG, TAG + "mGoogleApiClient not connected ,reconnect now");
                mDataLayerManager.connectGoogleApiClient(null);
            }
        }
    }

    private DataReadRequest queryFitnessData(long start, long end) {
        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                        // bucketByTime allows for a time span, whereas bucketBySession would allow
                        // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    public void onDataReadResult(DataReadResult dataReadResult) {
        Log.i(TAG,"onDataReadResult ");
        if (dataReadResult.getBuckets().size() > 0) {

            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        if(dataSet.getDataPoints().size() > 0) {
            DataPoint dataPoint = dataSet.getDataPoints().get(0);
            long now = System.currentTimeMillis();
            long midnight = ShareUtils.getMidnightMilles(now);

            List<Step_count> new_step_counts = new ArrayList<Step_count>();
            long totalSteps = 0;
            if (endAddData(dataPoint)) {
                printAllDataPoint("dumpDataSet");

                for (int i = 0; i < mDataPoints.size(); i++ ) {
                    DataPoint dp = mDataPoints.get(i);
                    long start = dp.getStartTime(TimeUnit.MILLISECONDS);
                    long end = dp.getEndTime(TimeUnit.MILLISECONDS);
                    long steps = dp.getValue(Field.FIELD_STEPS).asInt();
                    new_step_counts.add(new Step_count(null, start, end, steps, null));
                    totalSteps += steps;
                }
                if (StepHelper.getTodaySteps() != totalSteps) {
                    Step_countDao step_countDao = WApplication.getInstance().getDataHelper().getDaoSession().getStep_countDao();
                    List<Step_count> old_step_counts = step_countDao.queryBuilder().where(Step_countDao.Properties.Start.ge(midnight)).list();
                    step_countDao.deleteInTx(old_step_counts);
                    Step_count lastRecord = new_step_counts.get(new_step_counts.size() - 1);
                    lastRecord.setStep_count(lastRecord.getStep_count() + WApplication.getInstance().getTodaySteps() - totalSteps);
                    step_countDao.insertOrReplaceInTx(new_step_counts);
                }
                mDataPoints.clear();
                Log.d(TAG, "HistoryApi readData:   total steps " + totalSteps);
            }
        }
    }


    private boolean endAddData(DataPoint dataPoint){
        boolean  exist =  false;
        long totalReadData = 0;
        long todaySteps  = WApplication.getInstance().getTodaySteps();

        for(int i = 0; i < mDataPoints.size(); i++){
            DataPoint dp  = mDataPoints.get(i);
            if(dp.getStartTime(TimeUnit.SECONDS) >= mDailyTotalDataPoint.getStartTime(TimeUnit.SECONDS) ){
                if(dp.getStartTime(TimeUnit.MILLISECONDS) == dataPoint.getStartTime(TimeUnit.MILLISECONDS) ){
                    exist =  true;
                }else {
                    totalReadData += dp.getValue(Field.FIELD_STEPS).asInt();
                }
            }
        }
        if(!exist){
            mDataPoints.add(dataPoint);
        }

        boolean lastRecord  = ( dataPoint.getEndTime(TimeUnit.MILLISECONDS) == mDailyTotalDataPoint.getEndTime(TimeUnit.MILLISECONDS));
        printDataPoint("endAddData",dataPoint);
        printDataPoint("mDailyTotalDataPoint",mDailyTotalDataPoint);
        totalReadData += dataPoint.getValue(Field.FIELD_STEPS).asInt();
        return lastRecord || totalReadData >= todaySteps;
    }

    public  void rebuildTodayStepSegments() {
        if(USE_FITNESS) {
            Log.i(Utility.TAG, "HistoryApi rebuildTodayStepSegments start");
            long now = System.currentTimeMillis();
            long midnight = ShareUtils.getMidnightMilles(now);

            try {
                SimpleDateFormat sf = new SimpleDateFormat(DATE_FORMAT);
                for (long time = midnight; time < now; time += ONE_HOUR_MILLIES) {
                    long start = time;
                    long end = Math.min(start + ONE_HOUR_MILLIES, now);
                    Fitness.HistoryApi.readData(mDataLayerManager.getGoogleApiClient(), queryFitnessData(start, end)).setResultCallback(new ResultCallback<DataReadResult>() {
                        @Override
                        public void onResult(DataReadResult dataReadResult) {
                            onDataReadResult(dataReadResult);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void printAllDataPoint(String tag ){
        if(BuildConfig.DEBUG) {
            for (DataPoint dp : mDataPoints){
                printDataPoint(tag, dp);
            }
        }
    }

    private void printDataPoint( String tag,DataPoint dp ){
        if(BuildConfig.DEBUG) {
            long start = dp.getStartTime(TimeUnit.MILLISECONDS);
            long end = dp.getEndTime(TimeUnit.MILLISECONDS);
            long steps = dp.getValue(Field.FIELD_STEPS).asInt();
            String uid = dp.getOriginalDataSource().getDevice().getUid();

            Log.d(TAG, "HistoryApi readData: " + tag  + sf.format(start) + " end " + sf.format(end) + " steps " + steps + " uid " + uid);
    }
    }

}

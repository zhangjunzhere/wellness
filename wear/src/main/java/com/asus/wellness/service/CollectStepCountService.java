package com.asus.wellness.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asus.sharedata.ShareUtils;
import com.asus.sharedata.SyncIdleAlarm;
import com.asus.wellness.MeasureActivity;
import com.asus.wellness.StepCountCallback;
import com.asus.wellness.StepCountManager;
import com.asus.wellness.WApplication;
import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.microprovider.ActivityStatusTable;
import com.asus.wellness.microprovider.StepCountData;
import com.asus.wellness.microprovider.StepCountInfo;
import com.asus.wellness.notification.NotificaionHelper;
import com.asus.wellness.notification.NotificaionHelperRemindMe;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.sleep.SleepDataModel;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.dbhelper.Profile;

import java.util.Calendar;

import de.greenrobot.event.EventBus;


public class CollectStepCountService extends Service {    
    private final String TAG  = "CollectStepCountService";

    private StepCountManager stepCountManager = null;
    private DataLayerManager stepCountDataLayer = null;

    AlarmManager am, amNewDay;
    PendingIntent pi, piNewDay,piSleep;
    private int MSG_WALKING = 0;
    private int MSG_WALKEND = 1;


    public static final String ACTION_ALARM = "action_alarm";
    public static final String ACTION_CANCEL_ALARM = "action_cancel_alarm";
    public static final String ACTION_NEW_DAY = "action_new_day";
    public static final String ACTION_NEW_DAY_CANCEL = "action_new_day_cancel";
    public static  final String ACTION_SLEEP_DONE = "action_sleep_done";

    public static final String ACTION_GET_HISTORY_DATA = "action_get_history_data";

    public static boolean mShowStepGoalNotificationAlready = false;
    private long todayTimeMilles ;

    private SharedPreferences sp;
    public interface GoogleApiConnectCallback {
        public void onConnect();
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //if (BuildConfig.DEBUG) {
            Log.d(Utility.TAG, "CollectStepCountService start");
        //}

        sp = PreferenceManager.getDefaultSharedPreferences(CollectStepCountService.this);
        settingAlarmManager();
        //to receive step detector event
        EventBus.getDefault().register(this);


        if(stepCountDataLayer == null) {
            Log.i(Utility.TAG, TAG + "stepCountDataLayer initialize");
            stepCountDataLayer =  DataLayerManager.getInstance(this);
            stepCountDataLayer.connectGoogleApiClient(new GoogleApiConnectCallback() {
                @Override
                public void onConnect() {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "stepCountDataLayer connectGoogleApiClient");
                    stepCountDataLayer.sendWatchTypeToPhone(false);
                    stepCountManager.retriveDailyTotalStep();
                    //EBCommandUtils.getFitnessStep(this.getClass().getName());
                }
            });
        }
        if(stepCountManager == null) {
            Log.i(Utility.TAG, "registerStepCountSensor oncreate");
            stepCountManager = StepCountManager.getInstance(CollectStepCountService.this, mStepCountCallback);
            stepCountManager.registerStepCountSensor();
            stepCountManager.setDataLayerManager(stepCountDataLayer);
        }
    }

    public void onEvent(EBCommand ebCommand){
        if(stepCountManager != null && getClass().getName().equals(ebCommand.receiver)){
            Log.d(Utility.TAG, ebCommand.toString());
            if(EBCommand.COMMAND_REGISTER_COACH_SENSOR.equals(ebCommand.command)){
                stepCountManager.registerFitnessSensor();
            }else if(EBCommand.COMMAND_UNREGISTER_COACH_SENSOR.equals(ebCommand.command)) {
                stepCountManager.unRegisterFitnessSensor();
            }else if(EBCommand.COMMAND_SHOW_SLEEP_NOTIFICATION.equals(ebCommand.command)){
                boolean show = (boolean) ebCommand.param;
                if(show){
                    NotificaionHelper.getInstance(this).showSleepNotification(this);
                }else{
                    stopForeground(true);
                }
            }else if(EBCommand.COMMAND_START_SLEEP.equals(ebCommand.command)){
                boolean enabled =  (boolean) ebCommand.param;
                stepCountManager.enableSleepSensor(enabled);

                Intent sleepIntent = new Intent(CollectStepCountService.this, CollectStepCountService.class).setAction(ACTION_SLEEP_DONE);
                piSleep = PendingIntent.getService(CollectStepCountService.this, 1, sleepIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                SleepDataModel.getInstance().onSleepTriggered(enabled);
                am.cancel(piSleep);
                if(enabled){
                    long interval = 12*60*60*1000;
                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, piSleep);
                    startService(new Intent(this, WorkoutDataService.class));
                }else{
                    stopForeground(true);
                }
            }else if(EBCommand.COMMAND_GET_FITNESS_STEP.equals(ebCommand.command)){
                if(stepCountManager == null){
                    Log.i(Utility.TAG, "registerStepCountSensor COMMAND_GET_FITNESS_STEP");
                    stepCountManager = StepCountManager.getInstance(CollectStepCountService.this, mStepCountCallback);
                    stepCountManager.registerStepCountSensor();
					stepCountManager.setDataLayerManager(stepCountDataLayer);
                }
                stepCountManager.retriveDailyTotalStep();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        if (intent != null ) {
            String action = intent.getAction();
            if(action != null) {
                if (action.matches(MeasureActivity.START_DOWNLOAD_WELLNESS_FROM_PHONE)) {
                    stepCountDataLayer.sendMessageToPhone(DataLayerManager.START_DOWNLOAD_WELLNESS_PATH, "");
                } else if(action.matches(ACTION_ALARM)) {
                    int hourOfDayFrom = sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_FROM, SyncIdleAlarm.DEFAULT_IDLE_ALARM_HOUR_OF_DAY_FROM);
                    int minuteFrom = sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_FROM, SyncIdleAlarm.DEFAULT_IDLE_ALARM_MINUTE_FROM);
                    int hourOfDayTo = sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_TO, SyncIdleAlarm.DEFAULT_IDLE_ALARM_HOUR_OF_DAY_TO);
                    int minuteTo = sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_TO, SyncIdleAlarm.DEFAULT_IDLE_ALARM_MINUTE_TO);
                    int totalFrom = hourOfDayFrom * 60 + minuteFrom;
                    int totalTo = hourOfDayTo * 60 + minuteTo;

                    Calendar c = Calendar.getInstance();
                    int totalCur = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);

                    boolean b = (totalTo >= totalFrom && totalCur >= totalFrom && totalCur <= totalTo) || (totalTo < totalFrom && (totalCur >= totalFrom || totalCur <= totalTo));
                    Log.d("circle", "hF = " + hourOfDayFrom + ", mF = " + minuteFrom + ", hT = " + hourOfDayTo + ", mT = " + minuteTo + ", cF = " + totalCur + ", boolean = " + b);
                    boolean isCharging = Utility.isPowerConnected(this);
                    boolean isSleeping = SleepDataModel.getInstance().getSleepEnabled();
                    boolean isInAlertTime= ((totalTo >= totalFrom && totalCur >= totalFrom && totalCur <= totalTo) || (totalTo < totalFrom && (totalCur >= totalFrom || totalCur <= totalTo)));
                    boolean isUsbConnected = Utility.isUsbConnected(this);
                    boolean isWearWorn = WApplication.getInstance().getWearWorn();
                    if (!isSleeping && !isCharging && isInAlertTime && !isUsbConnected && isWearWorn) {
                        NotificaionHelperRemindMe.getInstance(this).showRemingMeNotification();
                        Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
                        long[] pattern = {0, 200, 300, 200};
                        myVibrator.vibrate(pattern, -1);

                    }
                    int changeInterval = sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL, SyncIdleAlarm.DEFAULT_IDLE_ALARM_INTERVAL);
                    // changeInterval = 60000;
                    am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + changeInterval, pi);
                } else if(action.matches(ACTION_NEW_DAY)) {
                    Log.d("kim_bai", "service action_new_day");
                    if(NotificaionHelper.getInstance(this).isHasReachGoalNotification()) {
                        NotificaionHelper.getInstance(this).cancelNotification(NotificaionHelper.REACH_GOAL_NOTIFICATION_ID);
                    }
                    StepCountInfo.getInstance(CollectStepCountService.this).changeNextGoalForDayChange();
                    mShowStepGoalNotificationAlready = false;
                    //fix bug: 635011: [Wellness] watch face and wellenss microapp step not reset to 0
//                    Intent nightComming = new Intent(StepCountManager.ACTION_END_WALK);
//                    sendBroadcast(nightComming);
//                    StepHelper.broadcastSteps(CollectStepCountService.this, 0, 0);
                }else if(action.matches(ACTION_SLEEP_DONE)){
                    stepCountManager.enableSleepSensor(false);
                    SleepDataModel.getInstance().onSleepTriggered(false);
                    SleepDataModel.getInstance().setSleepStatus(SleepDataModel.eSleep.FINISH);
                    EBCommandUtils.changeSleepStatus(this.getClass().getName());
                }
                else if(action.matches(ACTION_GET_HISTORY_DATA))
                {
                    Log.d(Utility.TAG, ACTION_GET_HISTORY_DATA+" received");
                    stepCountManager.retriveDailyTotalStep();
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stepCountManager.unRegisterStepCountSensor();
        stepCountDataLayer.disConnectGoogleApiClient();
        unregisterReceiver(alarmBroadcastReceiver);
        //if (BuildConfig.DEBUG) {
        Log.d(Utility.TAG, TAG + "CollectStepCountService end");

        //for step detector event
        EventBus.getDefault().unregister(this);
        stepCountManager = null;
        stepCountDataLayer = null;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    private StepCountCallback mStepCountCallback = new StepCountCallback() {
        @Override
        public void onWalkStart(long time) {
            // TODO Auto-generated method stub
            Log.d("circle", "onwalkstart:" + time);
            am.cancel(pi);
        }

        @Override
        public void onWalkEnd(int stepNumbers, long time) {
            // TODO Auto-generated method stub
            Log.d("circle", "onwalkend:" + stepNumbers + " " + time);
//            mWalkingHander.removeMessages(MSG_WALKING);
            //fix bug tt 587383, step = -1
//            if(stepNumbers > 0) {
//                Intent intent = new Intent(WellnessMicroAppMain.ACTION_NO_ACTIVITY);
//                sendBroadcast(intent);
//                startAlarm();
//            }

            mWalkingHander.removeMessages(MSG_WALKEND );
            Message msg = Message.obtain();
            msg.what = MSG_WALKEND;
            mWalkingHander.sendMessageDelayed(msg, StepCountManager.ONE_MINUTE_MILLIES);
        }


        private Handler mWalkingHander = new Handler(new android.os.Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what == MSG_WALKING  ) {

                    long total_step = (long) msg.arg1;
                    Profile profile = ProfileHelper.getStandardProfile();

                    int heightInCM = profile.getHeight();
                    if(profile.getHeight_unit()==ProfileHelper.HEIGHT_UNIT_FT){
                        float ft=ProfileHelper.InchToFt(heightInCM);
                        heightInCM=(int) Math.round(ProfileHelper.ftToCm(ft));
                    }
                    int weightInKG = profile.getWeight();
                    if(profile.getWeight_unit() ==ProfileHelper.WEIGHT_UNIT_LBS){
                        weightInKG=(int) Math.round(ProfileHelper.LbsToKg(profile.getWeight()));
                    }

                   // Log.i("emily","updateSteps,height = "+profile.getHeight()+", weight = "+profile.getWeight());
                    long calories = ProfileHelper.getWalkCalories(heightInCM,weightInKG,total_step);

                    Intent intent = new Intent(WellnessMicroAppMain.ACTION_HAVE_ACTIVITY);
                    intent.putExtra(WellnessMicroAppMain.KEY_STEP_COUNTS, (int) total_step);
                    intent.putExtra(WellnessMicroAppMain.KEY_ACTIVTY_TYPE, ActivityStatusTable.TYPE_WALK);
                    intent.putExtra(WellnessMicroAppMain.KEY_TOTAL_STEP_COUNT, (int)total_step);
                    intent.putExtra(WellnessMicroAppMain.KEY_CALORIES_BURNED, (int)calories);
                    sendBroadcast(intent);

//                    StepHelper.broadcastSteps(CollectStepCountService.this, (int) total_step, (int)calories);
                    onWalkEnd((int)total_step, System.currentTimeMillis());

                    Log.d(Utility.TAG, "mWalkingHander broadcast greendao: " + " totalStep: " +total_step);
                    //smile_gao add for reach goal notification 2014/12/24
                    needReachGoalNotification(total_step);
                }else if(msg.what == MSG_WALKEND){
                    startAlarm();
                    stepCountManager.rebuildTodayStepSegments();
                }

                return false;
            }
        });

        @Override
        public void onWalking(float step) {
            // TODO Auto-generated method stub
            Log.d(Utility.TAG, "onWalking : " + step + " TodayTotalStep: " + StepHelper.getTodaySteps());
            mWalkingHander.removeMessages(MSG_WALKING);
            Message msg = Message.obtain();
            msg.what = MSG_WALKING;
            msg.arg1 = (int) step;
            mWalkingHander.sendMessageDelayed(msg, 100);
            WApplication.getInstance().setTodaySteps((int)step);
        }

        private void needReachGoalNotification(float nowSteps) {
            if (mShowStepGoalNotificationAlready || NotificaionHelper.getInstance(CollectStepCountService.this).isHasReachGoalNotification()) {
                return;
            }

            StepCountData stepdata = StepCountInfo.getInstance(CollectStepCountService.this).getStepCountInfo();
            float hasWalkSteps =  stepdata.DAILY_TOTAL_STEPS;

            Log.i("smile", String.valueOf(stepdata.DAILY_TARGET_STEPS) + " " + String.valueOf(stepdata.DAILY_TOTAL_STEPS) + " " + String.valueOf(nowSteps));

            if (hasWalkSteps > 0 && hasWalkSteps >= stepdata.DAILY_TARGET_STEPS) {
                // NotificaionHelper.getInstance(CollectStepCountService.this).setHasShowReachGoalNotification(true);
                NotificaionHelper.getInstance(CollectStepCountService.this).showReachGoalNotification(stepdata.DAILY_TARGET_STEPS);
                mShowStepGoalNotificationAlready = true;
            }
        }
    };


    public void settingAlarmManager() {
        // Register Broadcast Receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ALARM);
        intentFilter.addAction(ACTION_CANCEL_ALARM);
        intentFilter.addAction(ACTION_NEW_DAY);
        intentFilter.addAction(ACTION_NEW_DAY_CANCEL);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);

        registerReceiver(alarmBroadcastReceiver, intentFilter);
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        startAlarm();

        amNewDay = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        todayTimeMilles = System.currentTimeMillis();
    }

    private void startAlarm() {
        if (sp.getBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, false)) {
            Intent alarmIntent = new Intent(ACTION_ALARM);
            sendBroadcast(alarmIntent);
        }
    }

    public BroadcastReceiver alarmBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.matches(ACTION_ALARM)) {
                am.cancel(pi);
                Intent alarmIntent = new Intent(CollectStepCountService.this, CollectStepCountService.class);
                int changeInterval = sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL, SyncIdleAlarm.DEFAULT_IDLE_ALARM_INTERVAL);
                alarmIntent.setAction(ACTION_ALARM);
               // changeInterval = 60000;
                Log.d("circle", "change alarm interval to:" + changeInterval);
                pi = PendingIntent.getService(CollectStepCountService.this, 1, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + changeInterval, pi);
            } else if (action.matches(ACTION_CANCEL_ALARM)) {
                Log.d("circle", "cancel alarm");
                am.cancel(pi);
            } else if(action.matches(ACTION_NEW_DAY)) {
                amNewDay.cancel(piNewDay);
                Intent alarmIntent = new Intent(CollectStepCountService.this, CollectStepCountService.class);
                alarmIntent.setAction(ACTION_NEW_DAY);
                piNewDay = PendingIntent.getService(CollectStepCountService.this, 1, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, 24);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                Log.d("kim_bai", "new day..." + c.getTimeInMillis() + ", " + c.getTimeInMillis() + ", " + c.getTimeInMillis());
                amNewDay.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), piNewDay);
            } else if(action.matches(ACTION_NEW_DAY_CANCEL)) {
                Log.d("kim_bai", "new day cancel...");
                amNewDay.cancel(piNewDay);
            }else if(action.matches(Intent.ACTION_TIME_TICK)) {
                Long timeMilles = System.currentTimeMillis();
                if(!StepCountManager.isSameDay(todayTimeMilles, timeMilles)){
                    long todaySteps = StepHelper.getTodaySteps();
                    Log.d("kim_bai", "new day ACTION_TIME_CHANGED todaySteps : " + todaySteps);
                    todayTimeMilles = timeMilles;
                    mStepCountCallback.onWalking(todaySteps);
                }
            }
        }

    };
}

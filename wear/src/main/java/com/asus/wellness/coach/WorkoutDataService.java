package com.asus.wellness.coach;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.asus.wellness.dbhelper.CoachItem;
import com.asus.wellness.notification.NotificaionHelper;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.sleep.SleepDataModel;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.utils.EBCommand;

import de.greenrobot.event.EventBus;

public class WorkoutDataService extends Service {
    private String TAG = this.getClass().getName();
    private Handler mTimerHandler = new Handler();
    private CoachDataModel mCoachDataModel;
    private String COACH_ACTION_TIME_DONE = "set_coach_time";

    private Profile mProfile ;

    private PendingIntent mPenddingIntent;


   @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && COACH_ACTION_TIME_DONE.equals(intent.getAction())){
            mCoachDataModel.setTotalTime(mCoachDataModel.getTarget());
            onTargetAchived();
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mProfile = ProfileHelper.getStandardProfile();
        mCoachDataModel = ((WApplication)getApplication()).getCoachDataModel();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopForeground(true);
        EventBus.getDefault().unregister(this);
    }

    //for evenetbus ,receive command from WorkoutControllerLayout
    public void onEvent(EBCommand command){
//        if(this.getClass().getName().equals(command.receiver)){
            Log.d(Utility.TAG, "onEvent " + command.toString());
            if(EBCommand.COMMAND_FITNESS_ACTIVITY.equals(command.command)){
                long value  = (long)command.param;
                onFitActivityDataChanged(value);
            }else if(EBCommand.COMMAND_COACH_STATE_CHANGED.equals(command.command)){
                onCoachStateChanged();


            }else if(EBCommand.COMMAND_SHOW_COACH_NOTIFICATION.equals(command.command)){
                stopForeground(true);
                boolean show = (boolean)command.param;
                if(show) {
                    NotificaionHelper.getInstance(this).showCoachNotification(this);
                }
            }else if(EBCommand.COMMAND_START_ACTIVITY.equals(command.command)){
                Intent intent = new Intent();
                intent.setClassName(this, (String) command.param);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else if(EBCommand.COMMAND_SLEEP_VALUE.equals(command.command)){
                onSleepValueChanged((long) command.param);
            }
    }

    private void onCoachStateChanged(){
        String sender = this.getClass().getName();
        EBCommand feedback  =  new EBCommand( sender,CollectStepCountService.class.getName(),EBCommand.COMMAND_REGISTER_COACH_SENSOR, null);
        feedback.command = EBCommand.COMMAND_UNREGISTER_COACH_SENSOR;

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        switch (mCoachDataModel.getState()){
            case START:
                mCoachDataModel.startCoach();
                mCoachDataModel.setState(CoachDataModel.eState.PLAY);
            case PLAY:
            case RESUME:
                mCoachDataModel.setStartTime(System.currentTimeMillis());
                mCoachDataModel.setHistoryInterval(mCoachDataModel.getTotalTime());
                if(mCoachDataModel.getGoal() == CoachDataModel.eGoal.TIME  && mCoachDataModel.getState() == CoachDataModel.eState.PLAY){
                    Intent intent  = new Intent(this,this.getClass());
                    intent.setAction(COACH_ACTION_TIME_DONE);
                    mPenddingIntent= PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    long targetMillion = (mCoachDataModel.getTarget() - mCoachDataModel.getHistoryInterval())*1000;
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + targetMillion, mPenddingIntent);
                }
                feedback.command = EBCommand.COMMAND_REGISTER_COACH_SENSOR;
                EBCommandUtils.showCoachNotification(sender,true);
                break;
            case PAUSE:
                EBCommandUtils.showCoachNotification(sender,true);
            case STOP:
            case FINISH:{
                long quantity = mCoachDataModel.getTotalQuantity() - mCoachDataModel.getHistoryQuantity();
                if(quantity > 0) {
                    CoachItem item = new CoachItem();
                    item.setStart(mCoachDataModel.getStartTime());
                    item.setEnd(System.currentTimeMillis());
                    item.setValue(quantity);
                    mCoachDataModel.addCoachItem(item);
                }
                mCoachDataModel.setHistoryQuantity(mCoachDataModel.getTotalQuantity());
            }
            alarmManager.cancel(mPenddingIntent);
            if(mCoachDataModel.getState() == CoachDataModel.eState.STOP || mCoachDataModel.getState() == CoachDataModel.eState.FINISH ){
                mCoachDataModel.endCoach();
            }
            default:
                break;
        }
        EventBus.getDefault().post(feedback);
    }

    private void onTargetAchived() {
        mCoachDataModel.setState(CoachDataModel.eState.STOP);
//        onCoachStateChanged();
        //finish StartWorkoutActivity
        EBCommand feedback  =  new EBCommand(this.getClass().getName(),StartWorkoutActivity.class.getName(),EBCommand.COMMAND_COACH_STATE_CHANGED, null);
        EventBus.getDefault().post(feedback);
        //vibrate to remind use
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }

    private void onFitActivityDataChanged(long value){
        switch (mCoachDataModel.getState()) {
            case PLAY:
                mCoachDataModel.setStartQuantity(value);
                mCoachDataModel.setState(CoachDataModel.eState.RESUME);
                break;
            case RESUME: {
                long total_value = mCoachDataModel.getHistoryQuantity() + value - mCoachDataModel.getStartQuantity();
               mCoachDataModel.setTotalQuantity(total_value);
                mCoachDataModel.setCoachInfo(mProfile, total_value);
               
            }

            if(mCoachDataModel.achieveTargetValue()){
                onTargetAchived();
            }else {
                EBCommand feedback  =  new EBCommand(this.getClass().getName(),StartWorkoutActivity.class.getName(),EBCommand.COMMAND_COACH_DATA, String.valueOf(mCoachDataModel.getTotalQuantity()));
                EventBus.getDefault().post(feedback);
            }
            break;
            case PAUSE:
            case STOP:
            case FINISH:
                break;
            default:
                break;
        }
    }

    private void onSleepValueChanged(long value){
        Log.d(Utility.TAG, "onSleepValueChanged" + value );
//        log(event.timestamp, event.sensor.getType(), event.accuracy, event.values);
        SleepDataModel sleepDataModel   = SleepDataModel.getInstance();
        sleepDataModel.onValueChanged(value);
//        SleepItem sleepItem = new SleepItem(mCoachDataModel.getSleepId(),System.currentTimeMillis(),value);
//        SleepItemDao sleepItemDao = WApplication.getInstance().getDataHelper().getDaoSession().getSleepItemDao();
//        sleepItemDao.insert(sleepItem);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}

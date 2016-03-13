package com.asus.wellness.utils;

import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.StartWorkoutActivity;
import com.asus.wellness.coach.SummeryWorkoutActivity;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.fragment.TargetStatusFragment;
import com.asus.wellness.notification.NotificationWorkoutControllerActivity;
import com.asus.wellness.service.CollectStepCountService;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/7/1.
 */
public class EBCommandUtils {
    public static void showSleepNotification(String sender,boolean show){
        excuteCommand(sender, CollectStepCountService.class.getName(), EBCommand.COMMAND_SHOW_SLEEP_NOTIFICATION, show);
    }

    public static void enterAmbientMode(String sender,boolean ambient){
        excuteCommand(sender, "fake_fragment",EBCommand.COMMAND_AMBIENT_MODE,ambient);
    }

    public static void startSleep(String sender,boolean enable){
        excuteCommand(sender, CollectStepCountService.class.getName(), EBCommand.COMMAND_START_SLEEP, enable);
    }

    public static void onEndSyncTodaySteps(String sender){
        excuteCommand(sender, TargetStatusFragment.class.getName(), EBCommand.COMMAND_TODAY_STEP_SYNCDONE, null);
    }

    public static void showCoachNotification(String sender, boolean show) {
        excuteCommand(sender, WorkoutDataService.class.getName(), EBCommand.COMMAND_SHOW_COACH_NOTIFICATION, show);
    }

    public  static void showSummery(String sender) {
        excuteCommand(sender, WorkoutDataService.class.getName(), EBCommand.COMMAND_START_ACTIVITY, SummeryWorkoutActivity.class.getName());
    }

    public  static void changeCoachState(String sender) {
        excuteCommand(sender, WorkoutDataService.class.getName(), EBCommand.COMMAND_COACH_STATE_CHANGED, null );
    }

    public  static void startActivity(String sender,String className) {
        excuteCommand(sender, WorkoutDataService.class.getName(), EBCommand.COMMAND_START_ACTIVITY, className );
    }

    public static void changeSleepStatus(String sender){
        excuteCommand(sender, CollectStepCountService.class.getName(), EBCommand.COMMAND_SLEEP_STATE, null);
    }

    public static void getFitnessStep(String sender){
        excuteCommand(sender, CollectStepCountService.class.getName(), EBCommand.COMMAND_GET_FITNESS_STEP, null);
    }

    public static void excuteCommand(String sender, String receiver, String command, Object param){
        EBCommand ebCommand = new EBCommand(sender,receiver, command, param);
        EventBus.getDefault().post(ebCommand);
    }
}

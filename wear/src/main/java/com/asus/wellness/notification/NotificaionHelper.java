package com.asus.wellness.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.SummeryWorkoutActivity;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.sleep.SleepActivity;
import com.asus.wellness.sleep.SleepNofication;
import com.asus.wellness.utils.EBCommand;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2014/12/18.
 */
public class NotificaionHelper {
    private static NotificaionHelper mNotificationheler;
    public static int REACH_GOAL_NOTIFICATION_ID = 1000;
    public static int COACH_NOTIFICATION_ID = 1001;
    public static int SLEEP_NOTIFICATION_ID = 1002;

    public static final String TARGET_GOAL = "targetgoal";
    private Context mContext;
    private boolean hasReachGoalNotification = false;

    private NotificationManagerCompat notificationManager = null;

    // Constructor 1
    private NotificaionHelper() {
    }

    // Constructor 2
    private NotificaionHelper(Context context) {
        mContext = context;
    }

    // Singleton Pattern
    public static NotificaionHelper getInstance(Context context) {
        if (mNotificationheler == null) {
            mNotificationheler = new NotificaionHelper(context);
        }
        return mNotificationheler;
    }

    public void cancelNotification(int id) {
        hasReachGoalNotification = false;

        // Get an instance of the NotificationManager service
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(mContext);
        }
        notificationManager.cancel(id);

        // Cancel Send New Day Broadcast
        mContext.sendBroadcast(new Intent(CollectStepCountService.ACTION_NEW_DAY_CANCEL));
    }

    public void showNotification(int id, Notification notification) {
        // Get an instance of the NotificationManager service
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(mContext);
        }
        notificationManager.cancel(REACH_GOAL_NOTIFICATION_ID);
        notificationManager.notify(id, notification);
    }

    public boolean isHasReachGoalNotification() {
        return hasReachGoalNotification;
    }

    public void setHasReachGoalNotification(boolean hasReachGoalNotification) {
        this.hasReachGoalNotification = hasReachGoalNotification;
    }

    public void showReachGoalNotification(long stepGoal) {
        cancelNotification(REACH_GOAL_NOTIFICATION_ID);
        hasReachGoalNotification = true;

        // ReachGoal PendingIntent
        Intent reachGoalIntent = new Intent(mContext, ReachGoalActivity.class);
        PendingIntent reachGoalPendingIntent = PendingIntent.getActivity(mContext, 0, reachGoalIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Tap PendingIntent
        Intent tapIntent = new Intent(mContext, TapActivity.class);
        tapIntent.putExtra(TARGET_GOAL, stepGoal);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(mContext, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Tap WearableExtender
        NotificationCompat.WearableExtender wearableExtenderTap = new NotificationCompat.WearableExtender()
                .setDisplayIntent(tapPendingIntent)
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN);

        // Tap NotificationBuilder
        NotificationCompat.Builder notificationBuilderTap = new NotificationCompat.Builder(mContext).extend(wearableExtenderTap);
        float val = mContext.getResources().getDimension(R.dimen.notification_reach_goal_height);
        Log.i("smile", "notificaiotn height: " + val+" stepgoal: "+stepGoal);
        // WearableExtender
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setCustomContentHeight((int) val)
                .setDisplayIntent(reachGoalPendingIntent)
                .setBackground(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_micro_ic_bg1))
                .addPage(notificationBuilderTap.build());

        // Main NotificationBuilder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.mipmap.asus_icon_app_wellness)
                        .setVibrate(new long[]{2000})
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle(mContext.getString(R.string.met_goal))
                        .setContentText(mContext.getString(R.string.reach_goal))
                        .extend(wearableExtender);

        // Show Notification
        showNotification(REACH_GOAL_NOTIFICATION_ID, notificationBuilder.build());

        // Send New Day Broadcast
        mContext.sendBroadcast(new Intent(CollectStepCountService.ACTION_NEW_DAY));
    }

    public void showCoachNotification(Service service ) {

        float val = mContext.getResources().getDimension(R.dimen.notification_coach_height);
        Log.i("smile", "val: " + val);

        NotificationCompat.WearableExtender controllerPage = new NotificationCompat.WearableExtender()
                .setDisplayIntent(PendingIntent.getActivity(mContext, 0,  new Intent(mContext, NotificationWorkoutControllerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN);

        // WearableExtender
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setCustomContentHeight((int) val)
                .setDisplayIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, NotificationWorkoutInfoActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setBackground(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_micro_ic_bg1));
               // .addPage( new NotificationCompat.Builder(mContext).extend(controllerPage).build());

        // Main NotificationBuilder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.mipmap.asus_icon_app_wellness)
                        .setContentTitle(getCoachStateTitle(mContext.getString(R.string.coach)))
                        .extend(wearableExtender) ;
        // Show Notification
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(mContext);
        }
        service.startForeground(COACH_NOTIFICATION_ID,  notificationBuilder.build());
    }

    public void showSleepNotification(Service service) {
        Intent info = new Intent(mContext, SleepNofication.class);
        PendingIntent InfopenddingIntent = PendingIntent.getActivity(mContext, 0, info, PendingIntent.FLAG_UPDATE_CURRENT);
        float val = mContext.getResources().getDimension(R.dimen.notification_reach_goal_height);
        Log.i("smile", "val: " + val);

        // WearableExtender
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setCustomContentHeight((int) val)
                .setDisplayIntent(InfopenddingIntent)
                .setBackground(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_micro_ic_bg))
                ;

        // Main NotificationBuilder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.mipmap.asus_wellness_sleep_icon)
                        .setContentTitle(mContext.getString(R.string.sleep_tracking))
                        .extend(wearableExtender) ;
        // Show Notification
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(mContext);
        }

        service.startForeground(SLEEP_NOTIFICATION_ID,  notificationBuilder.build());
    }

    public String getCoachStateTitle(String title){
        String coach = title + "  ";
        switch (WApplication.getInstance().getCoachDataModel().getState()){
            case START:
            case PLAY:
            case RESUME:
                coach += mContext.getString(R.string.coach_resume)  ;
                break;
            case PAUSE:
                coach += mContext.getString(R.string.coach_pause)  ;
                break;
            case STOP:
            case FINISH:
                coach  += mContext.getString(R.string.coach_finished)  ;
            default:
                break;
        }
        return coach;
    }

}

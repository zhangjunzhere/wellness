package com.asus.wellness.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.asus.wellness.R;

/**
 * Created by smile_gao on 2014/12/18.
 */
public class NotificaionHelperRemindMe {
    private static NotificaionHelperRemindMe mNotificationheler;
    public static int WARM_UP_NOTIFICATION_ID = 1001;
    private Context mContext;
    private boolean hasReachGoalNotification = false;
    private NotificationManagerCompat notificationManager = null;
    private Notification notification = null;

    // Constructor 1
    private NotificaionHelperRemindMe() {
    }

    // Constructor 2
    private NotificaionHelperRemindMe(Context context) {
        mContext = context;
    }

    // Singleton Pattern
    public static NotificaionHelperRemindMe getInstance(Context context) {
        if (mNotificationheler == null) {
            mNotificationheler = new NotificaionHelperRemindMe(context);
        }
        return mNotificationheler;
    }

    public void cancelNotification(int id) {
        // Get an instance of the NotificationManager service
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(mContext);
        }
        notificationManager.cancel(id);
    }

    public void showNotification(int id, Notification notification) {
        // Get an instance of the NotificationManager service
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(mContext);
        }
        notificationManager.notify(id, notification);
    }

    public void showRemingMeNotification() {
        cancelNotification(WARM_UP_NOTIFICATION_ID);

        // Main NotificationBuilder // Fix bug of language of notification don't change when switch languages.
       // if (notification == null) {
            notification =
                    new NotificationCompat.Builder(mContext)
                            .setVibrate(new long[]{0})
                            .setSmallIcon(R.mipmap.asus_icon_app_wellness)
                            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_micro_ic_bg))
                            .setContentTitle(mContext.getString(R.string.warm_up))
                            .setContentText(mContext.getResources().getString(R.string.remind_me_to_move)).build();
        //}

        // Show Notification
        showNotification(WARM_UP_NOTIFICATION_ID, notification);
    }

}

package com.asus.wellness.sleep;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

import com.asus.wellness.R;

/**
 * Created by larrylf_lin on 2015/10/27.
 */
public class SleepTrackingNotification {


    private static SleepTrackingNotification instance;
    private BroadcastReceiver mReceiver;
    public  static final String CLICK_ACTION = "com.asus.wellness.notification.click";
    private  final int TRACKING_NOTIFICATION_ID = 10001;
    private   Notification status;
    public static SleepTrackingNotification getInstance(){
        if (instance == null){
            instance = new SleepTrackingNotification();
        }
        return  instance;
    }

//    public void  registerTrackingNotification(Context context){
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(CLICK_ACTION);
//        context.registerReceiver(mReceiver, filter);
//        if (mReceiver == null) {
//            mReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//
//                    if (intent.getAction().equals(CLICK_ACTION)) {
//                        cancelTrackingNotification(context);
//                    }
//                }
//
//            };
//        }
//    }

//    public void unRegisterTrackingNotification(Context context){
//        context.unregisterReceiver(mReceiver);
//    }


    public void showRemoteViewNotification(Context context) {

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.sleep_tracking_notification);

        //这部分主要是对RemoteViews进行相关设定，关于Button的点击响应在这部分完成。
        Intent intent = new Intent();
        intent = new Intent(this.CLICK_ACTION);
        PendingIntent pendingIntent = null;
        pendingIntent = PendingIntent.getBroadcast(context,
                0 /* no requestCode */, intent, 0 /* no flags */);


        views.setOnClickPendingIntent(R.id.notification_stop, pendingIntent);

        //这部分内容主要是对整个Notification进行相关设定。
        status = new Notification();
        status.contentView = views;
        //status.flags |= Notification.FLAG_ONGOING_EVENT;
        status.icon = R.mipmap.asus_wellness_icon;

        NotificationManager manager = (NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(TRACKING_NOTIFICATION_ID, status);
    }

    public  void cancelTrackingNotification(Context context){
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(TRACKING_NOTIFICATION_ID);

    }


}

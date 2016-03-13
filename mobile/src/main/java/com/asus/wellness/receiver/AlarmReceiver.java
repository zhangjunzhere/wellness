package com.asus.wellness.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asus.wellness.ui.MainWellness;
import com.asus.wellness.utils.Utility;

import java.util.Calendar;

/**
 * Created by Kim_Bai on 5/28/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
  //  private static int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
     //   Toast.makeText(context, "Time Up! " + count, Toast.LENGTH_SHORT).show();
     //   count++;
        Intent intentUpadteDate = new Intent(MainWellness.ACTION_UPDATE_DATE);
        context.sendBroadcast(intentUpadteDate);
    }

    /**
     * ????????12:00???????
     * @param context
     */
    public void startAlarm(Context context) {
      //  Toast.makeText(context, "Start Alarm", Toast.LENGTH_SHORT).show();

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 8:30 a.m.
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), Utility.ONE_DAY_MS, alarmIntent);
    }

    /**
     * ????????
     * @param context
     */
    public void cancelAlarm(Context context) {
     //   Toast.makeText(context, "Cancel Alarm", Toast.LENGTH_SHORT).show();

        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}

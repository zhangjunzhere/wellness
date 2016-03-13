package com.asus.wellness.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.microprovider.ActivityStatusTable;

/**
 * Created by smile_gao on 2015/6/17.
 */
public class ActivityHelper {
    public  static Intent getActivityIntent(Context context)
    {
        Cursor cursor=context.getContentResolver().query(ActivityStatusTable.TABLE_URI, null, null, null, null);
        int type = 0;
        Intent intent=new Intent();
        if(cursor.moveToFirst()){
            type=cursor.getInt(cursor.getColumnIndex(ActivityStatusTable.COLUMN_TYPE));
            intent.putExtra(WellnessMicroAppMain.KEY_ACTIVTY_TYPE, type);
            switch(type){
                case ActivityStatusTable.TYPE_WALK:
                    intent.putExtra(WellnessMicroAppMain.KEY_STEP_COUNTS, cursor.getInt(cursor.getColumnIndex(ActivityStatusTable.COLUMN_STEP)));
                    break;
                case ActivityStatusTable.TYPE_DRIVE:
                    intent.putExtra(WellnessMicroAppMain.KEY_DISTANCE, cursor.getInt(cursor.getColumnIndex(ActivityStatusTable.COLUMN_DISTANCE)));
                    break;
                case ActivityStatusTable.TYPE_BIKE:
                    if(cursor.getInt(cursor.getColumnIndex(ActivityStatusTable.COLUMN_STEP))>0){
                        intent.putExtra(WellnessMicroAppMain.KEY_STEP_COUNTS, cursor.getInt(cursor.getColumnIndex(ActivityStatusTable.COLUMN_STEP)));
                    }
                    intent.putExtra(WellnessMicroAppMain.KEY_DISTANCE, cursor.getFloat(cursor.getColumnIndex(ActivityStatusTable.COLUMN_DISTANCE)));
                    break;
            }
        }
        cursor.close();
        return  intent;
    }
}

package com.asus.wellness.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.EcgTable;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2015/6/17.
 */
public class Profiledb {
    public static void updateProfile(Context context,String photourl)
    {
        Cursor cursor=context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
        if(cursor.moveToFirst()){
            long start_time=cursor.getLong(cursor.getColumnIndex(ProfileTable.COLUMN_START_TIME));
            ContentValues cv=new ContentValues();
            cv.put(ProfileTable.COLUMN_PHOTO, photourl);
            context.getContentResolver().update(ProfileTable.TABLE_URI, cv, ProfileTable.COLUMN_START_TIME+"=?", new String[]{String.valueOf(start_time)});
        }
        cursor.close();
    }
}

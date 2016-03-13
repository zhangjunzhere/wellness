package com.asus.wellness.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2014/12/25.
 */
public class StepGoalHelper {
        private static  StepGoalHelper mInstance = null;
        private Context mContext = null;
    private  StepGoalHelper(Context context)
    {
            mContext = context;
    }
    public static  StepGoalHelper getmInstance(Context context)
    {
        if(mInstance == null)
        {
            mInstance = new StepGoalHelper(context);
        }
        return  mInstance;
    }

    private String todayDateTime;
    public void saveStepGoalToDB(int goal){
        Log.i("smile","saveStepGoalToDB "+goal);
        todayDateTime = Utility.getDateTime(System.currentTimeMillis(), "yyyy-MM-dd");
        Cursor cursor=mContext.getContentResolver().query(StepGoalTable.TABLE_URI, null
                , StepGoalTable.COLUMN_DATE_TIME_MILLI+"=?", new String[]{todayDateTime}, null);

        ContentValues cv=new ContentValues();
        cv.put(StepGoalTable.COLUMN_DATE_TIME_MILLI, todayDateTime);
        cv.put(StepGoalTable.COLUMN_STEP_GOAL, goal);

        if(cursor.moveToFirst()){
            if(cursor.getInt(cursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL)) == goal) {
                cursor.close();
                return;
            }
            mContext.getContentResolver().update(StepGoalTable.TABLE_URI, cv, StepGoalTable.COLUMN_DATE_TIME_MILLI+"=?", new String[]{todayDateTime});
        }
        else{
            mContext.getContentResolver().insert(StepGoalTable.TABLE_URI, cv);
        }

        cursor.close();
    }

    private String nextDateTime;
    public void saveNextStepGoalToDB(int nextGoal){
        //fix bug 641530
        saveStepGoalToDB(nextGoal);
        nextDateTime = Utility.getDateTime(System.currentTimeMillis() + Utility.ONE_DAY_MS, "yyyy-MM-dd");
        Cursor cursor=mContext.getContentResolver().query(StepGoalTable.TABLE_URI, null
                , StepGoalTable.COLUMN_DATE_TIME_MILLI+"=?", new String[]{nextDateTime}, null);

        ContentValues cv=new ContentValues();
        cv.put(StepGoalTable.COLUMN_DATE_TIME_MILLI, nextDateTime);
        cv.put(StepGoalTable.COLUMN_STEP_GOAL, nextGoal);
        if(cursor.moveToFirst()){
            mContext.getContentResolver().update(StepGoalTable.TABLE_URI, cv, StepGoalTable.COLUMN_DATE_TIME_MILLI+"=?", new String[]{nextDateTime});
        }
        else{
            mContext.getContentResolver().insert(StepGoalTable.TABLE_URI, cv);
        }

        cursor.close();
    }

    public void deleteNextStepGoalFromDB(){
        Log.d("kim_bai", "delete next goal");
        nextDateTime = Utility.getDateTime(System.currentTimeMillis() + Utility.ONE_DAY_MS, "yyyy-MM-dd");
        mContext.getContentResolver().delete(StepGoalTable.TABLE_URI, StepGoalTable.COLUMN_DATE_TIME_MILLI+"=?", new String[]{nextDateTime});
    }
}

package com.asus.wellness.microprovider;

import android.content.Context;
import android.database.Cursor;

import com.asus.wellness.utils.Utility;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.ProfileDao;

import java.util.Calendar;
import java.util.List;

/**
 * Created by smile_gao on 2014/12/24.
 */
public class StepCountInfo {

    private  static StepCountInfo mInstance;
    private  static Context mContext;
    private StepCountInfo()
    {

    }
    private StepCountInfo(Context cnxt)
    {
        mContext = cnxt;
    }
    public  static  StepCountInfo getInstance(Context conxt)
    {
        if(mInstance == null)
        {
            synchronized (StepCountInfo.class) {
                if(mInstance == null)
                     mInstance = new StepCountInfo(conxt);
            }
        }
        return  mInstance;
    }

    public StepCountData getStepCountInfo(){
        long todayStartMilli, todayEndMilli;
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DATE);

        int totalSteps=0, targetSteps= Utility.TARGET_GOAL;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        todayStartMilli=calendar.getTimeInMillis();
        todayStartMilli=todayStartMilli/1000*1000;
        calendar.set(year, month, day, 23, 59, 59);
        todayEndMilli=calendar.getTimeInMillis();

        Cursor cursor=mContext.getContentResolver().query(StepCountTable.TABLE_URI, null
                , StepCountTable.COLUMN_START+">=? and "+StepCountTable.COLUMN_END+"<=?"
                , new String[]{String.valueOf(todayStartMilli), String.valueOf(todayEndMilli)}, null);
        if(cursor.moveToFirst()){
            do{
                totalSteps+=cursor.getInt(cursor.getColumnIndex(StepCountTable.COLUMN_STEP_COUNT));
            }while(cursor.moveToNext());
        }
        cursor.close();

        Cursor stepGoalCursor=mContext.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null, null);
        if(stepGoalCursor.moveToFirst()){
            targetSteps=stepGoalCursor.getInt(stepGoalCursor.getColumnIndex(ProfileTable.COLUMN_STEP_GOAL));
        }
        if(targetSteps==0)
        {
            targetSteps= Utility.TARGET_GOAL;
        }
        stepGoalCursor.close();
        return new StepCountData(totalSteps, targetSteps);
    }

    public void saveNextStepGoalToDB(int nextGoal){
        ProfileDao profileDao =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
        List<Profile> list = profileDao.loadAll();
        if(list !=null && list.size()>0)
        {
            Profile p = list.get(0);
            p.setStep_goal(nextGoal);
            p.setNext_step_goal(nextGoal);
            profileDao.update(p);
        }
        else
        {
            Profile p = new Profile();
            p.setAge(ProfileTable.DEFAULT_AGE);
            p.setGender(ProfileTable.MALE);
            p.setHeight_unit(ProfileTable.HEIGHT_UNIT_CM);
            p.setHeight(ProfileTable.DEFAULT_HEIGHT);
            p.setWeight_unit(ProfileTable.WEIGHT_UNIT_KG);
            p.setWeight(ProfileTable.DEFAULT_WEIGHT);
            p.setStart_time(System.currentTimeMillis());
            p.setStep_goal(Utility.TARGET_GOAL);
            p.setNext_step_goal(nextGoal);
            p.setDistance_unit(ProfileTable.DISTANTCE_UNIT_KM);
            p.setIsprofileset(false);
            profileDao.insert(p);

        }
        mContext.getContentResolver().notifyChange(ProfileTable.TABLE_URI,null);
//        Cursor cursor = mContext.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
//
//        ContentValues cv = new ContentValues();
//        cv.put(ProfileTable.COLUMN_STEP_GOAL, nextGoal);
//    //    cv.put(ProfileTable.COLUMN_NEXT_STEP_GOAL, nextGoal);
//
//        if(cursor.moveToFirst()){
//            mContext.getContentResolver().update(ProfileTable.TABLE_URI, cv, null, null);
//        } else {
//            mContext.getContentResolver().insert(ProfileTable.TABLE_URI, cv);
//        }
//
//        cursor.close();
    }


    public void changeNextGoalForDayChange()
    {
        ProfileDao profileDao =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
        List<Profile> list = profileDao.loadAll();
        if(list !=null && list.size()>0)
        {
            Profile p = list.get(0);
            if(p.getNext_step_goal() == null || p.getNext_step_goal() ==-1)
            {
                return;
            }
            p.setStep_goal(p.getNext_step_goal());
            p.setNext_step_goal(-1);
            profileDao.update(p);
            mContext.getContentResolver().notifyChange(ProfileTable.TABLE_URI,null);
        }
    }
}

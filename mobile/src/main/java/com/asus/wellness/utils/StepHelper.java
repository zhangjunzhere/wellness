package com.asus.wellness.utils;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.asus.sharedata.ShareUtils;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Activity_state;
import com.asus.wellness.dbhelper.Activity_stateDao;
import com.asus.wellness.dbhelper.Coach;
import com.asus.wellness.dbhelper.CoachDao;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.WellnessProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by smile_gao on 2015/8/4.
 */
public class StepHelper {
    public static final long ONE_MINUTE_TIMEOUT = 1 * 60 * 1000;//1 mins

    public  static void writeStepCountInfo(long startTime, long endTime, int step, long deviceId, Context context){
        //to fix charles bug, avoid redundant data inserted
        Log.i("smile","writeStepCountInfo");
        String selection = ActivityStateTable.COLUMN_TYPE +"="+ ActivityStateTable.TYPE_WALK  +
                " and " + ActivityStateTable.COLUMN_START +"="+startTime +
                " and " + ActivityStateTable.COLUMN_END+"="+ endTime +
                " and " + ActivityStateTable.COLUMN_STEP_COUNT+"="+ step;
        Cursor   cursor = context.getContentResolver().query(ActivityStateTable.TABLE_URI,null,selection,null,null,null);
        int count =  cursor.getCount();
        cursor.close();
        if( count > 0){
            return ;
        }

        //to do if too much step in shot period, means need merge into last step info
        boolean needInsert = true;
        //to do if too much step in shot period, means need merge into last step info. assume  max 5 steps/s
        long period =  (endTime - startTime)/1000;
        String sortOrder = ActivityStateTable.COLUMN_START + " desc limit 1";
        int id = -1;

        selection = ActivityStateTable.COLUMN_TYPE + "=" + ActivityStateTable.TYPE_WALK+" and deviceId = "+deviceId;
        cursor = context.getContentResolver().query(ActivityStateTable.TABLE_URI,null,selection,null,sortOrder,null);
        if(cursor.moveToFirst()){
            id = cursor.getInt(cursor.getColumnIndex(ActivityStateTable.COLUMN_ID));
            long  start =cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_START));
            long  lastEndTime =cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_END));
            int step_count = cursor.getInt(cursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
            if(isSameDay(startTime,start) &&(startTime - lastEndTime < ONE_MINUTE_TIMEOUT  || period * 5 < step)){
                needInsert = false;
                startTime = start;
                step += step_count;
            }
        }
        if(cursor!=null)
             cursor.close();

        //fix bug: start > end, root cause?
        if(endTime < startTime){
            endTime = System.currentTimeMillis();
        }

        ContentValues cv=new ContentValues();
        cv.put(ActivityStateTable.COLUMN_START, startTime);
        cv.put(ActivityStateTable.COLUMN_END, endTime);
        cv.put(ActivityStateTable.COLUMN_STEP_COUNT, step);
        cv.put(ActivityStateTable.COLUMN_TYPE, ActivityStateTable.TYPE_WALK);
        cv.put(ParseDataManager.COLUMN_DEVICE_ID, deviceId);

        if(needInsert){
            context.getContentResolver().insert(ActivityStateTable.TABLE_URI, cv);
        }else{
            String where = ActivityStateTable.COLUMN_ID + "="  + id;
            context.getContentResolver().update(ActivityStateTable.TABLE_URI, cv,where,null);
        }
    }
    public static void insertCoachs(List<Coach> list,Device deviceDb)
    {
       CoachDao coachDao = WApplication.getInstance().getDataHelper().getDaoSession().getCoachDao();
       List<Coach> insertList = new ArrayList<>();
        for (Coach coach: list
             ) {
           List<Coach>  tempList = coachDao.queryBuilder().where(CoachDao.Properties.Start.eq(coach.getStart()),CoachDao.Properties.End.eq(coach.getEnd()),CoachDao.Properties.DeviceId.eq(deviceDb.getId())).list();
            if(tempList == null || tempList.size()==0)
            {
                coach.setDeviceId(deviceDb.getId());
                coach.setId(null);
                insertList.add(coach);
            }
        }
        if(insertList.size()>0)
        {
            Log.i("smile","insertCoachs size: "+insertList.size());
            coachDao.insertOrReplaceInTx(insertList);
        }

    }
    public static ContentProviderOperation getInsertStepCountContentProviderOperation(long startTime, long endTime, long step, long deviceId, Context context,int type)
    {
        //to fix charles bug, avoid redundant data inserted
        int mtype = type<=0 ? ActivityStateTable.TYPE_WALK: type;
        Log.i("smile","writeStepCountInfo");
        String selection = ActivityStateTable.COLUMN_TYPE +"="+ mtype  +
                " and " + ActivityStateTable.COLUMN_START +"="+startTime +
                " and  deviceId ="+ deviceId;
            //    " and " + ActivityStateTable.COLUMN_TYPE+"="+ mtype;
            //    " and " + ActivityStateTable.COLUMN_STEP_COUNT+"="+ step;
        Cursor   cursor = context.getContentResolver().query(ActivityStateTable.TABLE_URI,null,selection,null,null,null);
        int count =  cursor.getCount();
        if( count > 0){
            if(cursor.moveToFirst()) {
                long dbendtime = cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_END));
                long id = cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_ID));
                Log.i("smile", "step exist endtime: " + endTime + " db endtime:" + dbendtime);
                if (endTime != dbendtime) {
                    ContentValues cv = new ContentValues();
                  //  cv.put(ActivityStateTable.COLUMN_ID, id);
                    cv.put(ActivityStateTable.COLUMN_END, endTime);
                    cv.put(ActivityStateTable.COLUMN_STEP_COUNT, step);
                    cv.put(ActivityStateTable.COLUMN_TYPE, mtype);
                    cv.put(ParseDataManager.COLUMN_DEVICE_ID, deviceId);
                    cursor.close();
                    return ContentProviderOperation.newUpdate(ActivityStateTable.TABLE_URI).withSelection(ActivityStateTable.COLUMN_ID+"=?" , new String[]{String.valueOf(id)}).withValues(cv).build();
                }
            }
            cursor.close();
            return null;
        }
        else
        {
            cursor.close();
        }
        ContentValues cv=new ContentValues();
        cv.put(ActivityStateTable.COLUMN_START, startTime);
        cv.put(ActivityStateTable.COLUMN_END, endTime);
        cv.put(ActivityStateTable.COLUMN_STEP_COUNT, step);
        cv.put(ActivityStateTable.COLUMN_TYPE, mtype);
        cv.put(ParseDataManager.COLUMN_DEVICE_ID, deviceId);
       return ContentProviderOperation.newInsert(ActivityStateTable.TABLE_URI).withValues(cv).build();
    }
    public static void writeStepInfoInTransaction(ArrayList<ContentProviderOperation> list, Context context)
    {
        try
        {
            context.getContentResolver().applyBatch(WellnessProvider.AUTHORITY,list);
        }catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (OperationApplicationException e)
        {
            e.printStackTrace();
        }
    }


    private static Boolean isSameDay(long dateTime1, long dateTime2){
        //cross date
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(dateTime1);
        int date1=cal.get(Calendar.DATE);
        cal.setTimeInMillis(dateTime2);
        int date2=cal.get(Calendar.DATE);
        return date1 == date2;
    }

    public static long getTodayTotalStepsByDevice(Device device)
    {
        if(device.getId() == null)
        {
            return  0;
        }
        Activity_stateDao activity_stateDao = WApplication.getInstance().getDataHelper().getDaoSession().getActivity_stateDao();
        long midtime = ShareUtils.getMidnightMilles(System.currentTimeMillis());
        List<Activity_state> list = activity_stateDao.queryBuilder().where(Activity_stateDao.Properties.DeviceId.eq(device.getId()),Activity_stateDao.Properties.Start.ge(midtime)).list();
        long sum =0;
        if(list!=null && list.size()>0)
        {
            for (Activity_state a: list  ) {
                sum+=a.getStep_count();
            }
        }
        return sum;
    }
    public static void removeAllTodaySteps(Device device)
    {
        Activity_stateDao activity_stateDao = WApplication.getInstance().getDataHelper().getDaoSession().getActivity_stateDao();
        long midtime = ShareUtils.getMidnightMilles(System.currentTimeMillis());
        List<Activity_state> list = activity_stateDao.queryBuilder().where(Activity_stateDao.Properties.DeviceId.eq(device.getId()),Activity_stateDao.Properties.Start.ge(midtime)).list();
        activity_stateDao.deleteInTx(list);
        Log.i("smile","removeAllTodaySteps ");
    }
}

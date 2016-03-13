package com.asus.wellness.utils;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.asus.sharedata.SyncData;
import com.asus.sharedata.SyncProfile;
import com.asus.wellness.DataLayerManager;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Activity_state;
import com.asus.wellness.dbhelper.Activity_stateDao;
import com.asus.wellness.dbhelper.DaoSession;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.ProfileDao;
import com.asus.wellness.dbhelper.Step_goal;
import com.asus.wellness.dbhelper.Step_goalDao;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.EcgTable;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.provider.StepGoalHelper;
import com.asus.wellness.provider.WellnessProvider;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by smile_gao on 2015/8/4.
 */
public class ProfileHelper {
    public static final long ONE_MINUTE_TIMEOUT = 1 * 60 * 1000;//1 mins

    public static void updateProfileStartTime() {
       DaoSession daoSession = WApplication.getInstance().getDataHelper().getDaoSession();
        ProfileDao profileDao = daoSession.getProfileDao();
       List<Profile> profiles = profileDao.loadAll();
        if (profiles.size() > 0) {
            long theOldestMeasureTime =getStartMeasureTime();
            Profile profile = profiles.get(0);
            profile.setStart_time(theOldestMeasureTime);
            profileDao.update(profile);
        }
    }

    public static long getStartMeasureTime() {
        DaoSession daoSession = WApplication.getInstance().getDataHelper().getDaoSession();
        long theOldestMeasureTime = System.currentTimeMillis();
        List<Ecg> ecgs = daoSession.getEcgDao().queryBuilder().orderAsc(EcgDao.Properties.Measure_time).limit(1).list();
        if (ecgs.size() > 0) {
            theOldestMeasureTime = Math.min(theOldestMeasureTime, ecgs.get(0).getMeasure_time());
        }

        List<Activity_state> activity_states = daoSession.getActivity_stateDao().queryBuilder().orderAsc(Activity_stateDao.Properties.Start).limit(1).list();
        if (activity_states.size() > 0) {
            theOldestMeasureTime = Math.min(theOldestMeasureTime, activity_states.get(0).getStart());
        }
        return theOldestMeasureTime;
    }

    public static void updateProfilePhoto(String photourl)
    {
        DaoSession daoSession = WApplication.getInstance().getDataHelper().getDaoSession();
        ProfileDao profileDao = daoSession.getProfileDao();
        List<Profile> profiles = profileDao.loadAll();
        if (profiles.size() > 0) {
            Profile profile =  profiles.get(0);
            profile.setPhoto_path(photourl);
            profileDao.update(profile);
        }
//        Cursor cursor=context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
//        if(cursor.moveToFirst()){
//            long start_time=cursor.getLong(cursor.getColumnIndex(ProfileTable.COLUMN_START_TIME));
//            ContentValues cv=new ContentValues();
//            cv.put(ProfileTable.COLUMN_PHOTO, photourl);
//            context.getContentResolver().update(ProfileTable.TABLE_URI, cv, ProfileTable.COLUMN_START_TIME+"=?", new String[]{String.valueOf(start_time)});
//        }
//        cursor.close();
    }
    public static void updateProfileFromWear(Context context,DataItem dataItem)
    {
        Log.i("smile","updateProfileFromWear receive");
        DataMap data = DataMapItem.fromDataItem(dataItem).getDataMap();
      //  int age = data.getInt(SyncProfile.KEY_PROFILE_AGE, 25);
        int gender =data.getInt(SyncProfile.KEY_PROFILE_GENDER,0);
        int height = data.getInt(SyncProfile.KEY_PROFILE_HEIGHT,170);
        int heightunit = data.getInt(SyncProfile.KEY_PROFILE_HEIGHT_UNIT,0);
        int weight = data.getInt(SyncProfile.KEY_PROFILE_WEIGHT,70);
        int weightunit = data.getInt(SyncProfile.KEY_PROFILE_WEIGHT_UNIT, 0);
        int disunit = data.getInt(SyncProfile.KEY_PROFILE_DISTANCE_UNIT, 0);
        int stepgoal = data.getInt(SyncProfile.KEY_PROFILE_STEP_GOAL, 7000);
        String device = data.getString(SyncData.STEP_COUNT_DEIVCE_NAME);
        ProfileDao profileDao = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
        List<Profile> list = profileDao.loadAll();
        if(list!=null && list.size()>0)
        {
            Profile p = list.get(0);
          //  p.setAge(age);
            p.setGender(gender);
            p.setHeight(height);
            p.setHeight_unit(heightunit);
            p.setWeight(weight);
            p.setWeight_unit(weightunit);
            p.setDistance_unit(disunit);
            profileDao.update(p);
            context.getContentResolver().notifyChange(ProfileTable.TABLE_URI,null);
        }
        StepGoalHelper.getmInstance(context).saveStepGoalToDB(stepgoal);

    }
}

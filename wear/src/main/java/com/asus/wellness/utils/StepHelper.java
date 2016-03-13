package com.asus.wellness.utils;

import android.content.Intent;
import android.util.Log;

import com.asus.sharedata.CoachSyncItem;
import com.asus.wellness.WApplication;
import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.dbhelper.Activity_status;
import com.asus.wellness.dbhelper.Activity_statusDao;
import com.asus.wellness.dbhelper.Coach;
import com.asus.wellness.dbhelper.CoachDao;
import com.asus.wellness.dbhelper.CoachItem;
import com.asus.wellness.dbhelper.CoachItemDao;
import com.asus.wellness.dbhelper.DaoSession;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.Sleep;
import com.asus.wellness.dbhelper.SleepDao;
import com.asus.wellness.dbhelper.Step_count;
import com.asus.wellness.dbhelper.Step_countDao;
import com.asus.wellness.microprovider.ActivityStatusTable;
import com.asus.wellness.sync.CoachTempItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by smile_gao on 2015/7/13.
 */
public class StepHelper {

//    public static StepCountData getStepCountInfo(Context context){
//        if(context == null)
//        {
//            Log.i("smile","getStepCountInfo null");
//        }
//        long todayStartMilli, todayEndMilli;
//        Calendar today = Calendar.getInstance();
//        int year = today.get(Calendar.YEAR);
//        int month = today.get(Calendar.MONDAY);
//        int day = today.get(Calendar.DATE);
//
//        int totalSteps=0, targetSteps=Utility.TARGET_GOAL;
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month, day, 0, 0, 0);
//        todayStartMilli=calendar.getTimeInMillis();
//        todayStartMilli=todayStartMilli/1000*1000;
//        calendar.set(year, month, day, 23, 59, 59);
//        todayEndMilli=calendar.getTimeInMillis();
//
//        Cursor cursor=context.getContentResolver().query(StepCountTable.TABLE_URI, null
//                , StepCountTable.COLUMN_START+">=? and "+StepCountTable.COLUMN_END+"<=?"
//                , new String[]{String.valueOf(todayStartMilli), String.valueOf(todayEndMilli)}, null);
//        if(cursor.moveToFirst()){
//            do{
//                totalSteps+=cursor.getInt(cursor.getColumnIndex(StepCountTable.COLUMN_STEP_COUNT));
//            }while(cursor.moveToNext());
//        }
//        cursor.close();
//
////        Cursor stepGoalCursor=context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null, null);
////        if(stepGoalCursor.moveToFirst()){
////            targetSteps=stepGoalCursor.getInt(stepGoalCursor.getColumnIndex(ProfileTable.COLUMN_STEP_GOAL));
////        }
////        stepGoalCursor.close();
//        //fix bug 636570
//        List<Profile> list = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
//        if(list!=null && list.size()>0)
//        {
//            targetSteps = list.get(0).getStep_goal();
//            if(list.get(0).getName()==null && targetSteps == 0 )
//            {
//                targetSteps=Utility.TARGET_GOAL;
//            }
//        }
//        //smile_gao fix bug for steps <0
//        if(totalSteps<0)
//        {
//            totalSteps = 0;
//        }
//
//        return new StepCountData(totalSteps, targetSteps);
//    }

    public static int getTargetSteps(){
        int targetSteps = Utility.TARGET_GOAL;
        List<Profile> list = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
        //targetvalue in database maybe null
        if(list.size()>0 && list.get(0).getStep_goal() != null) //
        {
            targetSteps = list.get(0).getStep_goal();
        }
        return targetSteps;
    }

//    public static int getNowSteps(Intent mIntent){
//        int nowSteps=0;
//        if(mIntent!=null && mIntent.hasExtra(WellnessMicroAppMain.KEY_ACTIVTY_TYPE)){
//            int type=mIntent.getIntExtra(WellnessMicroAppMain.KEY_ACTIVTY_TYPE, -1);
//            if(type== ActivityStatusTable.TYPE_WALK){
//                nowSteps=mIntent.getIntExtra(WellnessMicroAppMain.KEY_STEP_COUNTS, 0);
//            }
//        }
//        //smile_gao
//        if(nowSteps<0)
//        {
//            nowSteps = 0;
//        }
//        return nowSteps;
//    }

    public static int getWalkingStepFromIntent(Intent mIntent){
        int nowSteps=0;
        nowSteps=mIntent.getIntExtra(WellnessMicroAppMain.KEY_TOTAL_STEP_COUNT , 0);
        //smile_gao
        if(nowSteps<0)
        {
            nowSteps = 0;
        }
        return nowSteps;
    }

    public static int getBurnedCaloriesFromIntent(Intent mIntent){
        int calories_burned = mIntent.getIntExtra(WellnessMicroAppMain.KEY_CALORIES_BURNED , 0);
        return calories_burned;
    }


    public static int getTodaySteps(){
        long todayStartMilli, todayEndMilli;
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONDAY);
        int day = today.get(Calendar.DATE);



        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        todayStartMilli=calendar.getTimeInMillis();
        todayStartMilli=todayStartMilli/1000*1000;
        calendar.set(year, month, day, 23, 59, 59);
        todayEndMilli=calendar.getTimeInMillis();
       // Log.i("smile","getTodaySteps "+year+" "+month+" "+day +" start: "+todayStartMilli+" end: "+todayEndMilli);
        return getSpeDayStepCont(todayStartMilli,todayEndMilli);

    }
    public static int getSpeDayStepCont(long todayStartMilli,long todayEndMilli)
    {
        int totalSteps=0;
        DaoSession daoSession =  WApplication.getInstance().getDataHelper().getDaoSession();
        Step_countDao step_countDao = daoSession.getStep_countDao();

        List<Step_count> step_counts = step_countDao.queryBuilder().where(Step_countDao.Properties.Start.ge(todayStartMilli), Step_countDao.Properties.End.le(todayEndMilli)).list();

        for(Step_count step_count: step_counts){
            totalSteps += step_count.getStep_count();
        }
//
//        Log.d(Utility.TAG, "getTodaySteps record:" + totalSteps );
//        Activity_statusDao  activity_statusDao= daoSession.getActivity_statusDao();
//        List<Activity_status> activity_statuses = activity_statusDao.queryBuilder().where(Activity_statusDao.Properties.Activity_type.eq(ActivityStatusTable.TYPE_WALK)).list();
//        if(activity_statuses.size() > 0){
//            totalSteps += activity_statuses.get(0).getStep();
//        }

        return totalSteps > 0? totalSteps : 0 ;
    }
    public static List<Step_count> getDaysStepConts(long todayStartMilli,long todayEndMilli)
    {
        int totalSteps=0;
        DaoSession daoSession =  WApplication.getInstance().getDataHelper().getDaoSession();
        Step_countDao step_countDao = daoSession.getStep_countDao();

        List<Step_count> step_counts = step_countDao.queryBuilder().where(Step_countDao.Properties.Start.ge(todayStartMilli), Step_countDao.Properties.End.le(todayEndMilli)).list();

        for(Step_count step_count: step_counts){
            totalSteps += step_count.getStep_count();
        }
        return  step_counts;

    }
//    public static long getWalkingSteps(){
//        DaoSession daoSession =  WApplication.getInstance().getDataHelper().getDaoSession();
//        long totalSteps = 0;
//        Activity_statusDao  activity_statusDao= daoSession.getActivity_statusDao();
//        List<Activity_status> activity_statuses = activity_statusDao.queryBuilder().where(Activity_statusDao.Properties.Activity_type.eq(ActivityStatusTable.TYPE_WALK)).list();
//        if(activity_statuses.size() > 0){
//            totalSteps = activity_statuses.get(0).getStep();
//        }
//
//        return totalSteps;
//    }

    public static void saveWalkingSteps(long step){
        Activity_statusDao activity_statusDao = WApplication.getInstance().getDataHelper().getDaoSession().getActivity_statusDao();
        List<Activity_status> activity_statusList =activity_statusDao.queryBuilder().limit(1).list();
        Activity_status activity_status;
        if(activity_statusList.size() > 0){
            activity_status = activity_statusList.get(0);
        }else{
            activity_status = new Activity_status();
        }
        activity_status.setActivity_type((long) ActivityStatusTable.TYPE_WALK);
        activity_status.setStep(step);
        activity_statusDao.insertOrReplace(activity_status);
    }
    public static   List<Step_count> getSyncStepCountList(Long endTime)
    {
//        List<Profile> profileList = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
//        //Long startTime = 0L;
//        if(profileList!=null && profileList.size()>0)
//        {
//            startTime = profileList.get(0).getStepsynctime();
//            startTime = startTime == null? 0L : startTime;
//        }
       QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getStep_countDao().queryBuilder();
       List<Step_count> list = qb.where(Step_countDao.Properties.End.gt(endTime)).build().list();
//        for (Step_count step: list
//             ) {
//            step.setId(null);
//        }
        return list;
    }

    public static   List<Ecg> getSyncEcgCountList(Long startTime)
    {
//        List<Profile> profileList = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
//        //Long startTime = 0L;
//        if(profileList!=null && profileList.size()>0)
//        {
//            startTime = profileList.get(0).getStepsynctime();
//            startTime = startTime == null? 0L : startTime;
//        }
        QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getEcgDao().queryBuilder();
        List<Ecg> list = qb.where(EcgDao.Properties.Measure_time.gt(startTime)).build().list();
//        for (Ecg ecg: list
//             ) {
//            ecg.setId(null);
//        }
        return list;
    }

    public static CoachTempItem getCoachItemList(Long endTime)
    {
        QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getCoachItemDao().queryBuilder();
        List<CoachItem> list = qb.where(CoachItemDao.Properties.End.gt(endTime)).build().list();
        List<CoachSyncItem> coachSyncItemList = new ArrayList<CoachSyncItem>();
        if(list!=null && list.size()>0)
        {
           Long coachid =  list.get(0).getCoach_id();
            if(coachid>0) {
                qb = WApplication.getInstance().getDataHelper().getDaoSession().getCoachDao().queryBuilder();
                List<Coach> coachlist = qb.where(CoachDao.Properties.Id.ge(coachid)).build().list();
                for(Coach coach : coachlist)
                {

                   List<CoachItem> coachItemList =   coach.getItems();
                    if(coachItemList!=null && coachItemList.size()>0)
                    {
                        for (CoachItem ci : coachItemList)
                        {
                            CoachSyncItem coachSyncItem = new CoachSyncItem(ci.getStart(),ci.getEnd(),ci.getValue(),coach.getDuration() , coach.getType());
                            coachSyncItemList.add(coachSyncItem);
                        }
                    }
                    //coach.setId(null);
                }
                CoachTempItem  cti = new CoachTempItem(coachSyncItemList,coachlist);
                return  cti;
            }
        }
      //  QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getCoachDao().queryBuilder();
      //  List<Step_count> list = qb.where(Step_countDao.Properties.End.gt(endTime)).build().list();

        return null;
    }

    public static   List<Sleep> getSleepList(Long endTime)
    {
        QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getSleepDao().queryBuilder();
        List<Sleep> sleeplist = qb.where(SleepDao.Properties.End.gt(endTime)).build().listLazyUncached();
//        for (Sleep s: sleeplist ) {
//            s.setId(null);
//        }
        return sleeplist;
    }

//    public static   List<SleepSyncItem> getSleepItemList(Long endTime)
//    {
//        QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getSleepItemDao().queryBuilder();
//        List<SleepItem> list = qb.where(SleepItemDao.Properties.Measure_time.gt(endTime)).build().list();
//        List<SleepSyncItem> syncsleepList = new ArrayList<SleepSyncItem>();
//        if(list!=null && list.size()>0)
//        {
//            Long sleepid =  list.get(0).getSleep_id();
//            if(sleepid >0) {
//                qb = WApplication.getInstance().getDataHelper().getDaoSession().getSleepDao().queryBuilder();
//                List<Sleep> sleeplist = qb.where(SleepDao.Properties.Id.ge(sleepid)).build().list();
//                for(Sleep sleep : sleeplist)
//                {
//                    List<SleepItem> sleepItemList =   sleep.getItems();
//                    if(sleepItemList !=null && sleepItemList.size()>0)
//                    {
//                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
//                        String comment = formatter.format(sleep.getEnd());
//                        for (SleepItem ci : sleepItemList)
//                        {
//                            SleepSyncItem syncitem = new SleepSyncItem(ci.getMeasure_time(),ci.getValue(), SyncSleep.SleepType,comment);
//                            syncsleepList.add(syncitem);
//                        }
//                    }
//                }
//            }
//        }
//        //  QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getCoachDao().queryBuilder();
//        //  List<Step_count> list = qb.where(Step_countDao.Properties.End.gt(endTime)).build().list();
//        return syncsleepList;
//    }

    public static   Step_count getLastStepCountItem()
    {
        QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getStep_countDao().queryBuilder();
        List<Step_count> list = qb.orderDesc(Step_countDao.Properties.Start).limit(1).list();
        if(list!=null && list.size()>0)
        {
            return  list.get(0);
        }
        return null;
    }
    public static   CoachItem getLastCoachItem()
    {
        QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getCoachItemDao().queryBuilder();
        List<CoachItem> list = qb.orderDesc(CoachItemDao.Properties.Start).limit(1).list();
        if(list!=null && list.size()>0)
        {
            return  list.get(0);
        }
        return null;
    }
    public static   Ecg getLastEcgItem()
    {
        QueryBuilder qb = WApplication.getInstance().getDataHelper().getDaoSession().getEcgDao().queryBuilder();
        List<Ecg> list = qb.orderDesc(EcgDao.Properties.Measure_time).limit(1).list();
        if(list!=null && list.size()>0)
        {
            return  list.get(0);
        }
        return null;
    }
    public static Long getLastSyncTime(Device d)
    {
        Long lastSyncTime = d.getStepsynctime();
        if(lastSyncTime == null)  // || p.getStepsynctime() == null || d.getStepsynctime()
        {
            lastSyncTime = 0L;
        }
        return  lastSyncTime;
    }
    public static Long getLastStepSyncTime(Device d)
    {
        Long lastSyncTime = d.getStepsynctime();
        if(lastSyncTime == null)  // || p.getStepsynctime() == null || d.getStepsynctime()
        {
            lastSyncTime = 0L;
        }
        Step_count lastStetcountItem = StepHelper.getLastStepCountItem();
        if(lastStetcountItem == null || lastSyncTime>= lastStetcountItem.getEnd() ) //for sync ,start to end
        {
            if(lastStetcountItem !=null)
            {
                Log.i("DateEventHelper","lastStetcountItem null or synctime >= last time "+lastSyncTime+"  "+lastStetcountItem.getEnd());
            }
            else {
                Log.i("DateEventHelper", "lastStetcountItem null or synctime >= last time " + lastSyncTime + "   ");
            }
            return -1L;
        }
        else  if(lastStetcountItem !=null)
        {
            Log.i("DateEventHelper","lastStetcountItem null or synctime >= last time "+lastSyncTime+"  "+lastStetcountItem.getEnd());
        }

        return lastSyncTime;
    }
    public static Long getLastCoachSyncTime(Device d)
    {
        Long lastSyncTime = d.getStepsynctime();
        if(lastSyncTime == null)  // || p.getStepsynctime() == null || d.getStepsynctime()
        {
            lastSyncTime = 0L;
        }
        CoachItem lastStetcountItem = StepHelper.getLastCoachItem();
        if(lastStetcountItem == null || lastSyncTime>= lastStetcountItem.getEnd() ) //for sync ,start to end
        {
            if(lastStetcountItem !=null)
            {
                Log.i("DateEventHelper","getLastCoachSyncTime null or synctime >= last time "+lastSyncTime+"  "+lastStetcountItem.getEnd());
            }
            else {
                Log.i("DateEventHelper", "getLastCoachSyncTime null or synctime >= last time " + lastSyncTime + "   ");
            }
            return -1L;
        }
        else  if(lastStetcountItem !=null)
        {
            Log.i("DateEventHelper","getLastCoachSyncTime null or synctime >= last time "+lastSyncTime+"  "+lastStetcountItem.getEnd());
        }

        return lastSyncTime;
    }
    public static Long getLastEcgSyncTime(Device d)
    {
        Long lastSyncTime = d.getStepsynctime();
        if(lastSyncTime == null)  // || p.getStepsynctime() == null || d.getStepsynctime()
        {
            lastSyncTime = 0L;
        }
        Ecg lastStetcountItem = StepHelper.getLastEcgItem();
        if(lastStetcountItem == null || lastSyncTime>= lastStetcountItem.getMeasure_time())
        {
            if(lastStetcountItem !=null)
            {
                Log.i("DateEventHelper","getLastEcgSyncTime null or synctime >= last time "+lastSyncTime+"  "+lastStetcountItem.getMeasure_time());
            }
            else {
                Log.i("DateEventHelper", "getLastEcgSyncTime null or synctime >= last time " + lastSyncTime + "  ");
            }
            return -1L;
        }
        else  if(lastStetcountItem !=null)
        {
            Log.i("DateEventHelper","getLastEcgSyncTime null or synctime >= last time "+lastSyncTime+"  "+lastStetcountItem.getMeasure_time());
        }
        return lastSyncTime;
    }
}

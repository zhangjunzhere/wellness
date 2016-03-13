package com.asus.wellness.cm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asus.sharedata.SyncIdleAlarm;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Activity_state;
import com.asus.wellness.dbhelper.DaoSession;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.dbhelper.DeviceDao;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.dbhelper.Location_change;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.ProfileDao;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.ui.setting.SettingActivity;
import com.asus.wellness.utils.LocationHelper;
import com.asus.wellness.utils.Utility;
import com.cmcm.common.statistics.CMAgent;
import com.cmcm.common.statistics.ReportLevel;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by waylen_wang on 2015/8/25.
 */
public class CmHelper {
    public final static String MEASURE_MSG_ID="measure_msg";
    public final static String ACTIVITY_MSG_ID="activity_msg";
    public final static String PROFILE_MSG_ID="profile_msg";
    public final static String APPSETTING_MSG_ID="appsetting_msg";
    public final static String PAGEACTION_MSG_ID="weeklypage_msg";
    private final static String HISTORY_DATA_UPLOADED="history_data_uploaded";
    public final static ReportLevel UPLOAD_ALL=ReportLevel.ALL;
    public final static ReportLevel UPLOAD_ACTIVE=ReportLevel.ACTIVE;
    public final static ReportLevel UPLOAD_NONE= ReportLevel.NONE;

    public static String getDeviceVersion(){
        return Build.VERSION.RELEASE;
    }

    public static void uploadSettingData(String backup,String tracking,String inspire,String encourage,boolean isOld){
        HashMap<String, String> data = new HashMap<>();
        if(isOld)
            data.put("uptype","2");
        else
            data.put("uptype","1");
        if(backup!=null)
            data.put("backup",backup);
        if(tracking!=null)
            data.put("activity_tracking",tracking);
        if(inspire!=null)
            data.put("inspire",inspire);
        if(encourage!=null)
            data.put("encourage_us",encourage);
        CMAgent.onEvent(APPSETTING_MSG_ID,data);
    }

    public static boolean isLocationEnable(Context context){
        boolean locationServiceEnabled = SettingActivity.checkLocationServiceEnable(context);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enable = sp.getBoolean(context.getString(R.string.pref_key_location), context.getResources().getBoolean(R.bool.default_location));
        return  locationServiceEnabled&&enable;
    }

    public static String[] heightWeightInfo(Profile profile){
        String[] info=new String[2];
        int heightInCM=profile.getHeight();
        if(profile.getHeight_unit()== ProfileTable.HEIGHT_UNIT_FT){
            float ft= Utility.InchToFt(heightInCM);
            heightInCM=(int) Math.round(Utility.ftToCm(ft));
        }
        info[0]=String.valueOf(heightInCM);
        int weightInKG=profile.getWeight();
        if(profile.getWeight_unit()== ProfileTable.WEIGHT_UNIT_LBS){
            weightInKG=(int) Math.round(Utility.LbsToKg(profile.getWeight()));
        }
        info[1]=String.valueOf(weightInKG);
        return info;
    }

    public static String[] getRemindTime(SharedPreferences sp,Context context){
        String[] info=new String[3];
        int fromH=sp.getInt(Utility.HOUR_OF_DAY_FROM, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_FROM);
        int fromM=sp.getInt(Utility.MINUTE_FROM, SyncIdleAlarm.DEFAULT_MINUTE_FROM);
        info[0]=String.valueOf(fromH*60+fromM);
        int toH=sp.getInt(Utility.HOUR_OF_DAY_TO, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_TO);
        int toM=sp.getInt(Utility.MINUTE_TO, SyncIdleAlarm.DEFAULT_MINUTE_TO);
        info[1]=String.valueOf(toH*60+toM);
        int entryId =sp.getInt(context.getString(R.string.pref_key_idle_alarm_duration_summary), 2);
        entryId=entryId+1;
        info[2]=String.valueOf(entryId);
        return info;
    }

    public static void uploadHistoryData(final Context context){
        final SharedPreferences mPres=PreferenceManager.getDefaultSharedPreferences(context);
        if(mPres.getBoolean(HISTORY_DATA_UPLOADED,false)){
            //History data upload only once
            return;
        }
        //ecg_msg
        DaoSession daoSession= WApplication.getInstance().getDataHelper().getDaoSession();
        EcgDao ecgDao = daoSession.getEcgDao();
        final List<Ecg> ecgList =ecgDao.loadAll();
        final List<Activity_state> activity_states = daoSession.getActivity_stateDao().loadAll();
        ProfileDao profileDao = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
        final List<Profile> profileList = profileDao.loadAll();

        AsyncTask task=new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    upload(context, mPres, ecgList, activity_states, profileList);
                }catch (Exception e)
                {
                    Log.i("smile","doInBackground: "+e.getMessage());
                }
                return null;
            }
        };
        task.execute();
    }

    private static void upload(Context context,SharedPreferences mPres,List<Ecg> ecgList, List<Activity_state> activity_states, List<Profile> profileList){
        HashMap<String,String > msg=new HashMap<>();
        if(ecgList!=null&&ecgList.size()!=0){
            for(Ecg ecg:ecgList){
                msg.clear();
                msg.put("uptype", "2");
                msg.put("measure_time", String.valueOf(ecg.getMeasure_time()));
                msg.put("measure_value", String.valueOf(ecg.getMeasure_value()));
                msg.put("measure_type", String.valueOf(ecg.getMeasure_type()));
//                if(ecg.getMeasure_comment()==null){
//                    msg.put("measure_comment", "1");
//                }else
//                    msg.put("measure_comment", String .valueOf(Integer.parseInt(ecg.getMeasure_comment())+1));
                if(ecg.getDevice()!=null)
                {
                    if(ecg.getDevice().getBlueaddr()!=null)
                         CMAgent.onDeviceEvent(ecg.getDevice().getBlueaddr(), CmHelper.getDeviceVersion(), CmHelper.MEASURE_MSG_ID, msg);
                }
                else
                {
                    if(ecg!=null && ecg.getDeviceId()!=null)
                         CMAgent.onDeviceEvent(String.valueOf(ecg.getDeviceId()), CmHelper.getDeviceVersion(), CmHelper.MEASURE_MSG_ID, msg);
                }
            }
        }

        //activity_msg
        if(activity_states!=null&&activity_states.size()!=0){
            for(Activity_state activity_state:activity_states){
                msg.clear();
                msg.put("uptype", "2");
                msg.put("activity_starttime", String.valueOf(activity_state.getStart()));
                msg.put("activity_endtime", String.valueOf(activity_state.getEnd()));
                msg.put("step_count", String.valueOf(activity_state.getStep_count()));
                ParseDataManager parseDataManager= ParseDataManager.getInstance();
                Profile profile=parseDataManager.getStandardProfile();
                String[] info= CmHelper.heightWeightInfo(profile);
                String kcal =String.valueOf(Utility.getWalkCalories(Integer.parseInt(info[0]), activity_state.getStep_count().intValue(), Integer.parseInt(info[1])));
                msg.put("kcal_count",kcal);
                Location_change location_change= LocationHelper.getInstance(context).getLocationChangeByTime(activity_state.getStart());
                if(location_change!=null) {
                    msg.put("get_location_time", String.valueOf(location_change.getGet_location_time()));
                    msg.put("latitude", String.valueOf(location_change.getLatitude() * 1000));
                    msg.put("longitude", String.valueOf(location_change.getLongitude() * 1000));
                    msg.put("district", String.valueOf(location_change.getLongitude()));
                }
                if(activity_state.getDevice()!=null && activity_state.getDevice().getBlueaddr() !=null) {
                    CMAgent.onDeviceEvent(activity_state.getDevice().getBlueaddr(), CmHelper.getDeviceVersion(), CmHelper.ACTIVITY_MSG_ID, msg);
                }else {
                    CMAgent.onDeviceEvent(String.valueOf(activity_state.getDeviceId()), CmHelper.getDeviceVersion(), CmHelper.ACTIVITY_MSG_ID, msg);
                }
            }
        }

        //profile_msg
        Profile profile;
        HashMap<String, String> data = new HashMap<>();
        if (profileList.size() > 0) {
            profile = profileList.get(0);
            data.put("uptype", "2");
            //data.put("profile_name", profile.getName());
            if(profile.getPhoto_path()!=null)
                data.put("profile_photo", "1");
            else
                data.put("profile_photo", "0");
            data.put("age", String.valueOf(profile.getAge()));
            data.put("gender",String.valueOf(profile.getGender()+1));
            data.put("start_time", String.valueOf(profile.getStart_time()));
            String[] info= CmHelper.heightWeightInfo(profile);
            data.put("height",info[0]);
            data.put("weight", info[1]);
            CMAgent.onEvent(CmHelper.PROFILE_MSG_ID, data);
        }
        boolean track=mPres.getBoolean("pref_key_location", false);
        boolean inspire=mPres.getBoolean("pref_inspire_asus", true);
        boolean backup=mPres.getBoolean("pref_key_backup_restore", true);
        String isTrack=track?"1":"2";
        String isBackup=backup?"1":"2";
        String isInspire=inspire?"1":"2";
        uploadSettingData(isBackup, isTrack, isInspire, null, true);
        mPres.edit().putBoolean(HISTORY_DATA_UPLOADED,true).commit();
    }

    private static String buildBlueaddrFromName(String displayName){
        String blueAddr=null;
        if(displayName.length()>=4)
            blueAddr=displayName.substring(displayName.length()-4,displayName.length());
        if (blueAddr!=null) {
            StringBuilder builder = new StringBuilder(blueAddr);
            blueAddr = builder.insert(2, ":").toString();
        }else
            blueAddr="";
        return blueAddr;
    }
    public static String findFromDevice(String displayName){
        if(displayName==null){
            return WApplication.getInstance().getConnectedDevice().getBlueaddr();
        }
        DaoSession daoSession= WApplication.getInstance().getDataHelper().getDaoSession();
        DeviceDao deviceDao=daoSession.getDeviceDao();
        List<Device> devices=deviceDao.queryBuilder().where(DeviceDao.Properties.Blueaddr.like("%"+buildBlueaddrFromName(displayName))).list();
        if(devices!=null&&devices.size()!=0){
            return devices.get(0).getBlueaddr();
        }else {
            return WApplication.getInstance().getConnectedDevice().getBlueaddr();
        }
    }

}

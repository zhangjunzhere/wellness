package com.asus.wellness.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.asus.sharedata.ShareUtils;
import com.asus.sharedata.SyncProfile;
import com.asus.wellness.ListenerService;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.ProfileDao;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;

import java.util.List;

/**
 * Created by smile_gao on 2015/7/27.
 */
public class ProfileHelper {

    public static final int HEIGHT_UNIT_CM=0;
    public static final int HEIGHT_UNIT_FT=1;
    public static final int WEIGHT_UNIT_KG=0;
    public static final int WEIGHT_UNIT_LBS=1;

    public static  void updateProfile(Context context,DataMap data,DataLayerManager dataLayerManager)
    {
       ProfileDao profileDao =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
       Profile profile = getStandardProfile();
      //  context.getContentResolver().delete(ProfileTable.TABLE_URI, null, null);
       // DataMap data= DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
       String name = data.getString(SyncProfile.KEY_PROFILE_NAME);
       String photouri = data.getString(SyncProfile.KEY_PROFILE_PHOTO_URL);
       Integer age = data.getInt(SyncProfile.KEY_PROFILE_AGE);
        if(age == null)
        {
            age = ProfileTable.DEFAULT_AGE;
        }
       Integer gender = data.getInt(SyncProfile.KEY_PROFILE_GENDER);
       Integer height =  data.getInt(SyncProfile.KEY_PROFILE_HEIGHT);
        if(height == null)
        {
            height = ProfileTable.DEFAULT_HEIGHT;
        }
       Integer heightunit = data.getInt(SyncProfile.KEY_PROFILE_HEIGHT_UNIT);
        if(heightunit == null)
        {
            heightunit = ProfileTable.HEIGHT_UNIT_CM;
        }
       Integer weight =  data.getInt(SyncProfile.KEY_PROFILE_WEIGHT);
        if(weight == null)
        {
            weight = ProfileTable.WEIGHT_UNIT_KG;
        }
       Integer weightunit = data.getInt(SyncProfile.KEY_PROFILE_WEIGHT_UNIT);
        if(weightunit  == null)
        {
            weightunit = ProfileTable.WEIGHT_UNIT_KG;
        }
       Long starttime = data.getLong(SyncProfile.KEY_PROFILE_START_TIME);
       Integer stepgoal =  data.getInt(SyncProfile.KEY_PROFILE_STEP_GOAL, Utility.TARGET_GOAL);
       Integer nextstepgoal = -1;
       profile.setName(name);
        Log.i("smile","photo path "+photouri);
        if(photouri!=null && !profile.getPhoto_path().equals(photouri))
        {
            Log.i("smile","insert photo data "+photouri);
             Asset asset = data.getAsset(SyncProfile.KEY_PROFILE_PHOTO);
             Bitmap bmp =  dataLayerManager.getPhoto(asset);
            if(bmp!=null)
            {
                byte[] d = ShareUtils.toByteArray(bmp);
                profile.setPhotodata(d);
            }
        }
        profile.setPhoto_path(photouri);
        profile.setAge(age);
        profile.setGender(gender);
        profile.setHeight(height);
        profile.setHeight_unit(heightunit);
        profile.setWeight_unit(weightunit);
        profile.setWeight(weight);
        profile.setStart_time(starttime);
        profile.setStep_goal(stepgoal);
        profile.setNext_step_goal(nextstepgoal);
        profile.setIsprofileset(true);
       // Profile profile=new Profile(null,name,photouri,age,gender,height,heightunit,weight,weightunit,starttime,stepgoal,nextstepgoal,1,true,null,null);
        profileDao.insertOrReplace(profile);
        context.getContentResolver().notifyChange(ProfileTable.TABLE_URI,null);
       // context.getContentResolver().insert(ProfileTable.TABLE_URI, cv);
    }
    public static  void updateLastSyncStepTime(long startTime)
    {
        ProfileDao profileDao =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
        List<Profile> list = profileDao.loadAll();
        if(list!=null && list.size() >0 )
        {
            Profile p = list.get(0);
            p.setStepsynctime(startTime);
            profileDao.update(p);
        }
    }
    public static  void updateLastSyncEcgTime(long startTime)
    {
        ProfileDao profileDao =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
        List<Profile> list = profileDao.loadAll();
        if(list!=null && list.size() >0 )
        {
            Profile p = list.get(0);
            p.setEcgsynctime(startTime);
            profileDao.update(p);
        }
    }
    public static  void updatePhotoData(Context context,byte[] photodata,String photoUrl)
    {
        if(photoUrl==null)
        {
            Log.i("smile","updatePhotoData photoUrl= null");
            return;
        }
        ProfileDao profileDao =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
        Profile p = getStandardProfile();
        p.setPhoto_path(photoUrl);
        p.setPhotodata(photodata);
        profileDao.insertOrReplace(p);
        context.getContentResolver().notifyChange(ProfileTable.TABLE_URI, null);
    }
    public static long getWalkCalories(long heightInCM, long weightInKG,long stepCounts) {
        float distance = (float) getWalkDistanceInCM(heightInCM, stepCounts) / 100 / 1000;
        long calories_burned = (long) (0.76f * (float) weightInKG * (float) distance);

        return calories_burned > 0 ? calories_burned : 0;
    }

    public static long getWalkDistanceInCM(long heightInCM, long stepCounts) {
        return getWalkStepLength(heightInCM) * stepCounts;
    }
    public static long getWalkStepLength(long heightInCM) {
        return Math.max(50,heightInCM - 100L);
    }

    public static float InchToFt(float inch) {
        return inch / 12;
    }

    public static float ftToCm(float ft) {
        return (30.48f * ft);
    }

    public static float LbsToKg(float lbs) {
        return (lbs * 0.45359237f);
    }

    public static Profile getStandardProfile(){
        Profile profile;
        List<Profile> profiles = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
        if(profiles.size() > 0){
            profile = profiles.get(0);
        }else{
            profile = new Profile();
            final int defHeight = 170; //170cm
            final int defWeight = 70; //60kg
            profile.setHeight(defHeight);
            profile.setHeight_unit(HEIGHT_UNIT_CM);
            profile.setWeight(defWeight);
            profile.setWeight_unit(WEIGHT_UNIT_KG);
            profile.setDistance_unit(ProfileTable.DISTANTCE_UNIT_KM);
        }

       /* if(profile.getHeight_unit()==HEIGHT_UNIT_FT){
            float ft=Utils.InchToFt(profile.getHeight());
            int heightInCM=(int) Math.round(Utils.ftToCm(ft));
            profile.setHeight(heightInCM);
        }
        if(profile.getWeight_unit() ==WEIGHT_UNIT_LBS){
            int weightInKG=(int) Math.round(Utils.LbsToKg(profile.getWeight()));
            profile.setWeight(weightInKG);
        }*/

        return profile;
    }

    public static long getWalkCalories(long total_step) {
        Profile profile = ProfileHelper.getStandardProfile();
        int heightInCM = profile.getHeight();
        if (profile.getHeight_unit() == ProfileHelper.HEIGHT_UNIT_FT) {
            float ft = ProfileHelper.InchToFt(heightInCM);
            heightInCM = (int) Math.round(ProfileHelper.ftToCm(ft));
        }
        int weightInKG = profile.getWeight();
        if (profile.getWeight_unit() == ProfileHelper.WEIGHT_UNIT_LBS) {
            weightInKG = (int) Math.round(ProfileHelper.LbsToKg(profile.getWeight()));
        }
        // Log.i("emily","updateSteps,height = "+profile.getHeight()+", weight = "+profile.getWeight());
        long calories = ProfileHelper.getWalkCalories(heightInCM, weightInKG, total_step);
        return calories;
    }
}

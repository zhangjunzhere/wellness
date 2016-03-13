package com.asus.wellness.Profile.model;

import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.ProfileDao;
import com.asus.wellness.utils.Utility;

import java.util.List;

/**
 * Created by smile_gao on 2015/5/14.
 */
public class ProfileModel {
    Profile mProfile =null;
    public ProfileModel()
    {
        init();
    }

    /**
     * get profile dao
     * @return
     */
    public ProfileDao getProfileDao()
    {
        return WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
    }

    /**
     * get profile , if not exist ,insert one with default value
     * male, age 25, height unit cm, height 160, weight unit kg, weight 50, goal 5000, distance unit km, is profile set false
     */
    private  void init()
    {
        List<Profile> list = getProfileDao().loadAll();
        if(list ==null || list.size() ==0)
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
            p.setNext_step_goal(-1);
            p.setDistance_unit(ProfileTable.DISTANTCE_UNIT_KM);
            p.setIsprofileset(false);
          //  Profile p = new Profile(null,"","",30,ProfileTable.MALE, 170,ProfileTable.HEIGHT_UNIT_CM,60,ProfileTable.WEIGHT_UNIT_KG,System.currentTimeMillis(),7000,-1,ProfileTable.DISTANTCE_UNIT_KM,false,null,null,null);
            Long  id= getProfileDao().insert(p);
            if(id!=null)
            {
                mProfile =  getProfileDao().load(id);
                return;
            }
        }
        if(list.size()>0)
        {
            mProfile = list.get(0);
            if(mProfile.getIsprofileset()==null || mProfile.getDistance_unit()==null)
            {
                mProfile.setIsprofileset(false);
                mProfile.setDistance_unit(ProfileTable.DISTANTCE_UNIT_KM);
                getProfileDao().update(mProfile);
            }

        }

    }

    /**
     * get gender
     * @return "male" or "female"
     */
    public  int getGender()
    {
        return  mProfile.getGender()==null? ProfileTable.MALE : mProfile.getGender();
    }

    /**
     * set gender
     * @param gender "male" or "female"
     */
    public void setGender(int gender)
    {
        mProfile.setGender(gender);
    }

    /**
     * get age
     * @return int age
     */
    public int getAge()
    {
        return  mProfile.getAge()== null? ProfileTable.DEFAULT_AGE: mProfile.getAge();
    }

    /**
     * set age
     * @param age
     */
    public void setAge(int age)
    {
        mProfile.setAge(age);
    }


    /**
     * get height unit
     * @return  CM or FT/IN
     */
    public  int getHeightUnit()
    {
        return  mProfile.getHeight_unit() == null? ProfileTable.HEIGHT_UNIT_CM: mProfile.getHeight_unit();
    }

    /**
     * @param heightunit  CM or FT/IN
     */
    public void setHeightunit(int heightunit)
    {
        mProfile.setHeight_unit(heightunit);
    }

    /**
     * get height
     * @return  float in cm
     */
    public int  getHeight()
    {
        return  mProfile.getHeight();
    }

    /**
     * set height
     * @param height  float with cm
     */
    public void setHeight(int height)
    {
        mProfile.setHeight(height);
    }

    /**
     * get weight unit
     * @return KG or Lbs
     */
    public int getWeightUnit()
    {
        return  mProfile.getWeight_unit()==null?  ProfileTable.WEIGHT_UNIT_KG: mProfile.getWeight_unit();
    }

    /**
     * @param weightunit KG or LBs
     */
    public void setWeightunit(int weightunit)
    {
        mProfile.setWeight_unit(weightunit);
    }


    /**
     * get weight
     * @return KG or LBs
     */
    public Integer getWeight()
    {
        return  mProfile.getWeight();
    }

    /**
     * set weight
     * @param weight  KG or LBs
     */
    public void setWeight(int weight)
    {
        mProfile.setWeight(weight);
    }

    /**
     * get activity goal
     * @return  0-5 (5k,7.5k,10k,12k, more than 12k)
     */
    public int getStepgoal()
    {
        if(mProfile.getStep_goal()==null)
        {
            mProfile.setStep_goal(Utility.TARGET_GOAL);
        }
        return  mProfile.getStep_goal();
    }
    /**
     * get activity goal
     * @return  0-5 (5k,7.5k,10k,12k, more than 12k)
     */
    public int getStepgoalTargetIndex()
    {
        int goal =  mProfile.getStep_goal();
        int index =0;
        if( goal<=5000)
        {
            index =0;
        }else  if( goal<=7500)
        {
            index =1;
        }else  if( goal<=10000)
        {
            index =2;
        }else  if( goal<=12500)
        {
            index =3;
        }
        else
        {
            index =4;
        }
        return  index;
    }
    /**
     * set goal
     * @param goal 0-5
     */
    public void setStepgoal(int goal)
    {
        mProfile.setStep_goal(goal);
    }


    /**
     * get distance unit
     * @return  KM or Miles
     */
    public int getDistanceUnit()
    {
        return  mProfile.getDistance_unit()==null ? ProfileTable.DISTANTCE_UNIT_KM : mProfile.getDistance_unit() ;
    }

    /**
     * @param distanceUnit  KM or Miles
     */
    public void setDistanceUnit(int distanceUnit)
    {
        mProfile.setDistance_unit(distanceUnit);
    }

    /**
     * is profile set
     * @return  boolean
     */
    public Boolean getIsProfileSet()
    {
        return  mProfile.getIsprofileset() == null ? false : mProfile.getIsprofileset();
    }

    /**
     * @param isProfileSet boolean
     */
    public void setIsProfileSet(Boolean isProfileSet)
    {
        mProfile.setIsprofileset(isProfileSet);
    }

    /**
     * update profile db
     */
    public void update()
    {
       getProfileDao().update(mProfile);
    }
    public void reset()
    {
        getProfileDao().refresh(mProfile);
    }





}

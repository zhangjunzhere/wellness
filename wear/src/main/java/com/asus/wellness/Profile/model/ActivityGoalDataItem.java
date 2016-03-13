package com.asus.wellness.Profile.model;

/**
 * Created by smile_gao on 2015/9/6.
 */
public class ActivityGoalDataItem {
    private  String mGoal;
    private  String mDes;
    public ActivityGoalDataItem(String key,String des)
    {
       mGoal = key;
        mDes = des;
    }
    public String getGoal()
    {
        return mGoal;
    }
    public  String getDes()
    {
        return  mDes;
    }
}

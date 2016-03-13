package com.asus.wellness.ga;

/**
 * Created by larrylf_lin on 2015/12/3.
 */
public class GACategory {

    //Category
    public static  final  String CategoryProfile = "Profile";
    public static  final  String CategorySleep = "AllSleep";
    public static final String CategoryWorkoutCoach = "WorkoutCoach";
    public static final String CategoryUserSteps = "AllSteps";

    //Action
    public static final String ActionSleepTracker= "UserSleepTracker";
    public static final String ActionAVGSleepTime= "UserAVGSleepTime";
    public static final String ActionRun = "UserRun";
    public static final String ActionPushUp = "UserPushUp";
    public static final String ActionSitUp = "UserSitUp";
    public static  final  String ActionAvgStep = "UserAvgStep";
    public  static  final  String ActionMale = "Male";
    public static  final  String ActionFemale = "Female";

    //Label
    public  static  final  String LabelGender = "Gender";
    public  static  final  String LabelSleep = "Sleep";
    public  static  final  String LabelRun = "Run";
    public  static  final  String LabelPushUp = "PushUp";
    public  static  final  String LabelSitup = "SitUp";
    public  static  final  String LabelSteps = "Steps";

    private static   int mSelectedGender = 0;  //0: Male 1: Female
    public static final String mTrackKey1 = "&cd1";
    public static final String mTrackKey2 = "&cd2";

    public  static  void  setSelectGender(int selectedGender){
        mSelectedGender = selectedGender;
    }

    public  static  int getSelectGender(){
        return  mSelectedGender;
    }

    public  static String getActionSleepTracker(){

        String result = mSelectedGender == 0? ActionSleepTracker+ActionMale: ActionSleepTracker + ActionFemale;
        return  result;
    }

    public  static String getAVGSleepTime(){

        String result = mSelectedGender == 0? ActionAVGSleepTime+ActionMale: ActionAVGSleepTime + ActionFemale;
        return  result;
    }

    public  static String getActionRun(){

        String result = mSelectedGender == 0? ActionRun+ActionMale: ActionRun + ActionFemale;
        return  result;
    }

    public  static String getActionPushUp(){

        String result = mSelectedGender == 0? ActionPushUp+ActionMale: ActionPushUp + ActionFemale;
        return  result;
    }

    public  static String getActionSitUp(){

        String result = mSelectedGender == 0? ActionSitUp+ActionMale: ActionSitUp + ActionFemale;
        return  result;
    }

    public  static String getActionAvgStep(){

        String result = mSelectedGender == 0? ActionAvgStep +ActionMale: ActionAvgStep + ActionFemale;
        return  result;
    }

    public  static String getActionSex(){

        String result = mSelectedGender == 0? ActionMale: ActionFemale;
        return  result;
    }
}

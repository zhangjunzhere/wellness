package com.asus.wellness.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.asus.wellness.ui.MainWellness;

/**
 * Created by smile_gao on 2015/7/2.
 */
public class MySharePreferences {
    private static SharedPreferences mSharedPreference=null;
 //   private static MySharePreferences mPreference=null;
 //   public static String mDefaultFileNameKey="defaultFileNameKey";
 //   final public static  String FILENAME_INDEX="file_index";
     public static String IsRobin="watchisrobin";

    public static void init(Context context)
    {
        if(mSharedPreference==null)
            //preference=PreferenceManager.getDefaultSharedPreferences(context);
            mSharedPreference=context.getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
    }
    public static void setValue(String key,String value)
    {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static void setValue(String key,int value)
    {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public static void setValue(String key,Boolean value)
    {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static String getValue(String key,String defval)
    {
        return mSharedPreference.getString(key, defval);
    }
    public static int getValue(String key,int defval)
    {
        return mSharedPreference.getInt(key, defval);
    }
    public static Boolean getValue(String key,Boolean defval)
    {
        return mSharedPreference.getBoolean(key, defval);
    }


}

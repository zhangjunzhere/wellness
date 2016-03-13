package com.asus.wellness.ga;

import android.util.Log;

import java.lang.reflect.Method;

  
public class AsusSystemProperties {  
    private static final String TAG = "AsusSystemProperties";
  
    // String SystemProperties.get(String key){}  
    public static String get(String key) {  
        init();  
          
        String value = null;  
          
        try {  
            value = (String) mGetMethod.invoke(mClassType, key);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
          
        return value;  
    }

    public static String get(String key,String defaultValue) {
        init();

        String value = null;

        try {
            value = (String) mGetMethod.invoke(mClassType, key,defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    //int SystemProperties.get(String key, int def){}  
    public static int getInt(String key, int def) {  
        init();  
          
        int value = def;  
        try {  
            Integer v = (Integer) mGetIntMethod.invoke(mClassType, key, def);  
            value = v.intValue();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return value;  
    }

    public static long getLong(final String key, final long def) {
        init();
        try {
            return ((Long) mGetLongMethod.invoke(null, key, def)).longValue();
        } catch (Exception e) {
            Log.e(TAG, "Platform error: " + e.toString());
            return def;
        }
    }

    public static boolean getBoolean(final String key, final boolean def) {
        init();
        try {
            return (Boolean)mGetBooleanMethod.invoke(null, key, def);
        } catch (Exception e) {
            Log.e(TAG, "Platform error: " + e.toString());
            return def;
        }
    }


    public static int getSdkVersion() {  
        return getInt("ro.build.version.sdk", -1);  
    }  
      
    //-------------------------------------------------------------------  
    private static Class<?> mClassType = null;  
    private static Method mGetMethod = null;  
    private static Method mGetIntMethod = null;
    private static Method mGetLongMethod = null;
    private static Method mGetBooleanMethod = null;

    private static void init() {  
        try {  
            if (mClassType == null) {  
                mClassType = Class.forName("android.os.SystemProperties");  
                  
                mGetMethod = mClassType.getDeclaredMethod("get", String.class);  
                mGetIntMethod = mClassType.getDeclaredMethod("getInt", String.class, int.class);
                mGetBooleanMethod = mClassType.getMethod("getBoolean", String.class, boolean.class);
                mGetLongMethod = mClassType.getMethod("getLong",String.class,long.class);
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}  



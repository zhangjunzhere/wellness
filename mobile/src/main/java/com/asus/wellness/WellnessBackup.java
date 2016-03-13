package com.asus.wellness;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.app.backup.BackupManager;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asus.wellness.dbhelper.DaoMaster;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.provider.WellnessSQLiteOpenHelper;
import com.asus.wellness.ui.MainWellness;

public class WellnessBackup extends BackupAgentHelper {
    private static final String TAG = "WellnessBackup";
    
    private static final String KEY_WELLNESS_PREFERENCE = "key_wellness_preference";
    private static final String KEY_WELLNESS_DATABASE = "key_wellness_database";

    private static final String KEY_LAST_TIME_DATA_CHANGED_CALLED = "key_last_time_data_changed_called";
    private static final long UPDATE_THRESHOLD = 24 * 60 * 60 * 1000; // 24HR


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        BackupHelper helper = new SharedPreferencesBackupHelper(this, getPackageName()+"_preferences");
        addHelper(KEY_WELLNESS_PREFERENCE, helper);
        helper = new DatabaseBackupHelper(this);
        addHelper(KEY_WELLNESS_DATABASE, helper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) throws IOException {
        Log.d(TAG,"onBackup");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.getBoolean(getString(R.string.pref_key_backup_restore), getResources().getBoolean(R.bool.default_backup_restore))){
            Log.d(TAG,"onBackup skip, pref_key_backup_restore is false");
            return;
        }
        synchronized (MainWellness.sLockObject) {
            super.onBackup(oldState, data, newState);
        }
        Log.d(TAG,"onBackup finished");
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
            ParcelFileDescriptor newState) throws IOException {
        Log.d(TAG,"onRestore appVersionCode="+appVersionCode+", "+newState);
        
        synchronized (MainWellness.sLockObject) {
            super.onRestore(data, appVersionCode, newState);
        }
        
      //  WellnessSQLiteOpenHelper helper = new WellnessSQLiteOpenHelper(this,DatabaseBackupHelper.BACKUP_DATABASE_NAME);
//        DataHelper dh =  DataHelper.getInstance(this);
//        SQLiteDatabase backupDB = dh.getDaoSession().getDatabase(); //wellness.getWritableDatabase();
//     //   SQLiteDatabase backupDB = helper.getWritableDatabase();
//        WellnessSQLiteOpenHelper.onUpgradeDatabase(backupDB, backupDB.getVersion(), WellnessSQLiteOpenHelper.DATABASE_VERSION);
//        backupDB.close();
       // helper.close();
    }
    
    
    public static void dataChanged(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sp.getBoolean(context.getString(R.string.pref_key_backup_restore), context.getResources().getBoolean(R.bool.default_backup_restore))){
            Log.d(TAG,"dataChanged skip, pref_key_backup_restore is false");
            return;
        }
        
        long time = sp.getLong(KEY_LAST_TIME_DATA_CHANGED_CALLED, 0);
        long now = System.currentTimeMillis();
        if (now - time > UPDATE_THRESHOLD){
            sp.edit().putLong(KEY_LAST_TIME_DATA_CHANGED_CALLED, now).commit();
            BackupManager bm = new BackupManager(context);
            bm.dataChanged();
            Log.d(TAG,"dataChanged called");
        }
    }

}

package com.asus.wellness.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;

import com.asus.sharedata.ShareUtils;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.ProfileDao;
import com.asus.wellness.dbhelper.SystemInfoDao;
import com.asus.wellness.dbhelper.Activity_statusDao;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.dbhelper.Step_countDao;
import com.asus.wellness.dbhelper.CoachDao;
import com.asus.wellness.dbhelper.CoachItemDao;
import com.asus.wellness.dbhelper.SleepDao;
import com.asus.wellness.utils.Utility;

import java.util.List;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * Master of DAO (schema version 1113): knows all DAOs.
*/
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 1216;

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(SQLiteDatabase db, boolean ifNotExists) {
        ProfileDao.createTable(db, ifNotExists);
        SystemInfoDao.createTable(db, ifNotExists);
        Activity_statusDao.createTable(db, ifNotExists);
        EcgDao.createTable(db, ifNotExists);
        Step_countDao.createTable(db, ifNotExists);
        CoachDao.createTable(db, ifNotExists);
        CoachItemDao.createTable(db, ifNotExists);
        SleepDao.createTable(db, ifNotExists);
    }
    
    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(SQLiteDatabase db, boolean ifExists) {
        ProfileDao.dropTable(db, ifExists);
        SystemInfoDao.dropTable(db, ifExists);
        Activity_statusDao.dropTable(db, ifExists);
        EcgDao.dropTable(db, ifExists);
        Step_countDao.dropTable(db, ifExists);
        CoachDao.dropTable(db, ifExists);
        CoachItemDao.dropTable(db, ifExists);
        SleepDao.dropTable(db, ifExists);
    }
    
    public static abstract class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }
    
    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            MigrationHelper.getInstance().migrate(db, Activity_statusDao.class, EcgDao.class, ProfileDao.class, Step_countDao.class, CoachDao.class,CoachItemDao.class,SleepDao.class);
            //remove today steps to sync with raw sensor version
            long todayMidNightTime = ShareUtils.getMidnightMilles(System.currentTimeMillis());
            String sqlDelete = "DELETE FROM step_count where start>=?";
            db.execSQL(sqlDelete, new Object[]{todayMidNightTime});
            Log.i("smile", "sqlDelete: " + sqlDelete);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "onDowngrade schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            MigrationHelper.getInstance().migrate(db, Activity_statusDao.class, EcgDao.class, ProfileDao.class, Step_countDao.class, CoachDao.class,CoachItemDao.class,SleepDao.class);
        }
    }

    public DaoMaster(SQLiteDatabase db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(ProfileDao.class);
        registerDaoClass(SystemInfoDao.class);
        registerDaoClass(Activity_statusDao.class);
        registerDaoClass(EcgDao.class);
        registerDaoClass(Step_countDao.class);
        registerDaoClass(CoachDao.class);
        registerDaoClass(CoachItemDao.class);
        registerDaoClass(SleepDao.class);
    }
    
    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }
    
    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }
    
}
package com.asus.wellness;

import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.app.backup.FileBackupHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import com.asus.wellness.dbhelper.Activity_stateDao;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.dbhelper.DeviceDao;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.dbhelper.MigrationHelper;
import com.asus.wellness.dbhelper.ProfileDao;
import com.asus.wellness.dbhelper.Step_goalDao;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.CoachTable;
import com.asus.wellness.provider.EcgTable;
import com.asus.wellness.provider.LocationChangeTable;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.provider.SleepTable;
import com.asus.wellness.provider.StepGoalTable;
import com.asus.wellness.provider.WellnessSQLiteOpenHelper;

public class DatabaseBackupHelper extends FileBackupHelper implements
        BackupHelper {
    private static final String TAG = "DatabaseBackupHelper";
    
    public static final String BACKUP_DATABASE_NAME = "database_backup.db";
    
    private static final String ATTACH_WELLNESS_DB = "ATTACH '/data/data/com.asus.wellness/databases/"+WellnessSQLiteOpenHelper.DATABASE_NAME+"' AS wellness;";
    private static final String ATTACH_BACKUP_DB = "ATTACH '/data/data/com.asus.wellness/databases/"+BACKUP_DATABASE_NAME+"' AS backupdb;";
    private static final String DETACH_WELLNESS_DB = "DETACH DATABASE wellness;";
    private static final String DETACH_BACKUP_DB = "DETACH DATABASE backupdb;";
    
    private static final String BACKUP_PROFILE = "insert into "+ProfileTable.TABLE_NAME+" select * from wellness."+ProfileTable.TABLE_NAME+";";
    private static final String RESTORE_PROFILE = "insert into "+ProfileTable.TABLE_NAME+" select * from backupdb."+ProfileTable.TABLE_NAME+";";
 //   private static final String RESTORE_PROFILE = "insert into "+ProfileTable.TABLE_NAME+" ('name','photo_path','age','gender','height','height_unit','weight','weight_unit','start_time','step_goal') select name, photo_path , age , gender , height , height_unit , weight , weight_unit , start_time , step_goal  from backupdb."+ProfileTable.TABLE_NAME+";";
    private static final String DELETE_PROFILE = "delete from "+ProfileTable.TABLE_NAME+";";
    
    private static long BACKUP_TIME_THRESHOLD = 6*31*24*60*60*1000l;// half year
    
    private Context mContext;
    public DatabaseBackupHelper(Context context) {
        super(context,"../databases/"+BACKUP_DATABASE_NAME);
        mContext = context;
    }

    @Override
    public void performBackup(ParcelFileDescriptor oldState,
            BackupDataOutput data, ParcelFileDescriptor newState) {
    	Log.d(TAG,"performBackup");
        //delete old backuped database if exists
        SQLiteDatabase.deleteDatabase(mContext.getDatabasePath(BACKUP_DATABASE_NAME));

        //Start to backup database
        long now = System.currentTimeMillis();
        DataHelper dh = new DataHelper(mContext,BACKUP_DATABASE_NAME);
     //   SQLiteOpenHelper helper = new WellnessSQLiteOpenHelper(mContext,BACKUP_DATABASE_NAME);
        SQLiteDatabase mBackupDB = dh.getDaoSession().getDatabase(); //helper.getWritableDatabase();
        mBackupDB.execSQL(ATTACH_WELLNESS_DB);
        mBackupDB.beginTransaction();
        try{
            mBackupDB.execSQL(BACKUP_PROFILE);
            mBackupDB.execSQL(createEcgBackupSQL(now));
            mBackupDB.execSQL(createActivityStateBackupSQL(now));
            mBackupDB.execSQL(createStepGoalBackupSQL(now));
            mBackupDB.execSQL(createLocationChangeBackupSQL(now));
            mBackupDB.execSQL(createSleepBackupSQL(now));
            mBackupDB.execSQL(createCoachBackupSQL(now));
            mBackupDB.setTransactionSuccessful();
        }finally{
        	mBackupDB.endTransaction();
        }
        mBackupDB.execSQL(DETACH_WELLNESS_DB);
        Log.d(TAG,"performBackup end");
        mBackupDB.close();
   //     helper.close();
        
        //backup database file
        super.performBackup(oldState, data, newState);
    }

    private static String createEcgBackupSQL(long now){
        long time = now - BACKUP_TIME_THRESHOLD;
        if (time > 0){
            return "insert into "+EcgTable.TABLE_NAME+" select * from wellness."+EcgTable.TABLE_NAME+" where "+EcgTable.COLUMN_MEASURE_TIME + " > "+time+";";
        }else{
            return "insert into "+EcgTable.TABLE_NAME+" select * from wellness."+EcgTable.TABLE_NAME+";";
        }
    }
    
    private static String createActivityStateBackupSQL(long now) {
        long time = now - BACKUP_TIME_THRESHOLD;
        if (time > 0){
            return "insert into "+ActivityStateTable.TABLE_NAME+" select * from wellness."+ActivityStateTable.TABLE_NAME+" where "+ActivityStateTable.COLUMN_START + " > "+time+";";
        }else{
            return "insert into "+ActivityStateTable.TABLE_NAME+" select * from wellness."+ActivityStateTable.TABLE_NAME+";";
        }
    }
    
    private static String createStepGoalBackupSQL(long now) {
        long time = now - BACKUP_TIME_THRESHOLD;
        if (time > 0){
            return "insert into "+StepGoalTable.TABLE_NAME+" select * from wellness."+StepGoalTable.TABLE_NAME+" where "+StepGoalTable.COLUMN_DATE_TIME_MILLI + " > "+time+";";
        }else{
            return "insert into "+StepGoalTable.TABLE_NAME+" select * from wellness."+StepGoalTable.TABLE_NAME+";";
        }
    }
    
    private static String createLocationChangeBackupSQL(long now) {
        long time = now - BACKUP_TIME_THRESHOLD;
        if (time > 0){
            return "insert into "+LocationChangeTable.TABLE_NAME+" select * from wellness."+LocationChangeTable.TABLE_NAME+" where "+LocationChangeTable.COLUMN_GET_LOCATION_TIME + " > "+time+";";
        }else{
            return "insert into "+LocationChangeTable.TABLE_NAME+" select * from wellness."+LocationChangeTable.TABLE_NAME+";";
        }
    }

    private static String createSleepBackupSQL(long now){
        long time = now - BACKUP_TIME_THRESHOLD;
        if (time > 0){
            return "insert into "+ SleepTable.TABLE_NAME+" select * from wellness."+SleepTable.TABLE_NAME+" where "+SleepTable.COLUMN_START + " > "+time+";";
        }else{
            return "insert into "+SleepTable.TABLE_NAME+" select * from wellness."+SleepTable.TABLE_NAME+";";
        }
    }
    private static String createCoachBackupSQL(long now){
        long time = now - BACKUP_TIME_THRESHOLD;
        if (time > 0){
            return "insert into "+ CoachTable.TABLE_NAME+" select * from wellness."+CoachTable.TABLE_NAME+" where "+CoachTable.COLUMN_START + " > "+time+";";
        }else{
            return "insert into "+CoachTable.TABLE_NAME+" select * from wellness."+CoachTable.TABLE_NAME+";";
        }
    }
    
    public static void restoreDatabase(Context context){
        Log.d(TAG,"restoreDatabase start");
        long time = SystemClock.elapsedRealtime();
        //smile_gao change to greendao
     //   WellnessSQLiteOpenHelper wellness = new WellnessSQLiteOpenHelper(context, WellnessSQLiteOpenHelper.DATABASE_NAME);
        DataHelper dhback = new DataHelper(context,BACKUP_DATABASE_NAME);
     //   MigrationHelper.getInstance().migrate(dhback.getDaoSession().getDatabase(),ProfileDao.class,DeviceDao.class);
        Log.d(TAG, "restoreDatabase after migrate");
        DataHelper dh =  DataHelper.getInstance(context);
        SQLiteDatabase db = dh.getDaoSession().getDatabase(); //wellness.getWritableDatabase();

        MigrationHelper.migrate(db, dhback.getDaoSession().getDatabase());

//
////        //end smile_gao change to greendao
//        db.execSQL(ATTACH_BACKUP_DB);
//        db.beginTransaction();
//        try{
//        	db.execSQL(DELETE_PROFILE);
//        	db.execSQL(deleteECGDataSQL());
//            db.execSQL(deleteActivityStateDataSQL());
//            db.execSQL(deleteStepGoalDataSQL());
//            db.execSQL(deleteLocationChangeDataSQL());
//            db.execSQL(RESTORE_PROFILE);
//            db.execSQL(createEcgRestoreSQL());
//            db.execSQL(createActivityStateRestoreSQL());
//            db.execSQL(createStepGoalRestoreSQL());
//            db.execSQL(createLocationChangeRestoreSQL());
//            db.setTransactionSuccessful();
//        }finally{
//        	db.endTransaction();
//        }
//        db.execSQL(DETACH_BACKUP_DB);
        //db.close();
        //wellness.close();
        Log.d(TAG,"restoreDatabase end, cost = " +(SystemClock.elapsedRealtime() - time)+" ms." );
    }
    
    private static String deleteECGDataSQL(){
    	return "delete from "+EcgTable.TABLE_NAME+";";
    }
    
    private static String deleteActivityStateDataSQL(){
    	return "delete from "+ActivityStateTable.TABLE_NAME+";";
    }
    
    private static String deleteStepGoalDataSQL(){
    	return "delete from "+StepGoalTable.TABLE_NAME+";";
    }
    
    private static String deleteLocationChangeDataSQL(){
    	return "delete from "+LocationChangeTable.TABLE_NAME+";";
    }
    
    private static String createEcgRestoreSQL(){
        return "insert into "+EcgTable.TABLE_NAME+" select * from backupdb."+EcgTable.TABLE_NAME+";";
    }
    
    private static String createActivityStateRestoreSQL() {
        return "insert into "+ActivityStateTable.TABLE_NAME+" select * from backupdb."+ActivityStateTable.TABLE_NAME+";";
    }
    
    private static String createStepGoalRestoreSQL() {
        return "insert into "+StepGoalTable.TABLE_NAME+" select * from backupdb."+StepGoalTable.TABLE_NAME+";";
    }
    private static String createLocationChangeRestoreSQL() {
        return "insert into "+LocationChangeTable.TABLE_NAME+" select * from backupdb."+LocationChangeTable.TABLE_NAME+";";
    }
}

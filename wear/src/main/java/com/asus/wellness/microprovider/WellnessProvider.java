package com.asus.wellness.microprovider;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.asus.wellness.WApplication;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.dbhelper.Profile;

public class WellnessProvider extends ContentProvider {

    public static final String DEBUG_TAG = "wellness_provider";
    public static final String AUTHORITY = "com.asus.wear.wellness.provider";
    private SQLiteDatabase mDB;
    private static UriMatcher mUriMatcher;

    public static final  String COLUMN_TOTAL_STEP = "total_step";
    public static final  String COLUMN_CALORIES_BURNED= "calories_burned";
    private static final String COUNT_STEP_COLARIES_PATH= "count_step_colaries";
    public static final Uri COUNT_STEP_COLARIES_URI= Uri.parse("content://"+ AUTHORITY +"/"+ COUNT_STEP_COLARIES_PATH);

    public static final int COUNT_STEP_COLARIES_CODE= 5;


    static {
        mUriMatcher = new UriMatcher(0);
        mUriMatcher.addURI(AUTHORITY, StepCountTable.TABLE_NAME, StepCountTable.URI_MATCH_CODE);
        mUriMatcher.addURI(AUTHORITY, ActivityStatusTable.TABLE_NAME, ActivityStatusTable.URI_MATCH_CODE);
        mUriMatcher.addURI(AUTHORITY, ProfileTable.TABLE_NAME, ProfileTable.URI_MATCH_CODE);
        mUriMatcher.addURI(AUTHORITY, EcgTable.TABLE_NAME, EcgTable.URI_MATCH_CODE);
        mUriMatcher.addURI(AUTHORITY, COUNT_STEP_COLARIES_PATH, COUNT_STEP_COLARIES_CODE);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // TODO Auto-generated method stub
        switch (mUriMatcher.match(arg0)) {
            case ProfileTable.URI_MATCH_CODE:
                return mDB.delete(ProfileTable.TABLE_NAME, arg1, arg2);
            case ActivityStatusTable.URI_MATCH_CODE:
                return mDB.delete(ActivityStatusTable.TABLE_NAME, arg1, arg2);
            case EcgTable.URI_MATCH_CODE:
                return mDB.delete(EcgTable.TABLE_NAME, arg1, arg2);
        }
        return 0;
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        // TODO Auto-generated method stub
        long id = 0;
        switch (mUriMatcher.match(arg0)) {
            case StepCountTable.URI_MATCH_CODE:
                id = mDB.insert(StepCountTable.TABLE_NAME, null, arg1);
                getContext().getContentResolver().notifyChange(arg0, null);
                return Uri.parse(StepCountTable.TABLE_URI + "/" + String.valueOf(id));
            case ActivityStatusTable.URI_MATCH_CODE:
                id = mDB.insert(ActivityStatusTable.TABLE_NAME, null, arg1);
                getContext().getContentResolver().notifyChange(arg0, null);
                return Uri.parse(ActivityStatusTable.TABLE_URI + "/" + String.valueOf(id));
            case ProfileTable.URI_MATCH_CODE:
                id = mDB.insert(ProfileTable.TABLE_NAME, null, arg1);
                getContext().getContentResolver().notifyChange(arg0, null);
                return Uri.parse(ProfileTable.TABLE_URI + "/" + String.valueOf(id));
            case EcgTable.URI_MATCH_CODE:
                id = mDB.insert(EcgTable.TABLE_NAME, null, arg1);
                getContext().getContentResolver().notifyChange(arg0, null);
                return Uri.parse(EcgTable.TABLE_URI + "/" + String.valueOf(id));
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        try {
            Log.d("provider", " onCreate");
            DataHelper dh = new DataHelper(getContext());
         //   dh.getDaoSession().getDatabase();
            if(WApplication.getDataHelper() ==null)
            {
                dh.getDaoSession();
                WApplication.setDataHelper(dh);
            }
            mDB = dh.getDaoSession().getDatabase();
        } catch (SQLiteException e) {
            Log.w(WellnessProvider.DEBUG_TAG, "SQLite exception : " + e);
            return false;
        }

        return true;
    }

    @Override
    public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
                        String arg4) {
        // TODO Auto-generated method stub
        switch (mUriMatcher.match(arg0)) {
            case StepCountTable.URI_MATCH_CODE:
                return mDB.query(StepCountTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
            case ActivityStatusTable.URI_MATCH_CODE:
                return mDB.query(ActivityStatusTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
            case ProfileTable.URI_MATCH_CODE:
                return mDB.query(ProfileTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
            case EcgTable.URI_MATCH_CODE:
                return mDB.query(EcgTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
            case COUNT_STEP_COLARIES_CODE:
                return getDailyStepColaries();
        }
        return null;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        // TODO Auto-generated method stub
        switch (mUriMatcher.match(arg0)) {
            case StepCountTable.URI_MATCH_CODE:
                return mDB.update(StepCountTable.TABLE_NAME, arg1, arg2, arg3);
            case ActivityStatusTable.URI_MATCH_CODE:
                return mDB.update(ActivityStatusTable.TABLE_NAME, arg1, arg2, arg3);
            case ProfileTable.URI_MATCH_CODE:
                int id = mDB.update(ProfileTable.TABLE_NAME, arg1, arg2, arg3);
                getContext().getContentResolver().notifyChange(arg0, null);
                return id;
        }
        return 0;
    }

    private Cursor getDailyStepColaries(){
        String[] columnNames = new String[]{
             COLUMN_TOTAL_STEP,
            COLUMN_CALORIES_BURNED
        };
        int total_step = StepHelper.getTodaySteps();
        Profile profile = ProfileHelper.getStandardProfile();

        int heightInCM = profile.getHeight();
        if(profile.getHeight_unit()==ProfileHelper.HEIGHT_UNIT_FT){
            float ft=ProfileHelper.InchToFt(heightInCM);
            heightInCM=(int) Math.round(ProfileHelper.ftToCm(ft));
        }
        int weightInKG = profile.getWeight();
        if(profile.getWeight_unit() ==ProfileHelper.WEIGHT_UNIT_LBS){
            weightInKG=(int) Math.round(ProfileHelper.LbsToKg(profile.getWeight()));
        }

        int calories = (int)ProfileHelper.getWalkCalories(heightInCM,weightInKG,total_step);
        MatrixCursor cursor = new MatrixCursor(columnNames);
        cursor.addRow(new Integer[]{total_step,calories});
        return cursor;
    }
}

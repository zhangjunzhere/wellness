package com.asus.wellness.provider;

import com.asus.wellness.utils.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class WellnessSQLiteOpenHelper {
	
	public static String DATABASE_NAME = "asus_wellness.db";
	public static int DATABASE_VERSION = 2;
	
	private static long MIN = 60*1000;
	private static long HOUR = 60*MIN;
	private static long DAY = 24*HOUR;
	private static long MONTH = DAY*31;
	
	public WellnessSQLiteOpenHelper(Context context, String name) {
//		super(context, name, null, DATABASE_VERSION);
	}

//	@Override
//	public void onCreate(SQLiteDatabase db) {
//		// TODO Auto-generated method stub
//		db.execSQL(ProfileTable.CREATE_TABLE);
//		db.execSQL(EcgTable.CREATE_TABLE);
//		db.execSQL(ActivityStateTable.CREATE_TABLE);
//		db.execSQL(StepGoalTable.CREATE_TABLE);
//		db.execSQL(LocationChangeTable.CREATE_TABLE);
//
//		//Insert fake data
////		insertFakeData(db);
//
//	}

    public static  void insertFakeData(SQLiteDatabase db) {
        Log.d("LZN", "insertFakeData start");
        long now = System.currentTimeMillis();
        Random random = new Random();
        int numRowsEcg = 24;   // ECG assume 24 rows a day
        int numRowsWalk = 100;  // Walk assume 100 rows a day
        int numRowsTrans = 20; // Drive & bike assume 20 rows a day
        int totalDays = 300;
        long activityWalkDuration = DAY / numRowsWalk;
        long activityNotWalkDuration = DAY / numRowsTrans;
        long duration = 0;
        ContentValues values = new ContentValues();
        for (int m = 0; m < 12; m++) {
            for (int d = 0; d < 31; d++) {
                db.beginTransaction();
                try {
                    // ECG
                    values.clear();
                    for (int i = 0; i < numRowsEcg; i++) {
                        values.put(EcgTable.COLUMN_MEASURE_TIME, (now - (m * MONTH) - ((d + 1) * DAY)) + (HOUR * i));
                        switch (i % 3) {
                            case EcgTable.TYPE_HEARTRATE:
                                values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_HEARTRATE);
                                break;
                            case EcgTable.TYPE_RELAX:
                                values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_RELAX);
                                break;
                            case EcgTable.TYPE_STRESS:
                                values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_STRESS);
                                break;
                            default:
                                values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_HEARTRATE);
                        }
                        values.put(EcgTable.COLUMN_MEASURE_VALUE, random.nextInt(101));
                        db.insert(EcgTable.TABLE_NAME, null, values);
                    }

                    values.clear();
                    // Step goal ony 1 row a day.
                    values.put(StepGoalTable.COLUMN_DATE_TIME_MILLI, (now - (m * MONTH) - ((d + 1) * DAY)) + (HOUR * random.nextInt(24)));
                    values.put(StepGoalTable.COLUMN_STEP_GOAL, random.nextInt(125001));
                    db.insert(StepGoalTable.TABLE_NAME, null, values);

                    values.clear();
                    // Activity state : Walk, Drive & bike
                    for (int i = 0; i < numRowsWalk; i++) {
                        duration = random.nextInt((int)activityWalkDuration);
                        values.put(ActivityStateTable.COLUMN_TYPE, ActivityStateTable.TYPE_WALK);
                        values.put(ActivityStateTable.COLUMN_START, (now - (m * MONTH) - ((d + 1) * DAY)) + (i * activityWalkDuration));
                        values.put(ActivityStateTable.COLUMN_END, (now - (m * MONTH) - ((d + 1) * DAY)) + ((i * activityWalkDuration) + duration));
                        values.put(ActivityStateTable.COLUMN_STEP_COUNT, random.nextInt(10000));
                        db.insert(ActivityStateTable.TABLE_NAME, null, values);
                    }

                    values.clear();
                    for (int i = 0; i < numRowsTrans; i++) {
                        duration = random.nextInt((int)activityNotWalkDuration);
                        values.put(ActivityStateTable.COLUMN_TYPE, random.nextBoolean() ? ActivityStateTable.TYPE_DRIVE : ActivityStateTable.TYPE_BIKE);
                        values.put(ActivityStateTable.COLUMN_START, (now - (m * MONTH) - ((d + 1) * DAY)) + (i * activityNotWalkDuration));
                        values.put(ActivityStateTable.COLUMN_END, (now - (m * MONTH) - ((d + 1) * DAY)) + ((i * activityNotWalkDuration) + duration));
                        values.put(ActivityStateTable.COLUMN_DISTANCE, random.nextInt(10000));
                        db.insert(ActivityStateTable.TABLE_NAME, null, values);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        }

        values.clear();
        values.put(ProfileTable.COLUMN_START_TIME, (System.currentTimeMillis() - totalDays * Utility.ONE_DAY_MS));
        db.insert(ProfileTable.TABLE_NAME, null, values);

        Log.d("LZN", "insertFakeData end " + (System.currentTimeMillis() - now) + "ms");
    }

//    @Override
//	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		// TODO Auto-generated method stub
////		db.execSQL("DROP TABLE IF EXISTS "+ ProfileTable.TABLE_NAME);
////		db.execSQL("DROP TABLE IF EXISTS "+ EcgTable.TABLE_NAME);
////		db.execSQL("DROP TABLE IF EXISTS "+ ActivityStateTable.TABLE_NAME);
////		db.execSQL("DROP TABLE IF EXISTS "+ StepGoalTable.TABLE_NAME);
////		db.execSQL("DROP TABLE IF EXISTS "+ LocationChangeTable.TABLE_NAME);
////		onCreate(db);
//    	onUpgradeDatabase(db,oldVersion,newVersion);
//	}
	
    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion){
    	//Create location table if not exists
    	db.execSQL(LocationChangeTable.CREATE_TABLE);
    	
    	if(oldVersion==1){
			db.execSQL("ALTER TABLE "+EcgTable.TABLE_NAME+" ADD COLUMN "+EcgTable.COLUMN_MEASURE_COMMENT+" TEXT");
			oldVersion=2;
		}
    }

    public static void insertData(ContentResolver CR) {
        Random random = new Random();
		SimpleDateFormat getYMD = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat setYMD = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


        int numRowsEcg = 12;   // ECG assume 24 rows a day
      //  int numRowsWalk = 6;  // Walk assume 100 rows a day
        int numRowsTrans = 5; // Drive & bike assume 20 rows a day
        int totalSetpCount = 5000000;
        int tmp;

        long activityNotWalkDuration = DAY / numRowsTrans;
        long duration = 0;
        ContentValues values = new ContentValues();
        Uri uri = null;
        int m = 0;
        int maxDay = 330;
        int totalDays = maxDay;
        for (int d = 0; d <maxDay; d++) {
        // ECG
            int numRowsWalk = 6 + random.nextInt(6);  // Walk assume 100 rows a day
            long activityWalkDuration = DAY / numRowsWalk * 14 / 24;

            Date nowDate=new Date();
            int min = random.nextInt(60);
            int sec = random.nextInt(60);
            String day = getYMD.format(nowDate)+" 32:" + String.valueOf(min)+ ":" + String.valueOf(sec);

            try {
                nowDate = setYMD.parse(day);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //long now = System.currentTimeMillis();
            long now = nowDate.getTime();

        	values.clear();
        	for (int i = 0; i < numRowsEcg; i++) {
        		values.put(EcgTable.COLUMN_MEASURE_TIME, (now - (m * MONTH) - ((d + 1) * DAY)) + (HOUR * i));
                switch (i % 2) {
                   case 1:
                        values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_HEARTRATE);
                        values.put(EcgTable.COLUMN_MEASURE_VALUE, random.nextInt(70) + 50);
                        break;
                   case EcgTable.TYPE_RELAX:
                        values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_RELAX);
                        values.put(EcgTable.COLUMN_MEASURE_VALUE, random.nextInt(85) + 10);
                        break;
                   //case EcgTable.TYPE_STRESS:
                   //     values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_STRESS);
                   //     break;
                   default:
                        values.put(EcgTable.COLUMN_MEASURE_TYPE, EcgTable.TYPE_HEARTRATE);
                        values.put(EcgTable.COLUMN_MEASURE_VALUE, random.nextInt(60) + 60);
                   }
                   uri = CR.insert(EcgTable.TABLE_URI, values);
           }
           values.clear();
           
           // Step goal ony 1 row a day.
           values.put(StepGoalTable.COLUMN_DATE_TIME_MILLI, (now - (m * MONTH) - ((d + 1) * DAY)) + (HOUR * random.nextInt(24)));
           values.put(StepGoalTable.COLUMN_STEP_GOAL, 20000);
           uri = CR.insert(StepGoalTable.TABLE_URI, values);
           values.clear();
           
           // Activity state : Walk, Drive & bike
           
           for (int i = 0; i < numRowsWalk; i++) {
               duration = random.nextInt((int)activityWalkDuration) / 2;
               values.put(ActivityStateTable.COLUMN_TYPE, ActivityStateTable.TYPE_WALK);
               values.put(ActivityStateTable.COLUMN_START, (now - (m * MONTH) - ((d + 1) * DAY)) + (i * activityWalkDuration));
               values.put(ActivityStateTable.COLUMN_END, (now - (m * MONTH) - ((d + 1) * DAY)) + ((i * activityWalkDuration) + duration));
               tmp = (int) ((random.nextInt(20) + 40) * duration / MIN);
               if( ( i == (numRowsWalk - 1) ) && (d == maxDay+1) )
               {
            	   values.put(ActivityStateTable.COLUMN_STEP_COUNT, totalSetpCount);
            	   if(totalSetpCount > 0)
            	   {
            		   uri = CR.insert(ActivityStateTable.TABLE_URI, values);
            	   }
               }
               else if(totalSetpCount - tmp >= 0)
               {
            	   values.put(ActivityStateTable.COLUMN_STEP_COUNT, tmp);
            	   totalSetpCount = totalSetpCount - tmp;
            	   uri = CR.insert(ActivityStateTable.TABLE_URI, values);
               }
               else if(totalSetpCount < 0)
               {
            	   values.put(ActivityStateTable.COLUMN_STEP_COUNT, 0);
            	   totalSetpCount = -1;
               }
              
               
           }
           values.clear();
 
           /*for (int i = 0; i < numRowsTrans; i++) {
               duration = random.nextInt((int)activityNotWalkDuration);
               values.put(ActivityStateTable.COLUMN_TYPE, random.nextBoolean() ? ActivityStateTable.TYPE_DRIVE : ActivityStateTable.TYPE_BIKE);
               values.put(ActivityStateTable.COLUMN_START, (now - (m * MONTH) - ((d + 1) * DAY)) + (i * activityNotWalkDuration));
               values.put(ActivityStateTable.COLUMN_END, (now - (m * MONTH) - ((d + 1) * DAY)) + ((i * activityNotWalkDuration) + duration));
               
               values.put(ActivityStateTable.COLUMN_DISTANCE, random.nextInt(10000));
               uri = CR.insert(ActivityStateTable.TABLE_URI, values);
           }*/

        }


        values.clear();
        values.put(ProfileTable.COLUMN_START_TIME, (System.currentTimeMillis() - totalDays * Utility.ONE_DAY_MS));
        int ids = CR.update(ProfileTable.TABLE_URI, values,null,null);

    }
}

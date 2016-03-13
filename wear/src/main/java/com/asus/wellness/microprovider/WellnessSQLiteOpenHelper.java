package com.asus.wellness.microprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WellnessSQLiteOpenHelper extends SQLiteOpenHelper{
	
	public static String DATABASE_NAME = "asus_wellness.db";
	public static int DATABASE_VERSION = 2;

	public WellnessSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i("smile","oncreate wellness sqlite");
		db.execSQL(StepCountTable.CREATE_TABLE);
		db.execSQL(ActivityStatusTable.CREATE_TABLE);
		db.execSQL(ProfileTable.CREATE_TABLE);
		db.execSQL(EcgTable.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
//		Log.i("smile","updategrade");
//		db.execSQL("DROP TABLE IF EXISTS "+ StepCountTable.TABLE_NAME);
//		db.execSQL("DROP TABLE IF EXISTS "+ ActivityStatusTable.TABLE_NAME);
//		db.execSQL("DROP TABLE IF EXISTS "+ ProfileTable.TABLE_NAME);
//		db.execSQL("DROP TABLE IF EXISTS "+ EcgTable.TABLE_NAME);
//		onCreate(db);
	}

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

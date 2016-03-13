package com.asus.wellness.provider;

import android.net.Uri;

public class SleepTable {
	public static final String TABLE_NAME = "sleep";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_START = "start";
	public static final String COLUMN_END = "end";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_DATA = "data";

	public static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
			"("+ 
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_START +" INTEGER,"+ 
			COLUMN_END +" INTEGER,"+
			COLUMN_DATE +" TEXT,"+
			COLUMN_DATA +" TEXT,"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 6;
}

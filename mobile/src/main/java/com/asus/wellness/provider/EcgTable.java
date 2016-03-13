package com.asus.wellness.provider;

import android.net.Uri;

public class EcgTable {
	public static final String TABLE_NAME = "ecg";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MEASURE_TIME = "measure_time";
	public static final String COLUMN_MEASURE_TYPE = "measure_type";
	public static final String COLUMN_MEASURE_VALUE = "measure_value";
	public static final String COLUMN_MEASURE_COMMENT = "measure_comment";
	
	public static final int TYPE_RELAX=0;
	public static final int TYPE_STRESS=1;
	public static final int TYPE_HEARTRATE=2;
	public static final int TYPE_SLEEP = 1001;
	
	public static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
			"("+ 
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_MEASURE_TIME +" INTEGER,"+ 
			COLUMN_MEASURE_VALUE +" INTEGER,"+
			COLUMN_MEASURE_TYPE +" INTEGER,"+
			COLUMN_MEASURE_COMMENT +" TEXT"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 2;
}

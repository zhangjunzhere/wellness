package com.asus.wellness.provider;

import android.net.Uri;

public class ActivityStateTable {
	public static final String TABLE_NAME = "activity_state";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_START = "start";
	public static final String COLUMN_END = "end";
	public static final String COLUMN_STEP_COUNT = "step_count";
	public static final String COLUMN_DISTANCE = "distance";
	public static final String COLUMN_TYPE = "type";
	
	public static final int TYPE_WALK=0;
	public static final int TYPE_DRIVE=1;
	public static final int TYPE_BIKE=2;

	public static final int TYPE_RUN=100;
	public static final int TYPE_PUSHUP=101;
	public static final int TYPE_SITUP=102;
	
	public static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
			"("+ 
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_START +" INTEGER,"+ 
			COLUMN_END +" INTEGER,"+ 
			COLUMN_STEP_COUNT +" INTEGER,"+ 
			COLUMN_DISTANCE +" INTEGER,"+ 
			COLUMN_TYPE +" INTEGER"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 3;
}

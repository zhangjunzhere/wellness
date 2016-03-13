package com.asus.wellness.microprovider;

import android.net.Uri;

public class ActivityStatusTable {
	public static final String TABLE_NAME = "activity_status";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_STEP = "step";
	public static final String COLUMN_DISTANCE = "distance";
	public static final String COLUMN_TYPE = "activity_type";
	
	public static final int TYPE_WALK=0;
	public static final int TYPE_DRIVE=1;
	public static final int TYPE_BIKE=2;
	
	public static final String CREATE_TABLE =
			"CREATE TABLE "+ TABLE_NAME +
			"("+ 
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_STEP +" INTEGER,"+ 
			COLUMN_DISTANCE +" INTEGER,"+
			COLUMN_TYPE +" INTEGER"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 3;
}

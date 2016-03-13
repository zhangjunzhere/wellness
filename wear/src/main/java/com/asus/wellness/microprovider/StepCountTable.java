package com.asus.wellness.microprovider;

import android.net.Uri;

public class StepCountTable {
	public static final String TABLE_NAME = "step_count";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_START = "start";
	public static final String COLUMN_END = "end";
	public static final String COLUMN_STEP_COUNT = "step_count";
	
	public static final String CREATE_TABLE =
			"CREATE TABLE "+ TABLE_NAME +
			"("+ 
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_START +" INTEGER,"+ 
			COLUMN_END +" INTEGER,"+
			COLUMN_STEP_COUNT +" INTEGER"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 2;
}

package com.asus.wellness.provider;

import android.net.Uri;

public class StepGoalTable {
	public static final String TABLE_NAME = "step_goal";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATE_TIME_MILLI = "date_milli";
	public static final String COLUMN_STEP_GOAL = "step_goal";

	public static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
			"("+
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_DATE_TIME_MILLI +" DATETIME DEFAULT CURRENT_DATE,"+
			COLUMN_STEP_GOAL +" INTEGER"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 4;
}

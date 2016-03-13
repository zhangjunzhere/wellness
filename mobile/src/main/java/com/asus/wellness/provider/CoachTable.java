package com.asus.wellness.provider;

import android.net.Uri;

public class CoachTable {
	public static final String TABLE_NAME = "coach";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_START = "start";
	public static final String COLUMN_END = "end";
	public static final String COLUMN_DURATION = "duration";
	public static final String COLUMN_VALUE = "value";
	public static final String COLUMN_PERCENT = "percent";
	public static final String COLUMN_TYPE = "type";

	public static final int TYPE_RUN=100;
	public static final int TYPE_PUSHUP=101;
	public static final int TYPE_SITUP=102;

	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 5;
}

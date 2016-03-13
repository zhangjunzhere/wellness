package com.asus.wellness.provider;

import android.net.Uri;

public class LocationChangeTable {
	public static final String TABLE_NAME = "location_change";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_GET_LOCATION_TIME = "get_location_time";
	public static final String COLUMN_LOCATION_LATITUDE = "latitude";
	public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
	public static final String COLUMN_DISTRICT = "district";
	
	public static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
			"("+ 
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_GET_LOCATION_TIME +" INTEGER,"+ 
			COLUMN_LOCATION_LATITUDE +" REAL,"+
			COLUMN_LOCATION_LONGITUDE +" REAL,"+
			COLUMN_DISTRICT +" TEXT"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 0;
}

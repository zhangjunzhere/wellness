package com.asus.wellness.provider;

import android.net.Uri;

public class ProfileTable {
	public static final String TABLE_NAME = "profile";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PHOTO = "photo_path";
	public static final String COLUMN_AGE = "age";
	public static final String COLUMN_GENDER = "gender";
	public static final String COLUMN_HEIGHT = "height";
	public static final String COLUMN_HEIGHT_UNIT = "height_unit";
	public static final String COLUMN_WEIGHT = "weight";
	public static final String COLUMN_WEIGHT_UNIT = "weight_unit";
	public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_BIRTHDAY = "birthday";
	
	public static final int MALE=0;
	public static final int FEMALE=1;
	
	public static final int HEIGHT_UNIT_CM=0;
	public static final int HEIGHT_UNIT_FT=1;
	
	public static final int WEIGHT_UNIT_KG=0;
	public static final int WEIGHT_UNIT_LBS=1;

	public static final int DISTANTCE_UNIT_KM=0;
	public static final int DISTANTCE_UNIT_MILES=1;

	public static final int DEFAULT_AGE=20;
	public static final int DEFAULT_HEIGHT=170;
	public static final int DEFAULT_WEIGHT=70;
	
	public static final String CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
			"("+ 
			COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COLUMN_NAME +" TEXT,"+ 
			COLUMN_PHOTO +" TEXT,"+
			COLUMN_AGE +" INTEGER,"+
			COLUMN_GENDER +" INTEGER,"+
			COLUMN_HEIGHT +" INTEGER,"+
			COLUMN_HEIGHT_UNIT +" INTEGER,"+
			COLUMN_WEIGHT +" INTEGER,"+
			COLUMN_WEIGHT_UNIT +" INTEGER,"+
			COLUMN_START_TIME +" INTEGER,"+
            COLUMN_BIRTHDAY +" INTEGER"+
			");";
	
	public static final Uri TABLE_URI = Uri.parse("content://"+ WellnessProvider.AUTHORITY +"/"+ TABLE_NAME);
	public static final int URI_MATCH_CODE = 1;
}

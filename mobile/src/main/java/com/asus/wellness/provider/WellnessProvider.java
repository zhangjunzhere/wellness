package com.asus.wellness.provider;


import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.asus.wellness.WellnessBackup;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.utils.DeviceHelper;

import java.util.ArrayList;

public class WellnessProvider extends ContentProvider {

	public static final String DEBUG_TAG="wellness_provider";
	public static final String AUTHORITY = "com.asus.wear.wellness.provider";
	private SQLiteDatabase mDB;
	private static UriMatcher mUriMatcher;
	static{
		mUriMatcher = new UriMatcher(0);
		mUriMatcher.addURI(AUTHORITY, EcgTable.TABLE_NAME, EcgTable.URI_MATCH_CODE);
		mUriMatcher.addURI(AUTHORITY, ProfileTable.TABLE_NAME, ProfileTable.URI_MATCH_CODE);
		mUriMatcher.addURI(AUTHORITY, ActivityStateTable.TABLE_NAME, ActivityStateTable.URI_MATCH_CODE);
		mUriMatcher.addURI(AUTHORITY, StepGoalTable.TABLE_NAME, StepGoalTable.URI_MATCH_CODE);
		mUriMatcher.addURI(AUTHORITY, LocationChangeTable.TABLE_NAME, LocationChangeTable.URI_MATCH_CODE);
		mUriMatcher.addURI(AUTHORITY, CoachTable.TABLE_NAME, CoachTable.URI_MATCH_CODE);
		mUriMatcher.addURI(AUTHORITY, SleepTable.TABLE_NAME, SleepTable.URI_MATCH_CODE);
	}

	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
		mDB.beginTransaction();
		ContentProviderResult[] results = null;
		try{
			results = super.applyBatch(operations);
			mDB.setTransactionSuccessful();
			WellnessBackup.dataChanged(getContext());

		}finally {
			mDB.endTransaction();
//			Uri uri = null;
//			for (ContentProviderOperation opt : operations) {
//				if (opt.getUri().equals(uri)) {
//					continue;
//				}
////				uri = opt.getUri();
////				getContext().getContentResolver().notifyChange(opt.getUri(), null);
////				Log.i(DEBUG_TAG, "notifychange endTransaction..uri:" + opt.getUri());
//			}
		}
		return results;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		switch(mUriMatcher.match(arg0)){
			case ProfileTable.URI_MATCH_CODE:
				return mDB.delete(ProfileTable.TABLE_NAME, arg1, arg2);
			case LocationChangeTable.URI_MATCH_CODE:
				return mDB.delete(LocationChangeTable.TABLE_NAME, arg1, arg2);
            case StepGoalTable.URI_MATCH_CODE:
                return mDB.delete(StepGoalTable.TABLE_NAME, arg1, arg2);
			case CoachTable.URI_MATCH_CODE:
				return mDB.delete(CoachTable.TABLE_NAME, arg1, arg2);
			case SleepTable.URI_MATCH_CODE:
				return mDB.delete(SleepTable.TABLE_NAME, arg1, arg2);
		}
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
	    Uri uri = null;
		switch(mUriMatcher.match(arg0)){
			case EcgTable.URI_MATCH_CODE:
			    uri = Uri.parse(EcgTable.TABLE_URI +"/"+ String.valueOf(mDB.insert(EcgTable.TABLE_NAME, null, arg1)));
			    break;
			case ProfileTable.URI_MATCH_CODE:
			    uri = Uri.parse(ProfileTable.TABLE_URI +"/"+ String.valueOf(mDB.insert(ProfileTable.TABLE_NAME, null, arg1)));
			    break;
			case ActivityStateTable.URI_MATCH_CODE:
			    uri = Uri.parse(ActivityStateTable.TABLE_URI +"/"+ String.valueOf(mDB.insert(ActivityStateTable.TABLE_NAME, null, arg1)));
			    break;
			case StepGoalTable.URI_MATCH_CODE:
			    uri = Uri.parse(StepGoalTable.TABLE_URI +"/"+ String.valueOf(mDB.insert(StepGoalTable.TABLE_NAME, null, arg1)));
			    break;
			case LocationChangeTable.URI_MATCH_CODE:
			    uri = Uri.parse(LocationChangeTable.TABLE_URI +"/"+ String.valueOf(mDB.insert(LocationChangeTable.TABLE_NAME, null, arg1)));
			    break;
			case CoachTable.URI_MATCH_CODE:
				uri = Uri.parse(CoachTable.TABLE_URI +"/"+ String.valueOf(mDB.insert(CoachTable.TABLE_NAME, null, arg1)));
				break;
			case SleepTable.URI_MATCH_CODE:
				uri = Uri.parse(SleepTable.TABLE_URI +"/"+ String.valueOf(mDB.insert(SleepTable.TABLE_NAME, null, arg1)));
				break;
		    default:
		        break;
		}
		if (uri != null){
		    WellnessBackup.dataChanged(getContext());
		}

        getContext().getContentResolver().notifyChange(arg0, null);
		return uri;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		try{
			DataHelper dh = DataHelper.getInstance(getContext());
			DeviceHelper.initRobinDevice(dh);
			mDB = dh.getDaoSession().getDatabase();
//		    mDB = new WellnessSQLiteOpenHelper(getContext(), WellnessSQLiteOpenHelper.DATABASE_NAME).getWritableDatabase();
//            WellnessSQLiteOpenHelper.insertFakeData(mDB);
		}catch(SQLiteException e){
			Log.w(WellnessProvider.DEBUG_TAG, "SQLite exception : "+ e);
			return false;
		}
		
		return true;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		try{
			switch(mUriMatcher.match(arg0)){
				case EcgTable.URI_MATCH_CODE:
					return mDB.query(EcgTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
				case ProfileTable.URI_MATCH_CODE:
					return mDB.query(ProfileTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
				case ActivityStateTable.URI_MATCH_CODE:
					return mDB.query(ActivityStateTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
				case StepGoalTable.URI_MATCH_CODE:
					return mDB.query(StepGoalTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
				case LocationChangeTable.URI_MATCH_CODE:
					return mDB.query(LocationChangeTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
				case CoachTable.URI_MATCH_CODE:
					return mDB.query(CoachTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
				case SleepTable.URI_MATCH_CODE:
					return mDB.query(SleepTable.TABLE_NAME, arg1, arg2, arg3, null, null, arg4);
			}
		}catch (Exception e){
			Log.e("larry", "query: " + e);
		}
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		switch(mUriMatcher.match(arg0)){
			case EcgTable.URI_MATCH_CODE:
				return mDB.update(EcgTable.TABLE_NAME, arg1, arg2, arg3);
			case ProfileTable.URI_MATCH_CODE:
				return mDB.update(ProfileTable.TABLE_NAME, arg1, arg2, arg3);
			case ActivityStateTable.URI_MATCH_CODE:
				return mDB.update(ActivityStateTable.TABLE_NAME, arg1, arg2, arg3);
			case StepGoalTable.URI_MATCH_CODE:
				return mDB.update(StepGoalTable.TABLE_NAME, arg1, arg2, arg3);
			case LocationChangeTable.URI_MATCH_CODE:
				return mDB.update(LocationChangeTable.TABLE_NAME, arg1, arg2, arg3);
			case CoachTable.URI_MATCH_CODE:
				return mDB.update(CoachTable.TABLE_NAME, arg1, arg2, arg3);
			case SleepTable.URI_MATCH_CODE:
				return mDB.update(SleepTable.TABLE_NAME, arg1, arg2, arg3);
		}
		return 0;
	}
}

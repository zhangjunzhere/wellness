package com.asus.wellness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.asus.wellness.provider.LocationChangeTable;
import com.asus.wellness.utils.LocationHelper;

public class LoadUnknownLocationTask extends AsyncTask<Void, Void, Boolean> {
	private Context mContext;
	
	public LoadUnknownLocationTask(Context context){
		mContext=context;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub
        Cursor cursor =null;
        try{
            WellnessLocationManager wlm=new WellnessLocationManager(mContext, null);
            cursor=mContext.getContentResolver().query(LocationChangeTable.TABLE_URI, null
                    , LocationChangeTable.COLUMN_DISTRICT+"=?"
                    , new String[]{WellnessLocationManager.DEFAULT_DISTRICT}
                    , LocationChangeTable.COLUMN_GET_LOCATION_TIME);

            if(cursor!=null && cursor.moveToFirst()){
                do{
                    double lat=cursor.getDouble(cursor.getColumnIndex(LocationChangeTable.COLUMN_LOCATION_LATITUDE));
                    double lon=cursor.getDouble(cursor.getColumnIndex(LocationChangeTable.COLUMN_LOCATION_LONGITUDE));
                    int id=cursor.getInt(cursor.getColumnIndex(LocationChangeTable.COLUMN_ID));
                    String district=cursor.getString(cursor.getColumnIndex(LocationChangeTable.COLUMN_DISTRICT));
                    Log.d("circle","database district:"+district);
                    if(district.matches(WellnessLocationManager.DEFAULT_DISTRICT)){
                        district=wlm.getDistrict(lat, lon);
                        if(district.matches(WellnessLocationManager.CANT_DETECT_DISTRICT)){
                            Log.d("circle","can not detect destrict");
                            mContext.getContentResolver().delete(LocationChangeTable.TABLE_URI
                                    , LocationChangeTable.COLUMN_ID+"=?", new String[]{String.valueOf(id)});
                        }
                        else{
                            Log.d("circle","update district:"+district);
                            ContentValues cv=new ContentValues();
                            cv.put(LocationChangeTable.COLUMN_DISTRICT, district);
                            mContext.getContentResolver().update(LocationChangeTable.TABLE_URI, cv
                                    , LocationChangeTable.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                            //smile_gao update lcoation
                            LocationHelper.getInstance(mContext).updateLocationDate(id,district);
                        }
                    }
                }while(cursor.moveToNext());
            }

        }catch (Exception e){
            if(e != null){
                Log.e("circle", e.toString());
            }
        }
        finally {
            if(cursor!=null)
                 cursor.close();
        }

        return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
}

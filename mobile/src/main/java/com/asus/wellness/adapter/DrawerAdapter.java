package com.asus.wellness.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.ProfileData;
import com.asus.wellness.R;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.provider.StepGoalTable;
import com.asus.wellness.ui.profile.DrawerUserHeadImageView;
import com.asus.wellness.ui.setting.SettingStepGoalActivity;
import com.asus.wellness.ui.profile.SetupProfileActivity;
import com.asus.wellness.utils.Utility;

public class DrawerAdapter extends BaseAdapter {

	private Context mContext;
	private Handler mMainHandler;
	java.text.DateFormat mDateFormat;
	public DrawerAdapter(Context context,Handler handler){
		mContext=context;
		mMainHandler = handler;
		mDateFormat=DateFormat.getDateFormat(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.drawer_profile_layout, null);
        }
		settingProfileLayout(convertView);
		return convertView;
	}
	
	private void settingProfileLayout(View convertView){
		DrawerUserHeadImageView image=(DrawerUserHeadImageView)convertView.findViewById(R.id.profile_photo);
		TextView name=(TextView)convertView.findViewById(R.id.tv_name);
		TextView gender=(TextView)convertView.findViewById(R.id.tv_gender);
		TextView weight=(TextView)convertView.findViewById(R.id.tv_weight);
		TextView age=(TextView)convertView.findViewById(R.id.tv_age);
		TextView height=(TextView)convertView.findViewById(R.id.tv_height);
		TextView start_measure_date=(TextView)convertView.findViewById(R.id.start_measure_date);
		TextView completion=(TextView)convertView.findViewById(R.id.completion);
		TextView total_steps=(TextView)convertView.findViewById(R.id.total_steps);
		TextView last_updated=(TextView)convertView.findViewById(R.id.last_updated);

        ProfileData profile=new ParseDataManager().getProfileData(mContext);
		//fix 538147 smile_gao 215.7.17
		if(profile == null)
		{
			return;
		}
		if(profile.photo_path==null || profile.photo_path.contains("com.google.android.apps.photos.content")){
			image.setImageResource(R.drawable.asus_wellness_photo_people_80);	
		}
		else{
			Bitmap bmp = Utility.getPhotoBitmap(mContext,profile.photo_path);
			if(bmp !=null)
			{
				image.setImageBitmap(bmp);
			}
		}
		image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.i("smile","image onclick");
				mMainHandler.sendMessage(Message.obtain(null, SetupProfileActivity.REQUEST_CODE_PICK_IMAGE));
			}
		});
		image.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				Log.i("smile","image long onclick");
				mMainHandler.sendMessage(Message.obtain(null, SetupProfileActivity.REQUEST_CODE_TAKE_PHOTO));
				return true;
			}
		});
		name.setText(profile.name);
		gender.setText(mContext.getResources().getStringArray(R.array.profile_gender_option)[profile.gender]);

        //emily ++++
        // age.setText(String.valueOf(profile.age));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal=Calendar.getInstance();
        if(profile.birthday == 0){
            int ages = profile.age;
            cal.add(Calendar.YEAR, -ages);
            cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
        }
        else {
            cal.setTimeInMillis(profile.birthday);
        }
        age.setText(formatter.format(cal.getTime()));
        //emily ----

        weight.setText(profile.weight+" "+mContext.getResources().getStringArray(R.array.profile_weight_option)[profile.weightUnit]);

        if(profile.heightUnit==ProfileTable.HEIGHT_UNIT_FT){
            int feet=(int) Utility.InchToFt(profile.height);
            int inch=(int) Math.round(profile.height-Utility.ftToInch(feet));
            if(inch == 12){
                inch = 0;
                ++feet;
            }
			height.setText(feet+"' "+inch+"\" "  +mContext.getResources().getStringArray(R.array.profile_height_option)[profile.heightUnit]);
        }
		else{
			height.setText(profile.height+" "+mContext.getResources().getStringArray(R.array.profile_height_option)[profile.heightUnit]);	
		}
		start_measure_date.setText(Utility.getDateTime(profile.start_time, mDateFormat));
		last_updated.setText(Utility.getLastUpdateTime(mContext, profile.start_time,mDateFormat));
		new LoadStepTask(completion, total_steps, profile.start_time).execute();
	}
	
	private int getTotalStepGoal(long startMeasureTimeMilli){
		long time=System.currentTimeMillis();
		int totalStepGoal=0;
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(startMeasureTimeMilli);
		cal.set(cal.get(Calendar.YEAR), cal.get(cal.MONTH), cal.get(cal.DATE), 0, 0, 0);
		while(time>cal.getTimeInMillis()){
			Cursor activityGoalCursor=mContext.getContentResolver().query(StepGoalTable.TABLE_URI, new String[]{StepGoalTable.COLUMN_STEP_GOAL}
						, StepGoalTable.COLUMN_DATE_TIME_MILLI+"<=?", new String[]{Utility.getDateTime(time, "yyyy-MM-dd")}, StepGoalTable.COLUMN_DATE_TIME_MILLI+" DESC");
			if(activityGoalCursor.moveToFirst()){
				int goal=activityGoalCursor.getInt(activityGoalCursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
				totalStepGoal+=goal;
			}
			else{
				totalStepGoal+=SettingStepGoalActivity.DEFAULT_STEP_GOAL;
			}
			activityGoalCursor.close();
			time-=86400000;
		}
		return totalStepGoal;
	}
	
	private int getTotalSteps(){
		int totalSteps=0;
		Cursor stepGoalCursor=mContext.getContentResolver().query(ActivityStateTable.TABLE_URI, null, null, null, null);
		if(stepGoalCursor.moveToFirst()){
			do{
				totalSteps+=stepGoalCursor.getInt(stepGoalCursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
			}while(stepGoalCursor.moveToNext());
		}
		stepGoalCursor.close();
		return totalSteps;
	}
	
	private class LoadStepTask extends AsyncTask<Void, Void ,int []>{
		
		private TextView mTvCompletion;
		private TextView mTvTotalSteps;
		private long mStartTime;
		
		public LoadStepTask(TextView tv_completion, TextView tv_total_steps, long startTime){
			mTvCompletion=tv_completion;
			mTvTotalSteps=tv_total_steps;
			mStartTime=startTime;
		}

		@Override
		protected int[] doInBackground(Void... params) {
			// TODO Auto-generated method stub
			int [] data=new int[2];
			data[0]=getTotalSteps();
			data[1]=getTotalStepGoal(mStartTime);
			return data;
		}

		@Override
		protected void onPostExecute(int [] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mTvTotalSteps.setText(Utility.commaNumber(result[0]));
			
			float percentageOfComplete=(float)result[0]/result[1];
			if(percentageOfComplete>=1){
				percentageOfComplete=1;
			}
			mTvCompletion.setText((int)(percentageOfComplete*100)+"%");
		}
	}
}

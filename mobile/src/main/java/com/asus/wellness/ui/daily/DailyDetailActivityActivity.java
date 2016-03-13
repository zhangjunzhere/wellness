package com.asus.wellness.ui.daily;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asus.sharedata.SleepTimeSpan;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.BikeInfo;
import com.asus.wellness.ParseDataManager.CarInfo;
import com.asus.wellness.ParseDataManager.Day;
import com.asus.wellness.ParseDataManager.HeartRateInfo;
import com.asus.wellness.ParseDataManager.ProfileData;
import com.asus.wellness.ParseDataManager.StepInfo;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Coach;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.CoachTable;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.provider.StepGoalTable;
import com.asus.wellness.ui.BaseActivity;
import com.asus.wellness.ui.WorkoutAdapter;
import com.asus.wellness.ui.setting.SettingStepGoalActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.TimeSpanItem;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.asus.wellness.ParseDataManager.WorkoutInfo;

import java.util.ArrayList;
import java.util.Calendar;

public class DailyDetailActivityActivity extends BaseActivity implements View.OnClickListener{
	
	private Day day;
	
	ParseDataManager parseDM;
	ArrayList<StepInfo> mArrayStepInfo;
	ArrayList<CarInfo> mArrayCarInfo;
	ArrayList<BikeInfo> mArrayBikeInfo;
	ArrayList<HeartRateInfo> mArrayHrInfo;
	ArrayList<WorkoutInfo> mPushupSitupInfo;
	ArrayList<WorkoutInfo> mArrayRunInfo;

	private ListView mRunListView;
	private ListView mPushupSitupListView;
	private WorkoutAdapter mRunAdapter;
	private WorkoutAdapter mPushupSitupAdapter;
	//private View mActivityGoalView;
	private ImageButton mGotoSettingGoalBtn;
	
	public static final String KEY_DAY_INFO="key_day_info";

	@Override
	public  String getPageName(){
		return  DailyDetailActivityActivity.class.getSimpleName();
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daily_detail_activity);
		day=(Day) getIntent().getSerializableExtra(KEY_DAY_INFO);
		parseDM= ParseDataManager.getInstance();
		setActionBar();
		/*getDataInfo();
		setDetailView();*/

		Utility.trackerScreennView(getApplicationContext(), "Goto BODY");
	}

	@Override
	public void onResume(){
		super.onResume();
		getDataInfo();
		setDetailView();
	}

	
	public void getDataInfo(){
		mArrayStepInfo=parseDM.getDayStepInfo(this, day);
		mArrayCarInfo=parseDM.getDayCarInfo(this, day);
		mArrayBikeInfo=parseDM.getDayBikeInfo(this, day);
		mArrayHrInfo=parseDM.getDayHeartRateInfo(this, day);
		mPushupSitupInfo = parseDM.getDayWorkoutInfo(this, day.startTimeMilli, day.endTimeMilli);
		mArrayRunInfo = parseDM.getDayRunInfo(this, day);
	}
	
	private void setActionBar(){
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
		int actionbarTitleId=getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView)findViewById(actionbarTitleId);
		if(abTitle != null) abTitle.setTextColor(0xff4c4c4c);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
			case android.R.id.home: 			
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setDetailView(){
		TextView stepIntensity=(TextView)findViewById(R.id.step_intensity);
		TextView stepGoal=(TextView)findViewById(R.id.step_goal);
		
		int goal=getStepGoal();
		String intensity = Utility.getIntensityString(this, goal);
		stepIntensity.setText(intensity);
		stepGoal.setText(Utility.formatNumber(goal));
		/*if(isSw800){
			TextView stepIntensityNondata=(TextView)findViewById(R.id.step_intensity_nondata);
			if(stepIntensityNondata!=null){
				stepIntensityNondata.setText(Utility.getIntensityString(this, goal));
			}
			TextView text=(TextView)findViewById(R.id.step_goal_sw800_nondata);
			if(text!=null){
				text.setText(Utility.commaNumber(goal));
			}
		}*/
		
		if(mArrayStepInfo==null && mArrayCarInfo==null && mArrayBikeInfo==null && mArrayHrInfo==null && mPushupSitupInfo==null && mArrayRunInfo==null){
			findViewById(R.id.has_data_container).setVisibility(View.GONE);
			//findViewById(R.id.nodata_step_goal_title).setVisibility(View.VISIBLE);

			TextView stepIntensity_nodata=(TextView)findViewById(R.id.nondata_step_intensity);
			TextView stepGoal_nodata=(TextView)findViewById(R.id.nodata_step_goal);

			stepIntensity_nodata.setText(intensity);
			stepGoal_nodata.setText(Utility.commaNumber(goal));

			mGotoSettingGoalBtn =(ImageButton)findViewById(R.id.goto_setting_btn_nodata);
			mGotoSettingGoalBtn.setClickable(true);
			mGotoSettingGoalBtn.setOnClickListener(this);
			mGotoSettingGoalBtn.setVisibility(isToday() ? View.VISIBLE: View.GONE);
		}
		else{
			//findViewById(R.id.nodata_step_goal_title).setVisibility(View.GONE);
			findViewById(R.id.no_data_container).setVisibility(View.GONE);
			TextView activityTime=(TextView)findViewById(R.id.tv_activity_time);
			TextView completion=(TextView)findViewById(R.id.tv_completion_percentage);
			TimeSpanItem timeSpan = getActivityTimeInMins();
			activityTime.setText(Utility.getHourMinStr(getApplicationContext(), timeSpan, false));

			setStepCalorDistance();
			/*totalStep.setText(Utility.commaNumber(getTotalSteps()));
			totalcalor.setText(Utility.commaNumber(getCalories()));*/

			int percentage=(int) ((float)getTotalSteps()/getStepGoal()*100);
			/*if(percentage>=100){
				percentage=100;
			}*/
			completion.setText(String.valueOf(percentage)+"%");
			//setWorkoutCompletion();
			
			addHeartRateIntensityLayout();
			
			setActivityInfo();
			setRunInfo();
			setPushupSitupInfo();
		}
	}
	private String getHourMinStr(TimeSpanItem sts)
	{
		String hourstr = " "+getString(R.string.detail_sleep_shorthour);
		String minstr = " "+getString(R.string.detail_sleep_shortmin);
		String ret="";
		if(sts.getHour()>0)
		{
			ret+=sts.getHour()+hourstr;
		}
		ret+=sts.getMinute()+minstr;
		return ret;
	}
	
	private int getStepGoal(){
		int goal= SettingStepGoalActivity.DEFAULT_STEP_GOAL;
		Cursor cursor=getContentResolver().query(StepGoalTable.TABLE_URI, null, StepGoalTable.COLUMN_DATE_TIME_MILLI+"<=?"
				, new String[]{Utility.getDateTime(day.startTimeMilli, "yyyy-MM-dd")}, StepGoalTable.COLUMN_DATE_TIME_MILLI+" DESC");
		if(cursor.moveToFirst()){
			goal=cursor.getInt(cursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
		}
		cursor.close();
		return goal;
	}
//	private int getActivityTimeInMins(){
//
//		long time=0;
//		if(mArrayStepInfo!=null){
//			for(StepInfo stepInfo:mArrayStepInfo){
//				time+=(stepInfo.endTime-stepInfo.startTime);
//			}
//		}
//
//		if(mArrayCarInfo!=null){
//			for(CarInfo carInfo:mArrayCarInfo){
//				time+=(carInfo.endTime-carInfo.startTime);
//			}
//		}
//
//		if(mArrayBikeInfo!=null){
//			for(BikeInfo bikeInfo:mArrayBikeInfo){
//				time+=(bikeInfo.endTime-bikeInfo.startTime);
//			}
//		}
//		return (int) ((float)time/1000/60);
//	}
	private TimeSpanItem getActivityTimeInMins(){
		TimeSpanItem timeall = new TimeSpanItem();
		if(mArrayStepInfo!=null){
			for(StepInfo stepInfo:mArrayStepInfo){
				//time+=(stepInfo.endTime-stepInfo.startTime);
				timeall.addTime(TimeSpanItem.creatTimeItem(stepInfo.startTime, stepInfo.endTime));
			}
		}

		if(mArrayCarInfo!=null){
			for(CarInfo carInfo:mArrayCarInfo){
				//time+=(carInfo.endTime-carInfo.startTime);
				timeall.addTime(TimeSpanItem.creatTimeItem(carInfo.startTime, carInfo.endTime));
			}
		}

		if(mArrayBikeInfo!=null){
			for(BikeInfo bikeInfo:mArrayBikeInfo){
				//time+=(bikeInfo.endTime-bikeInfo.startTime);
				timeall.addTime(TimeSpanItem.creatTimeItem(bikeInfo.startTime, bikeInfo.endTime));
			}
		}
	//	return (int) ((float)time/1000/60);
		long total = timeall.getTotalInMinutes();
		return  Utility.getTimeSpan(total);
		//return  timeall.getTotalInMinutes();
	}

	public int getTotalSteps(){
		int steps=0;
		if(mArrayStepInfo!=null){
			for(StepInfo stepInfo:mArrayStepInfo){
				steps+=stepInfo.stepCount;
			}
		}
		return steps;
	}

	public void setStepCalorDistance(){
		TextView totalStep=(TextView)findViewById(R.id.tv_total_step);
		TextView totalcalor=(TextView)findViewById(R.id.tv_total_calories);
		//TextView totalDistance=(TextView)findViewById(R.id.tv_total_distance);

		int steps=0, bikeCalories=0, stepCalories=0, walkDistance=0;
		Profile pfd=parseDM.getStandardProfile();
		if(mArrayStepInfo!=null){
			for(StepInfo stepInfo:mArrayStepInfo){
				steps+=stepInfo.stepCount;
			}

			int heightInCM=pfd.getHeight();
			if(pfd.getHeight_unit()==ProfileTable.HEIGHT_UNIT_FT){
				float ft=Utility.InchToFt(heightInCM);
				heightInCM=(int) Math.round(Utility.ftToCm(ft));
			}
			int weightInKG=pfd.getWeight();
			if(pfd.getWeight_unit()==ProfileTable.WEIGHT_UNIT_LBS){
				weightInKG=(int) Math.round(Utility.LbsToKg(pfd.getWeight()));
			}
			walkDistance=Utility.getWalkDistanceInCM(heightInCM, steps)/100;
			stepCalories= (int)Utility.getWalkCalories(heightInCM, steps, weightInKG);
		}

		totalStep.setText(Utility.formatNumber(steps));
		totalcalor.setText(Utility.formatNumber(bikeCalories + stepCalories));
		//totalDistance.setText(Utility.getTwoDigitFloatString((float) walkDistance / 1000));
	}

	public int getCalories(){
		int bikeCalories=0, stepCalories=0, walkDistance=0;
		Profile pfd=parseDM.getStandardProfile();

		if(mArrayBikeInfo!=null){
//			float bikeHour=0;
//			for(BikeInfo bikeInfo:mArrayBikeInfo){
//				bikeHour+=((float)(bikeInfo.endTime-bikeInfo.startTime)/1000/60/60);
//			}
//			bikeCalories=Utility.getBikeCalories(bikeHour, pfd.weight);
		}
		if(mArrayStepInfo!=null){
			int steps=0;
			for(StepInfo stepInfo:mArrayStepInfo){
				steps+=stepInfo.stepCount;
			}
            int heightInCM=pfd.getHeight();
            if(pfd.getHeight_unit()==ProfileTable.HEIGHT_UNIT_FT){
                float ft=Utility.InchToFt(heightInCM);
                heightInCM=(int) Math.round(Utility.ftToCm(ft));
            }
            int weightInKG=pfd.getWeight();
            if(pfd.getWeight_unit()==ProfileTable.WEIGHT_UNIT_LBS){
                weightInKG=(int) Math.round(Utility.LbsToKg(pfd.getWeight()));
            }
			walkDistance=Utility.getWalkDistanceInCM(heightInCM, steps)/100;
			stepCalories= (int)Utility.getWalkCalories(heightInCM, steps, weightInKG);
		}
		return (bikeCalories+stepCalories);
	}
	
	public void addHeartRateIntensityLayout(){
		if(!WApplication.getInstance().getConnectedDevice().getIsRobin() && Utility.isPadDevice(this))
		{
			findViewById(R.id.heart_intensity_detail_view).setVisibility(View.GONE);
			findViewById(R.id.no_data_heart_rate_exercise).setVisibility(View.GONE);
			return;
		}

		if(mArrayHrInfo!=null){
			View viewNodata = findViewById(R.id.no_data_heart_rate_exercise);
			View viewHeartRate  = findViewById(R.id.heart_rate_and_exercise_scroll_view);
			if(viewNodata!=null){
				viewNodata.setVisibility(View.GONE);
			}
			if(viewNodata!=null){
				viewHeartRate.setVisibility(View.VISIBLE);
			}
			findViewById(R.id.heart_rate_and_exercise_intensity_container).setVisibility(View.VISIBLE);
			TimeLineRelativeLayout timeline=(TimeLineRelativeLayout)findViewById(R.id.heart_rate_timeline);
			timeline.setNumDots(mArrayHrInfo.size());
			timeline.setLineLength(getResources().getDimensionPixelSize(R.dimen.daily_detail_heart_rate_timeline_length));

            View time_text=getLayoutInflater().inflate(R.layout.measure_time_text, null);
            int leftMargin = time_text.getRight();

			timeline.setTimeLineLeftMargin(getResources().getDimensionPixelSize(R.dimen.daily_detail_heart_rate_timeline_left_margin));
			timeline.setTimeLineTopMargin(getResources().getDimensionPixelSize(R.dimen.daily_detail_heart_rate_timeline_top_margin));
			LayoutParams params=(LayoutParams) timeline.getLayoutParams();
			params.height=timeline.getRequiredHeight();
			timeline.setLayoutParams(params);
            int timeLineLeftMargin_max = 0;
            int timeLineLeftMargin = 0;
			for(int i=0;i<mArrayHrInfo.size();i++){
                timeLineLeftMargin = testTimelineLeftMargin(mArrayHrInfo.get(i).measureTime);
                if(timeLineLeftMargin_max < timeLineLeftMargin) {
                    timeLineLeftMargin_max = timeLineLeftMargin;
                };
			}
            timeline.setTimeLineLeftMargin(timeLineLeftMargin);
            for(int i=0;i<mArrayHrInfo.size();i++){
                setTimeItem(timeline, i, mArrayHrInfo.get(i).measureTime);
                setHeartRateItem(timeline, i, mArrayHrInfo.get(i).measureValue);
            }
		}
	}

	public void setActivityInfo(){
		TextView tvWalkTime=(TextView)findViewById(R.id.walk_time);
//		TextView tvCarTime=(TextView)findViewById(R.id.car_time);
//		TextView tvBikeTime=(TextView)findViewById(R.id.bike_time);
		
		TextView tvWalkCal=(TextView)findViewById(R.id.walk_cal);
//		TextView tvCarCal=(TextView)findViewById(R.id.car_cal);
//		TextView tvBikeCal=(TextView)findViewById(R.id.bike_cal);
		
		TextView tvWalkSteps=(TextView)findViewById(R.id.walk_step);
/*      TextView tvCarDistance=(TextView)findViewById(R.id.car_distance);
        TextView tvBikeDistance=(TextView)findViewById(R.id.bike_distance);
        TextView tvIncludeBikeStep=(TextView)findViewById(R.id.bike_distance_step);*/

		int walkTime = 0, carTime=0, bikeTime=0, walkCal=0, carCal=0, bikeCal=0, walkDistance=0, carDistance=0, bikeDistance=0;
		ProfileData pfd=parseDM.getProfileData(this);

		int stepCounts=0;
        if(mArrayStepInfo!=null){
			TimeSpanItem timeall = new TimeSpanItem();
			for(StepInfo stepInfo:mArrayStepInfo){
				timeall.addTime(TimeSpanItem.creatTimeItem(stepInfo.startTime, stepInfo.endTime));
				//walkTime+=(stepInfo.endTime-stepInfo.startTime);
				stepCounts+=stepInfo.stepCount;
			}

            int heightInCM=pfd.height;
            if(pfd.heightUnit==ProfileTable.HEIGHT_UNIT_FT){
                float ft=Utility.InchToFt(heightInCM);
                heightInCM=(int) Math.round(Utility.ftToCm(ft));
            }
            int weightInKG=pfd.weight;
            if(pfd.weightUnit==ProfileTable.WEIGHT_UNIT_LBS){
                weightInKG=(int) Math.round(Utility.LbsToKg(pfd.weight));
            }
			//walkDistance=Utility.getWalkDistanceInCM(heightInCM, stepCounts)/100;
			walkCal=(int)Utility.getWalkCalories(heightInCM, stepCounts, weightInKG);
		//	walkTime=(walkTime/1000/60);
			walkTime = timeall.getTotalInMinutes();
		}
		
		if(mArrayCarInfo!=null){
			for(CarInfo carInfo:mArrayCarInfo){
				carTime+=(carInfo.endTime-carInfo.startTime);
				carDistance+=carInfo.distanceInMeter;
			}
			carTime=(carTime/1000/60);
		}
		
		if(mArrayBikeInfo!=null){
			for(BikeInfo bikeInfo:mArrayBikeInfo){
				bikeTime+=(bikeInfo.endTime-bikeInfo.startTime);
				bikeDistance+=bikeInfo.distanceInMeter;
			}
			float bikeHour=(float)bikeTime/1000/60/60;
			bikeTime=(bikeTime/1000/60);
            int weightInKG=pfd.weight;
            if(pfd.weightUnit==ProfileTable.WEIGHT_UNIT_LBS){
                weightInKG=(int) Math.round(Utility.LbsToKg(pfd.weight));
            }
			bikeCal=Utility.getBikeCalories(bikeHour, weightInKG);
		}
		
		tvWalkTime.setText(Utility.commaNumber(walkTime)+" "+getString(R.string.time_unit));
//		tvCarTime.setText(carTime+" "+getString(R.string.time_unit));
//		tvBikeTime.setText(Utility.commaNumber(bikeTime+carTime)+" "+getString(R.string.time_unit));
		
		tvWalkCal.setText(Utility.commaNumber(walkCal)+" "+getString(R.string.calories_unit));
//		tvBikeCal.setText(bikeCal+" "+getString(R.string.calories_unit));
//		tvCarCal.setText(carCal+" "+getString(R.string.calories_unit));
		

		tvWalkSteps.setText(String.valueOf(stepCounts)+" " + getString(R.string.daily_info_walk_unit));
		/*float transportDistance=((float)carDistance/1000)+((float)bikeDistance/1000);
		vWalkDistance.setText(Utility.getOneDigitFloatString((float)walkDistance/1000)+" "+getString(R.string.distance_unit));
		tvBikeDistance.setText(Utility.getOneDigitFloatString(transportDistance)+" "+getString(R.string.distance_unit));
		tvCarDistance.setText(Utility.getOneDigitFloatString((float)carDistance/1000)+" "+getString(R.string.distance_unit));*/
		
		tvWalkCal.setSelected(true);

		mGotoSettingGoalBtn =(ImageButton)findViewById(R.id.goto_setting_btn);
		mGotoSettingGoalBtn.setClickable(true);
		mGotoSettingGoalBtn.setOnClickListener(this);
		mGotoSettingGoalBtn.setVisibility(isToday() ? View.VISIBLE: View.GONE);

	}

	@Override
	public void onClick(View view){
		if(isToday()){
			if(view.getId() == R.id.goto_setting_btn || view.getId() == R.id.goto_setting_btn_nodata){
				Intent intent = new Intent(this,SettingStepGoalActivity.class);
				startActivity(intent);
			}
		}
	}

	private boolean isToday(){
		Calendar calendar = Calendar.getInstance();
		if(day.year==calendar.get(Calendar.YEAR) && (day.month-1)==calendar.get(Calendar.MONTH) && day.day==calendar.get(Calendar.DAY_OF_MONTH)){
			return true;
		}
		return  false;
	}

	public void setRunInfo(){
		if(mArrayRunInfo == null){
			findViewById(R.id.nodata_run).setVisibility(View.VISIBLE);
			return;
		}
		findViewById(R.id.nodata_run).setVisibility(View.GONE);
		mRunListView = (ListView)findViewById(R.id.run_list);
		mRunAdapter = new WorkoutAdapter(getApplication(),mArrayRunInfo);
		mRunListView.setAdapter(mRunAdapter);
		fixListViewHeight(mRunListView);
	}

	public void setPushupSitupInfo(){
		if(WApplication.getInstance().getConnectedDevice().getIsRobin())
		{
			findViewById(R.id.workout_title_2).setVisibility(View.GONE);
			findViewById(R.id.pushup_situp_list).setVisibility(View.GONE);
			findViewById(R.id.pushup_container).setVisibility(View.GONE);
			findViewById(R.id.situp_container).setVisibility(View.GONE);
			return;
		}

		findViewById(R.id.workout_title_2).setVisibility(View.VISIBLE);
		if (mPushupSitupInfo==null){
			findViewById(R.id.pushup_container).setVisibility(View.VISIBLE);
			findViewById(R.id.situp_container).setVisibility(View.VISIBLE);
			return;
		}

		findViewById(R.id.pushup_container).setVisibility(View.GONE);
		findViewById(R.id.situp_container).setVisibility(View.GONE);
		findViewById(R.id.pushup_situp_list).setVisibility(View.VISIBLE);
		mPushupSitupListView = (ListView)findViewById(R.id.pushup_situp_list);
		mPushupSitupListView.setFocusable(true);
		mPushupSitupAdapter = new WorkoutAdapter(getApplication(),mPushupSitupInfo);
		mPushupSitupListView.setAdapter(mPushupSitupAdapter);
		fixListViewHeight(mPushupSitupListView);
	}

	private void fixListViewHeight(ListView listView){
		WorkoutAdapter adapter = (WorkoutAdapter)listView.getAdapter();
		int totalHeight = 0;
		for (int index = 0; index<adapter.getCount(); index++){
			View itemView = adapter.getView(index, null, listView);
			itemView.measure(0,0);
			totalHeight += itemView.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		listView.getDividerHeight();
		params.height = totalHeight + (listView.getDividerHeight()*(adapter.getCount()-1));
		listView.setLayoutParams(params);
	}

    private int testTimelineLeftMargin(long time) {
        View time_text=getLayoutInflater().inflate(R.layout.measure_time_text, null);
        TextView timeText=(TextView)time_text.findViewById(R.id.measure_time);
        timeText.setText(Utility.getDateTime(time, "hh:mm a"));
        //get measured width and height
        time_text.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));

        return time_text.getMeasuredWidth()+25;
    }

	private void setTimeItem(TimeLineRelativeLayout timeLineView, int index, long time){
		View time_text=getLayoutInflater().inflate(R.layout.measure_time_text, null);
		TextView timeText=(TextView)time_text.findViewById(R.id.measure_time);
		timeText.setText(Utility.getDateTime(time, "hh:mm a"));
		
		//get measured width and height
        time_text.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
				MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));

		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin=timeLineView.getDotsPosition(index).x-time_text.getMeasuredWidth()-25;
        params.topMargin=timeLineView.getDotsPosition(index).y-time_text.getMeasuredHeight()/2;
		time_text.setLayoutParams(params);

		timeLineView.addView(time_text);
	}

	private void setHeartRateItem(TimeLineRelativeLayout timeLineView, int index, int measureValue){
		View heartRateItem=getLayoutInflater().inflate(R.layout.daily_detail_heart_rate_layout_item, null);
		TextView heart_rate_bpm=(TextView)heartRateItem.findViewById(R.id.heart_rate_bpm);
		TextView heart_rate_intensity=(TextView)heartRateItem.findViewById(R.id.heart_intensity_level);
		heart_rate_bpm.setText(String.valueOf(measureValue));
		heart_rate_intensity.setText(Utility.getIntensityLevel(this, measureValue));
        
		if(Utility.getSizeDimens(this)==Utility.SIZEDIMENS.SW600DP){
			   TextView heart_rate_level=(TextView)heartRateItem.findViewById(R.id.heart_rate_level);
			   heart_rate_level.setText(Utility.getIntensityLevelText(this, measureValue));
	    }else if(Utility.getSizeDimens(this)==Utility.SIZEDIMENS.SW800DP){
               TextView heart_rate_level=(TextView)heartRateItem.findViewById(R.id.heart_rate_level_800dp);
               heart_rate_level.setText(Utility.getIntensityLevelText(this, measureValue));	    
	    }
		
		heartRateItem.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
					MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
			
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.leftMargin=timeLineView.getDotsPosition(index).x+25;
		params.topMargin=timeLineView.getDotsPosition(index).y-heartRateItem.getMeasuredHeight()/2;
		heartRateItem.setLayoutParams(params);
		
		timeLineView.addView(heartRateItem);
	}

	private void setWorkoutCompletion(){
		TextView workoutCompletion = (TextView)findViewById(R.id.tv_workout_completion);
		String result = parseDM.getWorkoutCompletion(this, day);
		workoutCompletion.setText(result);
	}
}

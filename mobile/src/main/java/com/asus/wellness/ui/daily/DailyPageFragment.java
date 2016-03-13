package com.asus.wellness.ui.daily;

import java.util.ArrayList;

import com.asus.wellness.ActivityInfo;
import com.asus.wellness.EcgInfo;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.Day;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.WellnessLocationManager;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.EcgTable;
import com.asus.wellness.ui.BaseActivity;
import com.asus.wellness.ui.daily.NewTimelineView.TimeItem;
import com.asus.wellness.utils.Arith;
import com.asus.wellness.utils.LocationHelper;
import com.asus.wellness.utils.TimeSpanItem;
import com.asus.wellness.utils.Utility;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class DailyPageFragment extends Fragment{

	public static final String ONEDAY_TIME_MILLI="oneday_time_milli";
	public Day day;
	public ParseDataManager pfd;
	public String mLastDistrict=WellnessLocationManager.DEFAULT_DISTRICT;
	private boolean isChangeDistrict=true;
	private ArrayList<Integer> listLineColor=new ArrayList<Integer>();
	private Context mContext;
	private LayoutInflater mInflater;
	private AsyncTask loadTimelineTask;
    private String timeItem_template;
	private long mNowTime = 0;
	public long getNowTime()
	{
		return mNowTime;
	}
	public void setNowTime(long time)
	{
		 mNowTime = time;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		pfd= ParseDataManager.getInstance();
		mContext=getActivity();
		mInflater=inflater;
        View rootView = inflater.inflate(R.layout.wellness_info_daily_layout, container, false);
        
        Bundle args = getArguments();
        long time=System.currentTimeMillis();
        if(args!=null){
        	time=args.getLong(ONEDAY_TIME_MILLI);
        }
		Log.i("smile","DailyPageFragment oncreateview ");
		setDayInfo(rootView, time);
		
        return rootView;
	}

	private void setDayInfo(View rootView, long milliTime){
		day=pfd.getDay(milliTime);
		ArrayList<EcgInfo> ecgInfoArray=new ArrayList<EcgInfo>();
		ArrayList<ActivityInfo> activityInfoArray=new ArrayList<ActivityInfo>();
		EcgInfo sleepDayInfo = new EcgInfo();
		ParseDataManager parseDataManager = ParseDataManager.getInstance();;
		if(WApplication.getInstance().getConnectedDevice().getIsRobin())
		{
			ecgInfoArray = parseDataManager.getEcgInfoFromDb(getActivity(), day);
		}
		else{
			sleepDayInfo = parseDataManager.getDaySleepDataFromEcg(day.startTimeMilli);
		}
		activityInfoArray = parseDataManager.getActivityInfoFromDb(getActivity(), day);

		ArrayList<ActivityPoint> point=dividePoint(activityInfoArray);
		ArrayList<Object> sortedState=sortState(ecgInfoArray, point, sleepDayInfo);
		LinearLayout timelineContainer=(LinearLayout) rootView.findViewById(R.id.timeline_container);
        if(isSw800()){
        	rootView.findViewById(R.id.top_separate_line).setVisibility(View.GONE);
        }
		loadTimelineTask=new LoadTimelineTask(timelineContainer).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,sortedState);
		//loadTimelineTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,sortedState);
		   //execute(sortedState);
	}
	
	private boolean isSw800(){
		if(getActivity() instanceof BaseActivity){
			BaseActivity base=(BaseActivity) getActivity();
			return base.isSw800;
		}
		return false;
	}
	

	
	private ArrayList<ActivityPoint> dividePoint(ArrayList<ActivityInfo> activityInfoArray){
		ArrayList<ActivityPoint> activityPointList=new ArrayList<ActivityPoint>();
		for(ActivityInfo info:activityInfoArray){
            //try to fix bug tt 587383, step =-1
            if(info.stepCounts < 0){
                continue;
            }

			ActivityPoint pointStart=new ActivityPoint();
			pointStart.time=info.startTime;
			pointStart.startTime=info.startTime;
			pointStart.endTime=info.endTime;
			pointStart.activityType=info.activityType;
			pointStart.distance=info.distance;
			pointStart.isStart=true;
			pointStart.stepCounts=info.stepCounts;

			activityPointList.add(pointStart);
			
			ActivityPoint pointEnd=new ActivityPoint();
			pointEnd.time=info.endTime;
			pointEnd.isStart=false;
			activityPointList.add(pointEnd);
		}
		return activityPointList;
	}
	
	private void addNewTimeline(LinearLayout timelineContainer, ArrayList<ArrayList<TimeItem>> list){
		NewTimelineView newTimeline=new NewTimelineView(mContext);
		LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, 50);//Let onDraw() method is called.Setting real height is in the onDraw() method.
		newTimeline.setLayoutParams(params);
//		int hour=15*60*1000;
//		long time=System.currentTimeMillis();
//		ArrayList<ArrayList<TimeItem>> list=new ArrayList<ArrayList<TimeItem>>();
//		for(int i=0;i<10;i++){
//			time+=hour;
//			ArrayList<TimeItem> timeItem=new ArrayList<TimeItem>();
//			TimeItem t=new TimeItem();
//				t.view=getActivity().getLayoutInflater().inflate(R.layout.daily_info_heart_rate_layout, null);	
//				if(i%2==0){
//					t.district="aaaa";	
//				}
//				else{
//					t.district="aaab";	
//				}
//				t.view.setTag(NewTimelineView.TAG_ECG);
//			t.timeString=Utility.getDateTime(time, "hh:mm a");
//			timeItem.add(t);
//			if(i%2==1){
//				TimeItem t2=new TimeItem();
//				t2.view=getActivity().getLayoutInflater().inflate(R.layout.daily_info_stress_energy_layout, null);	
//				t2.view.setTag(NewTimelineView.TAG_ECG);
//				t2.district="aaaa";
//				timeItem.add(t2);
//				
//					TimeItem t1=new TimeItem();
//					t1.view=getActivity().getLayoutInflater().inflate(R.layout.daily_info_car_layout, null);	
//					t1.view.setTag(NewTimelineView.TAG_ACTIVTY_START);
//					t1.district="aaaa";
//					timeItem.add(t1);
//			}
////			else{
////				for(int j=0;j<3;j++){
////					TimeItem t1=newTimeline.new TimeItem();
////					t1.view=getActivity().getLayoutInflater().inflate(R.layout.daily_info_heart_rate_layout, null);	
////					t1.view.setTag(NewTimelineView.TAG_ECG);
////					t1.district="aaaa";
////					timeItem.add(t1);	
////				}
////			}
//			list.add(timeItem);
//		}
		Log.d("circle","new timeline size:"+list.size());
		newTimeline.setDataItem(list);
		timelineContainer.addView(newTimeline);
	}
	private String getDistrict(long starttime,long endtime){
		String district = LocationHelper.getInstance(getActivity()).getDistrict(starttime,endtime);
//		String district=WellnessLocationManager.DEFAULT_DISTRICT;
//		Cursor cursor=mContext.getContentResolver().query(LocationChangeTable.TABLE_URI, null
//				, LocationChangeTable.COLUMN_GET_LOCATION_TIME+"<=? and "+LocationChangeTable.COLUMN_DISTRICT+"!=?"
//				, new String[]{String.valueOf(time), WellnessLocationManager.DEFAULT_DISTRICT}
//				, LocationChangeTable.COLUMN_GET_LOCATION_TIME+" DESC");
//		if(cursor.moveToFirst()){
//			district=cursor.getString(cursor.getColumnIndex(LocationChangeTable.COLUMN_DISTRICT));
//		}
//		cursor.close();
		return district;
	}
	
	public void cancelLoadTimelineTask(){
		if(loadTimelineTask!=null){
			loadTimelineTask.cancel(true);	
		}
	}
	
	private class LoadTimelineTask extends AsyncTask<ArrayList<Object>, Void, ArrayList<ArrayList<TimeItem>>>{
		
		private LinearLayout mContainer;
		ProgressBar progressLoading=new ProgressBar(mContext);
		
		public LoadTimelineTask(LinearLayout container){
			mContainer=container;
            timeItem_template = getActivity().getString(R.string.time_template);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.gravity=Gravity.CENTER_HORIZONTAL;
			progressLoading.setLayoutParams(params);
			mContainer.addView(progressLoading);
		}

		@Override
		protected ArrayList<ArrayList<TimeItem>> doInBackground(ArrayList<Object>... params) {
			// TODO Auto-generated method stub
			return mergeSameTimePoint(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<ArrayList<TimeItem>> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mContainer.removeView(progressLoading);
			addNewTimeline(mContainer, result);
		}
	}
	

	
	private class ActivityPoint{
		public long startTime;
		public long endTime;
		public int stepCounts;
		public int activityType;
		public int distance;
		public long time;
		public boolean isStart=false;
	}
	
	private ArrayList<Object> sortState(ArrayList<EcgInfo> array_ecgInfo, ArrayList<ActivityPoint> array_activityInfo,EcgInfo sleepDayInfo){
		//Log.i("emily","sleepDayInfo = " + sleepDayInfo.measureType + ", " + sleepDayInfo.deepSeconds/(60*60));
		ArrayList<Object> sortedState=new ArrayList<Object>();

		//emily++++  insert sleepDayInfo into array_ecgInfo
		ArrayList<EcgInfo> sortedEcgSleep=new ArrayList<EcgInfo>();
		if(sleepDayInfo.hasData){
			while (array_ecgInfo.size()!=0 && sleepDayInfo.hasData){
				if(sleepDayInfo.measureTime > array_ecgInfo.get(0).measureTime){
					sortedEcgSleep.add(sleepDayInfo);
					sleepDayInfo.hasData=false;
				}
				else{
					sortedEcgSleep.add(array_ecgInfo.get(0));
					array_ecgInfo.remove(0);
				}
			}
		}
		if(array_ecgInfo.size()!=0){
			sortedEcgSleep.addAll(array_ecgInfo);
		}
		if(sleepDayInfo.hasData){
			sortedEcgSleep.add(sleepDayInfo);
		}
		//emily----

		while(sortedEcgSleep.size()!=0&&array_activityInfo.size()!=0){
			if(sortedEcgSleep.get(0).measureTime<array_activityInfo.get(0).time){
				sortedState.add(sortedEcgSleep.get(0));
				sortedEcgSleep.remove(0);
			}
			else{
				sortedState.add(array_activityInfo.get(0));
				array_activityInfo.remove(0);
			}
		}
		if(sortedEcgSleep.size()!=0){
			sortedState.addAll(sortedEcgSleep);
		}
		else{
			sortedState.addAll(array_activityInfo);
		}
		return sortedState;
	}
	
	private ArrayList<ArrayList<TimeItem>> mergeSameTimePoint(ArrayList<Object> info){

		String mLastTimeString="";
		ArrayList<ArrayList<TimeItem>> dataList=new ArrayList<ArrayList<TimeItem>>();
		ArrayList<TimeItem> timeItemList=new ArrayList<TimeItem>();
		long time1 = System.currentTimeMillis();
		for(int i=0;i<info.size();i++){
			Object item=info.get(i);
			TimeItem timeItem = null;
			if(item instanceof EcgInfo){
				timeItem = genEcgItem(item);
			}
			else if(item instanceof ActivityPoint){
				timeItem = genActivityItem(item);
			}
			if(timeItem == null)
			{
				continue;
			}
		//	TimeItem timeItem=getTimeItem(district, view, tag, timeString);
			if(i!=0){
				if(!mLastTimeString.matches(timeItem.timeString)){
					dataList.add(timeItemList);
					timeItemList=new ArrayList<TimeItem>();
				}	
			}	
			timeItemList.add(timeItem);
			if(i==info.size()-1){
				dataList.add(timeItemList);
			}
			mLastTimeString=timeItem.timeString;
		}
		long end = System.currentTimeMillis();
		Log.i("smile",(end-time1)+" mergesame ");
		return dataList;
	}
	private TimeItem genEcgItem(Object item)
	{
		String timeString = "", district="";
		View view = null;
		String tag="";
		EcgInfo ecgInfo=(EcgInfo) item;
		tag=NewTimelineView.TAG_ECG;
		timeString=Utility.getDateTime(mContext, ecgInfo.measureTime);
		district=getDistrict(ecgInfo.measureTime, -1);
        if(ecgInfo.measureType == EcgTable.TYPE_SLEEP){
            view=mInflater.inflate(R.layout.daily_info_sleep_layout, null);
            TextView scoreTv = (TextView) view.findViewById(R.id.tv_sleep_score);
            TextView hourTv = (TextView) view.findViewById(R.id.tv_sleep_hour);
           // TextView minTv = (TextView) view.findViewById(R.id.tv_sleep_min);

			//Utility.setSleepScore(scoreTv, ecgInfo.score, mContext);
			scoreTv.setText(String.valueOf(ecgInfo.score));

			TimeSpanItem timeSpanItem = Utility.getTimeSpan(ecgInfo.totalMins);
			hourTv.setText(Utility.getHourMinStr(mContext, timeSpanItem, false));
			//hourTv.setText(Utility.formatSleepTimes(ecgInfo.totalMins, mContext));
        }
		else if(ecgInfo.measureType==EcgTable.TYPE_HEARTRATE){
			view=mInflater.inflate(R.layout.daily_info_heart_rate_layout, null);
			TextView tv=(TextView)view.findViewById(R.id.heart_rate_bpm);
			TextView tvLevel=(TextView)view.findViewById(R.id.heart_rate_level);

			Utility.getFormatStr(String.valueOf(ecgInfo.measureValue), getResources().getString(R.string.daily_info_heart_rate_unit), mContext);
			tv.setText(Utility.getFormatStr(String.valueOf(ecgInfo.measureValue), getResources().getString(R.string.daily_info_heart_rate_unit), mContext));
			//tv.setText(String.valueOf(ecgInfo.measureValue));
			tvLevel.setText(Utility.getIntensityLevel(mContext, ecgInfo.measureValue));
		}
		else{
			view=mInflater.inflate(R.layout.daily_info_stress_energy_layout, null);
			TextView titleText=(TextView)view.findViewById(R.id.measure_energy_title);
			TextView valueText=(TextView)view.findViewById(R.id.measure_energy_value);
			ImageView image=(ImageView)view.findViewById(R.id.energy_or_stress_icon);
			if(ecgInfo.measureType==EcgTable.TYPE_RELAX){
				titleText.setText(mContext.getString(R.string.daily_info_energy_title));
				valueText.setText(Utility.intToStr(ecgInfo.measureValue));
				image.setImageResource(Utility.getRelaxDrawableId(ecgInfo.measureValue));
			}
			else if(ecgInfo.measureType==EcgTable.TYPE_STRESS){
				titleText.setText(mContext.getString(R.string.daily_info_stress_title));
				valueText.setText(Utility.intToStr(ecgInfo.measureValue));
				valueText.setTextColor(0xffc14343);
				image.setImageResource(Utility.getStressDrawableId(ecgInfo.measureValue));
			}
		}
		TimeItem timeItem=getTimeItem(district, view, tag, timeString);
		return  timeItem;
	}

	private TimeItem genActivityItem(Object item )
	{
		String timeString = "", district="";
		View view = null;
		String tag="";
		ActivityPoint activityPoint=(ActivityPoint) item;
		timeString=Utility.getDateTime(mContext,activityPoint.time);
		district=getDistrict(activityPoint.time,activityPoint.endTime);
		view=mInflater.inflate(R.layout.daily_info_walk_bike_layout, null);
		TextView tvSteps=(TextView) view.findViewById(R.id.tv_step_or_bike_steps);
		TextView tvDistance=(TextView) view.findViewById(R.id.tv_step_or_bike_distance);
		TextView tvType=(TextView) view.findViewById(R.id.tv_step_or_bike_type);
		TextView tvTime=(TextView) view.findViewById(R.id.tv_step_or_bike_time);
		TextView tvUnit=(TextView) view.findViewById(R.id.tv_step_or_bike_unit);
		ImageView img=(ImageView)view.findViewById(R.id.img_step_or_bike_icon);
		//TextView tvCal=(TextView) view.findViewById(R.id.tv_step_or_bike_kcal);

        //default value
		tvUnit.setText("");

		TimeSpanItem timeSpanItem = TimeSpanItem.creatTimeItem(activityPoint.startTime, activityPoint.endTime);
		tvTime.setText(Utility.getHourMinStr(mContext, timeSpanItem, false));

		/*if(activityPoint.stepCounts>0){
			Profile profile=pfd.getStandardProfile();
			int heightInCM=profile.getHeight();
			if(profile.getHeight_unit()== ProfileTable.HEIGHT_UNIT_FT){
				float ft=Utility.InchToFt(heightInCM);
				heightInCM=(int) Math.round(Utility.ftToCm(ft));
			}
			int weightInKG=profile.getWeight();
			if(profile.getWeight_unit()==ProfileTable.WEIGHT_UNIT_LBS){
				weightInKG=(int) Math.round(Utility.LbsToKg(profile.getWeight()));
			}
			String div = " / ";
			int distance = Utility.getWalkDistanceInCM(heightInCM, activityPoint.stepCounts) / 100;
			int res = R.string.distance_unit_miles;
			if (profile.getDistance_unit() == 0){
				res = R.string.distance_unit;
			}
			String dis=Utility.formatDistance(Utility.getWalkDistanceInCM(heightInCM, activityPoint.stepCounts) / 100.0, profile.getDistance_unit());
			tvDistance.setText(Utility.getFormatStr(dis, getResources().getString(res), mContext));

			String cal =div + Utility.formatNumber((int) Utility.getWalkCalories(heightInCM, activityPoint.stepCounts, weightInKG));
			tvCal.setText(Utility.getFormatStr(cal, getResources().getString(R.string.calories_unit), mContext));
			view.findViewById(R.id.info_div).setVisibility(View.VISIBLE);
		}*/

		switch (activityPoint.activityType) {
			case ActivityStateTable.TYPE_WALK:
				tvType.setText(R.string.daily_info_walk_type);
				tvSteps.setText(Utility.getFormatStr(Utility.formatNumber(activityPoint.stepCounts), mContext.getString(R.string.daily_info_walk_unit), mContext));
				//tvUnit.setText(R.string.daily_info_walk_unit);
				img.setImageResource(R.drawable.asus_wellness_ic_walk);
				int type = getWalkOrRunType(timeSpanItem.getTotalInMinutes(),activityPoint.stepCounts);
				if (type == ActivityStateTable.TYPE_RUN){
					tvType.setText(R.string.daily_info_run_type);
					img.setImageResource(R.drawable.asus_wellness_ic_run);
					view.findViewById(R.id.walk_run_info_line).setBackgroundColor(0xff228d54);//:  0xff81bc0f
				}
				break;
			case ActivityStateTable.TYPE_PUSHUP:
                tvType.setText(R.string.pushup);
                img.setImageResource(R.drawable.asus_wellness_ic_pushup_g);
				break;
			case ActivityStateTable.TYPE_SITUP:
                tvType.setText(R.string.situp);
                img.setImageResource(R.drawable.asus_wellness_ic_situp_g);
				break;
			case ActivityStateTable.TYPE_BIKE:
				tvDistance.setText(Utility.getOneDigitFloatString((float)activityPoint.distance/1000));
				tvType.setText(R.string.daily_info_transport_type);
				tvUnit.setText(R.string.distance_unit);
				img.setImageResource(R.drawable.asus_wellness_ic_traffic);
				break;
			default:
				break;
		}

//		if(activityPoint.activityType==ActivityStateTable.TYPE_WALK){
//			tvDistance.setText(String.valueOf(activityPoint.stepCounts));
//			tvType.setText(R.string.daily_info_walk_type);
//			tvTime.setText(calculateTimeInterval(activityPoint.startTime, activityPoint.endTime));
//			tvUnit.setText(R.string.daily_info_walk_unit);
//			img.setImageResource(R.drawable.asus_wellness_ic_walk);
//		}else if(){
//			tvDistance.setText(Utility.getOneDigitFloatString((float)activityPoint.distance/1000));
//			tvType.setText(R.string.daily_info_transport_type);
//			tvTime.setText(calculateTimeInterval(activityPoint.startTime, activityPoint.endTime));
//			tvUnit.setText(R.string.distance_unit);
//			img.setImageResource(R.drawable.asus_wellness_ic_traffic);
//		}

		if(activityPoint.isStart){
			tag=NewTimelineView.TAG_ACTIVTY_START;
		}
		else{
			tag=NewTimelineView.TAG_ACTIVTY_END;
		}
		TimeItem timeItem=getTimeItem(district, view, tag, timeString);
		return  timeItem;
	}

	private TimeItem getTimeItem(String district, View view, String tag, String timeString){
		TimeItem timeItem=new TimeItem();
		timeItem.district=district;
		view.setTag(tag);
		timeItem.view=view;
		timeItem.timeString=timeString;
		return timeItem;
	}
	
	//return best left margin for timeline
	private int testItemWidth(ArrayList<Object> arrayInfo){
		long time=System.currentTimeMillis();
		int minLeft=mContext.getResources().getDimensionPixelSize(R.dimen.default_start_position_x);
		for(Object info:arrayInfo){
			View view = null;
			if(info instanceof EcgInfo){
				EcgInfo ecgInfo=(EcgInfo)info;
				if(ecgInfo.measureType==EcgTable.TYPE_HEARTRATE){
					view=mInflater.inflate(R.layout.daily_info_heart_rate_layout, null);
					TextView tv=(TextView)view.findViewById(R.id.heart_rate_bpm);
					TextView tvLevel=(TextView)view.findViewById(R.id.heart_rate_level);
					tvLevel.setText(Utility.getIntensityLevel(mContext, ecgInfo.measureValue));
					String value  = Utility.getFormatStr(String.valueOf(ecgInfo.measureValue), mContext.getString(R.string.daily_info_heart_rate_unit), mContext).toString();
					tv.setText(value);
				}
				else{
					view=mInflater.inflate(R.layout.daily_info_stress_energy_layout, null);
					TextView titleText=(TextView)view.findViewById(R.id.measure_energy_title);
					TextView valueText=(TextView)view.findViewById(R.id.measure_energy_value);
					ImageView image=(ImageView)view.findViewById(R.id.energy_or_stress_icon);
					if(ecgInfo.measureType==EcgTable.TYPE_RELAX){
						titleText.setText(mContext.getString(R.string.daily_info_energy_title));
						valueText.setText(Utility.intToStr(ecgInfo.measureValue));
						image.setImageResource(Utility.getRelaxDrawableId(ecgInfo.measureValue));
					}
					else if(ecgInfo.measureType==EcgTable.TYPE_STRESS){
						titleText.setText(mContext.getString(R.string.daily_info_stress_title));
						valueText.setText(Utility.intToStr(ecgInfo.measureValue));
						valueText.setTextColor(0xffc14343);
						image.setImageResource(Utility.getStressDrawableId(ecgInfo.measureValue));
					}
				}	
			}
			else if(info instanceof ActivityInfo){
				ActivityInfo activityInfo=(ActivityInfo)info;
				view=mInflater.inflate(R.layout.daily_info_walk_bike_layout, null);
				TextView tvDistance=(TextView) view.findViewById(R.id.tv_step_or_bike_distance);
				TextView tvType=(TextView) view.findViewById(R.id.tv_step_or_bike_type);
				TextView tvTime=(TextView) view.findViewById(R.id.tv_step_or_bike_time);
				TextView tvUnit=(TextView) view.findViewById(R.id.tv_step_or_bike_unit);
				ImageView img=(ImageView)view.findViewById(R.id.img_step_or_bike_icon);
				if(activityInfo.activityType==ActivityStateTable.TYPE_WALK){
					tvDistance.setText(String.valueOf(activityInfo.stepCounts));
					tvType.setText(R.string.daily_info_walk_type);
					
					tvTime.setText(calculateTimeInterval(activityInfo.startTime, activityInfo.endTime));
					tvUnit.setText(R.string.daily_info_walk_unit);
					img.setImageResource(R.drawable.asus_wellness_ic_walk);
				}
				else{
					tvDistance.setText(Utility.getOneDigitFloatString((float)activityInfo.distance/1000));
					tvDistance.setTextColor(0xff327abd);
					tvType.setText(R.string.daily_info_transport_type);
					tvTime.setText(calculateTimeInterval(activityInfo.startTime, activityInfo.endTime));
					tvUnit.setText(R.string.distance_unit);
					img.setImageResource(R.drawable.asus_wellness_ic_traffic);
				}
			}
			view.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
					MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
			int left=minLeft-view.getMeasuredWidth()-15;
			if(left<0){
				minLeft=minLeft-left;
			}
		}
		Log.e("circle","testItemWidth time:"+(System.currentTimeMillis()-time));
		return minLeft;
	}
	
	private String calculateTimeInterval(long startTime, long endTime){
//		int startHour=Integer.valueOf(Utility.getDateTime(startTime, "HH"));
//		int startMin=Integer.valueOf(Utility.getDateTime(startTime, "mm"));
//		int endHour=Integer.valueOf(Utility.getDateTime(endTime, "HH"));
//		int endMin=Integer.valueOf(Utility.getDateTime(endTime, "mm"));
//		int totalMinStart=startHour*60+startMin;
//		int totalMinEnd=endHour*60+endMin;
//		int interval=totalMinEnd-totalMinStart;
//		int intervalHour=interval/60;
//		int intervalMin=interval%60;
		TimeSpanItem ti = TimeSpanItem.creatTimeItem(startTime, endTime);
		int intervalHour=ti.getHour();
		int intervalMin=ti.getMinute();
		String hourString="";
		if(intervalHour>=1){
			if(intervalHour==1){
				hourString=intervalHour+mContext.getString(R.string.daily_info_time_hour);
			}
			else{
				hourString=intervalHour+mContext.getString(R.string.daily_info_time_hours);
			}
		}
		
		String minString="";
        if(intervalMin < 1){
            intervalMin = 1;
        }
		minString=intervalMin+mContext.getString(R.string.daily_info_time_minute);
		String timeString=hourString+" "+minString;

		return timeString;
	}

	public static final int AVERAGE_RUN_STEPS = 100;
	private int getWalkOrRunType(int time, int steps){
		long avg = steps;
		if(time > 0){
			avg = Math.round(Arith.div(steps, time, 0));
		}

		if(avg >= AVERAGE_RUN_STEPS){
			//Log.i("emily","Type = Run, time = " + time + ", steps = " + steps);
			return ActivityStateTable.TYPE_RUN;

		}
		//Log.i("emily","Type = Walk, time = " + time + ", steps = " + steps);
		return ActivityStateTable.TYPE_WALK;
	}
}

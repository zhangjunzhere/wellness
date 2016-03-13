package com.asus.wellness;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.asus.sharedata.SleepData;
import com.asus.sharedata.SleepTimeSpan;
import com.asus.wellness.dbhelper.Coach;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.CoachTable;
import com.asus.wellness.provider.EcgTable;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.provider.StepGoalTable;
import com.asus.wellness.sleep.SleepHelper;
import com.asus.wellness.sleep.SleepInfo;
import com.asus.wellness.ui.setting.SettingStepGoalActivity;
import com.asus.wellness.ui.week.WeeklyPageFragment;
import com.asus.wellness.utils.Utility;


public class ParseDataManager implements Serializable{

    public static String STEP_DISTINCT_FILTER = " and " + ActivityStateTable.COLUMN_TYPE  + "=0 " +
			" group by " + ActivityStateTable.COLUMN_START;

//	public static String ACTIVITY_DISTINCT_FILTER = " group by " + ActivityStateTable.COLUMN_START;
   public static String ACTIVITY_DISTINCT_FILTER = STEP_DISTINCT_FILTER;

    public static final String COLUMN_DEVICE_ID = "deviceId";

	public class ProfileData{
		public String name;
		public int gender;
		public int age;
		public int height;
		public int heightUnit;
		public int weight;
		public int weightUnit;
		public String photo_path;
		public long start_time;
        public long birthday;
	}
	
	public class BikeInfo{
		public long startTime;
		public long endTime;
		public int distanceInMeter;
	}
	
	public class CarInfo{
		public long startTime;
		public long endTime;
		public int distanceInMeter;
	}
	
	public class StepInfo{
		public long startTime;
		public long endTime;
		public int stepCount;
	}
	
	public class HeartRateInfo{
		public long measureTime;
		public int measureValue;
	}
	
	public class RelaxInfo{
		public long measureTime;
		public int measureValue;
		public String measureComment;
	}
	
	public class StressInfo{
		public long measureTime;
		public int measureValue;
		public String measureComment;
	}

	public class WorkoutInfo{
		public int type;
		public long startTime;
		public long endTime;
		public long count;
		public int percent;
		public long duration;
	}
	
	public class Day implements Serializable{
		public long startTimeMilli;
		public long endTimeMilli;
		public int year;
		public int month;
		public int day;
	}

    private static ParseDataManager sParseDataManager;
    public  static ParseDataManager getInstance(){
        if(sParseDataManager == null){
            sParseDataManager = new ParseDataManager();
        }
        return  sParseDataManager;
    }

	public ProfileData getProfileData(Context context){
	//	Cursor cursor=context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
		ProfileData profile=null;
//		if(cursor.moveToFirst()){
//			profile=new ProfileData();
//			profile.name=cursor.getString(cursor.getColumnIndex(ProfileTable.COLUMN_NAME));
//			profile.gender=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_GENDER));
//			profile.age=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_AGE));
//			profile.height=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_HEIGHT));
//			profile.heightUnit=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_HEIGHT_UNIT));
//			profile.weight=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_WEIGHT));
//			profile.weightUnit=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_WEIGHT_UNIT));
//			profile.photo_path=cursor.getString(cursor.getColumnIndex(ProfileTable.COLUMN_PHOTO));
//			profile.start_time=cursor.getLong(cursor.getColumnIndex(ProfileTable.COLUMN_START_TIME));
//		}
//		cursor.close();
		List<Profile> list =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
		if( list.size() !=0)
		{
			Profile	tempprofile = list.get(0);
			profile=new ProfileData();
			profile.name=tempprofile.getName();
			profile.gender=tempprofile.getGender();
			profile.age  = tempprofile.getAge();
			profile.height= tempprofile.getHeight();
			profile.heightUnit=tempprofile.getHeight_unit();
			profile.weight=tempprofile.getWeight();
			profile.weightUnit= tempprofile.getWeight_unit();
			profile.photo_path= tempprofile.getPhoto_path();
			profile.start_time= tempprofile.getStart_time();
            profile.birthday = tempprofile.getBirthday();
		}
		return profile;
	}

    public static Profile getStandardProfile(){
        Profile profile;
        List<Profile> profiles = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
        if(profiles.size() > 0){
            profile = profiles.get(0);
        }else{
            profile = new Profile();
            final int defHeight = 170; //170cm
            final int defWeight = 70; //60kg
            profile.setHeight(defHeight);
            profile.setHeight_unit(ProfileTable.HEIGHT_UNIT_CM);
            profile.setWeight(defWeight);
            profile.setWeight_unit(ProfileTable.WEIGHT_UNIT_KG);
        }

       /* if(profile.getHeight_unit()==ProfileTable.HEIGHT_UNIT_FT){
            float ft=Utility.InchToFt(profile.getHeight());
            int heightInCM=(int) Utility.ftToCm(ft);
            profile.setHeight(heightInCM);
        }
        if(profile.getWeight_unit() ==ProfileTable.WEIGHT_UNIT_LBS){
            int weightInKG=(int) Utility.LbsToKg(profile.getWeight());
            profile.setWeight(weightInKG);
        }*/

        return profile;
    }

	
	public ArrayList<CarInfo> getDayCarInfo(Context context, Day day){

		Cursor cursor=context.getContentResolver().query(ActivityStateTable.TABLE_URI, null
				, ActivityStateTable.COLUMN_START+">=? and "+ActivityStateTable.COLUMN_END+"<=? and "+ActivityStateTable.COLUMN_TYPE+"=?"
                + getDeviceIdConfition()
				, new String[]{String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli), String.valueOf(ActivityStateTable.TYPE_DRIVE)}, ActivityStateTable.COLUMN_START);
		ArrayList<CarInfo> arrayCarInfo=null;
		if(cursor.moveToFirst()){
			arrayCarInfo=new ArrayList<CarInfo>();
			do{
				CarInfo carInfo=new CarInfo();
				carInfo.startTime=cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_START));
				carInfo.endTime=cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_END));
				carInfo.distanceInMeter=cursor.getInt(cursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
				arrayCarInfo.add(carInfo);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return arrayCarInfo;
	}
	
	public ArrayList<BikeInfo> getDayBikeInfo(Context context, Day day){

		Cursor cursor=context.getContentResolver().query(ActivityStateTable.TABLE_URI, null
				, ActivityStateTable.COLUMN_START+">=? and "+ActivityStateTable.COLUMN_END+"<=? and "+ActivityStateTable.COLUMN_TYPE+"=?"
                + getDeviceIdConfition()
				, new String[]{String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli), String.valueOf(ActivityStateTable.TYPE_BIKE)}, ActivityStateTable.COLUMN_START);
		ArrayList<BikeInfo> arrayBikeInfo=null;
		if(cursor.moveToFirst()){
			arrayBikeInfo=new ArrayList<BikeInfo>();
			do{
				BikeInfo bikeInfo=new BikeInfo();
				bikeInfo.startTime=cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_START));
				bikeInfo.endTime=cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_END));
				bikeInfo.distanceInMeter=cursor.getInt(cursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
				arrayBikeInfo.add(bikeInfo);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return arrayBikeInfo;
	}
	
	public ArrayList<StepInfo> getDayStepInfo(Context context, Day day){

		Cursor cursor=context.getContentResolver().query(ActivityStateTable.TABLE_URI, null
				, ActivityStateTable.COLUMN_START+">=? and "
                +ActivityStateTable.COLUMN_START+"<=? and "
                +ActivityStateTable.COLUMN_TYPE+"=? and "
                +  ActivityStateTable.COLUMN_STEP_COUNT + ">0"
				+ getDeviceIdConfition()
                + STEP_DISTINCT_FILTER
				, new String[]{ String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli), String.valueOf(ActivityStateTable.TYPE_WALK)},
                ActivityStateTable.COLUMN_START);
		ArrayList<StepInfo> arrayStepInfo=null;
		if(cursor.moveToFirst()){
			arrayStepInfo=new ArrayList<StepInfo>();
			do{
				StepInfo stepInfo=new StepInfo();
				stepInfo.startTime=cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_START));
				stepInfo.endTime=cursor.getLong(cursor.getColumnIndex(ActivityStateTable.COLUMN_END));
				stepInfo.stepCount=cursor.getInt(cursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
				arrayStepInfo.add(stepInfo);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return arrayStepInfo;
	}

	public ArrayList<WorkoutInfo> getDayRunInfo(Context context, Day day){
		Cursor cursor=context.getContentResolver().query(CoachTable.TABLE_URI, null
				, CoachTable.COLUMN_START+">=? and "
						+CoachTable.COLUMN_START+"<=? and "
						+CoachTable.COLUMN_TYPE+"=? and "
						+  CoachTable.COLUMN_DURATION + ">0"
						+ getDeviceIdConfition()
						//+ STEP_DISTINCT_FILTER
				, new String[]{ String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli), String.valueOf(CoachTable.TYPE_RUN)},
				ActivityStateTable.COLUMN_START);
		ArrayList<WorkoutInfo> arrayRunInfo=null;
		if(cursor!=null && cursor.moveToFirst()){
			arrayRunInfo=new ArrayList<WorkoutInfo>();
			do{
				WorkoutInfo info=new WorkoutInfo();
				info.type=(int)cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_TYPE));
				info.startTime=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_START));
				info.endTime=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_END));
				info.count=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_VALUE));
				info.percent=cursor.getInt(cursor.getColumnIndex(CoachTable.COLUMN_PERCENT));
				info.duration=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_DURATION));
				arrayRunInfo.add(info);
			}while(cursor.moveToNext());
		}
		if (cursor!=null){
			cursor.close();
		}
		return arrayRunInfo;
	}

	/* get pushup & situp info */
	public ArrayList<WorkoutInfo> getDayWorkoutInfo(Context context, long startTimeMilli, long endTimeMilli){
		Cursor cursor=context.getContentResolver().query(CoachTable.TABLE_URI, null
				, CoachTable.COLUMN_START+">=? and "
						+CoachTable.COLUMN_END+"<=? and ("
						+CoachTable.COLUMN_TYPE+"=? or "
						+CoachTable.COLUMN_TYPE+"=?) and "
						+CoachTable.COLUMN_DURATION + ">0"
						+ getDeviceIdConfition(),
				new String[]{ String.valueOf(startTimeMilli), String.valueOf(endTimeMilli),
						String.valueOf(CoachTable.TYPE_SITUP), String.valueOf(CoachTable.TYPE_PUSHUP)},
				CoachTable.COLUMN_START);

		ArrayList<WorkoutInfo> workoutInfo=null;
		if(cursor!=null && cursor.moveToFirst()){
			workoutInfo=new ArrayList<WorkoutInfo>();
			do{
				WorkoutInfo info=new WorkoutInfo();
				info.type=(int)cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_TYPE));
				info.startTime=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_START));
				info.endTime=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_END));
				info.count=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_VALUE));
				info.percent=cursor.getInt(cursor.getColumnIndex(CoachTable.COLUMN_PERCENT));
				info.duration=cursor.getLong(cursor.getColumnIndex(CoachTable.COLUMN_DURATION));
				workoutInfo.add(info);
			}while(cursor.moveToNext());
		}
		if(cursor!=null){
			cursor.close();
		}
		return workoutInfo;
	}
	
	public ArrayList<HeartRateInfo> getDayHeartRateInfo(Context context, Day day){
		Cursor cursor=context.getContentResolver().query(EcgTable.TABLE_URI, null
				, EcgTable.COLUMN_MEASURE_TIME+">=? and "+EcgTable.COLUMN_MEASURE_TIME+"<=? and "+EcgTable.COLUMN_MEASURE_TYPE+"=?"
                +  getDeviceIdConfition()
				, new String[]{String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli), String.valueOf(EcgTable.TYPE_HEARTRATE)}, EcgTable.COLUMN_MEASURE_TIME);
		
		ArrayList<HeartRateInfo> arrayHeartRateInfo=null;
		if(cursor.moveToFirst()){
			arrayHeartRateInfo=new ArrayList<HeartRateInfo>();
			do{
				HeartRateInfo hrInfo=new HeartRateInfo();
				hrInfo.measureTime=cursor.getLong(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_TIME));
				hrInfo.measureValue=cursor.getInt(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_VALUE));
				arrayHeartRateInfo.add(hrInfo);
			}while(cursor.moveToNext());

		}
		cursor.close();
		return arrayHeartRateInfo;
	}
	
	public ArrayList<RelaxInfo> getDayRelaxInfo(Context context, Day day){

        Cursor cursor=context.getContentResolver().query(EcgTable.TABLE_URI, null
				, EcgTable.COLUMN_MEASURE_TIME+">=? and "+EcgTable.COLUMN_MEASURE_TIME+"<=? and "+EcgTable.COLUMN_MEASURE_TYPE+"=?"
                + getDeviceIdConfition()
				, new String[]{String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli), String.valueOf(EcgTable.TYPE_RELAX)}, EcgTable.COLUMN_MEASURE_TIME);
		
		ArrayList<RelaxInfo> arrayRelaxInfo=null;
		if(cursor.moveToFirst()){
			arrayRelaxInfo=new ArrayList<RelaxInfo>();
			do{
				RelaxInfo relaxInfo=new RelaxInfo();
				relaxInfo.measureTime=cursor.getLong(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_TIME));
				relaxInfo.measureValue=cursor.getInt(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_VALUE));
				relaxInfo.measureComment=Utility.getRelaxLevelString(context, relaxInfo.measureValue, cursor.getInt(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_COMMENT)));
				arrayRelaxInfo.add(relaxInfo);
			}while(cursor.moveToNext());

		}
		cursor.close();
		return arrayRelaxInfo;
	}
	public String getDeviceIdConfition()
	{
		Long deviceId = WApplication.getInstance().getConnectedDevice().getId();
		String confition = deviceId == null ? " is " : " = ";
		String deviceCondiction = " and " + COLUMN_DEVICE_ID + confition + deviceId;
		return  deviceCondiction;
	}
	public ArrayList<StressInfo> getDayStressInfo(Context context, Day day){

		 getDeviceIdConfition();
		Cursor cursor=context.getContentResolver().query(EcgTable.TABLE_URI, null
				, EcgTable.COLUMN_MEASURE_TIME+">=? and "+EcgTable.COLUMN_MEASURE_TIME+"<=? and "+EcgTable.COLUMN_MEASURE_TYPE+"=? " +
				getDeviceIdConfition()
				, new String[]{String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli), String.valueOf(EcgTable.TYPE_STRESS)}, EcgTable.COLUMN_MEASURE_TIME);
		
		ArrayList<StressInfo> arrayStressInfo=null;
		if(cursor.moveToFirst()){
			arrayStressInfo=new ArrayList<StressInfo>();
			do{
				StressInfo stressInfo=new StressInfo();
				stressInfo.measureTime=cursor.getLong(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_TIME));
				stressInfo.measureValue=cursor.getInt(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_VALUE));
				stressInfo.measureComment=cursor.getString(cursor.getColumnIndex(EcgTable.COLUMN_MEASURE_COMMENT));
				arrayStressInfo.add(stressInfo);
			}while(cursor.moveToNext());

		}
		cursor.close();
		return arrayStressInfo;
	}

	public Day getDay(long time){
		return calculateOneDay(time);
	}
	
	private Day calculateOneDay(long milli){
		Day day=new Day();
		
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(milli);
		day.year=cal.get(Calendar.YEAR);
		day.month=cal.get(Calendar.MONTH)+1;
		day.day=cal.get(Calendar.DATE);
		
		cal.set(day.year, day.month-1, day.day, 0, 0, 0);
		long startTimeMilli=cal.getTimeInMillis();
		day.startTimeMilli=startTimeMilli/1000*1000;
		
		cal.set(day.year, day.month-1, day.day, 23, 59, 59);
		day.endTimeMilli=cal.getTimeInMillis();
		
		return day;
	}

	public Day getDayBefore(long milli){
		//milli = milli - 24 * 60 * 60 * 1000;
		Day day=new Day();

		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(milli);
		cal.add(Calendar.DATE,-1);
		day.year=cal.get(Calendar.YEAR);
		day.month=cal.get(Calendar.MONTH)+1;
		day.day=cal.get(Calendar.DATE) ;
		cal.set(day.year, day.month-1, day.day, 0, 0, 0);
		long startTimeMilli=cal.getTimeInMillis();
		day.startTimeMilli=startTimeMilli/1000*1000;

		cal.set(day.year, day.month-1, day.day, 23, 59, 59);
		day.endTimeMilli=cal.getTimeInMillis();
		Log.i("ga","day: " + day.month + " " + day.day + " " + day.startTimeMilli + " " + day.endTimeMilli);
		return day;
	}
	
	public ArrayList<Object> combineStressAndRelaxInfo(ArrayList<StressInfo> stressInfo, ArrayList<RelaxInfo> relaxInfo){
		ArrayList<Object> sortedState=null;
		if(stressInfo==null && relaxInfo==null){
			return sortedState;
		}
		else{
			sortedState=new ArrayList<Object>();
			if(stressInfo==null){
				sortedState.addAll(relaxInfo);
				return sortedState;
			}
			if(relaxInfo==null){
				sortedState.addAll(stressInfo);
				return sortedState;
			}
			
			while(stressInfo.size()!=0&&relaxInfo.size()!=0){
				if(stressInfo.get(0).measureTime<relaxInfo.get(0).measureTime){
					sortedState.add(stressInfo.get(0));
					stressInfo.remove(0);
				}
				else{
					sortedState.add(relaxInfo.get(0));
					relaxInfo.remove(0);
				}
			}
			if(stressInfo.size()!=0){
				sortedState.addAll(stressInfo);
			}
			else{
				sortedState.addAll(relaxInfo);
			}
			return sortedState;	
		}
	}
	
	public int getDayStepGoal(Context context, Day day){
		String date=Utility.getDateTime(day.startTimeMilli, "yyyy-MM-dd");
		Cursor cursor=context.getContentResolver().query(StepGoalTable.TABLE_URI, null, StepGoalTable.COLUMN_DATE_TIME_MILLI + "<=?", new String[]{date}, StepGoalTable.COLUMN_DATE_TIME_MILLI + " DESC");
		int stepGoal=SettingStepGoalActivity.DEFAULT_STEP_GOAL;
		if(cursor.moveToFirst()){
			stepGoal=cursor.getInt(cursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
		}
		cursor.close();
		return stepGoal;
	}

	public ArrayList<EcgInfo> getEcgInfoFromDb(Context context,Day day)
	{
		ArrayList<EcgInfo> ecgInfoArray=new ArrayList<EcgInfo>();
		Cursor ecgCursor=context.getContentResolver().query(EcgTable.TABLE_URI, null
				, EcgTable.COLUMN_MEASURE_TIME + ">=? and " + EcgTable.COLUMN_MEASURE_TIME + "<=? "+getDeviceIdConfition()
				, new String[]{String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli)}, EcgTable.COLUMN_MEASURE_TIME);
		if(ecgCursor.moveToFirst()){
			do{
				EcgInfo ecgInfo=new EcgInfo();
				ecgInfo.measureTime=ecgCursor.getLong(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_TIME));
				ecgInfo.measureType=ecgCursor.getInt(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_TYPE));
				ecgInfo.measureValue=ecgCursor.getInt(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_VALUE));
				ecgInfoArray.add(ecgInfo);
			}while(ecgCursor.moveToNext());
		}
		ecgCursor.close();
		return  ecgInfoArray;
	}
	public ArrayList<ActivityInfo> getActivityInfoFromDb(Context context,Day day)
	{
		ArrayList<ActivityInfo> activityInfoArray=new ArrayList<ActivityInfo>();
		Cursor activityCursor=context.getContentResolver().query(ActivityStateTable.TABLE_URI, null
				, ActivityStateTable.COLUMN_START+">=? and "+ActivityStateTable.COLUMN_START+"<=? and " + ActivityStateTable.COLUMN_STEP_COUNT + ">0 "+getDeviceIdConfition()
				+  ParseDataManager.ACTIVITY_DISTINCT_FILTER
				, new String[]{String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli)}, ActivityStateTable.COLUMN_START);
		if(activityCursor.moveToFirst()){
			//initial
			ActivityInfo mLastActivityInfo=new ActivityInfo();
			mLastActivityInfo.startTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START));
			mLastActivityInfo.endTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END));
			mLastActivityInfo.distance=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
			mLastActivityInfo.stepCounts=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
			mLastActivityInfo.activityType=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_TYPE));

			int ACTIVITY_TYPE_TRANSPORTING=1024;//transporting
			do{
				int activityType=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_TYPE));
				int lastActivityType=mLastActivityInfo.activityType;
				if(activityType==ActivityStateTable.TYPE_BIKE||activityType==ActivityStateTable.TYPE_DRIVE){
					activityType=ACTIVITY_TYPE_TRANSPORTING;
				}
				if(lastActivityType==ActivityStateTable.TYPE_BIKE||lastActivityType==ActivityStateTable.TYPE_DRIVE){
					lastActivityType=ACTIVITY_TYPE_TRANSPORTING;
				}
				if(lastActivityType!=activityType){
					if(detectOverlap(mLastActivityInfo.startTime, mLastActivityInfo.endTime
							, activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START))
							, activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END)))){
						if(activityType==ACTIVITY_TYPE_TRANSPORTING || activityType == ActivityStateTable.TYPE_PUSHUP || activityType == ActivityStateTable.TYPE_SITUP ){//walk to transporting
							//set walk end time to transporting start time
							ActivityInfo lastActivityInfo=activityInfoArray.remove(activityInfoArray.size()-1);
							lastActivityInfo.endTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START));
							activityInfoArray=checkActivityLargerThanOneMin(activityInfoArray, lastActivityInfo);

							ActivityInfo activityInfo=new ActivityInfo();
							activityInfo.startTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START));
							activityInfo.endTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END));
							activityInfo.distance=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
							activityInfo.stepCounts=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
							activityInfo.activityType=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_TYPE));
							activityInfoArray=checkActivityLargerThanOneMin(activityInfoArray, activityInfo);
							mLastActivityInfo=activityInfo;
						}
						else{
							//set walk start time as transporting end time
							long walkEndTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END));
							if(mLastActivityInfo.endTime<walkEndTime){
								ActivityInfo activityInfo=new ActivityInfo();
								activityInfo.startTime=mLastActivityInfo.endTime;
								activityInfo.endTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END));
								activityInfo.distance=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
								activityInfo.stepCounts=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
								activityInfo.activityType=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_TYPE));
								activityInfoArray=checkActivityLargerThanOneMin(activityInfoArray, activityInfo);
								mLastActivityInfo=activityInfo;
							}
						}
					}
					else{
						ActivityInfo activityInfo=new ActivityInfo();
						activityInfo.startTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START));
						activityInfo.endTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END));
						activityInfo.distance=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
						activityInfo.stepCounts=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
						activityInfo.activityType=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_TYPE));
						activityInfoArray=checkActivityLargerThanOneMin(activityInfoArray, activityInfo);
						mLastActivityInfo=activityInfo;
					}
				}
				else{
					ActivityInfo activityInfo=new ActivityInfo();
					activityInfo.startTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START));
					activityInfo.endTime=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END));
					activityInfo.distance=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
					activityInfo.stepCounts=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
					activityInfo.activityType=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_TYPE));
					activityInfoArray=checkActivityLargerThanOneMin(activityInfoArray, activityInfo);
					mLastActivityInfo=activityInfo;
				}
			}while(activityCursor.moveToNext());
		}
		activityCursor.close();
		return  activityInfoArray;
	}
	private boolean detectOverlap(long aStartTime, long aEndTime, long bStartTime, long bEndTime){
		if(aStartTime>=bEndTime || aEndTime<=bStartTime){
			return false;
		}
		else{
			return true;
		}
	}

	private ArrayList<ActivityInfo> checkActivityLargerThanOneMin(ArrayList<ActivityInfo> activityInfoArray, ActivityInfo activityInfo){
//		if(!Utility.getDateTime(activityInfo.startTime, "hh:mm a").matches(Utility.getDateTime(activityInfo.endTime, "hh:mm a"))){
		activityInfoArray.add(activityInfo);
//		}
		return activityInfoArray;
	}

    public  WeeklyPageFragment.DayInfo getDayInfo(Context context, long startTimeMilli,  long endTimeMilli){
        //Log.d("PHH", "getAllWeekDataInfo - activityCursor" );
        int countHeart=0, countRelaxation=0, countStress=0; int heartRate;
        WeeklyPageFragment.DayInfo dayInfo = new WeeklyPageFragment.DayInfo();
		dayInfo.heartRates = new ArrayList<Integer>();
        dayInfo.hasData = false;

        Cursor activityCursor=context.getContentResolver().query(ActivityStateTable.TABLE_URI, null
                , ActivityStateTable.COLUMN_START+">=? and "+ActivityStateTable.COLUMN_START+"<=?"
                + getDeviceIdConfition()
                ,  new String[]{String.valueOf(startTimeMilli), String.valueOf(endTimeMilli)}, ActivityStateTable.COLUMN_START);
        //Log.d("PHH", "getAllWeekDataInfo - ecgCursor" );
        Cursor ecgCursor=context.getContentResolver().query(EcgTable.TABLE_URI, null
                , EcgTable.COLUMN_MEASURE_TIME+">=? and "+EcgTable.COLUMN_MEASURE_TIME+"<=?"
                + getDeviceIdConfition()
                ,  new String[]{String.valueOf(startTimeMilli), String.valueOf(endTimeMilli)}, EcgTable.COLUMN_MEASURE_TIME);

        //hasData
		//emily+++
		EcgInfo sleepInfo = new EcgInfo();
		sleepInfo = getDaySleepDataFromEcg(startTimeMilli);
		if(sleepInfo.hasData) {
			dayInfo.totalMins = sleepInfo.totalMins;
			dayInfo.deepMins = sleepInfo.deepMins;
			dayInfo.lightMins = sleepInfo.lightMins;
			dayInfo.hasData = true;
		}

		int pushup = 0;
		int situp = 0;
		ArrayList<WorkoutInfo> workoutArray=getDayWorkoutInfo(context, startTimeMilli, endTimeMilli);
		if(workoutArray != null && workoutArray.size() > 0){
			for(WorkoutInfo info:workoutArray){
				if (info.type == CoachTable.TYPE_PUSHUP){
					pushup += info.count;
				}
				else if(info.type == CoachTable.TYPE_SITUP){
					situp += info.count;
				}
			}
			dayInfo.hasData = true;
		}
		dayInfo.pushupCounts = pushup;
		dayInfo.situpCounts = situp;
		//emily----

        if(activityCursor.getCount() > 0 || ecgCursor.getCount() > 0 )
        {
            dayInfo.hasData = true;
        }

        if(activityCursor != null && activityCursor.moveToFirst()){
            do{
                int type=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_TYPE));
                switch(type){
                    case ActivityStateTable.TYPE_BIKE:
                        long start=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START));
                        long end=activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_END));
                        dayInfo.bikeDistance+=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
                        dayInfo.bikeHour+=((float)(end-start)/1000/60/60);
                        break;
                    case ActivityStateTable.TYPE_DRIVE:
                        dayInfo.carDistance+=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_DISTANCE));
                        break;
                    case ActivityStateTable.TYPE_WALK:
                        dayInfo.stepCounts+=activityCursor.getInt(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_STEP_COUNT));
                        break;
                }
            }while(activityCursor.moveToNext());


        }
		activityCursor.close();
        if(ecgCursor != null && ecgCursor.moveToFirst()){
            do{
                int type=ecgCursor.getInt(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_TYPE));
                if(type==EcgTable.TYPE_HEARTRATE){
                    heartRate = ecgCursor.getInt(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_VALUE));
                    dayInfo.heartRates.add(heartRate);
                    dayInfo.avgHeartRate +=heartRate;
                    countHeart++;
                }else if (type==EcgTable.TYPE_RELAX){
                    dayInfo.avgRelaxation += ecgCursor.getInt(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_VALUE));
                    countRelaxation++;
                }else if (type==EcgTable.TYPE_STRESS){
                    dayInfo.avgStress  += ecgCursor.getInt(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_VALUE));
                    countStress++;
                }
                //else if(type==sleep)
            }while(ecgCursor.moveToNext());

        }
		ecgCursor.close();
        if(countHeart!=0){
            dayInfo.avgHeartRate /=countHeart;
        }
        if (countRelaxation != 0){
            dayInfo.avgRelaxation  /= countRelaxation;
        }
        if (countStress != 0){
            dayInfo.avgStress  /= countStress;
        }
        return dayInfo;
    }


	public EcgInfo getDaySleepDataFromEcg(long timeMilli){//(SleepData sleepDatass, long timeMilli, long spsan){
		EcgInfo dailyInfo = new EcgInfo();
		dailyInfo.measureType=EcgTable.TYPE_SLEEP;
		//long span = 15*60*1000;
		SleepData sleepData = SleepHelper.getSleepQualityByDate(timeMilli);

		if(sleepData!=null && sleepData.getDataList().size()>0){
			SleepTimeSpan total = sleepData.getTotalSleepTime();
			SleepTimeSpan deep = sleepData.getTotalDeepSleepTime();
			SleepTimeSpan light = sleepData.getTotalLightSleepTime();

			dailyInfo.measureTime = sleepData.getEndTime();
			dailyInfo.totalMins = sleepData.getSleepTimeMins(total);
			dailyInfo.deepMins = sleepData.getSleepTimeMins(deep);
			dailyInfo.lightMins = sleepData.getSleepTimeMins(light);
			dailyInfo.hasData = true;//dailyInfo.totalMins>0 ? true : false
			dailyInfo.score =  sleepData.getQuality();//dailyInfo.totalMins>0 ? (int)(dailyInfo.deepMins*100.0f/dailyInfo.totalMins) : 0;
		}
		else{
			dailyInfo.hasData = false;
		}

		return dailyInfo;
	}

	public String getWorkoutCompletion(Context context, Day day){
		Cursor cursor=context.getContentResolver().query(CoachTable.TABLE_URI, null
				, CoachTable.COLUMN_START+">=? and "
						+CoachTable.COLUMN_START+"<=? and ("
						+CoachTable.COLUMN_TYPE+"=? or "
						+CoachTable.COLUMN_TYPE+"=? or "
						+CoachTable.COLUMN_TYPE+"=?) and "
						+  CoachTable.COLUMN_DURATION + ">0"
						+ getDeviceIdConfition()
				, new String[]{ String.valueOf(day.startTimeMilli), String.valueOf(day.endTimeMilli),
						String.valueOf(CoachTable.TYPE_RUN), String.valueOf(CoachTable.TYPE_PUSHUP), String.valueOf(CoachTable.TYPE_SITUP)},
				ActivityStateTable.COLUMN_START);

		int totalCounts = 0;
		int completionCounts = 0;
		if(cursor!=null && cursor.moveToFirst()){
			totalCounts = cursor.getCount();
			do{
				if (cursor.getInt(cursor.getColumnIndex(CoachTable.COLUMN_PERCENT)) == 100){
					completionCounts++;
				}
			}while(cursor.moveToNext());
		}
		if(cursor!=null){
			cursor.close();
		}

		if(totalCounts > 0){
			return (String.valueOf(completionCounts) + "/" +String.valueOf(totalCounts));
		}
		else {
			return "0";
		}
	}
}

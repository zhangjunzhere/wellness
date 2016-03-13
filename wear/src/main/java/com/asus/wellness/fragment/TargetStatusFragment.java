package com.asus.wellness.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;
import com.asus.wellness.R.id;
import com.asus.wellness.StepCountManager;
import com.asus.wellness.TimeLineRelativeLayout;
import com.asus.wellness.chartview.BarChart;
import com.asus.wellness.dbhelper.Step_count;
import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.microprovider.WellnessProvider;
import com.asus.wellness.notification.ProfileTableObserver;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.StepHelper;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.ProfileDao;
import com.asus.wellness.utils.AsusLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TargetStatusFragment extends Fragment implements UpdateStepListener {
	int [] weekSteps=new int[7];
	private  static String Key_Toady= "key_today_steps";
	private  static String Key_Target= "key_target";
	private Handler delayHandler=new Handler();
	private TextView mTodayCaloriesView;
    private ProfileTableObserver mStepGoalTableObserver;
    private TextView stepGoalTv;
	private TextView mstepsTv;
	private BarChart mchart;
	private TimeLineRelativeLayout mtimeline;

    private TextView tomorrowGoalTv;
    private LinearLayout tomorrowGoalWithLL, tomorrowGoalWithoutLL;

    private String TIMELINE_RIGHTVIEW_TAG = "timeline_right_view_tag_";
    private String TIMELINE_LEFT_CALENDAR_TAG = "timeline_left_calendar_tag_";

    private long mTodaySteps = 0;
    private long mTargetSteps = 0;
    private long mCaloriesBurned = 0;

	private Runnable delayRunnable=new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			showIndicator();
		}
		
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("smile", "TargetStatusFragment onCreate");
	}

	public static TargetStatusFragment newInstance(Intent intent){
        TargetStatusFragment f = new TargetStatusFragment();
        return f;
	}
	
	private void showIndicator(){
		if(getActivity()!=null){
			getActivity().findViewById(id.indicator).setVisibility(View.VISIBLE);
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view=inflater.inflate(R.layout.target_status_layout, null);
		mTodayCaloriesView=(TextView) view.findViewById(R.id.today_calories);
		final ScrollView scroll=(ScrollView) view.findViewById(R.id.scrollview_container);
		scroll.getViewTreeObserver().addOnScrollChangedListener(new OnScrollChangedListener(){

			int mLastScrollY=0;
			
			@Override
			public void onScrollChanged() {
				// TODO Auto-generated method stub
				if(scroll.getScrollY()-mLastScrollY!=0){
					if(getActivity()!=null){
						getActivity().findViewById(R.id.indicator).setVisibility(View.INVISIBLE);
						delayHandler.removeCallbacks(delayRunnable);
						delayHandler.postDelayed(delayRunnable, 1000);		
					}
				}
				else{
					showIndicator();
				}
				mLastScrollY=scroll.getScrollY();
			}
			
		});
		mstepsTv = (TextView)view.findViewById(R.id.status);
		stepGoalTv = (TextView)view.findViewById(R.id.step_goal);

		mchart=(BarChart) view.findViewById(R.id.bar_chart);
		mtimeline=(TimeLineRelativeLayout) view.findViewById(R.id.step_count_timeline);
        // Register ContentObserver
        mStepGoalTableObserver = new ProfileTableObserver(mHandler);
        getActivity().getContentResolver().registerContentObserver(ProfileTable.TABLE_URI, true, mStepGoalTableObserver);

		EBCommandUtils.getFitnessStep(this.getClass().getName());

        tomorrowGoalTv = (TextView) view.findViewById(id.tomorrow_goal);
        tomorrowGoalWithLL = (LinearLayout) view.findViewById(id.tomorrow_goal_with);
        tomorrowGoalWithoutLL = (LinearLayout) view.findViewById(id.tomorrow_goal_without);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState!=null)
		{
			mTargetSteps = savedInstanceState.getLong(Key_Target);
			mTodaySteps =  savedInstanceState.getLong(Key_Toady);
		}
		Log.i("smile", "TargetStatusFragment onViewCreated " + mTodaySteps+" "+mTargetSteps);
		updateTodaySteps();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		if(mTodaySteps>0)
		{
			outState.putLong(Key_Toady,mTodaySteps);
		}
		if(mTargetSteps>0)
		{
			outState.putLong(Key_Target,mTargetSteps);
		}
		super.onSaveInstanceState(outState);
		Log.i("smile", "TargetStatusFragment onSaveInstanceState");
	}

	@Override
	public void onResume() {
		super.onResume();
		ProfileModel p = new ProfileModel();
		p.reset();
        mTargetSteps = StepHelper.getTargetSteps();
		Log.i("smile", "TargetStatusFragment onResume");
//        Cursor cursor = getActivity().getContentResolver().query(WellnessProvider.COUNT_STEP_COLARIES_URI,null,null,null,null);
//        if(cursor.moveToNext()){
//            mTodaySteps = cursor.getInt(cursor.getColumnIndex(WellnessProvider.COLUMN_TOTAL_STEP));
//            mCaloriesBurned =  cursor.getInt(cursor.getColumnIndex(WellnessProvider.COLUMN_CALORIES_BURNED));
//        }
//        cursor.close();

		mTodaySteps = StepHelper.getTodaySteps();
		mCaloriesBurned = ProfileHelper.getWalkCalories(mTodaySteps);
		updateTodaySteps();
	}

    private void updateTodaySteps()
    {
		if(!isVisible())
		{
			Log.e("smile", "calcStepsTvSize not isadded");
			return ;
		}
        //why nullpointer exception here ?
        if(mstepsTv != null) {
            //mTodaySteps = 1234567891;
            float size  = calcStepsTvSize(Utility.commaNumber(mTodaySteps));
            mstepsTv.setTextSize(size);

            mstepsTv.setText(Utility.commaNumber(mTodaySteps));
            String text = String.format(getString(R.string.text_step_goal), Utility.commaNumber(mTargetSteps));
            stepGoalTv.setText(text);
            addChartData();
            addTimeLine();
        }
    }
    
    private float calcStepsTvSize(String steps){

        Paint paint = new Paint();
        paint.setTextSize(getResources().getDimension(R.dimen.target_status_text_size));
        float stepsWidth=paint.measureText(steps);

        float maxWidth = getResources().getDimension(R.dimen.status_tv_maxwidth)-getResources().getDimension(R.dimen.target_status_text_size)*2;

        float rato = 1.0f;
        if(maxWidth < stepsWidth){
            rato = maxWidth/stepsWidth;
        }
        return  getResources().getDimensionPixelOffset(R.dimen.target_status_text_size)*rato;
    }



	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

			Log.i("smile", "handleMessage ");
			if(msg.what == ProfileTableObserver.PROFILE_CHANGE) {
                showTomorrowGoalLayoutOrNot(true);
            }
        }
    };


    private void showTomorrowGoalLayoutOrNot(boolean isDataChange) {
		if(!isVisible())
		{
			Log.i("TargetFrament","showTomorrowGoalLayoutOrNot not attach to activity");
			return;
		}
        int tomorrowGoal = -1;

  		ProfileDao profileDao = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
		List<Profile> profilelist = profileDao.loadAll();
        if(profilelist!=null && profilelist.size() > 0){
			Profile p = profilelist.get(0);
			Integer nextgoal = p.getNext_step_goal();
            tomorrowGoal = nextgoal== null ? -1: nextgoal; // stepGoalCursor.getInt(stepGoalCursor.getColumnIndex(ProfileTable.COLUMN_NEXT_STEP_GOAL));
            if(isDataChange) {
                int stepGoal = p.getStep_goal(); //stepGoalCursor.getInt(stepGoalCursor.getColumnIndex(ProfileTable.COLUMN_STEP_GOAL));
                String text=String.format(getString(R.string.text_step_goal), Utility.commaNumber(stepGoal));
                stepGoalTv.setText(text);
            }
        }
		AsusLog.i("smile","tomorrow goal: "+tomorrowGoal);
        if(tomorrowGoal == -1) {
            tomorrowGoalWithLL.setVisibility(View.GONE);
            tomorrowGoalWithoutLL.setVisibility(View.VISIBLE);
        } else {
            tomorrowGoalWithLL.setVisibility(View.VISIBLE);
            tomorrowGoalWithoutLL.setVisibility(View.GONE);
            tomorrowGoalTv.setText(getString(R.string.tomorrow_goal) + "\n" + tomorrowGoal + " " + getString(R.string.steps));
        }
        //stepGoalCursor.close();
    }


	
	private void addChartData(){

		long nowTime=System.currentTimeMillis();
		long nowZeroTime = Utility.getMidnightMilles(nowTime)  ;
		long lastSevenZeroTime = nowZeroTime - StepCountManager.ONE_DAY_MILLIES*6;
		getWeekDayTotalSteps(lastSevenZeroTime,nowZeroTime);
//		for(int i=0;i<7;i++){
//
//			weekSteps[6-i]= getWeekDayTotalSteps(nowTime);
//			nowTime-=86400000;
//		}
		mchart.setWeekSteps(weekSteps);
	}
	
	private void addTimeLine(){

		mtimeline.setNumDots(7);
		mtimeline.setLineLength(getResources().getDimensionPixelSize(R.dimen.timeline_line_length));
		addTimelineLeft(mtimeline);
		addTimelineRight(mtimeline);
	}
	
	private void addTimelineLeft(TimeLineRelativeLayout timeLine){
		Calendar cal=Calendar.getInstance();
		for(int i=0;i<7;i++){
            String tag = TIMELINE_LEFT_CALENDAR_TAG + i;
            View viewOld =  timeLine.findViewWithTag(tag);
            if(viewOld!=null) {
                //Log.i("emily","removeView, tag = " + tag);
                timeLine.removeView(viewOld);
            }

			TextView text=new TextView(getActivity());
            text.setTag(tag);
			text.setText(getWeekString(cal));
			text.setGravity(Gravity.CENTER);
			text.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
					MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
			
			RelativeLayout.LayoutParams params=new LayoutParams(timeLine.getDotsPosition(i).x, LayoutParams.WRAP_CONTENT);
			params.topMargin=timeLine.getDotsPosition(i).y-text.getMeasuredHeight()/2;
			text.setLayoutParams(params);
			
			timeLine.addView(text);
			cal.add(Calendar.DATE, -1);
		}
	}
	
	private void addTimelineRight(TimeLineRelativeLayout timeLine){
		Calendar cal=Calendar.getInstance();
		Profile profile = ProfileHelper.getStandardProfile();
		for(int i=0;i<7;i++){
			int steps=weekSteps[6-i];

			String tag = TIMELINE_RIGHTVIEW_TAG + i;
			LayoutInflater inflator=LayoutInflater.from(getActivity());
            View viewOld =  timeLine.findViewWithTag(tag);
            if(viewOld!=null) {
                //Log.i("emily","removeView, tag = " + tag);
                timeLine.removeView(viewOld);
            }

			View view=inflator.inflate(R.layout.timeline_step_calories, null);
            view.setTag(tag);
			TextView dateText=(TextView) view.findViewById(R.id.date_text);
			TextView stepText=(TextView) view.findViewById(R.id.steps_message);
			TextView caloriesText=(TextView) view.findViewById(R.id.calories_message);
           
			if(i==0){
				dateText.setTypeface(Typeface.DEFAULT_BOLD);
				stepText.setTypeface(Typeface.DEFAULT_BOLD);
			}

			String stepsString=String.valueOf(Utility.commaNumber(steps));
			//(steps>=StepCountManager.MAX_STEPS?(Utility.commaNumber(12500)+"+"):String.valueOf(Utility.commaNumber(steps)));
			stepText.setText(stepsString);
			
			dateText.setText(Utility.getDateTime(cal.getTimeInMillis(), "MM/dd"));
			dateText.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
					MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));

            caloriesText.setVisibility(View.VISIBLE);

            int heightInCM = profile.getHeight();
            if(profile.getHeight_unit()==ProfileHelper.HEIGHT_UNIT_FT){
                float ft=ProfileHelper.InchToFt(heightInCM);
                heightInCM=(int) Math.round(ProfileHelper.ftToCm(ft));
            }
            int weightInKG = profile.getWeight();
            if(profile.getWeight_unit() ==ProfileHelper.WEIGHT_UNIT_LBS){
                weightInKG=(int) Math.round(ProfileHelper.LbsToKg(profile.getWeight()));
            }

            caloriesText.setText(Utility.commaNumber(ProfileHelper.getWalkCalories(heightInCM, steps, weightInKG))+" "+getResources().getString(R.string.calories_unit));
            if(i==0){
                mTodayCaloriesView.setVisibility(View.VISIBLE);
                mTodayCaloriesView.setText(Utility.commaNumber((long)mCaloriesBurned)+" "+getResources().getString(R.string.calories_unit));
            }

			RelativeLayout.LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin=timeLine.getDotsPosition(i).x+getResources().getDimensionPixelSize(R.dimen.timeline_left_right_margin);
			params.rightMargin=getResources().getDimensionPixelSize(R.dimen.timeline_left_right_margin);
			params.topMargin=timeLine.getDotsPosition(i).y-dateText.getMeasuredHeight()/2;
			view.setLayoutParams(params);	
			timeLine.addView(view);
			cal.add(Calendar.DATE, -1);
		}
	}
	
	private void getWeekDayTotalSteps(long startTime,long endTime){


		//StepHelper.getDaysStepConts(startTime,endTime);
		long start=startTime;
		long end = startTime + StepCountManager.ONE_DAY_MILLIES;
		SimpleDateFormat dateFormat = new SimpleDateFormat(StepCountManager.DATE_FORMAT);

//        String todayDateString=Utility.getDateTime(System.currentTimeMillis(), "yyyyMMdd");
//        String targetDateString=Utility.getDateTime(endTime, "yyyyMMdd");
//        if(todayDateString.matches(targetDateString)){
//			totalSteps+=StepHelper.getNowSteps(mIntent);
            weekSteps[6] = (int)mTodaySteps;
//        }

		Log.i("smile","start: "+startTime+" end: "+endTime);
		List<Step_count> list = StepHelper.getDaysStepConts(startTime, endTime);
		if(list ==null || list.size() == 0)
		{
			for (int i=0;i<6;i++) {
				weekSteps[i] = 0;
			}
			Log.d("smile", "getWeekDayTotalSteps list zero");
			return  ;
		}
		for (int i=0;i<6;i++)
		{
			weekSteps[i]=0;
			start = startTime+ i * StepCountManager.ONE_DAY_MILLIES;
			end = start + StepCountManager.ONE_DAY_MILLIES;
			Log.d("smile", "getWeekDayTotalSteps : start " + dateFormat.format(start) + " end " + dateFormat.format(end)+"  "+start+"  "+end);
			for(Step_count stepcount: list)
			{
				if(stepcount.getStart()>=start &&  stepcount.getEnd() <end)
				{
					weekSteps[i]+=stepcount.getStep_count();
				}
			}
			Log.i("smile",weekSteps[i]+" step");
		}
	}
	
	private String getWeekString(Calendar cal){
		int week=cal.get(Calendar.DAY_OF_WEEK);
		switch(week){
			case Calendar.SUNDAY:
				return "S";
			case Calendar.MONDAY:
				return "M";
			case Calendar.TUESDAY:
				return "T";
			case Calendar.WEDNESDAY:
				return "W";
			case Calendar.THURSDAY:
				return "T";
			case Calendar.FRIDAY:
				return "F";
			case Calendar.SATURDAY:
				return "S";
			default:
				return "null";
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("smile","Target frament onDestory");
		delayHandler.removeCallbacks(delayRunnable);
		delayHandler= null;
        // Unregister ContentObserver
        getActivity().getContentResolver().unregisterContentObserver(mStepGoalTableObserver);
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		mStepGoalTableObserver = null;
//		mchart = null;
//		mtimeline.removeAllViews();
//		mtimeline = null;
//		tomorrowGoalTv = null;
//		tomorrowGoalWithLL = null;
//		tomorrowGoalWithoutLL = null;
//		mTodayCaloriesView = null;
//		stepGoalTv = null;
//		mstepsTv = null;



	}

	@Override
	public void updateSteps(Intent intent) {
        mTodaySteps = StepHelper.getWalkingStepFromIntent(intent);
        mCaloriesBurned = StepHelper.getBurnedCaloriesFromIntent(intent);
        Log.d(Utility.TAG, "updateSteps :" + mTodaySteps + " mCaloriesBurned: " + mCaloriesBurned);
		updateTodaySteps();
	}


}

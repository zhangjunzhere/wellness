package com.asus.wellness.ui.week;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Property;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.asus.sharedata.SleepData;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.chart.view.CalorieChartWeeklyView;
import com.asus.wellness.chart.view.ExerciseIntensityChartWeeklyView;
import com.asus.wellness.chart.view.RelaxationChartWeeklyView;
import com.asus.wellness.chart.view.ChartWeeklyView;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.sleep.SleepHelper;
import com.asus.wellness.ui.week.LevelDescriptionActivity;
import com.asus.wellness.ui.week.TimeLineWeeklyRelativeLayout;
import com.asus.wellness.utils.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;


public class WeeklyPageFragment extends Fragment implements OnClickListener, OnScrollChangedListener{

	public static final String NOW_WEEK="now_week";
	private Week mWeek=new Week();
	private ArrayList<DayInfo> arrayDayInfo=new ArrayList<DayInfo>();
	private int age;
	private int weightInKG;
	private int heightInCM;
	int mTotalCalories=0;
	View rootView;
	
	private ScrollView mScrollView;
	
	private String[] mDaysString;
	
	private CalorieChartWeeklyView mCalorieChartWeeklyView,mCalorieChartWeeklyViewNoData ;
	
	private ExerciseIntensityChartWeeklyView mExerciseIntensityChartWeeklyView;
	private RelaxationChartWeeklyView mRelaxationChartWeeklyView;
	
	private ImageButton mExerciseIntensityInfo;
	
	private String[] Month = new String[] {
            "N/A", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };
	private TimeLineWeeklyRelativeLayout mStepChartWeeklyView;

	private TextView mStepChartMonthText;
	private AnimatorSet mStepAnimatorSet;
	
	private boolean isFirstRelaxationChartAnimationPlayed = false;
	private boolean isFirstExerciseIntensityChartAnimationPlayed = false;
	private boolean isFirstStepChartAnimationPlayed = false;
	
	private TextView mRelaxationText, mStressText;
	
	public static final int MAX_STEP_CHART_LENGTH=250;
	public static final int MAX_BIKE_CHART_LENGTH=MAX_STEP_CHART_LENGTH;
	public static final int TOTAL_ACTIVITY_CHART_HEIGHT=30;
	
	
	private boolean mHasData;
	private Bundle args;
	private loadWeekInfo mloadWeekInfo;
	//smile_gao
	private LinearLayout mIntensityView =null;
	private LinearLayout mRelaxationView =null;
	//endsmile

    private LinearLayout mSleepView =null;
    private ChartWeeklyView mSleepCharBar;
	private TextView mDeepHoursTv;
	private TextView mLightHoursTv;

	private LinearLayout mWorkoutView =null;
	private ChartWeeklyView mWorkoutCharBar;
	private TextView mPushupTv;
	private TextView mSitupTv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		rootView=inflater.inflate(R.layout.wellness_info_weekly_layout, container, false);
		
		mScrollView = (ScrollView) rootView.findViewById(R.id.weekly_scroller);
		mScrollView.getViewTreeObserver().addOnScrollChangedListener(this);
		
        args = getArguments();
        
        age = args.getInt("age");
        weightInKG = args.getInt("weightInKG");
        heightInCM = args.getInt("heightInCM");
        int pos = args.getInt("pos"); // ?o??View??e????m
        mCalorieChartWeeklyView = (CalorieChartWeeklyView) rootView.findViewById(R.id.calorie_chart_weekly_view);
        mCalorieChartWeeklyViewNoData = (CalorieChartWeeklyView) rootView.findViewById(R.id.calorie_chart_weekly_view_no_data);
        
        mExerciseIntensityChartWeeklyView = (ExerciseIntensityChartWeeklyView) rootView.findViewById(R.id.exercise_intensity_chart_weekly_view);
        mExerciseIntensityChartWeeklyView.setOnClickListener(this);
        
        mRelaxationChartWeeklyView = (RelaxationChartWeeklyView) rootView.findViewById(R.id.relaxation_chart_weekly_view);
        mRelaxationChartWeeklyView.setOnClickListener(this);
        
        ///mStepChartWeeklyViewNoData = (TimeLineWeeklyRelativeLayout)rootView.findViewById(R.id.activity_chart_container_no_data);
        
        mStepChartWeeklyView = (TimeLineWeeklyRelativeLayout)rootView.findViewById(R.id.activity_chart_container);
        mStepChartWeeklyView.setOnClickListener(this);
        
        mStepChartMonthText = (TextView)rootView.findViewById(R.id.weekly_activity_month_text); 
        
        mRelaxationText = (TextView) rootView.findViewById(R.id.weekly_info_relaxation_text);
        mRelaxationText.setOnClickListener(this);
        
        mStressText = (TextView) rootView.findViewById(R.id.weekly_info_stress_text);
        mStressText.setOnClickListener(this);
        
        
        mExerciseIntensityInfo = (ImageButton) rootView.findViewById(R.id.ic_exercise_intensity_i);
        mExerciseIntensityInfo.setOnClickListener(this);
        mHasData = false;
		//smilegao
		mIntensityView = (LinearLayout)rootView.findViewById(R.id.intensityview);
		mRelaxationView = (LinearLayout)rootView.findViewById(R.id.relaxationview);

		// sleep++++
		mDeepHoursTv = (TextView)rootView.findViewById(R.id.weekly_sleep_deep_hours);
		mLightHoursTv = (TextView)rootView.findViewById(R.id.weekly_sleep_light_hours);
        mSleepView = (LinearLayout)rootView.findViewById(R.id.sleepview);
        mSleepCharBar = (ChartWeeklyView)mSleepView.findViewById(R.id.sleep_chart_weekly_view);
		int width = Utility.getScreenWidth(getActivity()) - (int)getResources().getDimension(R.dimen.weekly_page_margin_left)-(int)getResources().getDimension(R.dimen.weekly_page_margin_right) ;
		int barHeight = (int)getResources().getDimension(R.dimen.sleep_barchart_height);//chart_calorie_background_height);
		if (Utility.isPadDevice(getActivity())){
			width = (int)getResources().getDimension(R.dimen.weekly_activity_view_width);
		}
		mSleepCharBar.initBarChart(rootView,ChartWeeklyView.SLEEP_TYPE, width, barHeight, 0);
		// Sleep----

		// Workout++++
		mPushupTv = (TextView)rootView.findViewById(R.id.weekly_pushup_count);
		mSitupTv = (TextView)rootView.findViewById(R.id.weekly_situp_counts);
		mWorkoutView = (LinearLayout)rootView.findViewById(R.id.workoutview);
		mWorkoutCharBar = (ChartWeeklyView)mWorkoutView.findViewById(R.id.workout_chart_weekly_view);
		mWorkoutCharBar.initBarChart(rootView,ChartWeeklyView.WORKOUTP_TYPE, width, barHeight, 0);
		// Workout----

		runLoadWeekDataTask();

        return rootView;
	}
	void runLoadWeekDataTask()
	{
		if(mloadWeekInfo == null)
		{
			mloadWeekInfo = new loadWeekInfo();
			mloadWeekInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			if (mloadWeekInfo.isCancelled() || mloadWeekInfo.getStatus().equals(AsyncTask.Status.FINISHED))
			{
				mloadWeekInfo = new loadWeekInfo();
				mloadWeekInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		//EventBus.getDefault().registerSticky(this);
	}

	@Override
	public void onPause() {
		super.onPause();
	//	EventBus.getDefault().unregister(this);
	}
	public void onEvent(Device device)
	{
//		setupExerciseIntensityView();
//		setupRelaxationView();
		//fix bug  632277
//		runLoadWeekDataTask();
	}

	
	private void setupNoDataView() {
		//mCalorieChartWeeklyViewNoData.setDateString(mDaysString);
		
		if(getResources().getConfiguration().smallestScreenWidthDp >=800)
		{
			setActivityWeekLineViewNoData();
			rootView.findViewById(R.id.exercise_intensity_chart_weekly_no_data_view).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.relaxation_chart_weekly_no_data_view).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.calorie_chart_weekly_view_no_data).setVisibility(View.VISIBLE);
		}
		else
		{
			rootView.findViewById(R.id.has_data_container).setVisibility(View.GONE);
			rootView.findViewById(R.id.none_data_container).setVisibility(View.VISIBLE);
			mCalorieChartWeeklyViewNoData.setDateString(mDaysString);
		}
	}

	private void setupCalorieView() {
	    DayInfo info;
        Float[] datas =new Float[arrayDayInfo.size()];
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int currentIndex = 0;
		//smile_gao fix 635625
		mTotalCalories=0;
		//end smile
        for (int i = 0 ; i < arrayDayInfo.size();i++){
            info = arrayDayInfo.get(i);
            int bikeCalories=Utility.getBikeCalories(arrayDayInfo.get(i).bikeHour, weightInKG);
            float walkCalories=Utility.getWalkCalories(heightInCM, arrayDayInfo.get(i).stepCounts, weightInKG);
            float totalCalories=walkCalories;
            mTotalCalories+=totalCalories;
			// fix 22 step bug cal show 0 , lesss < 1 ,show 0
			if(totalCalories < 1)
			{
				totalCalories = 0;
			}
            datas[i] = new Float(totalCalories);
            if (info.month == month && info.day == day){
                currentIndex = i;
            }
        }
        mCalorieChartWeeklyView.setDateString(mDaysString);
        mCalorieChartWeeklyView.setDataValues(datas);
        mCalorieChartWeeklyView.setDateSelectorIndex(currentIndex);
    }

    private void setupRelaxationView() {

		if(!WApplication.getInstance().getConnectedDevice().getIsRobin())
		{
			mRelaxationView.setVisibility(View.GONE);
			return;
		}
		else
		{
			mRelaxationView.setVisibility(View.VISIBLE);
		}

        DayInfo info;
        Float[] datas =new Float[arrayDayInfo.size()];
        for (int i = 0 ; i < arrayDayInfo.size();i++){
            info = arrayDayInfo.get(i);
            datas[i] = new Float(info.avgRelaxation);
        }
        mRelaxationChartWeeklyView.setDateString(mDaysString);

        mRelaxationChartWeeklyView.setDataValues(datas);

        mRelaxationText.setActivated(true);
        mStressText.setActivated(false);
    }
	  
	private void setupStressView() {
        DayInfo info;
        Float[] datas =new Float[arrayDayInfo.size()];
        for (int i = 0 ; i < arrayDayInfo.size();i++){
            info = arrayDayInfo.get(i);
            datas[i] = new Float(info.avgStress);
        }
        mRelaxationChartWeeklyView.setDateString(mDaysString);
        mRelaxationChartWeeklyView.setDataValues(datas);
        
        mRelaxationText.setActivated(false);
        mStressText.setActivated(true);
    }

    private void setupExerciseIntensityView() {
		if(!WApplication.getInstance().getConnectedDevice().getIsRobin())
		{
			mIntensityView.setVisibility(View.GONE);
			return;
		}
		else
		{
			mIntensityView.setVisibility(View.VISIBLE);
		}

	    ArrayList<Float[]> data = new ArrayList<Float[]>();
	    DayInfo info;
	    List<Integer> heartRateList;
	    Float[] intensities;
	    for (int i = 0 ; i < arrayDayInfo.size();i++){
	        info = arrayDayInfo.get(i);
	        heartRateList = info.heartRates;
	        intensities = new Float[heartRateList.size()];
	        for (int j = 0 ; j < heartRateList.size(); j++){
	            intensities[j] = Utility.getIntensityLevelValue(age, heartRateList.get(j));
	        }
	        data.add(intensities);
	    }
	    mExerciseIntensityChartWeeklyView.setDateString(mDaysString);
	    mExerciseIntensityChartWeeklyView.setDataValues(data);
    }

    public static class DayInfo{
		public int stepCounts;
		public float bikeHour;
		public int bikeDistance;
		public int carDistance;
		public List<Integer> heartRates;
		public int avgHeartRate;
		public float avgRelaxation;
		public float avgStress;
		public int month;
		public int day;
        public Boolean hasData;
        public long deepMins;
		public long lightMins;
		public long totalMins;
		public long pushupCounts;
		public long situpCounts;
	}
	
	private ArrayList<DayInfo> getAllWeekDataInfo(Week week){
		ArrayList<DayInfo> arrayList=new ArrayList<DayInfo>();
		Calendar cal=Calendar.getInstance();
		Calendar endCalendar=Calendar.getInstance();//??o?{?b???
		endCalendar.set(week.endYear, week.endMonth - 1, week.endDay, 23, 59, 59);
		for(int i=0;i<7;i++){
			long startTimeMilli, endTimeMilli;
			if(i==0){
				cal.set(week.startYear, week.startMonth-1, week.startDay,0,0,0);
				startTimeMilli=cal.getTimeInMillis()/1000*1000;
				
				cal.set(week.startYear, week.startMonth-1, week.startDay, 23, 59, 59);
				endTimeMilli=cal.getTimeInMillis();
			}
			else{
				if(cal.after(endCalendar)){
					break;
				}
				cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE),0,0,0);
				startTimeMilli=cal.getTimeInMillis()/1000*1000;
				
				cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE), 23, 59, 59);
				endTimeMilli=cal.getTimeInMillis();
			}

			DayInfo dayInfo= ParseDataManager.getInstance().getDayInfo( getActivity(),  startTimeMilli,   endTimeMilli);

			dayInfo.month=cal.get(Calendar.MONTH)+1;
			dayInfo.day=cal.get(Calendar.DATE);

            mHasData = dayInfo.hasData;
			arrayList.add(dayInfo);
			cal.add(Calendar.DATE, 1);
		}
		return arrayList;
	}

	public class Week{
		public int startYear;
		public int startMonth;
		public int startDay;
		public int endYear;
		public int endMonth;
		public int endDay;
		public int curWeek;
	}
	
	private void setActivityWeekLineViewNoData()
	{
		LinearLayout l = (LinearLayout) rootView.findViewById(R.id.activity_chart_layout);
		View background = getActivity().getLayoutInflater().inflate(R.layout.weekly_activity_no_data, null);
		l.addView(background);
	}
	
	private void setActivityWeekLineView(){
			mStepChartWeeklyView.setNumDots(arrayDayInfo.size());
			mStepChartWeeklyView.setLineLength(getResources().getDimensionPixelSize(R.dimen.weekly_activity_timeline_length));
			mStepChartWeeklyView.setTimeLineLeftMargin(getResources().getDimensionPixelSize(R.dimen.weekly_activity_timeline_left_margin));
			mStepChartWeeklyView.setTimeLineTopMargin(getResources().getDimensionPixelSize(R.dimen.weekly_activity_timeline_top_margin));
			mStepChartWeeklyView.setTopSeparateLine(true);
			
			if (arrayDayInfo.size() > 0){
				// modify by benson
				String s;
				if(Month[arrayDayInfo.get(0).month].compareTo(Month[arrayDayInfo.get(arrayDayInfo.size()-1).month]) == 0)
				{
					s = Month[arrayDayInfo.get(0).month];
				}
				else
				{
					s = Month[arrayDayInfo.get(0).month] +" - "+Month[arrayDayInfo.get(arrayDayInfo.size()-1).month];
				}
				mStepChartMonthText.setText(s);
			}

			LayoutParams params=(LayoutParams) mStepChartWeeklyView.getLayoutParams();
			params.height=mStepChartWeeklyView.getRequiredHeight();
			addActivityItem(mStepChartWeeklyView);
	}
		
	
	
	private synchronized void addActivityItem(TimeLineWeeklyRelativeLayout timeLine){
		int totalSteps=0;
		List<Animator> animators=new ArrayList<Animator>();
		StringEvaluator evaluator = new StringEvaluator();
		mStepAnimatorSet = new AnimatorSet();
		int maxStep = 0;
		int maxStepIndex = -1;
		//fix bug
		timeLine.removeAllViews();
		for(int i=0;i<arrayDayInfo.size();i++){
			if (arrayDayInfo.get(i).stepCounts > maxStep){
				maxStep = arrayDayInfo.get(i).stepCounts;
				maxStepIndex = i;
			}
		}

		for(int i=0;i<arrayDayInfo.size();i++){
			DayInfo dayInfo=arrayDayInfo.get(i);
			//date
			View dateView=getActivity().getLayoutInflater().inflate(R.layout.weekly_activity_text_layout, null);
			TextView text=(TextView)dateView.findViewById(R.id.date_step_text);
			if (i > 0 && dayInfo.month != arrayDayInfo.get(i-1).month){
				text.setText(dayInfo.month+"/"+dayInfo.day);
			}else{
				text.setText(""+dayInfo.day);
			}
			text.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
					     MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
			
			RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.leftMargin=(timeLine.getDotsPosition(i).x-text.getMeasuredWidth())/2;
			params.topMargin=timeLine.getDotsPosition(i).y-text.getMeasuredHeight()/2;
			text.setLayoutParams(params);
		
			timeLine.addView(text);
			
			//chart
			View view=new View(getActivity());
			int defaultLength=getActivity().getResources().getDimensionPixelSize(R.dimen.weekly_activity_step_chart_length);
			float calWidth=(float)defaultLength*Math.min(dayInfo.stepCounts,12500)/12500;
			int chartWidth=(int) calWidth;
			int chartHeight=getResources().getDimensionPixelSize(R.dimen.weekly_activity_step_chart_height);
			if ( i == maxStepIndex){
				view.setId(1024);
			}
			
			RelativeLayout.LayoutParams chartParams=new RelativeLayout.LayoutParams(chartWidth, chartHeight);
			chartParams.leftMargin=timeLine.getDotsPosition(i).x+getActivity().getResources().getDimensionPixelSize(R.dimen.weekly_activity_timeline_offset);
			chartParams.topMargin=timeLine.getDotsPosition(i).y-chartHeight/2;
			view.setBackgroundColor(0xff188560);
			view.setLayoutParams(chartParams);
			timeLine.addView(view);
	
			//add animation to list
			view.setPivotX(0);
			ObjectAnimator anim = ObjectAnimator.ofFloat(view, "scaleX", 0.0f,0.0f);
            anim.setDuration(300*i);
            animators.add(anim);
			
			anim = ObjectAnimator.ofFloat(view, "scaleX", 0.0f,1.0f);
			anim.setDuration(1000);
			anim.setStartDelay(300*i);
			animators.add(anim);
			
			//Crown
			if (i == maxStepIndex){
				FrameLayout crownContainer = new FrameLayout(getActivity());
				RelativeLayout.LayoutParams crownContainerLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,view.getMeasuredHeight());
				crownContainerLp.leftMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.weekly_activity_crown_margin_left);
				crownContainerLp.addRule(RelativeLayout.RIGHT_OF, 1024);
				crownContainerLp.addRule(RelativeLayout.ALIGN_TOP, 1024);
				crownContainerLp.addRule(RelativeLayout.ALIGN_BOTTOM, 1024);
				crownContainer.setLayoutParams(crownContainerLp);
				
				ImageView crown = new ImageView(getActivity());
				crown.setImageResource(R.drawable.asus_wellness_ic_calories2);
				FrameLayout.LayoutParams crownLp = new  FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
				crownLp.gravity = Gravity.CENTER;
				crown.setLayoutParams(crownLp);
				
				crownContainer.addView(crown);
				timeLine.addView(crownContainer);
				
				//add animation to list
				anim = ObjectAnimator.ofFloat(crownContainer, "alpha", 0.0f,0.0f);
	            anim.setDuration(300*i);
	            animators.add(anim);
				
				anim = ObjectAnimator.ofFloat(crownContainer, "alpha", 0.0f,1.0f);
				anim.setDuration(1000);
				anim.setStartDelay(300*i);
				animators.add(anim);
			}
			        
			//step count
			View stepView=getActivity().getLayoutInflater().inflate(R.layout.weekly_activity_step_text_layout, null);
			TextView steps=(TextView)stepView.findViewById(R.id.step_text);

			String value = String.valueOf(dayInfo.stepCounts);
			//modify by benson
			steps.setText(String.format("%,d",dayInfo.stepCounts));
			steps.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
					MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
			RelativeLayout.LayoutParams stepsParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			stepsParams.topMargin=timeLine.getDotsPosition(i).y-steps.getMeasuredHeight()/2;
			stepsParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			steps.setLayoutParams(stepsParams);
			timeLine.addView(steps);
            
            //add animation to list
            anim = ObjectAnimator.ofObject(steps, StringEvaluator.TEXT , evaluator, "0","0");
            anim.setDuration(300*i);
            animators.add(anim);
            
            anim = ObjectAnimator.ofObject(steps, StringEvaluator.TEXT , evaluator, "0",value);
            anim.setDuration(1000);
            anim.setStartDelay(300*i);
            animators.add(anim);
			
			totalSteps+=dayInfo.stepCounts;
		}
		
		//Add animation list to layout
		mStepAnimatorSet.playTogether(animators);
		
		TextView totalStepsText=(TextView)rootView.findViewById(R.id.tv_total_steps);
		//totalStepsText.setText(Utility.intToStr(totalSteps));
		totalStepsText.setText(String.format("%,d",totalSteps));
	}
	
	public Week getWeekInfo(int nowWeek){
        Calendar cal = Calendar.getInstance();
        Week week = new Week();
        if (cal.getFirstDayOfWeek() == Calendar.SUNDAY && 
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DATE, -7);
        }
        if (nowWeek == 0) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // this monday
            cal.add(Calendar.DATE, 6); // this sunday
            week.endYear = cal.get(Calendar.YEAR);
            week.endMonth = cal.get(Calendar.MONTH) + 1;
            week.endDay = cal.get(Calendar.DATE);
            cal.add(Calendar.DATE, -6); // this monday
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // this monday
        for (int i = 0; i < -nowWeek; i++) {
            cal.add(Calendar.DATE, -1); // last sunday
            week.endYear = cal.get(Calendar.YEAR);
            week.endMonth = cal.get(Calendar.MONTH) + 1;
            week.endDay = cal.get(Calendar.DATE);
            cal.add(Calendar.DATE, -6); // last monday
        }
        week.startYear = cal.get(Calendar.YEAR);
        week.startMonth = cal.get(Calendar.MONTH) + 1;
        week.startDay = cal.get(Calendar.DATE);
        week.curWeek = cal.get(Calendar.WEEK_OF_YEAR);
        return week;
	}

    @Override
    public void onClick(View v) {
        if (v == mRelaxationText){
            setupRelaxationView();
        }else if (v == mStressText){
            setupStressView();
        }else if (v == mRelaxationChartWeeklyView){
            //mRelaxationChartWeeklyView.playAnimation();
        }else if (v == mExerciseIntensityChartWeeklyView){
            //mExerciseIntensityChartWeeklyView.playAnimation();
        }else if (v == mStepChartWeeklyView){
            //mStepAnimatorSet.start();
        }else if (v == mExerciseIntensityInfo){
        	Intent intent = new Intent(getActivity(),LevelDescriptionActivity.class);
        	startActivity(intent);
        	getActivity().overridePendingTransition(R.anim.slide_in_from_right,R.anim.still);
        }
    }

    @Override
    public void onScrollChanged() {
        if (!isFirstExerciseIntensityChartAnimationPlayed){
            Rect exerciseRect = new Rect();
            mScrollView.getHitRect(exerciseRect);
            if (mExerciseIntensityChartWeeklyView.getLocalVisibleRect(exerciseRect)
                    && exerciseRect.height() > mExerciseIntensityChartWeeklyView.getHeight()/3){
                isFirstExerciseIntensityChartAnimationPlayed = true;
                mExerciseIntensityChartWeeklyView.playAnimation();
            }
        }
        
        if (!isFirstRelaxationChartAnimationPlayed){
            Rect relaxationRect = new Rect();
            mScrollView.getHitRect(relaxationRect);
            if (mRelaxationChartWeeklyView.getLocalVisibleRect(relaxationRect)
                    && relaxationRect.height() > mRelaxationChartWeeklyView.getHeight()/3){
                isFirstRelaxationChartAnimationPlayed = true;
                mRelaxationChartWeeklyView.playAnimation();
            }
        }
        
        if (!isFirstStepChartAnimationPlayed){
            Rect stepRect = new Rect();
            mScrollView.getHitRect(stepRect);
            if (mStepChartWeeklyView.getLocalVisibleRect(stepRect)
                    && stepRect.height() > mStepChartWeeklyView.getHeight()/3){
                isFirstStepChartAnimationPlayed = true;
                mStepAnimatorSet.start();
            }
        }
    }
    
    private static class StringEvaluator implements TypeEvaluator<CharSequence>{
        public static final String PROPERTY_NAME = "text";
        public static final Property<TextView, CharSequence> TEXT =
                Property.of(TextView.class, CharSequence.class, PROPERTY_NAME);
        
        @Override
        public CharSequence evaluate(float fraction, CharSequence startValue,
                CharSequence endValue) {
        	
            int value = (int) (fraction*Integer.valueOf(endValue.toString()));
            //modify by benson
            return String.format("%,d",value);
            //return String.valueOf(value);
        }
        
    }
    
    
    private class loadWeekInfo extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			rootView.findViewById(R.id.has_data_container).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.has_loading_data).setVisibility(View.GONE);
			mDaysString = new String[7];
	        DayInfo info = null;
	        for (int i = 0 ; i < arrayDayInfo.size(); i++){
	            info = arrayDayInfo.get(i);
	            if (i == 0 || info.month != arrayDayInfo.get(i-1).month){
	            	mDaysString[i] = info.month+"/"+info.day;
	            }else{
	            	mDaysString[i] = ""+info.day;
	            }
	            
	        }
	      //  if(mHasData){
			try {
				setupCalorieView();
				setActivityWeekLineView();
				setupExerciseIntensityView();
				setupRelaxationView();
				setupSleepView();
				setupWorkoutView();
			}catch (Exception e)
			{
				Log.i("smile",e.getMessage());
			}
	       /* }
	        else{

	        	setupNoDataView();
	        	rootView.findViewById(R.id.calorie_chart_weekly_view).setVisibility(View.GONE);
	        	rootView.findViewById(R.id.relaxation_chart_weekly_view).setVisibility(View.GONE);
	        	rootView.findViewById(R.id.exercise_intensity_chart_weekly_view).setVisibility(View.GONE);
	        	rootView.findViewById(R.id.activity_chart_container).setVisibility(View.GONE); 	
	        }*/
			//
        	TextView text=(TextView)rootView.findViewById(R.id.tv_calories);
        	text.setText(String.format("%,d", mTotalCalories));
			//
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			//
			//Log.d("PHH", "PRE");
			mWeek=getWeekInfo(args.getInt(NOW_WEEK));
			Log.i("smile","week onPreExecute "+Utility.getCurrTime());
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Log.i("smile","week doInBackground1 "+Utility.getCurrTime());
			arrayDayInfo=getAllWeekDataInfo(mWeek);
			Log.i("smile","week doInBackground2 "+Utility.getCurrTime());
			return null;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
    }

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		if( mloadWeekInfo != null)
		{
			mloadWeekInfo.cancel(true);
		}
		super.onStop();
	}

    private void setupSleepView() {
		if (WApplication.getInstance().getConnectedDevice().getIsRobin()) {
			mSleepView.setVisibility(View.GONE);
			return;
		} else {
			mSleepView.setVisibility(View.VISIBLE);
		}
		DayInfo info;
		Long[] lightDatas = new Long[arrayDayInfo.size()];
		Long[] deepDatas = new Long[arrayDayInfo.size()];
		Long[] totalDatas = new Long[arrayDayInfo.size()];
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);

		int currentIndex = 0;
		//int sleepDays = 0;

		for (int i = 0; i < arrayDayInfo.size(); i++) {
			info = arrayDayInfo.get(i);
			lightDatas[i] = new Long(info.lightMins);
			deepDatas[i] = new Long(info.deepMins);
			totalDatas[i] = new Long(info.totalMins);
			if (info.month == month && info.day == day) {
				currentIndex = i;
			}
			/*if (totalDatas[i] != 0) {
				sleepDays++;
			}*/
		}

		mSleepCharBar.setDateString(mDaysString);
		mSleepCharBar.setSleepDatas(lightDatas, deepDatas, totalDatas);
		mSleepCharBar.setDateSelectorIndex(currentIndex);

		/*long lightAnalysis = 0;
		long deepAanalysis = 0;
		if(sleepDays!= 0){
			for (int i = 0; i < totalDatas.length; i++) {
				lightAnalysis += lightDatas[i];
				deepAanalysis += deepDatas[i];
			}
			lightAnalysis /= sleepDays;
			deepAanalysis /= sleepDays;
		}

		mLightHoursTv.setText(Utility.formatSleepTimes(lightAnalysis, getActivity()));
		mDeepHoursTv.setText(Utility.formatSleepTimes(deepAanalysis, getActivity()));*/
	}

	private void setupWorkoutView() {
		if (WApplication.getInstance().getConnectedDevice().getIsRobin()) {
			mWorkoutView.setVisibility(View.GONE);
			return;
		} else {
			mWorkoutView.setVisibility(View.VISIBLE);
		}

		int currentIndex = 0;
		/*long pushups = 0;
		long situps = 0;*/
		Long[] pushupsDatas = new Long[arrayDayInfo.size()];
		Long[] situpsDatas = new Long[arrayDayInfo.size()];
		Long[] totalDatas = new Long[arrayDayInfo.size()];
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);

		DayInfo info;
		for (int i = 0; i < arrayDayInfo.size(); i++) {
			info = arrayDayInfo.get(i);
			pushupsDatas[i] = new Long(info.pushupCounts);
			situpsDatas[i] = new Long(info.situpCounts);
			totalDatas[i] = pushupsDatas[i] + situpsDatas[i];
			if (info.month == month && info.day == day) {
				currentIndex = i;
			}
			/*pushups += info.pushupCounts;
			situps += info.situpCounts;*/
		}

		mWorkoutCharBar.setDateString(mDaysString);
		mWorkoutCharBar.setWorkoutDatas(pushupsDatas,situpsDatas, totalDatas);
		mWorkoutCharBar.setDateSelectorIndex(currentIndex);

		/*mPushupTv.setText(String.valueOf(pushups));
		mSitupTv.setText(String.valueOf(situps));*/
	}
}

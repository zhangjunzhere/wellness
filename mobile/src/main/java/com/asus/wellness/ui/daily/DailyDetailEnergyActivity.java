package com.asus.wellness.ui.daily;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.Day;
import com.asus.wellness.ParseDataManager.RelaxInfo;
import com.asus.wellness.ParseDataManager.StressInfo;
import com.asus.wellness.R;
import com.asus.wellness.ui.BaseActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class DailyDetailEnergyActivity extends BaseActivity {

	private ParseDataManager pfd;
	private ArrayList<StressInfo> mStressInfo;
	private ArrayList<RelaxInfo> mRelaxInfo;
	private Day day;

	@Override
	public  String getPageName(){
		return  DailyDetailEnergyActivity.class.getSimpleName();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daily_detail_energy);
		day=(Day) getIntent().getSerializableExtra(DailyDetailActivityActivity.KEY_DAY_INFO);
		setActionBar();
		getDataInfo();
		if(mStressInfo==null && mRelaxInfo==null){
			findViewById(R.id.no_data_container).setVisibility(View.VISIBLE);
			findViewById(R.id.has_data_container).setVisibility(View.GONE);
		}
		else{
			setOverallView();
			addMeasureTimeLine();	
		}

		Utility.trackerScreennView(getApplicationContext(), "Goto MIND");
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
	
	private void getDataInfo(){
		pfd= ParseDataManager.getInstance();
		mStressInfo=pfd.getDayStressInfo(this, day);
		mRelaxInfo=pfd.getDayRelaxInfo(this, day);
	}
	
	private void setActionBar(){
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
		int actionbarTitleId=getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView)findViewById(actionbarTitleId);
		if(abTitle != null) abTitle.setTextColor(0xff4c4c4c);
	}
	
	private void setOverallView(){
		TextView best_time=(TextView)findViewById(R.id.measure_time_best);
		TextView best_value=(TextView)findViewById(R.id.measure_value_best);
		TextView worst_time=(TextView)findViewById(R.id.measure_time_worst);
		TextView worst_value=(TextView)findViewById(R.id.measure_value_worst);
		TextView average=(TextView)findViewById(R.id.tv_average_energy);
		TextView total_measure_count=(TextView)findViewById(R.id.measure_times);
		TextView best_info_tag=(TextView)findViewById(R.id.best_info_tag);
		TextView best_info_text=(TextView)findViewById(R.id.best_info_text);
		TextView worst_info_tag=(TextView)findViewById(R.id.worst_info_tag);
		TextView worst_info_text=(TextView)findViewById(R.id.worst_info_text);
		
		int totalValue=0;
		int allInfoNum=0;
		
		Object bestInfo=getBestInfo();
		Object worstInfo=getWorstInfo();
		if(bestInfo instanceof StressInfo){
			StressInfo info=(StressInfo)bestInfo;
			best_time.setText(Utility.getDateTime(info.measureTime,"hh:mm a"));
			best_value.setText(Utility.intToStr(info.measureValue));
			best_info_tag.setText(getString(R.string.detail_energy_best_info_energy_level));
			best_info_text.setText(getString(R.string.daily_info_stress_title));
		}
		else if(bestInfo instanceof RelaxInfo){
			RelaxInfo info=(RelaxInfo)bestInfo;
			best_time.setText(Utility.getDateTime(info.measureTime,"hh:mm a"));
			best_value.setText(Utility.intToStr(info.measureValue));
			best_info_tag.setText(getString(R.string.detail_energy_best_info_energy_level));
			best_info_text.setText(getString(R.string.daily_info_energy_title));
		}
		
		if(worstInfo instanceof StressInfo){
			StressInfo info=(StressInfo)worstInfo;
			worst_time.setText(Utility.getDateTime(info.measureTime,"hh:mm a"));
			worst_value.setText(Utility.intToStr(info.measureValue));
			worst_info_tag.setText(getString(R.string.detail_energy_worst_info_stress_level));
			worst_info_text.setText(getString(R.string.daily_info_stress_title));
		}
		else if(worstInfo instanceof RelaxInfo){
			RelaxInfo info=(RelaxInfo)worstInfo;
			worst_time.setText(Utility.getDateTime(info.measureTime,"hh:mm a"));
			worst_value.setText(Utility.intToStr(info.measureValue));
			worst_info_tag.setText(getString(R.string.detail_energy_worst_info_stress_level));
			worst_info_text.setText(getString(R.string.daily_info_energy_title));
		}
		
		if(mStressInfo!=null){
			for(int i=0;i<mStressInfo.size();i++){
				totalValue+=(100-mStressInfo.get(i).measureValue);
			}
			allInfoNum+=mStressInfo.size();
		}
		if(mRelaxInfo!=null){
			for(int i=0;i<mRelaxInfo.size();i++){
				totalValue+=mRelaxInfo.get(i).measureValue;
			}
			allInfoNum+=mRelaxInfo.size();
		}
		if(allInfoNum<=2){
			worst_time.setText("- - : - -");
			worst_value.setText(0+"");
			worst_info_tag.setText(getString(R.string.detail_energy_worst_info_stress_level));
			worst_info_text.setText(getString(R.string.daily_info_energy_title));
			
			best_time.setText("- - : - -");
			best_value.setText(0+"");
			best_info_tag.setText(getString(R.string.detail_energy_best_info_energy_level));
			best_info_text.setText(getString(R.string.daily_info_energy_title));

			View best_info_icon=findViewById(R.id.best_info_icon);
			View worst_info_icon=findViewById(R.id.worst_info_icon);
			if(best_info_icon!=null){
				best_info_icon.setVisibility(View.GONE);
			}
			if(worst_info_icon!=null){
				worst_info_icon.setVisibility(View.GONE);
			}
		}
		average.setText(Utility.intToStr(totalValue/allInfoNum));
		total_measure_count.setText(Utility.intToStr(allInfoNum)+" ");
	}
	
	private Object getBestInfo(){
		int value=0;
		Object infoObject = null;
		if(mStressInfo!=null){
			for(StressInfo info:mStressInfo){
				if(info.measureValue<=100-value){
					value=100-info.measureValue;
					infoObject=info;
				}
			}
		}
		if(mRelaxInfo!=null){
			for(RelaxInfo info:mRelaxInfo){
				if(info.measureValue>=value){
					value=info.measureValue;
					infoObject=info;
				}
			}
		}
		return infoObject;
	}
	
	private Object getWorstInfo(){
		int value=0;
		Object infoObject = null;
		if(mStressInfo!=null){
			for(StressInfo info:mStressInfo){
				if(info.measureValue>=value){
					value=info.measureValue;
					infoObject=info;
				}
			}
		}
		if(mRelaxInfo!=null){
			for(RelaxInfo info:mRelaxInfo){
				if(info.measureValue<=100-value){
					value=100-info.measureValue;
					infoObject=info;
				}
			}
		}
		return infoObject;
	}
	
	private void addMeasureTimeLine(){
		if(!(mStressInfo==null && mRelaxInfo==null)){
			ArrayList<Object> combineInfo=pfd.combineStressAndRelaxInfo(mStressInfo, mRelaxInfo);
			TimeLineRelativeLayout measure_timeline=(TimeLineRelativeLayout) findViewById(R.id.measure_timeline);
			
			ArrayList<View> timelineComponent=new ArrayList<View>();//odd:time_text, even:value_text_layout
			int maxHeight=getResources().getDimensionPixelSize(R.dimen.measure_timeline_line_length);
			int timeLineLeftMargin=testTimelineLeftMargin(combineInfo);
			
			for(int i=0;i<combineInfo.size(); i++){
				Object info=combineInfo.get(i);
				View time_text=getLayoutInflater().inflate(R.layout.measure_time_text, null);
				TextView timeText=(TextView)time_text.findViewById(R.id.measure_time);
				
				View value_text_layout=getLayoutInflater().inflate(R.layout.measure_value_layout, null);
				ImageView value_icon=(ImageView)value_text_layout.findViewById(R.id.measure_icon);
				TextView value_text=(TextView)value_text_layout.findViewById(R.id.measure_value);
				TextView measure_result_text=(TextView)value_text_layout.findViewById(R.id.measure_result_text);
				
				
				if(info instanceof RelaxInfo){
					RelaxInfo rInfo=(RelaxInfo)info;
					timeText.setText(Utility.getDateTime(rInfo.measureTime,"hh:mm a"));
					value_text.setText(Utility.intToStr(rInfo.measureValue));
					measure_result_text.setText(rInfo.measureComment==null?Utility.getRelaxLevelString(this, rInfo.measureValue, 0):rInfo.measureComment);
					value_icon.setImageResource(Utility.getRelaxDrawableId(rInfo.measureValue));
				}
				else if(info instanceof StressInfo){
					StressInfo sInfo=(StressInfo)info;
					timeText.setText(Utility.getDateTime(sInfo.measureTime,"hh:mm a"));
					value_text.setTextColor(0xffc14343);
					value_text.setText(Utility.intToStr(sInfo.measureValue));
					value_icon.setImageResource(Utility.getStressDrawableId(sInfo.measureValue));
					measure_result_text.setText(sInfo.measureComment==null?Utility.getStressLevelString(this, sInfo.measureValue):sInfo.measureComment);
				}
				//get measured width and height
		        time_text.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
						MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
		        timelineComponent.add(time_text);

				//get measured width and height
				value_text_layout.measure(MeasureSpec.makeMeasureSpec(getResources().getDimensionPixelSize(R.dimen.weekly_activity_view_width)-timeLineLeftMargin-20, MeasureSpec.AT_MOST),   
						MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
				 timelineComponent.add(value_text_layout);
				if(value_text_layout.getMeasuredHeight()>maxHeight){
					maxHeight=value_text_layout.getMeasuredHeight();
				}
				
			}
			setTimeLineView(combineInfo.size(), timeLineLeftMargin, maxHeight);
			for(int i=0;i<timelineComponent.size(); i+=2){	
				RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.leftMargin=measure_timeline.getDotsPosition(i/2).x-timelineComponent.get(i).getMeasuredWidth()-15;
				params.topMargin=measure_timeline.getDotsPosition(i/2).y-timelineComponent.get(i).getMeasuredHeight()/2;
				timelineComponent.get(i).setLayoutParams(params);
				measure_timeline.addView(timelineComponent.get(i));	

				params=new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.weekly_activity_view_width)-timeLineLeftMargin-20, RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.leftMargin=measure_timeline.getDotsPosition(i/2).x+20;
				params.topMargin=measure_timeline.getDotsPosition(i/2).y-timelineComponent.get(i+1).findViewById(R.id.measure_info_container).getMeasuredHeight()/2;
				timelineComponent.get(i+1).setLayoutParams(params);
				measure_timeline.addView(timelineComponent.get(i+1));	
			}
		}
	}
	
	private void setTimeLineView(int numDots, int leftMargin, int maxHeight){
		TimeLineRelativeLayout measure_timeline=(TimeLineRelativeLayout) findViewById(R.id.measure_timeline);
		measure_timeline.setNumDots(numDots);
		measure_timeline.setLineLength(maxHeight);
		measure_timeline.setTimeLineLeftMargin(leftMargin);
		measure_timeline.setTimeLineTopMargin(getResources().getDimensionPixelSize(R.dimen.measure_timeline_top_margin));
		measure_timeline.setLastLine(true);
		
		LayoutParams params=(LayoutParams) measure_timeline.getLayoutParams();
		params.height=measure_timeline.getRequiredHeight();
		measure_timeline.setLayoutParams(params);
	}
	
	private int testTimelineLeftMargin(ArrayList<Object> combineInfo){
		int minLeft=getResources().getDimensionPixelSize(R.dimen.measure_timeline_left_margin);
		for(int i=0;i<combineInfo.size(); i++){
			Object info=combineInfo.get(i);
			View time_text=getLayoutInflater().inflate(R.layout.measure_time_text, null);
			TextView timeText=(TextView)time_text.findViewById(R.id.measure_time);
			if(info instanceof RelaxInfo){
				RelaxInfo rInfo=(RelaxInfo)info;
				timeText.setText(Utility.getDateTime(rInfo.measureTime,"hh:mm a"));
			}
			time_text.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
					MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
			int left=minLeft-time_text.getMeasuredWidth()-15;
			if(left<0){
				minLeft=minLeft-left;
			}
		}
		return minLeft;
	}
}

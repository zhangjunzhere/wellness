package com.asus.wellness.ui.daily;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.sharedata.SleepData;
import com.asus.sharedata.SleepTimeSpan;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.Day;
import com.asus.wellness.ParseDataManager.RelaxInfo;
import com.asus.wellness.ParseDataManager.StressInfo;
import com.asus.wellness.R;
import com.asus.wellness.ga.GACategory;
import com.asus.wellness.ga.GAHelper;
import com.asus.wellness.sleep.SleepHelper;
import com.asus.wellness.ui.BaseActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import originator.ailin.com.smartgraph.chart.barchart.SleepBarChart;
import originator.ailin.com.smartgraph.legend.Line;

public class DailyDetailSleepActivity extends BaseActivity {

	private ParseDataManager pfd;
	private ArrayList<StressInfo> mStressInfo;
	private ArrayList<RelaxInfo> mRelaxInfo;
	private Day day;
    SleepBarChart simpleBarChart;
    public static final String KEY_DAY_INFO="key_day_info";
	private View has_data_containerLayout;
	private View no_data_containerLayout;
	TextView tv_sleepstart;
	TextView tv_sleepend;
	TextView tv_totalsleep_hr;
	TextView tv_totalsleep_hr_text;
	TextView tv_totalsleep_min;
	TextView tv_deepQualityPercent;
	TextView tv_latency;
	TextView tv_deepSleepSpan;
	TextView tv_lightSleepSpan;
	TextView tv_wakeuptimes;
	TextView tv_tossandturn;
	@Override
	public  String getPageName(){
		return  DailyDetailSleepActivity.class.getSimpleName();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daily_detail_sleep);
		day=(Day) getIntent().getSerializableExtra(DailyDetailActivityActivity.KEY_DAY_INFO);
		has_data_containerLayout = findViewById(R.id.has_data_container);
		no_data_containerLayout = findViewById(R.id.no_data_container);
		tv_sleepstart = (TextView) findViewById(R.id.tv_sleep_starttime);
		tv_sleepend = (TextView) findViewById(R.id.tv_sleep_endtime);

		tv_totalsleep_hr = (TextView)findViewById(R.id.total_sleep_time_hr);
		tv_totalsleep_hr_text = (TextView)findViewById(R.id.total_sleep_time_hr_text);
		tv_totalsleep_min = (TextView)findViewById(R.id.total_sleep_time_min);

		tv_deepQualityPercent = (TextView)findViewById(R.id.sleep_quality);
		tv_latency = (TextView) findViewById(R.id.tv_sleep_latency);
		tv_deepSleepSpan=(TextView) findViewById(R.id.tv_deep_sleep_span);
		tv_lightSleepSpan=(TextView) findViewById(R.id.tv_light_sleep_span);
		tv_wakeuptimes = (TextView) findViewById(R.id.tv_wokeup);
		tv_tossandturn = (TextView) findViewById(R.id.tv_wakeup_sleep_span);
		View info = findViewById(R.id.ib_sleepinfo);
		info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(DailyDetailSleepActivity.this, SleepInfoListActivity.class);
				startActivity(i);
			}
		});

		View tips = findViewById(R.id.sleep_tips);
		View tipslayout= findViewById(R.id.sleep_tips_layout);
		TextView tv_tips = (TextView)findViewById(R.id.tv_tips);
		Random random=new Random();
		final int index =random.nextInt(tipstitlearray.length);
		int tipsid = tipstitlearray[index];
		setTipString(tv_tips,tipsid);
		if(tipslayout!=null)
		{
			tipslayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(DailyDetailSleepActivity.this, SleepInfoDetailActivity.class);
					i.putExtra(SleepInfoDetailActivity.IS_TIPS_KEY,true);
					i.putExtra("key",index);
					startActivity(i);
				}
			});
		}

		setActionBar();
		//getDataInfo();
		/*if(mStressInfo==null && mRelaxInfo==null){
			findViewById(R.id.no_data_container).setVisibility(View.VISIBLE);
			findViewById(R.id.has_data_container).setVisibility(View.GONE);
		}
		else{
			setOverallView();
			addMeasureTimeLine();	
		}*/

		Utility.trackerScreennView(getApplicationContext(), "Goto Sleep");

		simpleBarChart = (SleepBarChart)findViewById(R.id.my_sleep_chart);
		if(!Utility.isPadLayout(this))
		{

			int height = Utility.getScreenHeight(this);
			Log.i("smile","daily sleep height: "+height);
			ViewGroup.LayoutParams layoutParams = simpleBarChart.getLayoutParams();
			layoutParams.height = height/4;
			//LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height/3);
			simpleBarChart.setLayoutParams(layoutParams);

		}
		showSleepData();
		GAHelper.getInstance(getApplicationContext()).sendEvent(GACategory.CategorySleep, GACategory.getActionSleepTracker(), GACategory.LabelSleep, -1);

	}
	int [] tipstitlearray = new int[]{
			R.string.stick_to_a_sleep_schedule_title,
			R.string.exercise_is_great_title,
			R.string.avoid_cafeine_and_nicotine_title,
			R.string.avoid_alcoholic_drinks_before_bed_title,
			R.string.avoid_large_meals_title,
			R.string.if_possible_title,
			R.string.dont_take_naps_title,
			R.string.relax_before_bed_title,
			R.string.take_a_lot_bath_title,
			R.string.have_a_good_sleeping_title,
			R.string.have_the_right_sunlight_title,
			R.string.dont_lie_in_bed_title,
			R.string.see_a_health_title
	};
	void setTipString(TextView tv,int titleid)
	{

		String t1 = getString(tipstitlearray[0]);
		try{
			t1 = getString(titleid);
		}catch (Exception e)
		{

		}
		String tipsstr = getString(R.string.sleep_tips_title);

		tv.setText(tipsstr+"："+t1);
	}
	void showSleepData()
	{

		SleepData sleepData = SleepHelper.getSleepQualityByDate(day.startTimeMilli);
		drawChart(sleepData);
		if(sleepData ==null)
		{
			return;
		}
		SleepTimeSpan sts = sleepData.getTotalSleepTime();
		showTotalTime(sts);
	//	tv_totalsleep.setText(totalsleepstr);

		SleepTimeSpan stsdeep = sleepData.getTotalDeepSleepTime();
		SpannableString totalDeepSleepSpan = getHourMinStr(stsdeep);
		tv_deepSleepSpan.setText(totalDeepSleepSpan);

		SleepTimeSpan	stslight = sleepData.getTotalLightSleepTime();
		SpannableString totalLightSleepSpan = getHourMinStr(stslight);
		tv_lightSleepSpan.setText(totalLightSleepSpan);

		int waketimes = sleepData.getWokeupTimes();
	//	tv_wakeuptimes.setText(String.valueOf(waketimes));

		SleepTimeSpan totalSleepsts = sleepData.getTotalSleepTime();
		SleepTimeSpan wakespan = totalSleepsts.subTimeSpan(stsdeep).subTimeSpan(stslight);
		SpannableString wakeStr = getHourMinStr(wakespan,waketimes);
		tv_tossandturn.setText(wakeStr);
		sts = sleepData.getLatency();
		SpannableString totalLatency = getHourMinStr(sts);
		tv_latency.setText(totalLatency);

		//String deeppercent = String.valueOf(sleepData.getQuality())+" ";
		//tv_deepQualityPercent.setText(deeppercent);
		tv_deepQualityPercent.setText(String.valueOf(sleepData.getQuality()));
		//Utility.setSleepScoreSleepActivity(tv_deepQualityPercent,sleepData.getQuality(),this);


	}
	private void showTotalTime(SleepTimeSpan sts)
	{
		int visible = sts.hour>0 ? View.VISIBLE : View.GONE;
		tv_totalsleep_hr.setVisibility(visible);
		tv_totalsleep_hr_text.setVisibility(visible);
		tv_totalsleep_hr.setText(String.valueOf(sts.hour));
		tv_totalsleep_min.setText(String.valueOf(sts.min));
	}
	private SpannableString getHourMinStr(SleepTimeSpan sts,int wokeuptime)
	{
		String hourstr = " "+getString(R.string.daily_info_time_hours)+" ";
		String minstr = " "+getString(R.string.time_unit)+" ";
		String ret="";
		if(sts.hour>0)
		{
			ret+=sts.hour+hourstr;
		}
		ret+=sts.min+minstr;
		ret+= wokeuptime+" "+getString(R.string.count_time);
		SpannableString ss =new SpannableString(ret);
		int start = 0;
		int end =0;
		if(sts.hour>0)
		{
			end = String.valueOf(sts.hour).length();
			ss.setSpan(new TextAppearanceSpan(this,R.style.timestring_big),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			start = end+hourstr.length();
		}
		end = start + String.valueOf(sts.min).length();
		ss.setSpan(new TextAppearanceSpan(this, R.style.timestring_big), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		start = end+minstr.length();
		end = start + String.valueOf(wokeuptime).length();
		ss.setSpan(new TextAppearanceSpan(this, R.style.timestring_big), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}
	private SpannableString getHourMinStr(SleepTimeSpan sts)
	{
		String hourstr = " "+getString(R.string.daily_info_time_hours)+" ";
		String minstr = " "+getString(R.string.time_unit);
		String ret="";
		if(sts.hour>0)
		{
			ret+=sts.hour+hourstr;
		}

		ret+=sts.min+minstr;
		SpannableString ss =new SpannableString(ret);
		int start = 0;
		int end =0;
		if(sts.hour>0)
		{
			end = String.valueOf(sts.hour).length();
			ss.setSpan(new TextAppearanceSpan(this,R.style.timestring_big),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			start = end+hourstr.length();
		}
		end = start + String.valueOf(sts.min).length();
		ss.setSpan(new TextAppearanceSpan(this,R.style.timestring_big),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return ss;
	}
	void drawChart(SleepData sleepData)
	{
		if(sleepData!=null && sleepData.getDataList()!=null && sleepData.getDataList().size()>0)
		{
			has_data_containerLayout.setVisibility(View.VISIBLE);
			no_data_containerLayout.setVisibility(View.GONE);
			String datastr="";
			for(int i=0;i<sleepData.getDataList().size();i++)
			{
				datastr+=sleepData.getDataList().get(i)+" ";
			}
			Log.i("smile", "datastr: " + datastr);
			simpleBarChart.setUseformobile();
			simpleBarChart.setSleepData(sleepData.getDataList(), sleepData.getStartTime(),sleepData.getEndTime(),sleepData.getFirstSleepTime(),sleepData.getLastSleepTime());
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
			String sleepstart = formatter.format(sleepData.getStartTime());
			tv_sleepstart.setText(sleepstart);
			String sleepend = formatter.format(sleepData.getEndTime());
			tv_sleepend.setText(sleepend);
		}
		else
		{
			has_data_containerLayout.setVisibility(View.GONE);
			no_data_containerLayout.setVisibility(View.VISIBLE);
		}
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
	


	
	private void setActionBar(){
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
		int actionbarTitleId=getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView)findViewById(actionbarTitleId);
		if(abTitle != null) abTitle.setTextColor(0xff4c4c4c);
	}
}

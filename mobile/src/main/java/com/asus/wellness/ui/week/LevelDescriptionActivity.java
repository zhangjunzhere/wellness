package com.asus.wellness.ui.week;

import com.asus.wellness.R;
import com.asus.wellness.ui.BaseActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class LevelDescriptionActivity extends BaseActivity {

	@Override
	public String getPageName(){
		return LevelDescriptionActivity.class.getSimpleName();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBar();
		setContentView(R.layout.level_description_layout);
		
		String[] level = getResources().getStringArray(R.array.exercise_intensity);
		TextView levelText = (TextView)findViewById(R.id.level_desc_l5_textview);
		levelText.setText(level[4]);
		levelText = (TextView)findViewById(R.id.level_desc_l4_textview);
		levelText.setText(level[3]);
		levelText = (TextView)findViewById(R.id.level_desc_l3_textview);
		levelText.setText(level[2]);
		levelText = (TextView)findViewById(R.id.level_desc_l2_textview);
		levelText.setText(level[1]);
		levelText = (TextView)findViewById(R.id.level_desc_l1_textview);
		levelText.setText(level[0]);
		if(getResources().getConfiguration().smallestScreenWidthDp >=800)
		{
			setLevelExplainText();
		}

		Utility.trackerScreennView(getApplicationContext(), "Goto LevelDescription");
	}
	
	
	private void setLevelExplainText()
	{
		TextView light = (TextView) findViewById(R.id.level_desc_light_text);
		TextView moderate = (TextView) findViewById(R.id.level_desc_moderate_text);
		String s = (String) light.getText();
		String s2 = (String) moderate.getText();
		int index,index2;
		index = s.indexOf("(");
		index2 = s2.indexOf("(");
		int adjIndex = index - 1;
		int adjIndex2 = index2 - 1;
		if((s.length()!=s.getBytes().length)&& (index == -1))
		{

			index = s.indexOf(" ");
			index2 = s2.indexOf(" ");
			adjIndex =index;
			adjIndex2 = index2;
		}
		
		if(index != -1)
		{
			light.setText(setStringStyle(s,index,adjIndex,0xFF40beea));
			moderate.setText(setStringStyle(s2,index2,adjIndex2,0xFF0a95c6));
		}
	}
	
	private SpannableStringBuilder setStringStyle(String s,int index,int adjIndex,int color)
	{
		String s1 = s.substring(0, adjIndex);
		String s2 = s.substring(index, s.length());
		s = s1 +"\n" + s2;
		SpannableStringBuilder ssb = new SpannableStringBuilder(s);
		ssb.setSpan(new ForegroundColorSpan(color), 0, adjIndex, ssb.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(new ForegroundColorSpan(0xFFb7b7b7), adjIndex+1, s.length(), ssb.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		return ssb;
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.still, R.anim.slide_out_to_right);
	}
}

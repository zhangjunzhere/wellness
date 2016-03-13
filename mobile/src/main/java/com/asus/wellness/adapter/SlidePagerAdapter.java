package com.asus.wellness.adapter;
import java.util.Calendar;

import com.asus.wellness.ParseDataManager;
import com.asus.wellness.R;
import com.asus.wellness.ParseDataManager.ProfileData;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.ui.daily.DailyPageFragment;
import com.asus.wellness.ui.MainWellness;
import com.asus.wellness.ui.week.WeeklyPageFragment;
import com.asus.wellness.ui.week.WeeklyPageFragment.Week;
import com.asus.wellness.utils.Utility;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.TextView;

//ViewTitlePagerAdapter
public class SlidePagerAdapter extends FragmentStatePagerAdapter {

	private int ONE_DAY_MILLI=86400000;
	private int mViewType=0;
	private Context mContext;
    private int mTotalItemCount = 1;
    private LayoutInflater inflater;
    
    private int age;
    private int weightInKG;
    private int heightInCM;
    
    private String[] Month = new String[] {
            "N/A", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };

	public SlidePagerAdapter(FragmentManager fm, int type, Context context) {
		super(fm);
		mViewType=type;
		mContext=context;
        ParseDataManager pdm =  ParseDataManager.getInstance();
        ProfileData pfd = pdm.getProfileData(mContext);
        if(pfd == null)
        {
            return;
        }
        mTotalItemCount = getTotalCountByTimeMS(pfd.start_time);
        
        inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        getProfileTable();
        setupTitle();
	}

    private int getTotalCountByTimeMS(long timeMS) {
        int count = 1;
        long oneWeekInterval = ONE_DAY_MILLI * 7;
        int adjust = 2;

        Calendar cal1 = Calendar.getInstance();
        if (cal1.getFirstDayOfWeek() == Calendar.SUNDAY && 
                cal1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal1.add(Calendar.DATE, -7);
        }
        cal1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        long thisWeekFirstTimeMS = cal1.getTimeInMillis();

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timeMS);
        if(cal2.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            adjust = 1;
        }

        if (!(timeMS > thisWeekFirstTimeMS
                || (cal1.getTime().getYear() == cal2.getTime().getYear()
                && cal1.getTime().getMonth() == cal2.getTime().getMonth() 
                && cal1.getTime().getDate() == cal2.getTime().getDate()))) {
            count = (int)((thisWeekFirstTimeMS - timeMS) / oneWeekInterval + adjust);
        }


        Log.i("smile","adust: "+adjust+" timeMS:  "+timeMS+" cal2: "+cal2.getTime().toString()+" thisWeekFirstTimeMS: "+thisWeekFirstTimeMS+" cal1: "+cal1.getTime().toString()+" count: "+count);

        return count;
    }
    
    private void getProfileTable()
    {
        Cursor cursor=mContext.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
        if(cursor.moveToFirst()){
            age = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_AGE));
        	weightInKG=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_WEIGHT));
        	int height=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_HEIGHT));
        	int unit=cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_HEIGHT_UNIT));
        	if(unit==ProfileTable.HEIGHT_UNIT_FT){
        		float ft=Utility.InchToFt(height);
        		heightInCM=(int) Math.round(Utility.ftToCm(ft));
        	}
        	else{
        		heightInCM=height;
        	}

            int weight_unit = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_WEIGHT_UNIT));
            if (weight_unit == ProfileTable.WEIGHT_UNIT_LBS){
                weightInKG = (int) Math.round(Utility.LbsToKg(weightInKG));
            }
        }
        cursor.close();
    }

    private void setupTitle() {
        float scaleRatio, dimenPix;
        scaleRatio = mContext.getResources().getDisplayMetrics().density;
        dimenPix = mContext.getResources().getDimension(R.dimen.weekpagertitle_textsize);
        PagerTabStrip titleStrip = (PagerTabStrip)((Activity)mContext).findViewById(R.id.pager_tab_strip);
        
        if(mContext.getResources().getConfiguration().smallestScreenWidthDp >=800)
        {
        	titleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 1);
        	titleStrip.setDrawFullUnderline(false);
        }
        else
        {
        	titleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (dimenPix/scaleRatio));
        }
    }

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		Fragment fragment = null;
		if(mViewType==MainWellness.TYPE_DAILY_VIEW){
			fragment = new DailyPageFragment();
	        Bundle args = new Bundle();
	        args.putLong(DailyPageFragment.ONEDAY_TIME_MILLI, System.currentTimeMillis()-(mTotalItemCount-1-arg0)*ONE_DAY_MILLI);
	        fragment.setArguments(args);	
		}
		else if(mViewType==MainWellness.TYPE_WEEKLY_VIEW){
			fragment = new WeeklyPageFragment();
			ViewPager vp = (ViewPager)((Activity)mContext).findViewById(R.id.wellness_info_pager);
			int currentPos = vp.getCurrentItem();
			Bundle args = new Bundle();
	        args.putInt(WeeklyPageFragment.NOW_WEEK, -1*(mTotalItemCount-1-arg0));
	        args.putInt("age", age);
	        args.putInt("weightInKG", weightInKG);
	        args.putInt("heightInCM", heightInCM);
	        int flagPos = -1;
	        if(arg0 == currentPos)
	        {
	        	flagPos = 1;
	        }
	        args.putInt("pos",flagPos);
	        fragment.setArguments(args);
		}
		return fragment;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mTotalItemCount;
	}

    @Override
    public CharSequence getPageTitle(int position) {
        if (mViewType == MainWellness.TYPE_WEEKLY_VIEW) {
            ViewPager vp = (ViewPager)((Activity)mContext).findViewById(R.id.wellness_info_pager);
            WeeklyPageFragment fragment = (WeeklyPageFragment)getItem(position);
            Week wk = fragment.getWeekInfo(-1 * (mTotalItemCount - 1 - position));
            int currentPos = vp.getCurrentItem();
            String weekTitle;
            String startMonthStr = Month[wk.startMonth];
            String startDateStr = String.valueOf(wk.startDay);
            String endMonthStr = Month[wk.endMonth];
            String endDateStr = String.valueOf(wk.endDay);
            int flagPos;
            flagPos=-1;
            if(position == currentPos) {
                weekTitle = startMonthStr + startDateStr + " - " + endMonthStr + endDateStr;
                flagPos=0;
            } else {
                //weekTitle = startMonthStr + startDateStr + " - " + endDateStr;
                weekTitle = startDateStr + " - " + endDateStr;
                if(position > currentPos)
                {
                	flagPos=1;
                }
                else
                {
                	flagPos=-1;
                }
            }
            

            SpannableStringBuilder ssb;
            if(mContext.getResources().getConfiguration().smallestScreenWidthDp >=800)
            {
            	ssb = new SpannableStringBuilder(" ");
            	Bitmap bm = createIcon(String.valueOf(wk.curWeek),weekTitle,flagPos);
            	ImageSpan span = new ImageSpan(mContext,bm,ImageSpan.ALIGN_BASELINE);
            	ssb.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else
            {
            	ssb = new SpannableStringBuilder(weekTitle);
            }
            //

            
            
            
            //
            return ssb;
        } else if (mViewType == MainWellness.TYPE_DAILY_VIEW) {
            if (position == mTotalItemCount - 1) {
                return "Today";
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis() - (mTotalItemCount - 1 - position)
                        * ONE_DAY_MILLI);
                return (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE);
            }
        }
        return "";
    }
    
    private Bitmap createIcon(String t1, String t2,int f)
    {
    	View v = inflater.inflate(R.layout.weekly_title_bitmap, null);
    	TextView tv1 = (TextView) v.findViewById(R.id.weekly_title_1);
    	TextView tv2 = (TextView) v.findViewById(R.id.weekly_title_2);
    	if (f != 0)
    	{
    		tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.main_page_weelky_pagertabstrip_up_textSize));
    		tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.main_page_weelky_pagertabstrip_down_textSize));	
    		if(f == 1)
    		{
    			tv1.setTextColor(0xFFCFCFCF);
    			tv2.setTextColor(0xFFCFCFCF);
    		}
    		else
    		{
    			tv1.setTextColor(0xFF646464);
    			tv2.setTextColor(0xFF646464);
    		}
    		tv2.setPadding(0, 0, 0, 2);
    	}
    	else
    	{
    		tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.main_page_weelky_pagertabstrip_curr_up_textSize));
    		tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.main_page_weelky_pagertabstrip_curr_down_textSize));
    		tv1.setTextColor(0xFF000000);
    		tv2.setTextColor(0xFF000000);
    	}
    	tv1.setText("Week"+t1);
    	tv2.setText(t2);
    	v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
    	return v.getDrawingCache();
    }


}

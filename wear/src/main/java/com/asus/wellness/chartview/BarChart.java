package com.asus.wellness.chartview;

import java.util.Calendar;

import com.asus.wellness.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BarChart extends View {
	
	private int mBarWidth=20;
	private int mBarHeight=150;
	private int mBarSpace=15;
	private int mBarLeftOffset=10;
	private int mCrownBarSpace=5;
	private int mBarWeekTextSpace=10;
	
	Bitmap crownBitmap;
	private Context mContext;
	
	int [] mWeekSteps=null;
	
	public BarChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
		crownBitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_app_micro_crown);
        initBarChart();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Log.i("smile","onDetachedFromWindow crownbitmap");
		if(crownBitmap!=null && !crownBitmap.isRecycled())
		{
			crownBitmap.recycle();
			crownBitmap = null;
		}
	}

	public BarChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		crownBitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_app_micro_crown);
        initBarChart();
		// TODO Auto-generated constructor stub
	}
	
	public BarChart(Context context) {
		super(context);
		mContext=context;
		crownBitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_app_micro_crown);
        initBarChart();
		// TODO Auto-generated constructor stub
	}

    private void initBarChart(){
        mBarWidth = (int) mContext.getResources().getDimension(R.dimen.barchart_width);
        mBarSpace = (int) mContext.getResources().getDimension(R.dimen.barchart_space);
        mBarLeftOffset = (int) mContext.getResources().getDimension(R.dimen.barchart_left_offset);
        mBarWeekTextSpace = (int) mContext.getResources().getDimension(R.dimen.barchart_week_text_space);
        //Log.i("emily","mBarWidth =" + mBarWidth+", mBarSpace = " +mBarSpace +", mBarLeftOffset = " + mBarLeftOffset +", mBarWeekTextSpace= " + mBarWeekTextSpace);
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		drawBar(canvas);
		drawIndicator(canvas);
		drawBottomLine(canvas);
		drawBottomWeekText(canvas);		
	}
	
	private void drawBottomWeekText(Canvas canvas){
		Paint paint=new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.target_week_text_size));
		paint.setAntiAlias(true);
		paint.setAlpha(126);
		
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DATE, -6);
		for(int i=0;i<7;i++){
			String weekText=this.getWeekString(cal);
			
			float textWidth=paint.measureText(weekText);
			Rect bound=new Rect();
			paint.getTextBounds(weekText, 0, 1, bound);
			
			int left=(int) (mBarLeftOffset+(mBarWidth+mBarSpace)*i+mBarWidth/2-textWidth/2);
			int top=crownBitmap.getHeight()+mCrownBarSpace+mBarHeight+(bound.bottom-bound.top)+mBarWeekTextSpace;
			if(i==6){
				paint.setAlpha(255);
				paint.setTypeface(Typeface.DEFAULT_BOLD);
			}
			canvas.drawText(weekText, left, top, paint);
			
			cal.add(Calendar.DATE, 1);
		}
	}
	
	private void drawBottomLine(Canvas canvas){
		Paint paint=new Paint();
		paint.setColor(Color.WHITE);
		int y=crownBitmap.getHeight()+mBarHeight+mCrownBarSpace;
		canvas.drawLine(0, y, getWidth(), y, paint);
	}
	
	private void drawIndicator(Canvas canvas){
		float fiveKHeight=5000f/12500f*mBarHeight;
		float tenKHeight=10000f/12500f*mBarHeight;
		
		int startX=mBarLeftOffset;
		int stopX=startX+mBarWidth*7+mBarSpace*6;
		int fiveKY=(int) (mBarHeight-fiveKHeight+crownBitmap.getHeight()+mCrownBarSpace);
		int tenKY=(int) (mBarHeight-tenKHeight+crownBitmap.getHeight()+mCrownBarSpace);
		
		Paint paint=new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(3);
		paint.setPathEffect(new DashPathEffect(new float[] {2,10}, 0));
		
		Paint textPaint=new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.indicator_text_size));
		textPaint.setAntiAlias(true);
		
		Path path=new Path();
		path.moveTo(startX, tenKY);
		path.lineTo(stopX, tenKY);
		canvas.drawPath(path, paint);
		int tenKXStart=stopX+0;
		int tenKYStart=tenKY+10;
		canvas.drawText(mContext.getString(R.string.step_indicator_10k), tenKXStart, tenKYStart, textPaint);

		path=new Path();
		path.moveTo(startX, fiveKY);
		path.lineTo(stopX, fiveKY);
		float fiveKWidth=textPaint.measureText(mContext.getString(R.string.step_indicator_5k));
		float tenKWidth=textPaint.measureText(mContext.getString(R.string.step_indicator_10k));
		canvas.drawPath(path, paint);
		canvas.drawText(mContext.getString(R.string.step_indicator_5k), tenKXStart+tenKWidth-fiveKWidth, fiveKY+10, textPaint);
	}
	
	private void drawBar(Canvas canvas){
		if(mWeekSteps == null)
		{
			Log.i("smile","mWeekSteps null");
			return;
		}
		int left=mBarLeftOffset, top=crownBitmap.getHeight()+mCrownBarSpace, right=left+mBarWidth, bottom=top+mBarHeight;
		
		Paint barBackgroudPaint=new Paint();
		barBackgroudPaint.setColor(0x33ffffff);
		
		Paint barforgroudPaint=new Paint();
		barforgroudPaint.setColor(0xaaffffff);
		
		int maxStep=0;
		for(int i=0;i<mWeekSteps.length;i++){
			if(mWeekSteps[i]>=maxStep){
				maxStep=mWeekSteps[i];
			}
		}
		
		for(int i=0;i<7;i++){
			if(mWeekSteps[i]==maxStep && mWeekSteps[i]>0){
				canvas.drawBitmap(crownBitmap, left+mBarWidth/2-crownBitmap.getWidth()/2, top-crownBitmap.getHeight()-mCrownBarSpace, new Paint());	
			}
			
			Rect backRect=new Rect(left,top,right,bottom);
			canvas.drawRect(backRect, barBackgroudPaint);

			int stepHeight=(int) (mWeekSteps[i]/12500f*mBarHeight);
			if(stepHeight>=mBarHeight){
				stepHeight=mBarHeight;
			}
			Rect foreRect=new Rect(left, top+(mBarHeight-stepHeight), right, bottom);
			if(i==6){
				barforgroudPaint.setColor(Color.WHITE);
			}
			canvas.drawRect(foreRect, barforgroudPaint);

			left+=mBarWidth+mBarSpace;
			right=left+mBarWidth;
		}
	}
	
	public void setWeekSteps(int[] data){
		mWeekSteps=data;
		invalidate();
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
}

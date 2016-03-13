package com.asus.wellness.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class CircleProgressBar extends View {
	
	private static final int DEFAULT_TOTAL_PROGRESS_COUNT = 30;
	private static final float DEFAULT_PROGRESS_STROKE_WIDTH_DP = 10.0f;
	
	private int mCircleColor = Color.WHITE;
	private float mStrokeWidth = 10.0f;
	private Paint mPaint;
	
	private CountDownTimer mCountDownTimer;

	private int mProgressCount = 0;
	private int mTimerCount = 0;
	private int mRRCount = 0;

	public CircleProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mStrokeWidth, context.getResources().getDisplayMetrics());
		mPaint = new Paint();
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setColor(mCircleColor);
		mPaint.setAntiAlias(true);
	}

	public CircleProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public CircleProgressBar(Context context) {
		this(context,null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		drawCircle(canvas);
	}

	private void drawCircle(Canvas canvas) {
		canvas.save();
		int w = getWidth();
		int h = getHeight();
		float raidus = Math.min(w, h);
		float half_width = mStrokeWidth/2.0f;
		RectF rect = new RectF(half_width,half_width,raidus-half_width,raidus-half_width);
		canvas.drawArc(rect, 270, (360.0f/DEFAULT_TOTAL_PROGRESS_COUNT) * mProgressCount, false, mPaint);
		canvas.restore();
	}

	public void startProgress(){
		mCountDownTimer =  new CountDownTimer(40000, 40000/DEFAULT_TOTAL_PROGRESS_COUNT) {

		     public void onTick(long millisUntilFinished) {
		    	 mTimerCount += 1;
		    	 setProgressCount(mTimerCount);
		     }

		     public void onFinish() {
		     }
		  }.start();
	}
	
	public void increaseRRCount(){
		mRRCount += 1;
		if (mRRCount >= mTimerCount){
			mCountDownTimer.cancel();
			setProgressCount(mRRCount);
		}
	}
	
	public void setProgressCount(int Count){
		mProgressCount = Count;
		invalidate();
	}
	
	public void reset(){
		mCountDownTimer.cancel();
		mProgressCount = 0;
		mTimerCount = 0;
		mRRCount = 0;
	}
}

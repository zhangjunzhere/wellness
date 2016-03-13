package com.asus.wellness.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.v4.view.PagerTabStrip;
import android.util.AttributeSet;

import com.asus.wellness.R;

public class PagerTabStripPad extends PagerTabStrip {
	private Context mContext;
	private Paint mPaint;
	private Paint mLeftPaint;
	private Paint mLineDotPaint;
	private Path mLineDotPath;
	private PathEffect mEffects;
	private int mOldWdith;
	private int mCurrDisplayX;
	private int mIniFlag ;
	private int mFlag;

	public PagerTabStripPad(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mPaint  = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xFF000000);
		mPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.main_page_weelky_pagertabstrip_line_Height)*2);

	
		mLeftPaint  = new Paint();
		mLeftPaint.setAntiAlias(true);
		mLeftPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.main_page_weelky_pagertabstrip_line_Height));
		mLeftPaint.setColor(0xFF646464);
		mEffects = new DashPathEffect(new float[] {5,5},1);
		mLineDotPaint = new Paint();
		mLineDotPaint.setAntiAlias(true);
		mLineDotPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.main_page_weelky_pagertabstrip_line_Height)*2);
		mLineDotPaint.setAlpha(255);
		mLineDotPaint.setStyle(Paint.Style.STROKE);
		mLineDotPaint.setPathEffect(mEffects);
	    mLineDotPaint.setColor(0xFFCFCFCF);
	    mLineDotPath = new Path();
		// TODO Auto-generated constructor stub
	    mIniFlag = 0;
	    mFlag = 0;
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		//super.onDraw(canvas);
		if(mIniFlag == 0)
		{
			mIniFlag = 1;
			mOldWdith = getChildAt(1).getWidth();
			mCurrDisplayX = (int) getChildAt(1).getX();
		}
		int w  = getWidth();
		int h  = getHeight();
		int h2 = h / 2;
		int middleWdith = getChildAt(1).getWidth();
		int x = (int) getChildAt(1).getX();
		int disw =x + middleWdith ;
		
    	if( Math.abs(w/2 - x) < 200 )
    	{
    		mFlag = 0;
    	}
    	
    	if(getChildAt(2).getWidth() == 0)
    	{
    		mOldWdith =middleWdith;
    	}
    	
		
		if(((middleWdith - mOldWdith)==0)&&(mFlag == 0))
		{
			//Left Line
	    	canvas.drawLine(0, h2, x, h2, mLeftPaint);
	    	//Middle Line
	    	canvas.drawLine(x, h2, disw, h2, mPaint);
	    	//Right Line
	    	//Log.d("PHH",Integer.toString((int) getChildAt(2).getWidth()));
	    	if(getChildAt(2).getWidth() == 0)
	    	{
	    		mLineDotPath.reset();
	    		mLineDotPath.moveTo(disw, h2);
	    		mLineDotPath.lineTo(w, h2);
	    		canvas.drawPath(mLineDotPath, mLineDotPaint);
	    	}
	    	else
	    	{
	    		canvas.drawLine(disw, h2, w, h2, mLeftPaint);
	    	}
	    	//
	    	mCurrDisplayX = x;
		}
		else
		{
			mFlag = 1;
			if(mCurrDisplayX < w/2)
			{

				int leftWdith = (int) getChildAt(0).getWidth();
		    	mLineDotPath.reset();
		    	//mLineDotPath.moveTo(leftWdith, h2);
		    	/*
		    	mLineDotPath.moveTo(0, h2);
		    	mLineDotPath.lineTo(w, h2);
		    	canvas.drawPath(mLineDotPath, mLineDotPaint);
		    	*/
		    	canvas.drawLine(0, h2, w, h2, mLeftPaint);
		    	if( x < (w - (int)(2.5 * middleWdith)))
		    	{
		    		canvas.drawLine(x, h2, disw, h2, mPaint);
		    	}
		    	else
		    	{
		    		canvas.drawLine(0, h2, leftWdith, h2, mPaint);
		    	}  
		    	mCurrDisplayX = (int) getChildAt(0).getX();
			}
			else
			{

				int rightWdith = (int) getChildAt(2).getWidth();	
				canvas.drawLine(0, h2, w, h2, mLeftPaint);
		    	if( x > (int)(2.5 * middleWdith))
		    	{
		    		canvas.drawLine(x, h2,disw, h2, mPaint);
		    	}
		    	else
		    	{
		    		canvas.drawLine(w, h2, w - rightWdith, h2, mPaint);
		    	}
		    	mCurrDisplayX = (int) getChildAt(2).getX();
			}
		}

		mOldWdith = middleWdith;
	}

}

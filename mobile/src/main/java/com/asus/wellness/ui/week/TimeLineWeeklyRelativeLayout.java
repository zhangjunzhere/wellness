package com.asus.wellness.ui.week;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.asus.wellness.ui.daily.TimeLineRelativeLayout;

public class TimeLineWeeklyRelativeLayout extends TimeLineRelativeLayout {
	
	private Paint mLinePaint;
	

	public TimeLineWeeklyRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mLinePaint=new Paint();
		mLinePaint.setStrokeWidth(DEFAULT_LINE_WIDTH);
		mLinePaint.setColor(DEFAULT_LINE_COLOR);
	}

	public TimeLineWeeklyRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		// TODO Auto-generated constructor stub
	}

	public TimeLineWeeklyRelativeLayout(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void drawLine(Canvas canvas) {
		canvas.drawLine(0, 0, getWidth(), 0, mLinePaint);
		canvas.drawLine(mTimeLineLeftMargin, 0, mTimeLineLeftMargin, getHeight(), mLinePaint);
		canvas.drawLine(0, getHeight(), getWidth(), getHeight(), mLinePaint);
	}

	
	
}

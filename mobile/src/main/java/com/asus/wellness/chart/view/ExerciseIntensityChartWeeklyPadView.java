package com.asus.wellness.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

public class ExerciseIntensityChartWeeklyPadView extends ExerciseIntensityChartWeeklyView {

	
	
	public ExerciseIntensityChartWeeklyPadView(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	protected int calTotalWidth() {
		return (getBackgroundWidth()+( getMaxDateWidth()/2 * 5));
	}

	@Override
	protected void drawBackgroundBottom(Canvas canvas) {
		// TODO Auto-generated method stub
	   // Background Bottom
		// super.drawBackgroundBottom(canvas);
	}

	@Override
	protected int calStartX() {
		return (getMaxDateWidth()*2);
	}

	@Override
	protected int calLevelOffsetX() {
		// TODO Auto-generated method stub
		//return super.calLevelOffsetX();
		return ( getMaxDateWidth()/2*3 );
	}

	@Override
	protected int calTotalHeight() {
		// TODO Auto-generated method stub
		return getBaseHeight();
	}

	@Override
	protected float calDateTimeStart() {
		// TODO Auto-generated method stub
		//return super.calDateTimeStart();
		
		return (getMaxDateWidth()*2)+5;
	}

	@Override
	protected float calDateTimeGap() {
		// TODO Auto-generated method stub
		//return super.calDateTimeGap();
		return ((getBackgroundWidth()-10) / 6.0f)-1;
	}

	@Override
	protected float setPointOffsetX() {
		// TODO Auto-generated method stub
		//return super.setPointOffsetX();
		return (float) 10;
	}

	@Override
	protected int calDayWidth() {
		// TODO Auto-generated method stub
		//return super.calDayWidth();
		
		return ((getBackgroundWidth()-10)/6 - 1);
	}

}

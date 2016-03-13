package com.asus.wellness.chart.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.asus.wellness.R;

public class RelaxationChartWeeklyPadView extends RelaxationChartWeeklyView {

	public RelaxationChartWeeklyPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void drawBackgroundBottom(Canvas canvas) {
		// TODO Auto-generated method stub
		//super.drawBackgroundBottom(canvas);
	}

	@Override
	protected int calTotalWidth() {
		return (getBackgroundWidth()+getMaxDateWidth()*2+calExtraAvgLineWdith());
	}
	

	@Override
	protected int calTotalHeight() {
		return getBaseHeight();
	}

	@Override
	protected int calStartX() {
		return getMaxDateWidth();
		//return 
	}
	

	@Override
	protected int calStartY() {
		// TODO Auto-generated method stub
		return ( super.calStartY() - 6);
	}

	@Override
	protected int calLevelGap() {
		return (getBackgroundHeight()-12) / 4;
	}

	@Override
	protected float calDateTimeStart() {
		// TODO Auto-generated method stub
		return getMaxDateWidth()+getResources().getDimensionPixelSize(R.dimen.chart_energy_PointOffsetX);
	}

	@Override
	protected float calDateTimeGap() {
		// TODO Auto-generated method stub
		return ((getBackgroundWidth()-10) / 6.0f - 1);
	}

	@Override
	protected int setPointOffsetX() {
		// TODO Auto-generated method stub
		return getResources().getDimensionPixelSize(R.dimen.chart_energy_PointOffsetX);
	}

	@Override
	protected int calDayWidth() {
		// TODO Auto-generated method stub
		return ((getBackgroundWidth()-10)/6);
	}


	@Override
	protected int calExtraAvgLineWdith() {
		// TODO Auto-generated method stub
		int addWdith,subWdith;
		addWdith = getResources().getDimensionPixelSize(R.dimen.chart_energy_AvgLineExtraWidth);
		subWdith = getResources().getDimensionPixelSize(R.dimen.chart_energy_BackgoundExtraWidth);
		return addWdith-subWdith;
	}
	
	


   
}

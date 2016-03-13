package com.asus.wellness;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import com.asus.wellness.R;

public class TimeLineRelativeLayout extends RelativeLayout {

	private int mDotsNum=10;
	protected int mTimeLineLeftMargin=50;
	protected int mTimeLineTopMargin=20;
	private int mLineLength=50;
	private int mDotsRadius=3;
	private boolean isLastLine=false;
	private boolean isTopLine=false;
	private ArrayList<Integer> mListLineColor=null;
	
	public static final int DEFAULT_LINE_COLOR=0xffffffff; 
	public static final int DEFAULT_LOCATION_LINE_COLOR=0xff556caa; 
	public static final int DEFAULT_LINE_WIDTH=1; 
	public static final int DEFAULT_LOCATION_LINE_WIDTH=5; 
	
	public TimeLineRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
        initTimeLine();
		// TODO Auto-generated constructor stub
	}
	
	public TimeLineRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
        initTimeLine();
		// TODO Auto-generated constructor stub
	}
	
	public TimeLineRelativeLayout(Context context) {
		super(context);
		setWillNotDraw(false);
        initTimeLine();
		// TODO Auto-generated constructor stub
	}

    //emily++++
    private void  initTimeLine(){
        mTimeLineLeftMargin = (int)getResources().getDimension(R.dimen.timeline_left_margin);
        mTimeLineTopMargin = (int)getResources().getDimension(R.dimen.timeline_top_margin);
    }
    //emily----

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if(isTopLine){
			drawTopSeparateLine(canvas);
		}
		drawLine(canvas);
		drawDots(canvas);
	}
	
	private void drawTopSeparateLine(Canvas canvas){
		Paint paint=new Paint();
		paint.setColor(0xff7dae1e);
		paint.setStrokeWidth(2);
		canvas.drawLine(0, 0, this.getContext().getResources().getDisplayMetrics().widthPixels, 0, paint);
	}
	
	protected void drawLine(Canvas canvas){
		int posX=mTimeLineLeftMargin, posY=mTimeLineTopMargin
				, mLastPosX=mTimeLineLeftMargin, mLastPosY=mTimeLineTopMargin;
		
		Paint linePaint=new Paint();
		linePaint.setStrokeWidth(DEFAULT_LINE_WIDTH);
		linePaint.setColor(DEFAULT_LINE_COLOR);
		
		for(int i=0;i<mDotsNum;i++){
			if(i!=0){
				if(mListLineColor!=null){
					int color=mListLineColor.get(i-1);
					if(color==DEFAULT_LOCATION_LINE_COLOR){
						linePaint.setStrokeWidth(DEFAULT_LOCATION_LINE_WIDTH);
					}
					else if(color==DEFAULT_LINE_COLOR){
						linePaint.setStrokeWidth(DEFAULT_LINE_WIDTH);
					}
					linePaint.setColor(color);
				}
				canvas.drawLine(mLastPosX, mLastPosY, posX, posY, linePaint);
			}
			mLastPosY=posY;
			posY+=mLineLength;
		}
		if(isLastLine){
			canvas.drawLine(mLastPosX, mLastPosY, posX, posY, linePaint);
		}
	}
	
	private void drawDots(Canvas canvas){
		int posX=mTimeLineLeftMargin, posY=mTimeLineTopMargin;
		
		Paint dotPaint=new Paint();
		dotPaint.setColor(Color.WHITE);
		dotPaint.setAntiAlias(true);
		
		for(int i=0;i<mDotsNum;i++){
			canvas.drawCircle(posX, posY, mDotsRadius, dotPaint);
			posY+=mLineLength;
		}
	}
	
	public void setNumDots(int num){
		mDotsNum=num;
		invalidate();
	}
	
	public void setTimeLineLeftMargin(int margin){
		mTimeLineLeftMargin=margin;
		invalidate();
	}
	
	public void setTimeLineTopMargin(int margin){
		mTimeLineTopMargin=margin;
		invalidate();
	}
	
	public void setDotsRadius(int radius){
		mDotsRadius=radius;
		invalidate();
	}
	
	public void setLineLength(int length){
		mLineLength=length;
		invalidate();
	}
	
	public int getLineLength(){
		return mLineLength;
	}
	
	public void setLastLine(boolean b){
		isLastLine=b;
		invalidate();
	}
	
	public Point getDotsPosition(int index){
		int posX=mTimeLineLeftMargin;
		int posY=mTimeLineTopMargin+index*mLineLength;
		return new Point(posX, posY);
	}
	
	public int getRequiredHeight(){
		Point point=getDotsPosition(mDotsNum-1);
		int height=point.y+mTimeLineTopMargin;
		if(isLastLine){
			height+=this.mLineLength;
		}
		return height;
	}
	
	public void setTopSeparateLine(boolean visible){
		isTopLine=visible;
		invalidate();
	}
	
	public void setDifferentLineColor(ArrayList<Integer> listLineColor){
		mListLineColor=listLineColor;
		invalidate();
	}
}

package com.asus.wellness.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.asus.wellness.R;

import java.util.ArrayList;
import java.util.List;

public class ExerciseIntensityChartWeeklyView extends View {

    private static final long ANIMATION_DURATION = 1000;
    private static final long ANIMATION_DURATION_DAY_STEP = 200;
    private static final long ANIMATION_DURATION_LEVEL_STEP = 200;
    private static String[] LEVEL = {"L5","L4","L3","L2","L1"};
    private Drawable mBackground;
    private Drawable mBackgroundNoData; // benson
    private Drawable mBackgroundBottom;
    private Drawable mBlueCircleL1,mBlueCircleL2,mBlueCircleL3,mBlueCircleL4,mBlueCircleL5;
    private TextPaint mTextPaint;
    private FontMetricsInt mFontMetricsInt = new FontMetricsInt();
    
    private int totalWidth;
    private int totalHeight;
    private int maxDateWidth;
    private int charWidth;
    private int textHeight;
    private int marginBetweenBackgroundBottomAndText;
    
    //chart start position to draw point and curve
    private int startX;
    private int startY;
    
    private int dayWidth;
    private float pointOffsetX;
    
    private String[] mDates;
    private List<Float[]> mDataValues;
    private List<Point> mPointsList;
    
    private boolean isPlayingAnimation=false;
    private long mAnimationStartTime;

    private float mPointHalfSize;

    private float[] DEFAULT_ANIMATION_TRANSLATE_OFFSET_X = new float[7];
    private float[] DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y = new float[7];
    
    public ExerciseIntensityChartWeeklyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mTextPaint = new TextPaint();
        mTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_exercise_date_text_font_size));
        mTextPaint.setColor(getResources().getColor(R.color.chart_label_text_color));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Align.CENTER);
        maxDateWidth =Math.round(mTextPaint.measureText("00/00"));
        charWidth = Math.round(mTextPaint.measureText("1"));
        mTextPaint.getFontMetricsInt(mFontMetricsInt);
        textHeight = 0 - mFontMetricsInt.ascent;
        //���ϦW�٤��@��,Benson���W���ܦ��@��,����800dp���W��
        mBackground = getResources().getDrawable(R.drawable.asus_wellness_bg_exercise_intensity_bg1);
        mBackground.setBounds(0, 0, mBackground.getIntrinsicWidth(), mBackground.getIntrinsicHeight());
        //
        
        //3D Bottom
        mBackgroundBottom = getResources().getDrawable(R.drawable.asus_wellness_bg_exercise_intensity_bg2);
        mBackgroundBottom.setBounds(0, 0, mBackgroundBottom.getIntrinsicWidth(), mBackgroundBottom.getIntrinsicHeight());
        //
        mBlueCircleL1 = getResources().getDrawable(R.drawable.asus_wellness_ic_blue5);
        mBlueCircleL1.setBounds(-(mBlueCircleL1.getIntrinsicWidth()/2), -(mBlueCircleL1.getIntrinsicHeight()/2),
                mBlueCircleL1.getIntrinsicWidth()-(mBlueCircleL1.getIntrinsicWidth()/2), mBlueCircleL1.getIntrinsicHeight()-(mBlueCircleL1.getIntrinsicHeight()/2));
        mBlueCircleL2 = getResources().getDrawable(R.drawable.asus_wellness_ic_blue4);
        mBlueCircleL2.setBounds(-(mBlueCircleL2.getIntrinsicWidth()/2), -(mBlueCircleL2.getIntrinsicHeight()/2),
                mBlueCircleL2.getIntrinsicWidth()-(mBlueCircleL2.getIntrinsicWidth()/2), mBlueCircleL2.getIntrinsicHeight()-(mBlueCircleL2.getIntrinsicHeight()/2));
        mBlueCircleL3 = getResources().getDrawable(R.drawable.asus_wellness_ic_blue3);
        mBlueCircleL3.setBounds(-(mBlueCircleL3.getIntrinsicWidth()/2), -(mBlueCircleL3.getIntrinsicHeight()/2),
                mBlueCircleL3.getIntrinsicWidth()-(mBlueCircleL3.getIntrinsicWidth()/2), mBlueCircleL3.getIntrinsicHeight()-(mBlueCircleL3.getIntrinsicHeight()/2));
        mBlueCircleL4 = getResources().getDrawable(R.drawable.asus_wellness_ic_blue2);
        mBlueCircleL4.setBounds(-(mBlueCircleL4.getIntrinsicWidth()/2), -(mBlueCircleL4.getIntrinsicHeight()/2),
                mBlueCircleL4.getIntrinsicWidth()-(mBlueCircleL4.getIntrinsicWidth()/2), mBlueCircleL4.getIntrinsicHeight()-(mBlueCircleL4.getIntrinsicHeight()/2));
        mBlueCircleL5 = getResources().getDrawable(R.drawable.asus_wellness_ic_blue1);
        mBlueCircleL5.setBounds(-(mBlueCircleL5.getIntrinsicWidth()/2), -(mBlueCircleL5.getIntrinsicHeight()/2),
                mBlueCircleL5.getIntrinsicWidth()-(mBlueCircleL5.getIntrinsicWidth()/2), mBlueCircleL5.getIntrinsicHeight()-(mBlueCircleL5.getIntrinsicHeight()/2));
        //Half width of data point, for now L1-L5 all same size.
        mPointHalfSize = mBlueCircleL1.getIntrinsicWidth()/2.0f;
        
        marginBetweenBackgroundBottomAndText = getResources().getDimensionPixelSize(R.dimen.chart_margin_between_bottom_text);
        
        //
        //totalWidth = mBackgroundBottom.getIntrinsicWidth() + maxDateWidth; 
        totalWidth  = calTotalWidth();
        totalHeight = calTotalHeight();

        //
        startX =calStartX(); 
        startY = mBackground.getIntrinsicHeight();
        dayWidth = calDayWidth();
        pointOffsetX = setPointOffsetX();
        
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[0]=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -40, getResources().getDisplayMetrics());
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[1]=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30, getResources().getDisplayMetrics());
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[2]=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -20, getResources().getDisplayMetrics());
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[3]=0;
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[4]=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[5]=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[6]=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[0]=0;
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[1]=0;
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[2]=0;
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[3]=0;
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[4]=0;
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[5]=0;
        DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[6]=0;
        
        
    }

    //Get information
    protected int getBackgroundBottomWidth()
    {
    	return mBackgroundBottom.getIntrinsicWidth();
    }
    
    protected int getMaxDateWidth()
    {
    	return maxDateWidth;
    }
    
    protected int getBackgroundWidth()
    {
    	return mBackground.getIntrinsicWidth();
    }
    
    protected int getBackgroundBottomHeight()
    {	
    	return mBackgroundBottom.getIntrinsicHeight();
    }
    
    protected int getBaseHeight()
    {
    	return mBackground.getIntrinsicHeight()  + marginBetweenBackgroundBottomAndText + textHeight;
    }
    
    // �v��VIEW�Ѽ� 
    
    protected int calTotalWidth()
    {
    	return mBackgroundBottom.getIntrinsicWidth() + maxDateWidth;
    }
    
    protected int calTotalHeight()
    {
		return getBaseHeight()+getBackgroundBottomHeight();
    }
    
    protected int calStartX()
    {
    	return ( (totalWidth - mBackground.getIntrinsicWidth())/2);
    }
    
    protected float calDateTimeStart()
    {
    	return (totalWidth - mBackgroundBottom.getIntrinsicWidth())/2.0f ;
    }
    
    protected float calDateTimeGap()
    {
    	return (mBackgroundBottom.getIntrinsicWidth()/6.0f) ;
    }
    
    protected float setPointOffsetX()
    {
    	return 0;
    }
    
    protected int calDayWidth()
    {
    	
    	return (mBackground.getIntrinsicWidth())/6;
    }

    protected int calLevelOffsetX()
    {
    	
    	return maxDateWidth/2;
    }
    
    ///
    @Override
    protected void onDraw(Canvas canvas) {
    	//Log.d("PHH", "E"+Integer.toString(totalWidth));
    	super.onDraw(canvas);

        int length = 0;
        for(Float[] list : mDataValues) {
            if(list.length != 0) {
                length = list.length;
                break;
            }
        }

        if(length == 0) {
            drawNoDataString(canvas);
        }

        drawBackground(canvas);
        drawLevel(canvas);
        drawBackgroundBottom(canvas);
        
        
        drawDateTime(canvas);
        drawDataPoint(canvas);
        if (isPlayingAnimation){
            updatePointAnimation();
            invalidate();
        }else{
            setDrawingCacheEnabled(true);
        }
    }

	private void drawNoDataString(Canvas canvas) {
		// TODO Auto-generated method stub

        Log.d("kim_bai", "draw No Data String");
		String noDataText;
		noDataText = getResources().getString(R.string.none_data_inside);
		TextPaint mNoDataTextPaint;
		mNoDataTextPaint = new TextPaint();
        mNoDataTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        mNoDataTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_calorie_no_data_txt_font_size));
        mNoDataTextPaint.setColor(getResources().getColor(R.color.chart_calorie_no_data_text_color));
        mNoDataTextPaint.setAntiAlias(true);
        FontMetrics fontMetrics = mNoDataTextPaint.getFontMetrics();
        int h = (int)( fontMetrics.descent - fontMetrics.ascent) / 2;
        int noDataTextMargingLeft, noDataTextMargingTop;
        noDataTextMargingLeft =(int) (startX +( (mBackground.getIntrinsicWidth() - mNoDataTextPaint.measureText(noDataText)) / 2));
        noDataTextMargingTop = ( mBackground.getIntrinsicHeight() + h ) / 2;
        canvas.drawText(noDataText, noDataTextMargingLeft , noDataTextMargingTop, mNoDataTextPaint);
	}

	private void drawDataPoint(Canvas canvas) {
        //Draw datapoint if exists
        if (mDataValues != null){
            canvas.save();
            canvas.translate(startX, startY);
            Point point;
            if (isPlayingAnimation){
                for (int i = 0; i < mPointsList.size() ; i++){
                    point = mPointsList.get(i);
                    canvas.save();
                    canvas.translate(point.x+DEFAULT_ANIMATION_TRANSLATE_OFFSET_X[point.day]*(1-point.animOffsetXScale),
                                     point.y+DEFAULT_ANIMATION_TRANSLATE_OFFSET_Y[point.day]*(1-point.animOffsetYScale));
                    
                    Drawable drawable;
                    if (point.level == 0){
                        drawable = mBlueCircleL1;
                    }else if (point.level == 1){
                        drawable = mBlueCircleL2;
                    }else if (point.level == 2){
                        drawable = mBlueCircleL3;
                    }else if (point.level == 3){
                        drawable = mBlueCircleL4;
                    }else {
                        drawable = mBlueCircleL5;
                    }
                    float size = mPointHalfSize*point.animScale;
                    drawable.setBounds(-(int)size,-(int)size,(int)size,(int)size);
                    drawable.setAlpha(point.animAlpha);
                    drawable.draw(canvas);
                    canvas.restore();
                }
            }else{
                mBlueCircleL1.setBounds(-(mBlueCircleL1.getIntrinsicWidth()/2), -(mBlueCircleL1.getIntrinsicHeight()/2),
                        mBlueCircleL1.getIntrinsicWidth()-(mBlueCircleL1.getIntrinsicWidth()/2), mBlueCircleL1.getIntrinsicHeight()-(mBlueCircleL1.getIntrinsicHeight()/2));
                mBlueCircleL1.setAlpha(255);
                mBlueCircleL2.setBounds(-(mBlueCircleL2.getIntrinsicWidth()/2), -(mBlueCircleL2.getIntrinsicHeight()/2),
                        mBlueCircleL2.getIntrinsicWidth()-(mBlueCircleL2.getIntrinsicWidth()/2), mBlueCircleL2.getIntrinsicHeight()-(mBlueCircleL2.getIntrinsicHeight()/2));
                mBlueCircleL2.setAlpha(255);
                mBlueCircleL3.setBounds(-(mBlueCircleL3.getIntrinsicWidth()/2), -(mBlueCircleL3.getIntrinsicHeight()/2),
                        mBlueCircleL3.getIntrinsicWidth()-(mBlueCircleL3.getIntrinsicWidth()/2), mBlueCircleL3.getIntrinsicHeight()-(mBlueCircleL3.getIntrinsicHeight()/2));
                mBlueCircleL3.setAlpha(255);
                mBlueCircleL4.setBounds(-(mBlueCircleL4.getIntrinsicWidth()/2), -(mBlueCircleL4.getIntrinsicHeight()/2),
                        mBlueCircleL4.getIntrinsicWidth()-(mBlueCircleL4.getIntrinsicWidth()/2), mBlueCircleL4.getIntrinsicHeight()-(mBlueCircleL4.getIntrinsicHeight()/2));
                mBlueCircleL4.setAlpha(255);
                mBlueCircleL5.setBounds(-(mBlueCircleL5.getIntrinsicWidth()/2), -(mBlueCircleL5.getIntrinsicHeight()/2),
                        mBlueCircleL5.getIntrinsicWidth()-(mBlueCircleL5.getIntrinsicWidth()/2), mBlueCircleL5.getIntrinsicHeight()-(mBlueCircleL5.getIntrinsicHeight()/2));
                mBlueCircleL5.setAlpha(255);
                for (int i = 0; i < mPointsList.size() ; i++){
                    point = mPointsList.get(i);
                    canvas.save();
                    canvas.translate(point.x,  point.y);
                    if (point.level == 0){
                        mBlueCircleL1.draw(canvas);
                    }else if (point.level == 1){
                        mBlueCircleL2.draw(canvas);
                    }else if (point.level == 2){
                        mBlueCircleL3.draw(canvas);
                    }else if (point.level == 3){
                        mBlueCircleL4.draw(canvas);
                    }else {
                        mBlueCircleL5.draw(canvas);
                    }
                    canvas.restore();
                }
            }
            canvas.restore();
        }
    }

    private void drawDateTime(Canvas canvas) {
        //Draw date if exists
        if (mDates != null){
        	float start = calDateTimeStart();
            float gap = calDateTimeGap();
            for (int i = 0 ; i < mDates.length; i++){
                if (mDates[i] != null){
                    canvas.drawText(mDates[i], start+ i*gap, totalHeight, mTextPaint);
                }
            }
        }
    }
    
    private void drawBackground(Canvas canvas) {
        //Draw background
        canvas.save();
        canvas.translate(startX, 0);
        mBackground.draw(canvas);
        canvas.restore();

    }
    
    private void drawBackgroundNoData(Canvas canvas) {
        //Draw background
    	mBackgroundNoData = getResources().getDrawable(R.drawable.asus_wellness_bg_exercise_intensity_nodata);
        mBackgroundNoData.setBounds(0, 0, mBackgroundNoData.getIntrinsicWidth(), mBackgroundNoData.getIntrinsicHeight());
    	canvas.save();
        canvas.translate(startX, 0);
        mBackgroundNoData.draw(canvas);
        canvas.restore();

    }
    
    protected void drawLevel(Canvas canvas){
    	//Draw level
        canvas.save();
        int gap = mBackground.getIntrinsicHeight()/5;
        
        canvas.translate(calLevelOffsetX(), gap/2 + textHeight/2);
        for (int i = 0 ; i < LEVEL.length ; i++){
            canvas.drawText(LEVEL[i], 0, 0, mTextPaint);
            canvas.translate(0, gap);
        }
        canvas.restore();
    	
    }
  
    protected void drawBackgroundBottom(Canvas canvas) {
    	//Draw background bottom
        canvas.save();
        canvas.translate(maxDateWidth/2, startY);
        mBackgroundBottom.draw(canvas);
        canvas.restore();
	}

    /**
     * Should set 7 strings for a week
     * @param dates
     */
    public void setDateString(String[] dates){
        mDates = dates;
        setDrawingCacheEnabled(false);
        invalidate();
    }
    
    /**
     * Should set float[7][?] floats for a week
     * @param data
     */
    public void setDataValues(List<Float[]> data){
        mDataValues = data;
        calculatePoints();
        setDrawingCacheEnabled(false);
        invalidate();
    }

    private void calculatePoints() {
        mPointsList = new ArrayList<Point>();
        if (mDataValues != null){
            int heightGap = mBackground.getIntrinsicHeight()/5;
            float heightRatio = heightGap/0.1f;
            //generate points
            Float[] valuesADay;
            Point point;
            float y;
            int level;
            for (int i = 0; i < mDataValues.size() ; i++){
                valuesADay = mDataValues.get(i);
                for (int j = 0 ; j < valuesADay.length ; j++){
                    
                    if (valuesADay[j] < 0.6){
                        level = 0;
                        y = -(int)((heightGap/0.6)*valuesADay[j]);
                    }else if (valuesADay[j] < 0.7){
                        level = 1;
                        y = - (int)(heightGap+(heightRatio*(valuesADay[j]-0.6)));
                    }else if (valuesADay[j] < 0.8){
                        level = 2;
                        y = -(int)((heightGap*2)+(heightRatio*(valuesADay[j]-0.7)));
                    }else if (valuesADay[j] < 0.9){
                        level = 3;
                        y = -(int)((heightGap*3)+(heightRatio*(valuesADay[j]-0.8)));
                    }else {
                        level = 4;
                        y = -(int)((heightGap*4)+(heightRatio*(valuesADay[j]-0.9)));
                    }
                    point = new Point(i*dayWidth + pointOffsetX, y, i,level);
                    mPointsList.add(point);
                }
                
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(totalWidth, totalHeight);
    }
    
    public void playAnimation(){
        setDrawingCacheEnabled(false);
        isPlayingAnimation = true;
        mAnimationStartTime = System.currentTimeMillis();
        updatePointAnimation();
        invalidate();
    }

    private void updatePointAnimation() {
        long now = System.currentTimeMillis();
        
        if (now > mAnimationStartTime + ANIMATION_DURATION + ANIMATION_DURATION_DAY_STEP*7+ANIMATION_DURATION_LEVEL_STEP*2){
            isPlayingAnimation = false;
            return;
        }
        for (int i = 0 ; i < mPointsList.size();i++){
            mPointsList.get(i).updateAnimation(mAnimationStartTime, now);
        }
    }
    
    private static class Point {
        private static AccelerateInterpolator sAccelerateInterpolator = new AccelerateInterpolator();
        //private static OvershootInterpolator sOvershootInterpolator = new OvershootInterpolator(2.0f);
        private static Interpolator sOvershootInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                if (input < 0) {
                    return 0;
                }else if (input < 0.5f){
                    return input/0.5f;
                }else if (input < 0.75f){
                    return 1+((input-0.5f)/0.25f)*0.5f;
                }else if (input < 1.0f){
                    return 1+(0.5f-((input-0.75f)/0.25f)*0.5f);
                }
                return 1;
            }
        };
        private Point(float x, float y, int day, int level){
            this.x = x;
            this.y = y;
            this.day = day;
            this.level = level;
        }
        
        private void updateAnimation(long startTime, long currentTime){
            long now = currentTime;
            long duration = now - (startTime+ANIMATION_DURATION_DAY_STEP*day + ANIMATION_DURATION_LEVEL_STEP*Math.abs(level-2));
            animRatio = 0;
            if (duration > 0){
                animRatio = duration /(float) ANIMATION_DURATION;
                if (animRatio >= 1.0f){
                    animRatio = 1;
                }
                float alphaScale = sAccelerateInterpolator.getInterpolation(animRatio)*4;
                if (alphaScale > 1.0f){
                    alphaScale = 1.0f;
                }
                animAlpha = (int)(255*alphaScale);

                animScale = 2-sOvershootInterpolator.getInterpolation(animRatio);
                animOffsetXScale = sOvershootInterpolator.getInterpolation(animRatio);
                if (animOffsetXScale > 1.0f){
                    animOffsetXScale = 1.0f;
                }
                animOffsetYScale = animOffsetXScale;
                
            }else{
                animRatio = 0;
                animAlpha = 0;
                animScale = 2;
                animOffsetXScale = 0;
                animOffsetYScale = 0;
            }
        }
        
        final float x, y;
        final int day,level;
        float animRatio,animScale;
        int animAlpha;
        float animOffsetXScale, animOffsetYScale;
        
        @Override
        public String toString() {
            return x + ", " + y;
        }
    }

    
}

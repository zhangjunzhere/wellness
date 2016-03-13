package com.asus.wellness.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.asus.wellness.R;

import java.util.ArrayList;
import java.util.List;

public class RelaxationChartWeeklyView extends View {

    private static final long ANIMATION_DURATION = 1000;
    private static String[] LEVEL = {"100","75","50","25","0"};
    private Drawable mBackground;
    private Drawable mBackgroundBottom;
    private Drawable mBackgroundNoData;
    private Drawable mColor;
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
    private int pointOffsetX;
    
    private int avgPositionY;
    private int avgLineWdith;
    private TextPaint mAvgTextPaint;
    private int avgTextWidth;
    
    
    private String[] mDates;
    private Float[] mDataValues;
    private List<Integer> mPointList;
    private Path mPointPath,mPointPathAnimation;
    
    private boolean isPlayingAnimation=false;
    private long mAnimationStartTime;
    private AccelerateDecelerateInterpolator mAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
//smile_gao add for avg string 2014/12/4
    private String mAvgStr;
//end smile
    public RelaxationChartWeeklyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mTextPaint = new TextPaint();
        mTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_exercise_date_text_font_size));
        mTextPaint.setColor(getResources().getColor(R.color.chart_label_text_color));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Align.CENTER);
        
        mAvgTextPaint = new TextPaint();
        mAvgTextPaint.setTypeface(Typeface.create("Roboto-Medium", Typeface.NORMAL));
        mAvgTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_energy_Avg_font_size));
        mAvgTextPaint.setColor(getResources().getColor(R.color.chart_engery_avg_font_color));
        mAvgTextPaint.setAntiAlias(true);
        mAvgTextPaint.setTextAlign(Align.LEFT);
        avgTextWidth=Math.round(mAvgTextPaint.measureText("Avg."));
        
        
        maxDateWidth =Math.round(mTextPaint.measureText("00/00"));
        charWidth = Math.round(mTextPaint.measureText("1"));
        mTextPaint.getFontMetricsInt(mFontMetricsInt);
        textHeight = 0 - mFontMetricsInt.ascent;

        mBackground = getResources().getDrawable(R.drawable.asus_wellness_bg_energy);
        mBackground.setBounds(0, 0, mBackground.getIntrinsicWidth(), mBackground.getIntrinsicHeight());
        
        
        mBackgroundBottom = getResources().getDrawable(R.drawable.asus_wellness_bg_exercise_intensity_bg2);
        mBackgroundBottom.setBounds(0, 0, mBackgroundBottom.getIntrinsicWidth(), mBackgroundBottom.getIntrinsicHeight());

        mColor = getResources().getDrawable(R.drawable.chart_relaxation_stress_color);
        mColor.setBounds(0, 0, mBackground.getIntrinsicWidth(), mBackground.getIntrinsicHeight());
        
        marginBetweenBackgroundBottomAndText = getResources().getDimensionPixelSize(R.dimen.chart_margin_between_bottom_text);
        
        totalWidth = calTotalWidth();
        totalHeight= calTotalHeight();


        
        startX = calStartX();
        startY = calStartY();
        dayWidth = calDayWidth();
        pointOffsetX = setPointOffsetX();
        avgLineWdith = calAvgLineWdith();
        //Disable Hardware acceleration for this view
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //smile_gao add 2014/12/4  for agv string length
        mAvgStr = getResources().getString(R.string.weekly_info_energy_avg);
        int avgwidth = getAvgStringWidth(mAvgStr);
        calcAvgLineWidthWhenAvgStringLong(avgwidth);
        //end smile_gao 2014/12/4
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
    
    protected int getBackgroundHeight()
    {
    	return mBackground.getIntrinsicHeight();
    	
    }
    
    protected int getBackgroundBottomHeight()
    {	
    	return mBackgroundBottom.getIntrinsicHeight();
    }
    
    protected int getBaseHeight()
    {
    	return mBackground.getIntrinsicHeight()  + marginBetweenBackgroundBottomAndText + textHeight +(textHeight/2);
    }
    
    protected int getAvgTextWdith()
    {
    	return avgTextWidth;
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
    	return (totalWidth - mBackground.getIntrinsicWidth())/2;
    }
    
    protected int calStartY()
    {
    	return (textHeight/2) + mBackground.getIntrinsicHeight();
    }
    
    protected float calDateTimeStart()
    {
    	return (totalWidth - mBackgroundBottom.getIntrinsicWidth())/2.0f ;
    }
    
    protected float calDateTimeGap()
    {
    	return (mBackgroundBottom.getIntrinsicWidth()/6.0f) ;
    }
    
    protected int calDayWidth()
    {
    	return (mBackground.getIntrinsicWidth())/6;
    }
    
    protected int calLevelGap()
    {
    	return mBackground.getIntrinsicHeight()/4;
    }
    
    protected int setPointOffsetX()
    {
    	return 0;
    }
    protected int calExtraAvgLineWdith()
    {
		return getResources().getDimensionPixelSize(R.dimen.chart_energy_AvgLineExtraWidth);
    	
    }
    
    protected int calAvgLineWdith()
    {
    	return mBackground.getIntrinsicWidth() +  calExtraAvgLineWdith();
    }
    

    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float value = 0;
        for(Float f : mDataValues) {
            if(f != 0) {
                value = f;
                break;
            }
        }

        if(value == 0) {
            drawNoDataString(canvas);
        }

        drawDateTime(canvas);
        drawDataPoint(canvas);
        drawAvgLine(canvas);
        drawBackground(canvas);
        drawBackgroundBottom(canvas);
        if (isPlayingAnimation){
            updateAnimationPath();
            invalidate();
        }else{
            setDrawingCacheEnabled(true);
        }
    }
    private void drawAvgLine(Canvas canvas) {
		// TODO Auto-generated method stub
    	if (mDataValues != null){
    		canvas.save();
    		canvas.translate(startX + pointOffsetX, startY);
    		Paint paint = new Paint();
    		paint.setColor(0xFF942268);
    		paint.setStrokeWidth(1);
            //smile_gao modify avg length 2014/12/4
            //draw line
    		canvas.drawLine(0, avgPositionY, avgLineWdith, avgPositionY, paint);

            //draw avg
            FontMetrics fontMetrics = mAvgTextPaint.getFontMetrics();
    		int h =(int)(  fontMetrics.ascent + fontMetrics.descent) / 2;
    		canvas.drawText(mAvgStr, avgLineWdith+5, avgPositionY - h, mAvgTextPaint);
    		canvas.restore();
            //end smile_gao 2014/12/4
    	}
	}
    //smile_gao add for avg string length 2014/12/4
    private int getAvgStrPading()
    {
        return getResources().getDimensionPixelSize(R.dimen.chart_energy_AvgStringPading);
    }
    private void calcAvgLineWidthWhenAvgStringLong(int avgstrlength)
    {
        int sumwidth = avgstrlength+getAvgStrPading()+avgLineWdith+startX + pointOffsetX;
    //    Log.i("smile","sum: "+String.valueOf(sumwidth));
        if( sumwidth>totalWidth)
            avgLineWdith =avgLineWdith-(sumwidth-totalWidth)-12;// totalWidth - avgstrlength-30-startX-pointOffsetX;
    }
    private int getAvgStringWidth(String avg)
    {
        Rect rect = new Rect();
        mAvgTextPaint.getTextBounds(avg,0,avg.length(),rect);

        return rect.right-rect.left;
    }
    //end smile_gao 2014/12/4


	private void drawNoDataString(Canvas canvas) {
		// TODO Auto-generated method stub
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
        noDataTextMargingTop = ( mBackground.getIntrinsicHeight() + h + textHeight/2) / 2;
        canvas.drawText(noDataText, noDataTextMargingLeft , noDataTextMargingTop, mNoDataTextPaint);
	}
    private void drawDataPoint(Canvas canvas) {
        //Draw datapoint if exists
        if (mDataValues != null){
            canvas.save();
            //canvas.translate(startX , startY);
            canvas.translate(startX + pointOffsetX, startY);
            if (isPlayingAnimation){
                canvas.clipPath(mPointPathAnimation);
            }else{
                canvas.clipPath(mPointPath);
            }
            canvas.translate(0, -mBackground.getIntrinsicHeight());
            mColor.draw(canvas);
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
        canvas.translate(startX, (textHeight/2));
        mBackground.draw(canvas);
        canvas.restore();
        
        //Draw level
        canvas.save();
        canvas.translate(maxDateWidth/2, textHeight);
        int gap = calLevelGap();
        for (int i = 0 ; i < LEVEL.length ; i++){
            canvas.drawText(LEVEL[i], 0, 0, mTextPaint);
            canvas.translate(0, gap);
        }
        canvas.restore();

        
    }
    
    private void drawBackgroundNoData(Canvas canvas) {
    	mBackgroundNoData = getResources().getDrawable(R.drawable.asus_wellness_bg_energy_nodata);
        mBackgroundNoData.setBounds(0, 0, mBackgroundNoData.getIntrinsicWidth(), mBackgroundNoData.getIntrinsicHeight());
        
    	//Draw background
        canvas.save();
        canvas.translate(startX, (textHeight/2));
        mBackgroundNoData.draw(canvas);
        canvas.restore();
        
        //Draw level
        canvas.save();
        canvas.translate(maxDateWidth/2, textHeight);
        int gap = calLevelGap();
        for (int i = 0 ; i < LEVEL.length ; i++){
            canvas.drawText(LEVEL[i], 0, 0, mTextPaint);
            canvas.translate(0, gap);
        }
        canvas.restore();

        
    }
    
    protected void drawBackgroundBottom(Canvas canvas)
    {
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
     * Should set float[7] floats for a week
     * @param data
     */
    public void setDataValues(Float[] data){
        mDataValues = data;
        calculatePoints();
        setDrawingCacheEnabled(false);
        invalidate();
    }

    private void calculatePoints() {
        mPointList = new ArrayList<Integer>();
        mPointPath = new Path();
        int cnt = 0;
        int sum = 0;
        avgPositionY = 0;
        if (mDataValues != null){
            int lastX = 0;
            int lastY = 0;
            float ratio = mBackground.getIntrinsicHeight()/100.0f;
            for (int i = 0; i < mDataValues.length ; i++){
                if (mDataValues[i] != null){
                    lastX = dayWidth*i  ;
                    lastY = -Math.round(mDataValues[i] * ratio);

                    if(lastY != 0 )
                    { 
                    	sum = sum + Math.abs(lastY);
                    	cnt = cnt + 1;
                    }
                    mPointPath.lineTo(lastX, lastY);
                }
                mPointList.add(lastY);
            }
            if (lastX > 0){
                mPointPath.lineTo(lastX, 0);
            }
            mPointPath.close();
            if(cnt != 0)
            {
            	avgPositionY = -(int)(sum / cnt);
            }
        }
    }
    
    public void playAnimation(){
        setDrawingCacheEnabled(false);
        isPlayingAnimation = true;
        mAnimationStartTime = System.currentTimeMillis();
        updateAnimationPath();
        invalidate();
    }

    private void updateAnimationPath() {
        long duration = System.currentTimeMillis() - mAnimationStartTime;
        float scale = duration /(float) ANIMATION_DURATION;
        if (scale >= 1.0f){
            scale = 1;
            isPlayingAnimation = false;
            return;
        }
        scale = mAccelerateDecelerateInterpolator.getInterpolation(scale);
        mPointPathAnimation = new Path();
        if (mDataValues != null){
            int lastX = 0;
            int lastY = 0;
            float ratio = mBackground.getIntrinsicHeight()/100.0f;
            for (int i = 0; i < mDataValues.length ; i++){
                if (mDataValues[i] != null){
                    lastX = dayWidth*i ;
                    lastY = -Math.round(mDataValues[i] * ratio*scale);
                    mPointPathAnimation.lineTo(lastX, lastY);
                }
            }
            if (lastX > 0){
                mPointPathAnimation.lineTo(lastX, 0);
            }
            mPointPathAnimation.close();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(totalWidth, totalHeight);
    }
}

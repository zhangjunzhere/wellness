package com.asus.wellness.chart.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.asus.wellness.R;

public class CalorieChartWeeklyView extends View{
    
    private final static int INVALID = Integer.MIN_VALUE;
    
    private float TOUCH_MOVE_THRESHOLD;

    private Drawable mCrown;
    private Drawable mBackgroundBottom;
    private Drawable mDateSelector;
    private Drawable mNoDataImage;
    private Drawable mNoDateBackground;
    private TextPaint mDateTextPaint;
    private TextPaint mDateLargeTextPaint;
    private TextPaint mNoDataTextPaint;
    private Paint mCircleStrokePaint;
    private Paint mCircleFillPaint;
    private Paint mCurvePaint;
    private TextPaint mDigitalTextPaint;
    private TextPaint mUnitTextPaint;
    private FontMetricsInt mFontMetricsInt = new FontMetricsInt();
    private FontMetricsInt mLargeFontMetricsInt = new FontMetricsInt();
    
    private boolean isFullBackground = false;
    private int noDataImageMargingTop;
    private int noDataTextMargingTop;
    private int noDataTextMargingLeft;
    private int backgroundHeight;
    private int backgroundOffsetX;
    private int marginBetweenCrownAndCircle;
    private int circleRadius;
    private int circleRadiusLarger;
    private int dateSelectorBottomHeight;
    private int totalWidth;
    private int totalHeight;
    private int maxLargeDateWidth;
    private int marginBetweenBackgroundBottomAndText;
    private int marginBetweenBackgroundBottomAndLargeText;
    private int backgroundPaddingTop,backgroundPaddingBottom;
    private int digitalMarginLeft,digitalMarginBottom,unitMarginLeft;
    private float dateFontSize,dateFontSizeLarger;
    
    //chart start position to draw point and curve
    private int startX;
    private int startY;
    
    private int dayWidth;
    
    private float scaleRatio = 1.0f;
    
    private int targetX = INVALID;
    
    private final static String unitText= "kcal";
    private final static String mDigitalTextFormatter = "%d";
    private final static String mZeroDigitalText = "0";
    private String[] digitalText = {mZeroDigitalText,mZeroDigitalText,mZeroDigitalText,mZeroDigitalText,mZeroDigitalText,mZeroDigitalText,mZeroDigitalText};
    private String noDataText;
    
    private String[] mDates;
    private Float[] mDataValues = null;
    private List<Point> mPointList = new ArrayList<Point>();;
    private Path mPointPath = new Path();
    private Path mPointPathClip = new Path();
    private int mCrownIndex = -1;
    private float mMaxData = Float.MIN_VALUE, mMinData = Float.MAX_VALUE;
    
    public CalorieChartWeeklyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TOUCH_MOVE_THRESHOLD = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        
        mCircleStrokePaint = new Paint();
        mCircleStrokePaint.setStyle(Style.STROKE);
        mCircleStrokePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.chart_calorie_circle_width));
        mCircleStrokePaint.setAntiAlias(true);
        
        mCircleFillPaint = new Paint();
        mCircleFillPaint.setStyle(Style.FILL);
        mCircleFillPaint.setColor(Color.WHITE);
        
        mCurvePaint = new Paint();
        mCurvePaint.setStyle(Style.STROKE);
        mCurvePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.chart_calorie_curve_width));
        mCurvePaint.setAntiAlias(true);
        
        mDateTextPaint = new TextPaint();
        mDateTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        dateFontSize = getResources().getDimension(R.dimen.chart_date_text_font_size);
        dateFontSizeLarger = getResources().getDimension(R.dimen.chart_date_text_font_size_large);
        mDateTextPaint.setTextSize(dateFontSize);
        mDateTextPaint.setColor(getResources().getColor(R.color.chart_date_text_color));
        mDateTextPaint.setAntiAlias(true);
        mDateTextPaint.getFontMetricsInt(mFontMetricsInt);
        mDateTextPaint.setTextAlign(Align.CENTER);
        
        mDateLargeTextPaint = new TextPaint();
        mDateLargeTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mDateLargeTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_date_text_font_size_large));
        mDateLargeTextPaint.setColor(getResources().getColor(R.color.chart_date_text_color));
        mDateLargeTextPaint.setAntiAlias(true);
        maxLargeDateWidth =Math.round(mDateLargeTextPaint.measureText("00/00"));
        mDateLargeTextPaint.getFontMetricsInt(mLargeFontMetricsInt);
        
        mDigitalTextPaint = new TextPaint();
        mDigitalTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mDigitalTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_calorie_digital_text_font_size));
        mDigitalTextPaint.setColor(getResources().getColor(R.color.chart_calorie_digital_text_color));
        mDigitalTextPaint.setAntiAlias(true);
        
        mUnitTextPaint = new TextPaint();
        mUnitTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        mUnitTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_calorie_unit_text_font_size));
        mUnitTextPaint.setColor(getResources().getColor(R.color.chart_calorie_digital_text_color));
        mUnitTextPaint.setAntiAlias(true);
        
        mNoDataTextPaint = new TextPaint();
        mNoDataTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        mNoDataTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_calorie_no_data_txt_font_size));
        mNoDataTextPaint.setColor(getResources().getColor(R.color.chart_calorie_no_data_text_color));
        mNoDataTextPaint.setAntiAlias(true);
        
        dateSelectorBottomHeight = getResources().getDimensionPixelSize(R.dimen.chart_calorie_date_selector_bottom_height);
        digitalMarginLeft = getResources().getDimensionPixelSize(R.dimen.chart_calorie_digital_text_margin_left);
        digitalMarginBottom = getResources().getDimensionPixelSize(R.dimen.chart_calorie_digital_text_margin_bottom);
        unitMarginLeft = getResources().getDimensionPixelSize(R.dimen.chart_calorie_unit_text_margin_left);

        isFullBackground = getResources().getBoolean(R.bool.chart_calorie_is_full_background);
        backgroundHeight = getResources().getDimensionPixelSize(R.dimen.chart_calorie_background_height);
        backgroundOffsetX = getResources().getDimensionPixelSize(R.dimen.chart_calorie_background_offsetX);
        mBackgroundBottom = getResources().getDrawable(R.drawable.asus_wellness_bg_calories_bg);
        mBackgroundBottom.setBounds(0, -mBackgroundBottom.getIntrinsicHeight(), mBackgroundBottom.getIntrinsicWidth(), 0);
        mDateSelector = getResources().getDrawable(R.drawable.asus_wellness_bg_calories_round2);
        mDateSelector.setBounds(-(mDateSelector.getIntrinsicWidth()/2), -mDateSelector.getIntrinsicHeight(), mDateSelector.getIntrinsicWidth()-(mDateSelector.getIntrinsicWidth()/2), 0);
        mCrown = getResources().getDrawable(R.drawable.asus_wellness_ic_calories2);
        mCrown.setBounds(-(mCrown.getIntrinsicWidth()/2), -mCrown.getIntrinsicHeight(), mCrown.getIntrinsicWidth()-(mCrown.getIntrinsicWidth()/2), 0);
        
        mNoDataImage = getResources().getDrawable(R.drawable.asus_wellness_bg_calories_nodata);
        mNoDataImage.setBounds(0, 0, mNoDataImage.getIntrinsicWidth(), mNoDataImage.getIntrinsicHeight());
        
        noDataImageMargingTop = getResources().getDimensionPixelSize(R.dimen.chart_calorie_no_data_img_margin_top);
        noDataTextMargingTop = getResources().getDimensionPixelSize(R.dimen.chart_calorie_no_data_txt_margin_top);
        noDataText = getResources().getString(R.string.none_data_inside);
        
        marginBetweenCrownAndCircle = getResources().getDimensionPixelSize(R.dimen.chart_calorie_crown_margin_bottom);
        
        marginBetweenBackgroundBottomAndText = getResources().getDimensionPixelSize(R.dimen.chart_margin_between_bottom_text);
        marginBetweenBackgroundBottomAndLargeText = getResources().getDimensionPixelSize(R.dimen.chart_margin_between_bottom_text_large);
        
        if (isFullBackground){
        	totalWidth = mBackgroundBottom.getIntrinsicWidth();
        }else{
        	totalWidth = mBackgroundBottom.getIntrinsicWidth() + Math.max(maxLargeDateWidth, mDateSelector.getIntrinsicWidth());
        }
        
        totalHeight = backgroundHeight + dateSelectorBottomHeight;
        
        circleRadius = getResources().getDimensionPixelSize(R.dimen.chart_calorie_circle_radius);
        circleRadiusLarger = getResources().getDimensionPixelSize(R.dimen.chart_calorie_circle_radius_larger);
        backgroundPaddingTop = getResources().getDimensionPixelSize(R.dimen.chart_calorie_background_padding_top);
        backgroundPaddingBottom = getResources().getDimensionPixelSize(R.dimen.chart_calorie_background_padding_bottom);
        
        startX = (totalWidth - mBackgroundBottom.getIntrinsicWidth())/2 + backgroundOffsetX;
        startY = backgroundHeight-backgroundPaddingBottom;
        
        dayWidth = (mBackgroundBottom.getIntrinsicWidth()-backgroundOffsetX*2)/6;
        
        noDataTextMargingLeft = (int)(totalWidth/2 - mNoDataTextPaint.measureText(noDataText)/2);
        
        
        Shader shader = new LinearGradient(0, -(backgroundHeight-backgroundPaddingTop-backgroundPaddingBottom), 0, 0,
                getResources().getColor(R.color.chart_calorie_curve_color_start),
                getResources().getColor(R.color.chart_calorie_curve_color_end), TileMode.MIRROR);
        mCurvePaint.setShader(shader);
        mCircleStrokePaint.setShader(shader);
        
        currentX = startX;
        targetX = currentX;
       
        //Disable Hardware acceleration for this view
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    
   
    private int mCurrnetIndex;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDataValues == null){
        	//
        	//
        	//
        	//
        	
        	if(getResources().getConfiguration().smallestScreenWidthDp >=800)
        	{
        		
        		drawBackgroundNoData(canvas);
        	}
        	else
        	{
        		drawBackground(canvas);
        		drawDateTime(canvas);
        		drawNoDataImage(canvas);
        	}
        	drawNoDataString(canvas);
        	
        	return;
        }
        if (targetX != currentX){
            //Calculate animation accelerate speed 
            updateCurrentX();
        }
        
        //Calculate animation scale ratio
        mCurrnetIndex = calculateDateSelectorIndex(currentX);
        float currentIndexPositionX = startX + dayWidth*mCurrnetIndex;
        scaleRatio = 1.0f - (Math.abs(currentIndexPositionX - currentX)/(float)(dayWidth/2));
        if (scaleRatio < 0){
            scaleRatio = 0;
        }else if (scaleRatio > 1){
            scaleRatio = 1.0f;
        }
        
        drawBackground(canvas);
        drawDateTime(canvas);
        drawDateSelector(canvas);
        drawCalorieText(canvas);
        drawDataPointPath(canvas);
        drawDataPoint(canvas);
        drawCrown(canvas);
        if (targetX == currentX){
            step = INVALID;
            setDrawingCacheEnabled(true);
        }else{
            invalidate();
        }
    }

    private void drawNoDataString(Canvas canvas) {
    	canvas.drawText(noDataText, noDataTextMargingLeft, noDataTextMargingTop, mNoDataTextPaint);
	}

	private void drawNoDataImage(Canvas canvas) {
		canvas.save();
        canvas.translate(startX-backgroundOffsetX, noDataImageMargingTop);
        mNoDataImage.draw(canvas);
        canvas.restore();
	}


	private int step = INVALID;
    private float sign;
    private void updateCurrentX() {
        if (step == INVALID){
            int delta = Math.abs(targetX - currentX);
            sign = Math.signum(targetX - currentX);
            step = Math.max(delta/20, 10);
        }
        currentX = (int)(currentX + sign*step);
        if (sign >= 1){
            if (currentX > targetX){
                currentX = targetX;
            }
        }else{
            if (currentX < targetX){
                currentX = targetX;
            }
        }
    }

    private void drawCrown(Canvas canvas) {
        if (mCrownIndex != -1){
            canvas.save();
            canvas.translate(startX+dayWidth*mCrownIndex, backgroundPaddingTop-marginBetweenCrownAndCircle);
            mCrown.draw(canvas);
            canvas.restore();
        }
    }

    private void drawCalorieText(Canvas canvas) {
        canvas.save();
        canvas.translate(currentX,startY+backgroundPaddingBottom-digitalMarginBottom);
        String text = mZeroDigitalText;
        if (mPointList.size() >1){
            //Draw dynamic digital text.
            int x = currentX-startX;
            int i = 1;
            for ( ; i < mPointList.size(); i++){
                if (mPointList.get(i).x > x){
                    break;
                }
            }

            if (i >= mPointList.size()){
            	int val = Integer.parseInt(digitalText[mPointList.size()-1]);
            	text = String.format("%,d",val);
            	
            	//Log.d("PHH", text);
            	//text = String.format("%,d", digitalText[mPointList.size()-1]);
            }else{
            	float scale =(x % dayWidth) / (float)dayWidth;
                float range = mDataValues[i]-mDataValues[i-1];
                int val = (int)(mDataValues[i-1]+range*scale);
                text = String.format("%,d",val);
                //text = ""+(int)(mDataValues[i-1]+range*scale);
            }
        }else{
            text = digitalText[0];
        }
        
        if (mCurrnetIndex > 3){
            float digitalWidth = mDigitalTextPaint.measureText(text);
            float offset = digitalMarginLeft+digitalWidth+unitMarginLeft+mUnitTextPaint.measureText(unitText);
            canvas.drawText(text, -offset, 0, mDigitalTextPaint);
            canvas.drawText(unitText, -offset+digitalWidth+unitMarginLeft, 0, mUnitTextPaint);
        }else{
            canvas.drawText(text, digitalMarginLeft, 0, mDigitalTextPaint);
            float digitalWidth = mDigitalTextPaint.measureText(text);
            canvas.drawText(unitText, digitalMarginLeft+digitalWidth+unitMarginLeft, 0, mUnitTextPaint);
        }
        
        canvas.restore();
    }

    private void drawDataPointPath(Canvas canvas) {
        canvas.save();
        canvas.translate(startX, startY);
        canvas.drawPath(mPointPath, mCurvePaint);
        canvas.restore();
    }

    private void drawDateSelector(Canvas canvas) {
        canvas.save();
        canvas.translate(startX,startY);
        canvas.clipPath(mPointPathClip);
        canvas.translate(-startX, -startY);
        
        canvas.translate(currentX, totalHeight);
        mDateSelector.draw(canvas);
        canvas.restore();
    }

    private void drawDataPoint(Canvas canvas) {
        //Draw datapoint if exists
        canvas.save();
        canvas.translate(startX, startY);
        Point point;
        float RadiusRange = circleRadiusLarger - circleRadius;
        float radius;
        for (int i = 0 ; i < mPointList.size(); i++){
            point = mPointList.get(i);
            if (mCurrnetIndex != i){
                radius = circleRadius;
            }else{
                radius = circleRadius+(RadiusRange*(scaleRatio));
            }
            canvas.drawCircle(point.x, point.y, radius, mCircleFillPaint);
            canvas.drawCircle(point.x, point.y, radius, mCircleStrokePaint);
        }
        canvas.restore();
    }

    private void drawDateTime(Canvas canvas) {
        //Draw date if exists
        if (mDates != null){
            int heightRange = marginBetweenBackgroundBottomAndLargeText - marginBetweenBackgroundBottomAndText;
            float fontSizeRange = dateFontSizeLarger - dateFontSize;
            for (int i = 0 ; i < mDates.length; i++){
                if (mDates[i] != null){
                    if (mCurrnetIndex == i && mDataValues != null){
                        mDateTextPaint.setTextSize(dateFontSize + (scaleRatio*fontSizeRange));
                        canvas.drawText(mDates[i], startX+dayWidth*i, backgroundHeight + marginBetweenBackgroundBottomAndText- mFontMetricsInt.top + (scaleRatio*heightRange), mDateTextPaint);
                    }else{
                        mDateTextPaint.setTextSize(dateFontSize);
                        canvas.drawText(mDates[i], startX+dayWidth*i, backgroundHeight + marginBetweenBackgroundBottomAndText- mFontMetricsInt.top, mDateTextPaint);
                    }
                }
            }
        }
    }

    private void drawBackground(Canvas canvas) {
        //Draw background bottom
        canvas.save();
        canvas.translate(startX-backgroundOffsetX, backgroundHeight);
        mBackgroundBottom.draw(canvas);
        canvas.restore();
    }
    private void drawBackgroundNoData(Canvas canvas) {
    	
    	mNoDateBackground = getResources().getDrawable(R.drawable.asus_wellness_bg_calories_bg2);
    	mNoDateBackground.setBounds(0, 0, mNoDateBackground.getIntrinsicWidth(), mNoDateBackground.getIntrinsicHeight());
        
    	//Draw background bottom
        canvas.save();
        canvas.translate(startX-backgroundOffsetX, 0);
        mNoDateBackground.draw(canvas);
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
     * @param dates
     */
    public void setDataValues(Float[] data){
        mDataValues = data;
        updateDigitalStrings();
        updateMinMaxData();
        calculatePoints();
        setDrawingCacheEnabled(false);
        invalidate();
    }
    
    private void updateDigitalStrings() {
        digitalText = new String[mDataValues.length];
        for (int i = 0 ; i < digitalText.length; i++){
            if (mDataValues[i] != null){
                digitalText[i] = String.format(mDigitalTextFormatter, (int)mDataValues[i].floatValue());
            }else{
                digitalText[i] = mZeroDigitalText;
            }
        }
        if (digitalText.length == 0){
            digitalText = new String[1];
            digitalText[0] = mZeroDigitalText;
        }
    }

    /**
     * Set the index of selector drawer position
     * @param index between 0 and (dataValues.length-1)
     */
    public void setDateSelectorIndex(int index){
        currentX = (int) (startX + dayWidth*Math.min(index, mDataValues.length-1));
        targetX = currentX;
        invalidate();
    }
    
    /**
     * Show animation of selector drawer to the index position
     * @param index between 0 and (dataValues.length-1)
     */
    public void animationDateSelectorToIndex(int index){
        targetX = (int) (startX + dayWidth*Math.min(index, mDataValues.length-1));
        invalidate();
    }

    private void calculatePoints() {
        mPointList = new ArrayList<Point>();
        mPointPath = new Path();
        if (mDataValues != null){
            //generate points
            int height = backgroundHeight-backgroundPaddingTop-backgroundPaddingBottom;
            float range = mMaxData-mMinData;
            for (int i = 0; i < mDataValues.length ; i++){
                if (range == 0){
                    mPointList.add(new Point(dayWidth*i,0));
                }else{
                    mPointList.add(new Point(dayWidth*i,-((mDataValues[i]-mMinData)/range)*height));
                }
            }
            
            //calculate cubic spline data
            if(mPointList.size() > 1){
                Point next,prev;
                for(int i = 0; i < mPointList.size(); i++){
                    if(i >= 0){
                        Point point = mPointList.get(i);

                        if(i == 0){
                            next = mPointList.get(i + 1);
                            point.dx = ((next.x - point.x) / 6);
                            point.dy = ((next.y - point.y) / 6);
                        }
                        else if(i == mPointList.size() - 1){
                            prev = mPointList.get(i - 1);
                            point.dx = ((point.x - prev.x) / 6);
                            point.dy = ((point.y - prev.y) / 6);
                        }
                        else{
                            next = mPointList.get(i + 1);
                            prev = mPointList.get(i - 1);
                            point.dx = ((next.x - prev.x) / 6);
                            point.dy = ((next.y - prev.y) / 6);
                        }
                    }
                }
                
                boolean first = true;
                for(int i = 0; i < mPointList.size(); i++){
                    Point point = mPointList.get(i);
                    if(first){
                        first = false;
                        mPointPath.moveTo(point.x, point.y);
                    }
                    else{
                        prev = mPointList.get(i - 1);
                        mPointPath.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);
                    }
                }
            }
        }
        
        mPointPathClip = new Path(mPointPath);
        if (mPointList.size() != 0){
            Point first = mPointList.get(0);
            Point last = mPointList.get(mPointList.size()-1);
            mPointPathClip.lineTo(last.x + dayWidth, last.y);
            mPointPathClip.lineTo(last.x + dayWidth, backgroundPaddingBottom+dateSelectorBottomHeight);
            mPointPathClip.lineTo(-dayWidth, backgroundPaddingBottom+dateSelectorBottomHeight);
            mPointPathClip.lineTo(-dayWidth, first.y);
            mPointPathClip.close();
        }
    }

    private void updateMinMaxData() {
        mMinData = Float.MAX_VALUE;
        mMaxData = Float.MIN_VALUE;
        for (int i = 0 ; i < mDataValues.length; i++){
            if (mDataValues[i] < mMinData){
                mMinData = mDataValues[i];
            }
            if (mDataValues[i] > mMaxData){
                mMaxData = mDataValues[i];
                mCrownIndex = i;
            }
        }
        Log.i("smile","updateMinMaxData: "+mMinData+" "+mMaxData); //fix bug mMaxData
        if (Math.abs(mMaxData-mMinData)<0.01){
            Log.i("smile","updateMinMaxData: less 0.01"); //fix bug mMaxData
            mCrownIndex = -1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(totalWidth, totalHeight);
    }
    

    private int currentPointId = INVALID;
    private int currentX = INVALID;
    private float firstPositionX = INVALID;
    private boolean moving = false; 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mDataValues == null){
        	return false;
        }
        int action = event.getActionMasked();
        int index = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN){
            float positionY = event.getY(index);
            if(positionY < mBackgroundBottom.getIntrinsicHeight()){
                return false;
            }

            if (currentPointId == INVALID){
                currentPointId = event.getPointerId(index);
                targetX = (int) event.getX(index);
                getParent().requestDisallowInterceptTouchEvent(true);
                firstPositionX = event.getX(index);
                moving = false;
            }
        }else if (action == MotionEvent.ACTION_MOVE){
            if (currentPointId != INVALID){
                for (int i = 0 ; i < event.getPointerCount(); i++){
                    int id = event.getPointerId(i);
                    if (currentPointId == id){
                        if (moving == true ||  Math.abs(event.getX(i) - firstPositionX) > TOUCH_MOVE_THRESHOLD){
                            targetX = (int) event.getX(i);
                            currentX = targetX;
                            moving = true;
                        }
                    }
                }
            }
        }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            if (currentPointId == event.getPointerId(index)){
                currentPointId = INVALID;
                targetX = (int) event.getX(index);
                
                targetX = (int) (startX + dayWidth*calculateDateSelectorIndex(targetX));
            }
        }else if (action == MotionEvent.ACTION_CANCEL){
            currentPointId = INVALID;
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        
        if (action == MotionEvent.ACTION_UP){
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        
        //make sure the x-axis range is acceptable
        if (targetX < startX){
            targetX = startX;
        }else if (targetX > startX + dayWidth*(Math.max(1, mDataValues.length)-1) ){
            targetX = (int) (startX + dayWidth*(Math.max(1, mDataValues.length)-1));
            
        }
        if (moving && action == MotionEvent.ACTION_MOVE){
            currentX = targetX;
        }
        invalidate();
        return true;
    }



    private int calculateDateSelectorIndex(int x) {
        int start = (int) (startX - (dayWidth/2));
        int index = (int) ((x - start) / dayWidth);
        if (index < 0){
            return 0;
        }else if ( index > 6){
            return 6;
        }
        return index;
    }



    private static class Point {
        private Point(float x, float y){
            this.x = x;
            this.y = y;
        }
        float x, y;
        float dx, dy;

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }
}

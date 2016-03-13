package com.asus.wellness.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.utils.Utility;

public class ChartWeeklyView extends View {

    private Context mContext;
    private float TOUCH_MOVE_THRESHOLD;
    private View mRootView;

    int mIndicatorHeight =2;
	private int mBarWidth=20;
	private int mBarHeight=150;
	private int mBarSpace=15;
	private int mBarLeftOffset=10;
    private int mBarBottomOffset=10;
	private int mBarWeekTextSpace=10;
    private int mDotsRadius = 2;
    private float scaleRatio = 1.0f;
    private float mMaxMins = 12*60;
    private int mSleep_level_1 = 8*60;
    private int mSleep_level_2 = 4*60;

    private int mDayWidth;
    private int mSelectorStartX = 0;
    private int mSelectorStartY = 0;

    private int mTotalHeight;
    private int mTotalWidth;
    private Drawable mDateSelector;

    private final static int INVALID = Integer.MIN_VALUE;
    private int currentPointId = INVALID;
    private float firstPositionX = INVALID;
    private boolean moving = false;
    private int currentX = 0;//Integer.MIN_VALUE;
    private int targetX = 0;//Integer.MIN_VALUE;
    private int mCurrnetIndex = 0;
    private int marginBetweenBackgroundBottomAndLargeText;
    private int marginBetweenBackgroundBottomAndText;
    private int mDateSelectorBottomHeight;
    private float mDateFontSizeLarger;
    private float mDateFontSize;
    private TextPaint mDateTextPaint;
    private TextPaint mDateLargeTextPaint;
    private int maxLargeDateWidth;
    private Paint.FontMetricsInt mLargeFontMetricsInt = new Paint.FontMetricsInt();
    private Paint.FontMetricsInt mFontMetricsInt = new Paint.FontMetricsInt();

    private String mIndicatorText_1 = "50";
    private String mIndicatorText_2 = "25";
    private int mChar_1_Color_from= 0xffffff;
    private int mChar_1_Color_to = 0xffffff;
    private int mChar_2_Color_from= 0xffffff;
    private int mChar_2_Color_to = 0xffffff;

    private String[] mDates;
    private Long[] mLightValues;
    private Long[] mDeepValues;
    private Long[] mTotalDataValues;

    public static String SLEEP_TYPE = "SLEEP_TYPE";
    public static String WORKOUTP_TYPE = "WORKOUT_TYPE";
    private boolean mHasDatas = false;
    private String mType = SLEEP_TYPE;

	public ChartWeeklyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
        setWillNotDraw(false);
        //initBarChart();
		// TODO Auto-generated constructor stub
	}

	public ChartWeeklyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
        setWillNotDraw(false);
        //initBarChart();
		// TODO Auto-generated constructor stub
	}

	public ChartWeeklyView(Context context) {
		super(context);
		mContext=context;
        setWillNotDraw(false);
        //initBarChart();
		// TODO Auto-generated constructor stub
	}

    public void initBarChart(View rootView,String type, int width, int barHeight, int leftMargin){
        mRootView = rootView;
        mType = type;
        TOUCH_MOVE_THRESHOLD = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

        if(type.equals(SLEEP_TYPE)){
            mChar_1_Color_from= getResources().getColor(R.color.sleep_deep_char_color_from);
            mChar_1_Color_to = getResources().getColor(R.color.sleep_deep_char_color_to);
            mChar_2_Color_from= getResources().getColor(R.color.sleep_light_char_color_from);
            mChar_2_Color_to = getResources().getColor(R.color.sleep_light_char_color_to);
        }
        else if(type.equals(WORKOUTP_TYPE)){
            mChar_1_Color_from= getResources().getColor(R.color.workout_pushup_char_color_from);
            mChar_1_Color_to = getResources().getColor(R.color.workout_pushup_char_color_to);
            mChar_2_Color_from= getResources().getColor(R.color.workout_situp_char_color_from);
            mChar_2_Color_to = getResources().getColor(R.color.workout_situp_char_color_to);
        }
        mTotalWidth = width;
        mBarHeight = barHeight;

        if(leftMargin > 0){
            mBarWidth = (int)Math.floor((mTotalWidth-leftMargin)/(7+2+0.5));
            mBarSpace = mBarWidth/3;
            mBarLeftOffset = leftMargin;
            Log.i("emily",mType+" reset leftMargin");
        }
        else{
            // 7条bar, 6条间隙.  间隙宽度 = bar/3, leftMargin = 1bar  rightMargin = 1bar/2
            mBarWidth =(int)Math.floor(mTotalWidth/(7+2+1.5));//(int) mContext.getResources().getDimension(R.dimen.sleep_barchart_width);
            mBarSpace = mBarWidth/3;
            mBarLeftOffset = mBarWidth;
            Log.i("emily",mType+" init leftMargin");
        }

        mBarWeekTextSpace = (int) mContext.getResources().getDimension(R.dimen.sleep_barchart_week_text_space);
        mBarBottomOffset =  (int)mContext.getResources().getDimension(R.dimen.sleep_barchart_bottom_offset);

        mDotsRadius = getResources().getDimensionPixelSize(R.dimen.sleep_barchart_bottom_circle_radius);
        mDateSelector = getResources().getDrawable(R.drawable.sleep_selector);
        mDateSelector.setBounds(-(mDateSelector.getIntrinsicWidth()/2), -mDateSelector.getIntrinsicHeight(),
                mDateSelector.getIntrinsicWidth()-(mDateSelector.getIntrinsicWidth()/2), 0);
        mDateSelectorBottomHeight = getResources().getDimensionPixelSize(R.dimen.chart_calorie_date_selector_bottom_height);
        mSelectorStartX = mBarLeftOffset + mBarWidth/2;
        mSelectorStartY = mBarHeight + mBarBottomOffset;
        mDayWidth = mBarWidth + mBarSpace;
        int dateSelectorBottomHeight = getResources().getDimensionPixelSize(R.dimen.sleep_barchart_bottom_offset);

        mTotalHeight = mBarHeight +  mDateSelector.getIntrinsicHeight() + mDateSelectorBottomHeight + mIndicatorHeight;

        if(mDates!=null && mDates.length>0){
            currentX =  mSelectorStartX + mDayWidth*Math.min(mCurrnetIndex, mDates.length-1);
        }
        else{
            currentX = mSelectorStartX;
        }
        targetX = currentX;

        mDateFontSize = getResources().getDimension(R.dimen.chart_date_text_font_size_large);
        marginBetweenBackgroundBottomAndText = getResources().getDimensionPixelSize(R.dimen.sleep_chart_margin_between_bottom_text);
        marginBetweenBackgroundBottomAndLargeText = getResources().getDimensionPixelSize(R.dimen.sleep_chart_margin_between_bottom_text_large);
        mDateTextPaint = new TextPaint();
        mDateTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mDateFontSize = getResources().getDimension(R.dimen.chart_date_text_font_size);
        mDateFontSizeLarger = getResources().getDimension(R.dimen.chart_date_text_font_size_large);
        mDateTextPaint.setTextSize(mDateFontSize);
        mDateTextPaint.setColor(getResources().getColor(R.color.chart_date_text_color));
        mDateTextPaint.setAntiAlias(true);
        mDateTextPaint.getFontMetricsInt(mFontMetricsInt);
        mDateTextPaint.setTextAlign(Paint.Align.CENTER);

        mDateLargeTextPaint = new TextPaint();
        mDateLargeTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mDateLargeTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_date_text_font_size_large));
        mDateLargeTextPaint.setColor(getResources().getColor(R.color.chart_date_text_color));
        mDateLargeTextPaint.setAntiAlias(true);
        maxLargeDateWidth =Math.round(mDateLargeTextPaint.measureText("00/00"));
        mDateLargeTextPaint.getFontMetricsInt(mLargeFontMetricsInt);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mTotalWidth, mTotalHeight);
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
       // Log.i("emily","onDraw()");
        //if(no date) else...
       /* if (targetX != currentX){
            //Calculate animation accelerate speed
            updateCurrentX();
        }*/
        currentX = targetX;
        Log.i("emily","draw = "+mType+", currentX = " + currentX +", index = " + mCurrnetIndex);

        //Calculate animation scale ratio
        mCurrnetIndex = calculateDateSelectorIndex(currentX);
        float currentIndexPositionX = mSelectorStartX + mDayWidth*mCurrnetIndex;
        scaleRatio = 1.0f - (Math.abs(currentIndexPositionX - currentX)/(float)((mDayWidth)/2));
        if (scaleRatio < 0){
            scaleRatio = 0;
        }else if (scaleRatio > 1){
            scaleRatio = 1.0f;
        }

        if(mTotalDataValues == null || mDates == null){
            //Log.i("emily","mDate null, type = " + mType);
            return;
        }

        //Log.i("emily","has data  = " + mHasDatas +  ", type = " + mType);
        if(mHasDatas){
            drawBottomLine(canvas);
            drawDateSelector(canvas);
            drawBottomDots(canvas);
            drawDateTime(canvas);
            drawBar(canvas);
            drawIndicator(canvas);

        }
        else{
            drawBottomLine(canvas);
            drawBottomDots(canvas);
            drawDateTime(canvas);
            drawNoDataString(canvas);
        }
        setDetailTitle();
	}

    private void drawIndicator(Canvas canvas){
        //Log.i("emily","drawIndicator()");
        float line_1__height=mBarHeight*mSleep_level_1/mMaxMins;  // line_1: 8h,   line_2: 4h
        float line_2__height=mBarHeight*mSleep_level_2/mMaxMins;

        int margin = (int)getResources().getDimension(R.dimen.char_bar_indicator_textview_margin_right);
        int startX=mBarLeftOffset-margin;

        int textViewWidth = Utility.calcTextViewWidth(mContext,mIndicatorText_1,mIndicatorText_2,
                getResources().getDimensionPixelSize(R.dimen.sleep_barchar_indicator_text_size))+margin;
        if(textViewWidth > startX){
            initBarChart(mRootView, mType,mTotalWidth, mBarHeight,textViewWidth+margin);
            invalidate();
            return;
        }
        int stopX=mTotalWidth;//startX+mBarWidth*mDates.length+mBarSpace*(mDates.length-1)+mBarLeftOffset;
        int line_1_Y=(int) (mBarHeight-line_1__height);
        int line_2_Y=(int) (mBarHeight-line_2__height);

        Paint dotPaint=new Paint();
        dotPaint.setColor(mContext.getResources().getColor(R.color.sleep_line_color));
        dotPaint.setStyle(Style.STROKE);
        dotPaint.setStrokeWidth(4);
        /*dotPaint.setPathEffect(new DashPathEffect(new float[] {4,10}, 0));//{2,10}, 0));*/

        Paint textPaint=new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.sleep_barchar_indicator_text_size));
        textPaint.setAntiAlias(true);

        drawDotLine(canvas, startX, stopX, line_1_Y, textPaint);
        canvas.drawText(mIndicatorText_1, 0, line_1_Y+10, textPaint);

        drawDotLine(canvas, startX, stopX, line_2_Y, textPaint);
        canvas.drawText(mIndicatorText_2, 0, line_2_Y+10, textPaint);
    }

    private void drawDotLine(Canvas canvas, int startX, int endX, int y, Paint paint){
        int margin=getContext().getResources().getDimensionPixelSize(R.dimen.timeline_preview_dot_margin);
        int radius=getContext().getResources().getDimensionPixelSize(R.dimen.time_line_preview_circle_radius);

        while(startX <= endX){
            canvas.drawCircle(startX, y, radius, paint);
            startX += margin;
        }
    }

    private void drawBottomLine(Canvas canvas){
        Paint paint=new Paint();
        paint.setColor(mContext.getResources().getColor(R.color.sleep_line_color));
        int stopX = mTotalWidth;//mBarLeftOffset*2 + mBarWidth*7 + mBarSpace*6;
        int y=mBarBottomOffset + mBarHeight;
        canvas.drawLine(0, y, stopX, y, paint);
    }

    private void drawBottomDots(Canvas canvas){
        int dotInterval = mBarWidth + mBarSpace;
        int posX=mBarLeftOffset+mBarWidth/2;
        int posY=mBarHeight+mBarBottomOffset;

        Paint dotPaint=new Paint();
        dotPaint.setColor(Color.BLACK);
        dotPaint.setAntiAlias(true);

        for(int i=0;i<7;i++){
            canvas.drawCircle(posX, posY, mDotsRadius, dotPaint);
            posX+=dotInterval;
        }
    }

    private void drawDateTime(Canvas canvas) {
        if (mDates != null){
            int heightRange =  marginBetweenBackgroundBottomAndLargeText - marginBetweenBackgroundBottomAndText;//50;
            float fontSizeRange = mDateFontSizeLarger - mDateFontSize;
            int y = mBarHeight + marginBetweenBackgroundBottomAndText- mFontMetricsInt.top + mBarBottomOffset;
            for (int i = 0 ; i < mDates.length; i++){
                if (mDates[i] != null){
                    int x = mSelectorStartX+mDayWidth*i;
                    if (mCurrnetIndex == i && mTotalDataValues != null && mHasDatas){
                        mDateTextPaint.setTextSize(mDateFontSize + (scaleRatio*fontSizeRange));
                        canvas.drawText(mDates[i], mSelectorStartX+mDayWidth*i, y + (scaleRatio*heightRange), mDateTextPaint);
                    }else{
                        mDateTextPaint.setTextSize(mDateFontSize);
                        canvas.drawText(mDates[i], mSelectorStartX+mDayWidth*i, y, mDateTextPaint);
                    }
                }
            }
        }
    }

    private void drawBar(Canvas canvas){
        if(mTotalDataValues == null || mDates == null){
            Log.i("emily","mSleepData null");
            return;
        }

        int real_barHeight = mBarHeight + mIndicatorHeight;
        int left=mBarLeftOffset, top =0, right=left+mBarWidth, bottom=top+real_barHeight;

        for(int i=0;i<mDates.length;i++){
            float deepHours = mDeepValues[i];
            float lightHours = mLightValues[i];
            float wakeHours = mTotalDataValues[i] - mDeepValues[i] - mLightValues[i];

            int indicatorHeight = mIndicatorHeight;
            int lightHeight = (int)(mBarHeight * lightHours / mMaxMins);
            int deepHeight = (int)(mBarHeight * deepHours / mMaxMins);
            int wakeHeight = (int)(mBarHeight * wakeHours / mMaxMins);

            if(mType == SLEEP_TYPE || lightHours==0 || deepHours==0){
                indicatorHeight = 0;
            }

            Rect wakeRect = new Rect(left, top+(real_barHeight-wakeHeight), right, bottom);
            Rect lightRect=new Rect(left, top+(real_barHeight-lightHeight-wakeHeight), right, bottom-wakeHeight);
            Rect deepRect=new Rect(left, top+(real_barHeight-wakeHeight-lightHeight-deepHeight-indicatorHeight), right, bottom-wakeHeight-lightHeight-indicatorHeight);

            LinearGradient wake_gradientColor = new LinearGradient(left,top+(real_barHeight-lightHeight-wakeHeight),left,bottom,getResources().getColor(R.color.sleep_wake_color),
                    getResources().getColor(R.color.sleep_wake_color), Shader.TileMode.MIRROR);
            Paint wakePaint=new Paint();
            wakePaint.setShader(wake_gradientColor);

            LinearGradient light_gradientColor = new LinearGradient(left,top+(real_barHeight-lightHeight-wakeHeight),left,bottom-wakeHeight,
                    mChar_2_Color_from,mChar_2_Color_to, Shader.TileMode.MIRROR);
            Paint lightPaint=new Paint();
            lightPaint.setShader(light_gradientColor);

            LinearGradient deep_gradientColor = new LinearGradient(left,top+(real_barHeight-wakeHeight-lightHeight-deepHeight-indicatorHeight),left,bottom-wakeHeight-lightHeight-indicatorHeight,
                    mChar_1_Color_from,mChar_1_Color_to, Shader.TileMode.MIRROR);
            Paint deepPaint=new Paint();
            deepPaint.setShader(deep_gradientColor);

            canvas.drawRect(wakeRect, wakePaint);
            canvas.drawRect(lightRect, lightPaint);
            canvas.drawRect(deepRect, deepPaint);
            Paint indictorPaint = new Paint();
            indictorPaint.setColor(Color.WHITE);
           // canvas.drawRect(left,top+(real_barHeight-lightHeight-indicatorHeight),right,top+(real_barHeight-lightHeight), indictorPaint);

            left+=mBarWidth+mBarSpace;
            right=left+mBarWidth;
        }
    }


    private void drawDateSelector(Canvas canvas) {
        canvas.save();
        //canvas.clipRect(0, mBarHeight/2,mTotalWidth/2,mBarHeight/2);
        canvas.translate(currentX, mTotalHeight-mDateSelectorBottomHeight);
        mDateSelector.draw(canvas);
        canvas.restore();
    }

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
        Paint.FontMetrics fontMetrics = mNoDataTextPaint.getFontMetrics();
        int h = (int)( fontMetrics.descent - fontMetrics.ascent) / 2;
        int left = (int)(mTotalWidth - mNoDataTextPaint.measureText(noDataText))/2;
        int top = (mBarHeight + h)/2;
        canvas.drawText(noDataText, left , top, mNoDataTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (!mHasDatas || mDates == null || mTotalDataValues == null ) {
            return false;
        }

        int action = event.getActionMasked();
        int index = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN){
            float positionY = event.getY(index);
            if(positionY < mBarHeight || positionY > (mBarHeight+mDateSelector.getIntrinsicHeight())){
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

                targetX = (int) (mSelectorStartX + mDayWidth*calculateDateSelectorIndex(targetX));
            }
        }else if (action == MotionEvent.ACTION_CANCEL){
            currentPointId = INVALID;
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        if (action == MotionEvent.ACTION_UP){
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        //make sure the x-axis range is acceptable
        if (targetX < mSelectorStartX){
            targetX = mSelectorStartX;
        }else if (targetX > mSelectorStartX + mDayWidth*(Math.max(1, mDates.length)-1) ){
            targetX = (int) (mSelectorStartX + mDayWidth*(Math.max(1, mDates.length)-1));

        }
        if (moving && action == MotionEvent.ACTION_MOVE){
            currentX = targetX;
        }
        invalidate();
        return true;
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

    /**
     * Set the index of selector drawer position
     * @param index between 0 and (dataValues.length-1)
     */
    public void setDateSelectorIndex(int index){
        Log.i("emily",mType+", index = " + index);
        mCurrnetIndex = index;
        currentX = (int) (mSelectorStartX + mDayWidth*Math.min(index, mDates.length-1));
        targetX = currentX;
        invalidate();
    }

    /**
     * Show animation of selector drawer to the index position
     * @param index between 0 and (dataValues.length-1)
     */
    public void animationDateSelectorToIndex(int index){
        targetX = (int) (mSelectorStartX + mDayWidth*(Math.min(index, mDates.length-1)));
        invalidate();
    }

    private int calculateDateSelectorIndex(int x) {
        int start = (int) (mSelectorStartX - (mDayWidth/2));
        int index = (int) ((x - start) / mDayWidth);
        if (index < 0){
            return 0;
        }else if ( index > 6){
            return 6;
        }
        mCurrnetIndex = index;
        return index;
    }

    /**
     * Should set 7 strings for a week
     * @param dates
     */
    public void setDateString(String[] dates){
        mDates = dates;
        /*setDrawingCacheEnabled(false);
        invalidate();*/
    }

    public void setSleepDatas(Long[] light, Long[] deep, Long[] total){
        mLightValues = light;
        mDeepValues = deep;
        mTotalDataValues = total;
        /*updateDigitalStrings();
        updateMinMaxData();
        calculatePoints();
        setDrawingCacheEnabled(false);
        invalidate();*/

        mMaxMins = 12*60;
        int sleepDays = 0;
        if(mDates!=null && mTotalDataValues!=null){
            for(int i=0; i<mDates.length; i++){
                if (mTotalDataValues[i] > mMaxMins){
                    mMaxMins = mTotalDataValues[i];
                }
                if (mTotalDataValues[i] != 0){
                    sleepDays++;
                }
            }
        }
        mHasDatas = sleepDays==0 ? false : true;
        mSleep_level_1 = (int) mMaxMins * 2 / 3;
        mSleep_level_2 = (int) mMaxMins / 3;
        mIndicatorText_1 = String.valueOf(mSleep_level_1/60)+getResources().getString(R.string.detail_sleep_shorthour);
        mIndicatorText_2 = String.valueOf(mSleep_level_2/60)+getResources().getString(R.string.detail_sleep_shorthour);
    }

    public  void setWorkoutDatas(Long[] pushups, Long[] situps, Long[] total) {
        mLightValues = situps;
        mDeepValues = pushups;
        mTotalDataValues = total;
        /*updateDigitalStrings();
        updateMinMaxData();
        calculatePoints();
        setDrawingCacheEnabled(false);
        invalidate();*/

        mMaxMins = 75;
        int workoutDays = 0;
        if (mDates != null && mTotalDataValues != null) {
            for (int i = 0; i < mDates.length; i++) {
                if (mTotalDataValues[i] > mMaxMins) {
                    mMaxMins = mTotalDataValues[i];
                }
                if (mTotalDataValues[i] != 0) {
                    workoutDays++;
                }
            }
        }
        mHasDatas = workoutDays == 0 ? false : true;
        mSleep_level_1 = (int) mMaxMins * 2 / 3;
        mSleep_level_2 = (int) mMaxMins / 3;
        mIndicatorText_1 = String.valueOf(mSleep_level_1);
        mIndicatorText_2 = String.valueOf(mSleep_level_2);
    }

    private String NO_DATA_HEAD = "0 ";
    private void setDetailTitle(){
        if(mType.equals(WORKOUTP_TYPE)){
            TextView pushupTv = (TextView)mRootView.findViewById(R.id.weekly_pushup_count);
            TextView situpTv = (TextView)mRootView.findViewById(R.id.weekly_situp_counts);

            //Log.i("emily","workout  mCurrnetIndex  = " + mCurrnetIndex);
            if(mHasDatas) {
                pushupTv.setText(Utility.formatNumber(mDeepValues[mCurrnetIndex]) + " " + mContext.getString(R.string.count_time));
                situpTv.setText(Utility.formatNumber(mLightValues[mCurrnetIndex]) + " " + mContext.getString(R.string.count_time));
            }
            else{
                pushupTv.setText(NO_DATA_HEAD + mContext.getString(R.string.count_time));
                situpTv.setText(NO_DATA_HEAD + mContext.getString(R.string.count_time));
            }
        }
        else if(mType.equals(SLEEP_TYPE)){
            TextView deepHoursTv = (TextView)mRootView.findViewById(R.id.weekly_sleep_deep_hours);
            TextView lightHoursTv = (TextView)mRootView.findViewById(R.id.weekly_sleep_light_hours);
            TextView wakeHoursTv = (TextView)mRootView.findViewById(R.id.weekly_sleep_wake_hours);
          //  Log.i("emily","sleep  mCurrnetIndex  = " + mCurrnetIndex);

            if(mHasDatas){
                deepHoursTv.setText(Utility.formatSleepTimes(mDeepValues[mCurrnetIndex], mContext));
                lightHoursTv.setText(Utility.formatSleepTimes(mLightValues[mCurrnetIndex], mContext));
                wakeHoursTv.setText(Utility.formatSleepTimes(mTotalDataValues[mCurrnetIndex] - mDeepValues[mCurrnetIndex]- mLightValues[mCurrnetIndex], mContext));
            }else{
                deepHoursTv.setText(NO_DATA_HEAD + mContext.getString(R.string.detail_sleep_shortmin));
                lightHoursTv.setText(NO_DATA_HEAD + mContext.getString(R.string.detail_sleep_shortmin));
                wakeHoursTv.setText(NO_DATA_HEAD + mContext.getString(R.string.detail_sleep_shortmin));
            }

        }
    }

 }

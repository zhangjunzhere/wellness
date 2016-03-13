package originator.ailin.com.smartgraph.chart.barchart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import originator.ailin.com.smartgraph.R;
import originator.ailin.com.smartgraph.chart.BaseChart;

public class SleepBarChart extends BaseChart {

    private Paint mWhitePaint;
    private Paint mBlackPaint;
    private Paint mHourPaint;
    private Paint mHourTextPaint;
    private Paint mWakeupPaint;
    private Paint mLightSleepPaint;
    private Paint mDeepSleepPaint;
    private int mWidth;
    private int mHeight;
    //from start to first hour num postion
    private int mSleepWakeStartX =0;
    private int mSleepWakeEndX =0;
    // from first hour num position to real draw num postion
    private int mSleepFirstTextMarginLeft=0;
    private int mSleepDrawMarginLeft=0;
    private int mSleepDrawMarginRight=0;
    private float mTotalHours=0;
    private int mEveryHourSpanLength=0;
    private long mStartTime=0;
    private long mEndTime=0;
    private int mStartHour=0;

    private int[] mSleepData;
    private float mEveryItemLength;
    private int mAllDrawLength;
    private int sleepdeepcolorfrom;
    private int sleepdeepcolorto;
    private int sleeplightcolorfrom;
    private int sleeplightcolorto;
    private int sleepwakecolor;

    private int mDrawStartMarginX =0;
    private int mDrawEndMarginX =10;
    private float mWakeMarginRate=0f;
    private long mFirstSleepOnTime =0;
    private long mLastSleepOnTime=0;
    private float mSleepStartPos=-1;
    private float mSleepEndPos=0;
    private int mMarginBottom = 10;

    private int devideCount=3;
    private boolean mUseForMobile=false;

    private Drawable mSleepStartDrawable;
    private Drawable mAlarmDrawable;

    private boolean mAmbientMode = false;

    private int mHourTextSize=20;
    private int mTimeTextSize=30;
    /**
     * Constructor 1
     * @param context
     */
    public SleepBarChart(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor 2
     * @param context
     * @param attrs
     */
    public SleepBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initPrams();
    }
    public void setUseformobile()
    {
        devideCount = 4;
        mUseForMobile= true;
    }
    public void setAmbientMode(boolean isAmbientMode)
    {
        mAmbientMode = isAmbientMode;
        updatePannel();
    }
    void initPrams()
    {
        mWidth = getMeasuredWidth();
        mWidth =  mWidth-5>0 ? mWidth-5 : mWidth;
        mHeight = getMeasuredHeight();
      //  mHeight =  mHeight-5>0 ? mHeight-5 : mHeight;

        mSleepWakeEndX = Math.round(mWidth * (1 - mWakeMarginRate));
        mSleepWakeStartX = Math.round(mWidth*mWakeMarginRate);
        Log.i("smile","mWidth: "+mWidth);
        if(mSleepData!=null && mSleepData.length>0)
        {
            mAllDrawLength =  mSleepWakeEndX - mSleepWakeStartX- mDrawStartMarginX-mDrawEndMarginX;//-mSleepDrawMarginRight-mSleepDrawMarginLeft;
            calcHour(mAllDrawLength);
            mAllDrawLength = mAllDrawLength-mSleepDrawMarginRight-mSleepDrawMarginLeft;
            mEveryItemLength =  mAllDrawLength/1.0f/mSleepData.length;
        }
        Log.i("smile","mAllDrawLength: "+mAllDrawLength+" mSleepDrawMarginRight: "+mSleepDrawMarginRight+" mSleepDrawMarginLeft:  "+mSleepDrawMarginLeft);
        Log.i("smile", "mEveryItemLength: " + mEveryItemLength + " mEveryHourSpanLength: " + mEveryHourSpanLength + " mTotalHours:  " + mTotalHours);
       // setUseformobile();

    }

    public SleepTimeSpan getTimeSpan(long timespan)
    {
        int l =  (int)timespan;
        int day=l/(24*60);
        int hour=(l/60-day*24);
        int min=(l-day*24*60-hour*60);
        //  long s=(l/1000-day*24*60*60-hour*60*60-min*60);
        SleepTimeSpan ts = new SleepTimeSpan(hour,min);
        return  ts;
    }
    private void calcHour(int withoutMarginLength)
    {
        Calendar calendar =Calendar.getInstance();
        calendar.setTimeInMillis(mStartTime);
        int startDay =calendar.get(Calendar.DAY_OF_MONTH);
        int startHours =calendar.get(Calendar.HOUR_OF_DAY);
        int startMinuts=  calendar.get(Calendar.MINUTE);
        Log.i("smile", "start day: "+startDay + " hour: " + startHours + " min: " + startMinuts);
        calendar.setTimeInMillis(mEndTime);
        int endDay =calendar.get(Calendar.DAY_OF_MONTH);
        int endHours =calendar.get(Calendar.HOUR_OF_DAY);
        int endMinuts=  calendar.get(Calendar.MINUTE);
        Log.i("smile", "end day: "+endDay + " hour: " + endHours + " min: " + endMinuts);
        mStartHour = startHours+1>=24 ? 0: startHours+1;
        int ONE_MINUTE_MILLIES = 1*60*1000;
        int l =  (int)(mEndTime/ONE_MINUTE_MILLIES) - (int)(mStartTime/ONE_MINUTE_MILLIES);
        SleepTimeSpan sts = getTimeSpan(l);
        mTotalHours = sts.getTotalHour();

        if(mTotalHours<1)
        {
            Log.i("smile","mTotalHours<0 "+mStartTime+" end time: "+mEndTime);
            mTotalHours = 1;
        }
        mEveryHourSpanLength = (int)(withoutMarginLength/mTotalHours);
        if(startMinuts==0)
        {
            mSleepFirstTextMarginLeft = 0;
        }
        else
        {
            mSleepFirstTextMarginLeft = (int)Math.round((1-startMinuts/60.0)*mEveryHourSpanLength);
        }

//        if(endMinuts==0)
//        {
//            mSleepDrawMarginRight = 0;
//        }
//        else
//        {
//            mSleepDrawMarginRight = (int)Math.round((1-endMinuts/60.0)*mEveryHourSpanLength);
//        }

    }
    public int getDrawStartX()
    {
        return  mDrawStartMarginX +mSleepWakeStartX+mSleepDrawMarginLeft;
    }
    public int getDrawEndX()
    {
        return  mSleepWakeEndX-mSleepDrawMarginRight;
    }

    public int getWakeupHeightTop()
    {
        return  (mHeight-mMarginBottom)/devideCount*2 ;
    }
    public int getLightSleepHeightTop()
    {
        return  (mHeight-mMarginBottom)/devideCount;
    }
    public int  getWakeupWidth()
    {
        return mWidth;
    }
    private void fitTextSize()
    {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;

        Log.i("smile","mHourTextSize: "+mHourTextSize+" mTimeTextSize: "+mTimeTextSize+" width: "+width);
        Log.i("smile","dp: "+px2dip(getContext(),width));
        float rate = width/720;
//        mHourTextSize = (int)(mHourTextSize * rate);
//        mTimeTextSize = (int)(mTimeTextSize * rate);
    }
    public  int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    void init()
    {
        mHourTextSize =  getContext().getResources().getDimensionPixelOffset(R.dimen.sleep_hour_text_size);
        mTimeTextSize =  getContext().getResources().getDimensionPixelOffset(R.dimen.sleep_time_size);
      //  fitTextSize();

        sleepdeepcolorfrom = getResources().getColor(R.color.sleep_deep_char_color_from);
        sleepdeepcolorto =  getResources().getColor(R.color.sleep_deep_char_color_to);
        sleeplightcolorfrom =  getResources().getColor(R.color.sleep_light_char_color_from);
        sleeplightcolorto =  getResources().getColor(R.color.sleep_light_char_color_to);
        sleepwakecolor =  getResources().getColor(R.color.sleep_wake_color);

        mHourPaint = new Paint();
        mHourPaint.setColor(sleepwakecolor);
        mHourPaint.setStyle(Paint.Style.STROKE);
        mHourPaint.setAntiAlias(true);

        mHourTextPaint = new Paint();
        mHourTextPaint.setColor(Color.BLACK);
        mHourTextPaint.setStyle(Paint.Style.FILL);
        mHourTextPaint.setAntiAlias(true);


        mWakeupPaint = new Paint();
        mWakeupPaint.setColor(sleepwakecolor);
        mWakeupPaint.setStyle(Paint.Style.FILL);
        mWakeupPaint.setAntiAlias(true);

        mLightSleepPaint = new Paint();
        mLightSleepPaint.setColor(Color.GREEN);;
        mLightSleepPaint.setStyle(Paint.Style.FILL);
        mLightSleepPaint.setAntiAlias(true);

        mDeepSleepPaint = new Paint();
        mDeepSleepPaint.setColor(Color.RED);;
        mDeepSleepPaint.setStyle(Paint.Style.FILL);
        mDeepSleepPaint.setAntiAlias(true);

        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setStyle(Paint.Style.STROKE);
        mWhitePaint.setAntiAlias(true);

        mBlackPaint = new Paint();
        mBlackPaint.setColor(Color.BLACK);
        mBlackPaint.setStyle(Paint.Style.FILL);
        mBlackPaint.setAntiAlias(true);


        mSleepStartDrawable = getResources().getDrawable(R.drawable.asus_wellness_ic_eyeoff);
        mAlarmDrawable = getResources().getDrawable(R.drawable.asus_wellness_ic_eyeon);

    }
    public void setSleepData(List<Integer> list,long startTime,long endTime)
    {

        int[] d = new int[list.size()];
        for(int i=0;i<list.size();i++)
        {
            d[i] = list.get(i);
        }
        setSleepData(d,startTime,endTime,-1,-1);

    }
    public void setSleepData(List<Integer> list,long startTime,long endTime,long firstsleeptime, long lastsleeptime)
    {

        int[] d = new int[list.size()];
        for(int i=0;i<list.size();i++)
        {
            d[i] = list.get(i);
        }
        setSleepData(d,startTime,endTime,firstsleeptime,lastsleeptime);

    }
    public void setSleepData(int[] data,long startTime,long endTime,long firstsleeptime, long lastsleeptime)
    {
        mFirstSleepOnTime =  firstsleeptime;
        mLastSleepOnTime = lastsleeptime;
        mSleepData = data;
        mStartTime =  startTime;
        mEndTime = endTime;
    }
    private  void drawLightSleep(Canvas canvas)
   {
       if(mAmbientMode) {

           drawSleepData(canvas, mBlackPaint, 1);
           drawSleepData(canvas, mWhitePaint, 1);
       }
       else {
           Shader mShader = new LinearGradient(mWidth / 2, getLightSleepHeightTop(), mWidth / 2, getRectHeightButtom(), new int[]{sleeplightcolorfrom, sleeplightcolorto}, null, Shader.TileMode.REPEAT);
           mLightSleepPaint.setShader(mShader);
           drawSleepData(canvas, mLightSleepPaint, 1);
           drawLightSleepLine(canvas);
       }
   }
    private  void drawDeepSleep(Canvas canvas)
    {
        if(mAmbientMode)
        {
            drawSleepData(canvas, mBlackPaint, 2);
            drawSleepData(canvas, mWhitePaint, 2);
        }else
        {
            Shader mShader = new LinearGradient(mWidth/2,0,mWidth/2 ,getRectHeightButtom() , new int[]{sleepdeepcolorfrom,sleepdeepcolorto},null,Shader.TileMode.REPEAT);
            mDeepSleepPaint.setShader(mShader);
            drawSleepData(canvas, mDeepSleepPaint, 2);
        }

    }

    private int  getRectHeightButtom()
    {
        return  mUseForMobile ? (mHeight-mMarginBottom)/devideCount*(devideCount-1) : (mHeight-mMarginBottom);
    }
    private  void drawSleepData(Canvas canvas, Paint temppaint, int cmpvalue)
    {
        if(mSleepData!=null)
        {
            float start = -1;
            float end = 0;
            boolean isFirst = true;
            for (int i=0;i<mSleepData.length; i++)
            {
                if(mSleepData[i]>0)
                {
                    mSleepEndPos = getDrawStartX() + i* mEveryItemLength;
                    if(isFirst)
                    {
                        mSleepStartPos = mSleepEndPos;
                        isFirst = false;
                    }
                    mSleepEndPos+=mEveryItemLength;
                }
                if(mSleepData[i]== cmpvalue)
                {
                    if(start ==-1)
                    {
                        start = getDrawStartX() + i* mEveryItemLength;
                    }
                }
                else
                {
                    if(start>=0)
                    {
                        end =  getDrawStartX() + i* mEveryItemLength;
                        Log.i("smile", "drawSleep " + start + " end:  " + end);
                        int height =  cmpvalue ==1? getLightSleepHeightTop() : 0;
                        canvas.drawRect(start,height,end,getRectHeightButtom(),temppaint);
                        //                       return;
                        start =-1;
                        end = 0;
                    }

                }
                if(i ==  mSleepData.length-1)
                {
                    if(start>=0)
                    {
                        end =  getDrawStartX() + (i+1)* mEveryItemLength;
                        Log.i("smile", "drawSleep " + start + " end:  " + end);
                        int height =  cmpvalue ==1? getLightSleepHeightTop() : 0;
                        canvas.drawRect(start,height,end,getRectHeightButtom(),temppaint);
                        start =-1;
                        end = 0;
                    }
                }
            }
        }
    }
    private void drawWake(Canvas canvas)
    {

        int wakeupHeight = getWakeupHeightTop();
        //   mWakeupPaint.setShader(mShader);
        if(mAmbientMode)
        {
            canvas.drawRect(mDrawStartMarginX, wakeupHeight, mWidth, getRectHeightButtom(), mWhitePaint);
        } else {
            canvas.drawRect(mDrawStartMarginX, wakeupHeight, mWidth, getRectHeightButtom(), mWakeupPaint);
        }

        Log.i("smile", "wakeupheight: " + wakeupHeight + " width: " + mWidth + " height: " + mHeight);
      //  canvas.drawRect(mSleepStartX, getLightSleepHeight(), mSleepEndX, mHeight, mLightSleepPaint);
    }
    private void drawLightSleepLine(Canvas canvas)
    {
        if(mAmbientMode)
        {
           return;
        }
        mLightSleepPaint.setStrokeWidth(2);
        canvas.drawLine(mDrawStartMarginX, getLightSleepHeightTop(), mWidth, getLightSleepHeightTop(), mLightSleepPaint);

    }

    private void drawLines(Canvas canvas)
    {
        if(!mAmbientMode)
        {
            mWakeupPaint.setStrokeWidth(2);
            canvas.drawLine(mDrawStartMarginX, 0, mDrawStartMarginX, mHeight, mWakeupPaint);
            canvas.drawLine(mWidth, 0, mWidth, mHeight, mWakeupPaint);

            mDeepSleepPaint.setStrokeWidth(4);
            canvas.drawLine(mDrawStartMarginX,0,mWidth,0,mDeepSleepPaint);
        }
        else
        {
            mWhitePaint.setStrokeWidth(2);
            canvas.drawLine(mDrawStartMarginX, getRectHeightButtom(), mWidth, getRectHeightButtom(), mWhitePaint);
        }
    }

    private void drawHourText(Canvas canvas)
    {
        if(!mUseForMobile)
        {
            return;
        }
        long timeSecond = (mEndTime-mStartTime)/1000;
        Log.i("smile","barchart time: "+timeSecond);
//        if(timeSecond/1000<3600)
//        {
//            return;
//        }
        mHourTextPaint.setColor(Color.BLACK);
        mHourPaint.setStrokeWidth(2);
        mHourTextPaint.setStrokeWidth(2);
        mHourTextPaint.setTextSize(mHourTextSize);
        int hourLineEndY = getRectHeightButtom()+15;
        String text="1";
        float singleWidth =  mHourTextPaint.measureText(text);

        Rect bounds = new Rect();
        mHourTextPaint.getTextBounds( text, 0, text.length(), bounds );

        int hourLineStartY = getRectHeightButtom();
        int startHour = mStartHour;
        for (int i=0;i<mTotalHours+1;i++)
        {
            int  startX = mSleepFirstTextMarginLeft+mDrawStartMarginX +mSleepWakeStartX+ i* mEveryHourSpanLength;
            canvas.drawLine(startX ,hourLineStartY ,startX,hourLineEndY ,mHourPaint);
            float endx =startX;
            float halfHourWidth = singleWidth;
            if(startHour>9)
            {
                halfHourWidth = singleWidth;
            }
            else
            {
                halfHourWidth = singleWidth/2;
            }
            startX -= halfHourWidth;
            endx = startX+ singleWidth * 2;
            if(endx > getDrawEndX())
            {
                startX = (int)(startX - singleWidth * 2 -5);
            }
            canvas.drawText(String.valueOf(startHour),startX,hourLineEndY+20,mHourTextPaint);
            Log.i("smile", "drawHourText " + startX );
            startHour++;
            if(startHour>=24)
            {
                startHour =0;
            }
        }
        mHourTextPaint.setTextSize(mTimeTextSize);
        mHourTextPaint.setColor(sleeplightcolorfrom);
        //draw sleep start end text
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String sleepStart = formatter.format(mFirstSleepOnTime);
        String sleepEnd = formatter.format(mLastSleepOnTime);

        mHourTextPaint.setStrokeWidth(3);
        mHourTextPaint.getTextBounds(sleepEnd, 0, sleepEnd.length(), bounds);
        int posY = bounds.bottom -  bounds.top + hourLineEndY+20;
        if(posY>getHeight())
        {
            posY = getHeight()-5;
        }
        float lastAlarmTimeWidth = bounds.width();
        float lastAlarmTimeHeight = bounds.height();
        if(mSleepStartPos<0)
        {
            return;
        }
        canvas.drawLine(mSleepStartPos, hourLineStartY, mSleepStartPos, posY - lastAlarmTimeHeight, mHourTextPaint);
        canvas.drawLine(mSleepEndPos, hourLineStartY, mSleepEndPos, posY - lastAlarmTimeHeight, mHourTextPaint);
        Log.i("smile",posY+" posY "+getHeight());
        int drawableWidth = mSleepStartDrawable.getIntrinsicWidth();
        int drawableHeight = mSleepStartDrawable.getIntrinsicHeight();
        //draw time text
        if(mSleepEndPos > mWidth/2) {
            // draw alarm drawable
            float alarmStartDrawPos = mSleepEndPos;
            //start pos + text width + drawable width > all width -10
            if (alarmStartDrawPos + lastAlarmTimeWidth + drawableWidth > mWidth - 10) {
                alarmStartDrawPos = mWidth - lastAlarmTimeWidth - drawableWidth - 20;
            }
            int drawstart = (int) (alarmStartDrawPos);
            mAlarmDrawable.setBounds(drawstart, posY - drawableHeight, drawstart + drawableWidth, posY);
            mAlarmDrawable.draw(canvas);
            drawstart += drawableWidth + 10;
            canvas.drawText(sleepEnd, drawstart, posY, mHourTextPaint);


            //draw drawable sleep start;

            float SleepStartDrawPos = mSleepStartPos;
            if (SleepStartDrawPos + lastAlarmTimeWidth + drawableWidth > alarmStartDrawPos - 20) {
                SleepStartDrawPos = alarmStartDrawPos - 20 - lastAlarmTimeWidth - drawableWidth;
            }
            drawstart = (int) (SleepStartDrawPos);
            mSleepStartDrawable.setBounds(drawstart, posY - drawableHeight, drawstart + drawableWidth, posY);
            mSleepStartDrawable.draw(canvas);
            drawstart += drawableWidth + 10;
            canvas.drawText(sleepStart, drawstart, posY, mHourTextPaint);
        }
        else
        {

            //draw drawable sleep start;

            float SleepStartDrawPos = mSleepStartPos;

            int   drawstart = (int) (SleepStartDrawPos);
            mSleepStartDrawable.setBounds(drawstart, posY - drawableHeight, drawstart + drawableWidth, posY);
            mSleepStartDrawable.draw(canvas);
            drawstart =  drawstart + drawableWidth+10;
            canvas.drawText(sleepStart, drawstart, posY, mHourTextPaint);

            // draw alarm drawable
            float alarmStartDrawPos = mSleepEndPos;
            //start pos + text width + drawable width > all width -10
            float startDrawEndPos =  SleepStartDrawPos  + lastAlarmTimeWidth + drawableWidth+10;
            if (alarmStartDrawPos < startDrawEndPos+10) {
                alarmStartDrawPos = startDrawEndPos+10;
            }
            drawstart = (int) (alarmStartDrawPos);
            mAlarmDrawable.setBounds(drawstart, posY - drawableHeight, drawstart + drawableWidth, posY);
            mAlarmDrawable.draw(canvas);
            drawstart += drawableWidth + 10;
            canvas.drawText(sleepEnd, drawstart, posY, mHourTextPaint);



        }
    }
    @Override
    protected void drawBackground(Canvas canvas) {
        Log.d("kim", "onDraw");
        if(mSleepData != null) {


            drawWake(canvas);
            drawLightSleep(canvas);
            drawDeepSleep(canvas);
            drawLines(canvas);
            drawHourText(canvas);
        //    canvas.drawRect(0,wakeupHeight,mWidth,mHeight,mWakeupPaint);


            // Draw Grid
//            grid = new GridY(left, bottom, maxWidth, maxHeight, unitY);
//            showGrid(canvas, paint);

//            // Draw Title
//            if(titleText != null) {
//                title = new Title(left, bottom, titleText, titleSize, titleColor, maxWidth, maxHeight);
//                showTitle(canvas, paint);
//            }
//
//            // Draw PolarXY
//            polar = new PolarX(left, bottom, data.length, barObj.width, barObj.interval, polarsTextX, polarTextColorX);
//            showPolar(getResources(), canvas, paint);
//            polar = new PolarY(left, bottom, maxHeight, unitY, polarTextColorY);
//            showPolar(getResources(), canvas, paint);
//
//            // Draw label
//            if(label!=null) {
//                label = new Label(left, bottom, 1, labelsText, labelsTextColor, new int[]{color}, maxWidth, maxHeight);
//                showLabel(getResources(), canvas, paint);
//            }

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Thread drawThread = new Thread(new DrawRunnable(holder));
        drawThread.start();
    }
    private void updatePannel()
    {
        synchronized (this) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // Draw Chart Background
            drawBackground(canvas);

            // Draw Legend
//                legend = new SimpleBar(left, bottom, barObj, data, color, animTime);
//                showLegend(canvas, paint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    class DrawRunnable implements Runnable {
        SurfaceHolder holder;

        public DrawRunnable(SurfaceHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            updatePannel();
        }
    }
     class SleepTimeSpan {
        public int hour;
        public int min;
        public SleepTimeSpan(int h, int m)
        {
            hour = h;
            min = m;
        }
         public float getTotalHour() {  return  hour+ min/60.0f;}
        public int getTotalMinutes()
        {
            return (int)(hour*60+min);
        }
    }

}

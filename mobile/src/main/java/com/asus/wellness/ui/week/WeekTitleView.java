package com.asus.wellness.ui.week;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.ProfileData;
import com.asus.wellness.ui.MainWellness;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.R;

public class WeekTitleView extends FrameLayout implements View.OnTouchListener {
    private final String TAG = "WeekTitleView";

    Resources mResource = null;
    private Context mContext;
    private MainWellness mMainWellness;
    private View mWeekTitleView;
    private FrameLayout mWeekTitleContainer;

    private boolean isInited = false;

    private int mLayoutWidth;
    private int mLayoutHeight;

    private int mWidthBase = 180;
    private int mLargeTextViewWidth = 180;
    private int mSmallTextViewWidth = 180;

    private GestureDetector mGestureDetector = null;
    private MyGestureListener mGestureListener = null;

    private int mWeekTitleCount = 0;
    private List<View> mWeekTitleList = new ArrayList<View>();
    private ArrayList<Float> mWeekTitlePosList = new ArrayList<Float>();

    private boolean isGoNext = false;
    private boolean isJumpTo = false;

    enum VPState {
        VP_IDLE, VP_SCROLLING
    }
    private VPState mVpState = VPState.VP_IDLE;
    private VPState mPrevVpState =  VPState.VP_IDLE;

    private final int mDefaultListIndex = 4;
    private int mCurrentListIndex = mDefaultListIndex;
    private final int mDefaultWeekIndex = 0;
    private int mCurrentWeekIndex = mDefaultWeekIndex;
    private int mStartWeekIndex = Integer.MIN_VALUE;

    private float[] AlphaValue = {
            //0.0f, 0.2f, 0.4f, 0.6f, 1.0f
            0.0f, 1.0f, 1.0f, 1.0f, 1.0f
    };

    private int[] ColorValue = {
            0xffe2e2e2, 0xffe2e2e2, 0xffbcbcbc, 0xffa7a7a7, 0xff74a801
    };

    private String[] Month = new String[] {
            "N/A", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };

    class Week {
        public int startYear;
        public int startMonth;
        public int startDay;
        public int endYear;
        public int endMonth;
        public int endDay;
    }

    public WeekTitleView(Context context) {
        super(context);
        mContext = context;
        mMainWellness = (MainWellness)context;
        onInit();
    }

    public WeekTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mMainWellness = (MainWellness)context;
        onInit();
    }

    public WeekTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mMainWellness = (MainWellness)context;
        onInit();
    }

    private void onInit() {
        isInited = false;

        mResource = mContext.getResources();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mWeekTitleView = inflater.inflate(R.layout.week_title_view, null);
        mWeekTitleContainer = (FrameLayout)mWeekTitleView.findViewById(R.id.weektitle_container);

        addView(mWeekTitleView);

        mGestureListener = new MyGestureListener();
        mGestureDetector = new GestureDetector(mContext, mGestureListener);

        mWeekTitleContainer.setOnTouchListener(this);
    }

    private void setupWeekContainer() {
        int i;

        mWeekTitlePosList.clear();
        mWeekTitleList.clear();
        mWeekTitleCount = 0;
        for (i = 0; i < mWeekTitleContainer.getChildCount(); i++) {
            mWeekTitlePosList.add((float)((i - 1) * mSmallTextViewWidth));
            mWeekTitleList.add(mWeekTitleContainer.getChildAt(i));
            mWeekTitleCount++;
        }
        float pos = mWeekTitlePosList.get(mWeekTitleCount-1) + mSmallTextViewWidth;
        mWeekTitlePosList.add(pos);

        for (i = 0; i < mWeekTitleCount; i++) {
            final TextView tv = (TextView)mWeekTitleList.get(i);
            final FrameLayout.LayoutParams params;
            if(i == mCurrentListIndex) {
                params = new FrameLayout.LayoutParams(mLargeTextViewWidth, LayoutParams.WRAP_CONTENT);
            } else {
                params = new FrameLayout.LayoutParams(mSmallTextViewWidth, LayoutParams.WRAP_CONTENT);
            }
            tv.post(new Runnable() {
                @Override
                public void run() {
                    tv.setLayoutParams(params);
                }
            });
            tv.setX(mWeekTitlePosList.get(i));
            tv.setTextColor(ColorValue[i]);
            tv.setAlpha(AlphaValue[i]);
        }

        ParseDataManager pdm = ParseDataManager.getInstance();;
        ProfileData pfd = pdm.getProfileData(mContext);
        mStartWeekIndex = getWeekIndexByTimeMS(pfd.start_time);

        setWeekTitle();
        isInited = true;
    }

    private int getWeekIndexByTimeMS(long timeMS) {
        int index = 0;
        long oneWeekInterval = Utility.ONE_DAY_MS * 7;

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        long thisWeekFirstTimeMS = cal1.getTimeInMillis();

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timeMS);

        if (timeMS > thisWeekFirstTimeMS
                || (cal1.getTime().getYear() == cal2.getTime().getYear()
                        && cal1.getTime().getMonth() == cal2.getTime().getMonth() 
                        && cal1.getTime().getDate() == cal2.getTime().getDate())) {
            index = mDefaultWeekIndex;
        } else {
            index = -1 * (int)((thisWeekFirstTimeMS - timeMS) / oneWeekInterval + 1);
        }

        return index;
    }

    private void setSingleWeekTitle(int weekIndex, int listIndex) {
        Week wk = getWeekInfo(weekIndex);

        String weekTitle;
        String startMonthStr = Month[wk.startMonth];
        String startDateStr = String.valueOf(wk.startDay);
        String endMonthStr = Month[wk.endMonth];
        String endDateStr = String.valueOf(wk.endDay);

        if(weekIndex == mCurrentWeekIndex) {
            weekTitle = startMonthStr + " " + startDateStr + " - " + endMonthStr + " " + endDateStr;
        } else {
            weekTitle = startDateStr + " - " + endDateStr;
        }
        TextView tv = (TextView) mWeekTitleList.get(listIndex);
        tv.setText(weekTitle);
        if(weekIndex < mStartWeekIndex) {
            tv.setVisibility(View.INVISIBLE);
        } else {
            tv.setVisibility(View.VISIBLE);
        }
    }

    private void setWeekTitle() {
        int lIndex, wIndex;
        wIndex = mCurrentWeekIndex;
        for(int i = 0; i < mWeekTitleCount; i++) {
            lIndex = (mCurrentListIndex - i + mWeekTitleCount) % mWeekTitleCount;
            setSingleWeekTitle(wIndex, lIndex);
            wIndex--;
        }
    }

    private void updateWeekTitleUI() {
        int lIndex, aIndex;
        aIndex = AlphaValue.length - 1;
        for (int i = 0; i < mWeekTitleCount; i++) {
            lIndex = (mCurrentListIndex - i + mWeekTitleCount) % mWeekTitleCount;
            final TextView tv = (TextView)mWeekTitleList.get(lIndex);
            final FrameLayout.LayoutParams params;
            if (lIndex == mCurrentListIndex) {
                params = new FrameLayout.LayoutParams(mLargeTextViewWidth, LayoutParams.WRAP_CONTENT);
            } else {
                params = new FrameLayout.LayoutParams(mSmallTextViewWidth, LayoutParams.WRAP_CONTENT);
            }
            tv.setLayoutParams(params);
            tv.setTextColor(ColorValue[aIndex]);
            tv.setAlpha(AlphaValue[aIndex]);
            aIndex--;
        }
        setWeekTitle();
    }

    public Week getWeekInfo(int nowWeek) {
        Calendar cal = Calendar.getInstance();
        Week week = new Week();
        if (nowWeek == mDefaultWeekIndex) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);//this monday
            cal.add(Calendar.DATE, 6);//this sunday
            week.endYear = cal.get(Calendar.YEAR);
            week.endMonth = cal.get(Calendar.MONTH) + 1;
            week.endDay = cal.get(Calendar.DATE);
            cal.add(Calendar.DATE, -6);//this monday
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// this monday
        for (int i = 0; i < -nowWeek; i++) {
            cal.add(Calendar.DATE, -1);// last sunday
            week.endYear = cal.get(Calendar.YEAR);
            week.endMonth = cal.get(Calendar.MONTH) + 1;
            week.endDay = cal.get(Calendar.DATE);
            cal.add(Calendar.DATE, -6);// last monday
        }
        week.startYear = cal.get(Calendar.YEAR);
        week.startMonth = cal.get(Calendar.MONTH) + 1;
        week.startDay = cal.get(Calendar.DATE);
        return week;
    }

    public int getCurrentWeekIndex() {
        return mCurrentWeekIndex;
    }

    public boolean isJumpTo() {
        return isJumpTo;
    }

    public void notifyVpStateIdle() {
        mVpState = VPState.VP_IDLE;
        mPrevVpState = VPState.VP_IDLE;
        //Log.d(TAG, "notifyVpStateIdle(): state = " + mVpState + " , prev = " + mPrevVpState);
    }

    public void notifyVpStateScroll() {
        mPrevVpState = mVpState;
        mVpState = VPState.VP_SCROLLING;
        //Log.d(TAG, "notifyVpStateScroll(): state = " + mVpState + " , prev = " + mPrevVpState);
    }

    public void reset() {
        mCurrentListIndex = mDefaultListIndex;
        mCurrentWeekIndex = mDefaultWeekIndex;
        isJumpTo = false;
        mVpState = VPState.VP_IDLE;
        mPrevVpState = VPState.VP_IDLE;
        adjustPosition();
    }

    public void goNext() {
        goNextStep(1);
    }

    public void goPrevious() {
        goPreviousStep(1);
    }

    public void goNextStep(int step) {
        isJumpTo = false;
        for(int i = 0; i < step; i++) {
            scrollToNext();
        }
        updateWeekTitleUI();
    }

    public void goPreviousStep(int step) {
        isJumpTo = false;
        for(int i = 0; i < step; i++) {
            scrollToPrevious();
        }
        updateWeekTitleUI();
    }

    public void vpScrollX(float ratio) {
        //Log.d(TAG, "vpScrollX(): ratio = " + ratio);
        float scrollX = mWidthBase * ratio * -1;
        if(ratio < 0) isGoNext = false;
        else if(ratio > 0) isGoNext = true;
        doScrollX(scrollX);
        updateWeekTitleUI();
    }

    public void vpScrollDone() {
        isJumpTo = false;
        adjustPosition();
    }

    private void adjustPosition() {
        //Log.d(TAG, "adjustPosition(): mCurrentListIndex = " + mCurrentListIndex + 
        //        " , mCurrentWeekIndex = " + mCurrentWeekIndex);
        scrollByListIndex(mCurrentListIndex);
        updateWeekTitleUI();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        mLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);

        if(isInited) return;

        mSmallTextViewWidth = mLayoutWidth / 5;
        mWidthBase = mSmallTextViewWidth;
        mLargeTextViewWidth = mSmallTextViewWidth * 2;

        Log.d(TAG, "onMeasure(): W = " + mLayoutWidth + " , H = " + mLayoutHeight);
        Log.d(TAG, "onMeasure(): BaseWidth = " + mWidthBase + " , SmallWidth = " + mSmallTextViewWidth + 
                " , LargeWidth = " + mLargeTextViewWidth);

        setupWeekContainer();
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }

    private void doScrollX(float x) {
        float newX, newX2;
        View v;
        if(isGoNext && mCurrentWeekIndex == mDefaultWeekIndex) {
            v = mWeekTitleList.get(mCurrentListIndex);
            if(v.getX() <= mWeekTitlePosList.get(mDefaultListIndex)) return;
        }
        if(!isGoNext && mCurrentWeekIndex == mStartWeekIndex) {
            v = mWeekTitleList.get(mCurrentListIndex);
            if(v.getX() >= mWeekTitlePosList.get(mDefaultListIndex)) return;
        }
        for (int i = 0; i < mWeekTitleCount; i++) {
            v = mWeekTitleList.get(i);
            newX = v.getX() + x;
            if(newX < mWeekTitlePosList.get(0)) {
                newX2 = mWeekTitlePosList.get(mWeekTitleCount) + newX - mWeekTitlePosList.get(0);
            } else if(newX > mWeekTitlePosList.get(mWeekTitleCount)) {
                newX2 = mWeekTitlePosList.get(0) + newX - mWeekTitlePosList.get(mWeekTitleCount);
            } else {
                newX2 = newX;
            }
            v.setX(newX2);
        }
        if(isGoNext) {
            if((mWeekTitlePosList.get(mDefaultListIndex) - mWeekTitleList.get(mCurrentListIndex).getX()) > mWidthBase / 2) {
                mCurrentListIndex += 1;
                if(mCurrentListIndex >= mWeekTitleCount) {
                    mCurrentListIndex -= mWeekTitleCount;
                }
                mCurrentWeekIndex++;
            }
        } else {
            if((mWeekTitleList.get(mCurrentListIndex).getX() - mWeekTitlePosList.get(mDefaultListIndex)) > mWidthBase / 2) {
                mCurrentListIndex -= 1;
                if(mCurrentListIndex < 0) {
                    mCurrentListIndex += mWeekTitleCount;
                }
                mCurrentWeekIndex--;
            }
        }
    }

    private void scrollByListIndex(int listIndex) {
        int offset = listIndex - mDefaultListIndex;
        for (int i = 0; i < mWeekTitleCount; i++) {
            View v = mWeekTitleList.get(i);
            v.setX(mWeekTitlePosList.get((i - offset + mWeekTitleCount) % mWeekTitleCount));
        }
    }

    private void scrollToNext() {
        if(mCurrentWeekIndex < mDefaultWeekIndex) {
            mCurrentListIndex += 1;
            if(mCurrentListIndex >= mWeekTitleCount) {
                mCurrentListIndex -= mWeekTitleCount;
            }
            mCurrentWeekIndex++;
        }
        scrollByListIndex(mCurrentListIndex);
    }

    private void scrollToPrevious() {
        if(mCurrentWeekIndex > mStartWeekIndex) {
            mCurrentListIndex -= 1;
            if(mCurrentListIndex < 0) {
                mCurrentListIndex += mWeekTitleCount;
            }
            mCurrentWeekIndex--;
        }
        scrollByListIndex(mCurrentListIndex);
    }

    protected class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        float x1 = 0, x2 = 0;

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            //Log.d(TAG, "onSingleTapUp");
            if(mMainWellness.mViewPager.isFakeDragging()) {
                mMainWellness.mViewPager.endFakeDrag();
            }

            float tapX = ev.getX();
            int tapPosition = 0;
            int oriViewPagerItem = mMainWellness.mViewPager.getCurrentItem();
            int newViewPagerItem;

            if(tapX >= mWeekTitlePosList.get(1) && tapX < mWeekTitlePosList.get(2)) {
                tapPosition = -3;
            } else if(tapX >= mWeekTitlePosList.get(2) && tapX < mWeekTitlePosList.get(3)) {
                tapPosition = -2;
            } else if(tapX >= mWeekTitlePosList.get(3) && tapX < mWeekTitlePosList.get(4)) {
                tapPosition = -1;
            } else if(tapX >= mWeekTitlePosList.get(4) && tapX <= mWeekTitlePosList.get(5)) {
                tapPosition = 0;
            }

            isJumpTo = true;

            newViewPagerItem = oriViewPagerItem + tapPosition;
            mMainWellness.mViewPager.setCurrentItem(newViewPagerItem);

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            //Log.d(TAG, "onDoubleTap");
            return true;
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            //Log.d(TAG, "onDownd");
            x1 = 0;
            x2 = 0;
            if(mMainWellness.mViewPager.isFakeDragging()) {
                mMainWellness.mViewPager.endFakeDrag();
            }
            if(mPrevVpState == VPState.VP_IDLE && mVpState == VPState.VP_IDLE) {
                mMainWellness.mViewPager.beginFakeDrag();
            } else {
                mPrevVpState = VPState.VP_SCROLLING;
                mVpState = VPState.VP_SCROLLING;
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent ev) {
            //Log.d(TAG, "onShowPress");
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            //Log.d(TAG, "onLongPress");
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(TAG, "onScroll");
            if(x1 == 0 && x2 == 0) {
                x1 = e1.getX();
                x2 = e2.getX();
            } else {
                x2 = e2.getX();
            }
            float scrollX = x2 - x1;
            if(scrollX < 0 && scrollX > -8) {
                scrollX = -8;
            } else if(scrollX > 0 && scrollX < 8) {
                scrollX = 8;
            }
            x1 = x2;
            if(!(mVpState == VPState.VP_IDLE || (mVpState == VPState.VP_SCROLLING && mPrevVpState == VPState.VP_IDLE))) {
                return true;
            }
            if(!mMainWellness.mViewPager.isFakeDragging()) {
                mMainWellness.mViewPager.beginFakeDrag();
            }
            //Log.d(TAG, "onScroll(): scrollX = " + scrollX);
            mMainWellness.mViewPager.fakeDragBy(scrollX);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(TAG, "onFling");
            x1 = 0;
            x2 = 0;
            if(mMainWellness.mViewPager.isFakeDragging()) {
                mMainWellness.mViewPager.endFakeDrag();
            }
            return true;
        }
    }
}

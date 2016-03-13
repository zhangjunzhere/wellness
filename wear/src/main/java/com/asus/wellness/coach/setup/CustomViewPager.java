package com.asus.wellness.coach.setup;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by jz on 2015/7/16.
 */
public class CustomViewPager extends ViewPager {

    private boolean isPagingEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //emily++++
        /* google issue:  https://code.google.com/p/android/issues/detail?id=64553&q=onInterceptTouchEvent&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars
           fixed by swallowing the exception.
        */
        boolean result = false;
        try {
            result = super.onInterceptTouchEvent(event);
        }
        catch (IllegalArgumentException  ex){
        }
        //emily----
        return this.isPagingEnabled && result;//super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

    public boolean getPagingEnabled() {
        return this.isPagingEnabled ;
    }

}
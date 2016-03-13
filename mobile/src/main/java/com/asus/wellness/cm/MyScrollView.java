package com.asus.wellness.cm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.cmcm.common.statistics.CMAgent;

import java.util.HashMap;

/**
 * Created by waylen_wang on 2015/8/31.
 */
public class MyScrollView extends ScrollView {
    HashMap<String,String> data=new HashMap<>();
    boolean onEvent=false;

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyScrollView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_DOWN) {
            onEvent = false;
        }
        return super.onTouchEvent(ev);
    }
    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt){
        if(t>=oldt&&t>0){
            if(!onEvent) {
                data.clear();
                data.put("projectid", "weeklypage");
                data.put("operation", "2");

                if (getChildAt(0).getMeasuredHeight() == (getScrollY() + getHeight())) {
                    data.put("operation", "3");
                }
                CMAgent.onEvent(CmHelper.PAGEACTION_MSG_ID, data);
                onEvent = true;
            }else {
                if (getChildAt(0).getMeasuredHeight() == (getScrollY() + getHeight())) {
                    data.clear();
                    data.put("projectid", "weeklypage");
                    data.put("operation", "3");
                    CMAgent.onEvent(CmHelper.PAGEACTION_MSG_ID, data);
                }
            }
        }
    }
}

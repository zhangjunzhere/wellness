package com.asus.wellness.ui.daily;

import com.asus.wellness.R;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.utils.Utility;
import com.cmcm.common.statistics.CMAgent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.util.HashMap;

public class CustomScrollView extends ScrollView {

    private boolean isPadLayout = false;
	HashMap<String,String> data=new HashMap<>();
	boolean onEvent=false;

	public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        isPadLayout = Utility.isPadLayout(context);
	}
	
	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
        isPadLayout = Utility.isPadLayout(context);
	}
	
	public CustomScrollView(Context context) {
		super(context);
        isPadLayout = Utility.isPadLayout(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
        if (isPadLayout) {
            return super.onTouchEvent(ev);
        }

		int [] locationScrollView={0, 0};
		getLocationOnScreen(locationScrollView);
		
		int[] locationTransparent={0,0};
		View view=findViewById(R.id.transparent_view);
		view.getLocationOnScreen(locationTransparent);
		
		int bottom=locationTransparent[1]+view.getHeight();
//		Log.i("smile","scrollview: xy: "+locationScrollView[0]+"  "+locationScrollView[1]+"  transparent view:   "+locationTransparent[0]+"  "+locationTransparent[1]+" view height: "+view.getHeight()+" bottom "+bottom);
		if(ev.getAction()==MotionEvent.ACTION_DOWN){
			onEvent=false;
			if(ev.getY()<bottom-locationScrollView[1]){
				return false;
			}
		}
		return super.onTouchEvent(ev);
	}
	
	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt){
        if (isPadLayout) {
            ((ViewGroup)getParent()).findViewById(R.id.separate_line).setVisibility(View.GONE);
            return;
        }

		if(t>=oldt&&t>0){
			if(!onEvent) {
				data.clear();
				data.put("projectid", "dailypage");
				data.put("operation", "2");

				if (getChildAt(0).getMeasuredHeight() == (getScrollY() + getHeight())) {
					data.put("operation", "3");
				}
				CMAgent.onEvent(CmHelper.PAGEACTION_MSG_ID, data);
				onEvent = true;
			}else {
				if (getChildAt(0).getMeasuredHeight() == (getScrollY() + getHeight())) {
					data.clear();
					data.put("projectid", "dailypage");
					data.put("operation", "3");
					CMAgent.onEvent(CmHelper.PAGEACTION_MSG_ID, data);
				}
			}
		}

		View view=findViewById(R.id.transparent_view);
		if(t>=view.getHeight()){
			((ViewGroup)getParent()).findViewById(R.id.separate_line).setVisibility(View.VISIBLE);
		}
		else{
			((ViewGroup)getParent()).findViewById(R.id.separate_line).setVisibility(View.GONE);
		}
	}
}

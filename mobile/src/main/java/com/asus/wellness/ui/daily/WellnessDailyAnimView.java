package com.asus.wellness.ui.daily;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.asus.wellness.R;
import com.asus.wellness.ui.MainWellness;

public class WellnessDailyAnimView extends FrameLayout {
    private final String TAG = "WellnessDailyAnimView";

    Resources mResource = null;
    private Context mContext;
    private MainWellness mMainWellness;
    private View mWellnessDailyAnimView;

    public WellnessDailyAnimView(Context context) {
        super(context);
        mContext = context;
        mMainWellness = (MainWellness)context;
        onInit();
    }

    public WellnessDailyAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mMainWellness = (MainWellness)context;
        onInit();
    }

    public WellnessDailyAnimView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mMainWellness = (MainWellness)context;
        onInit();
    }

    private void onInit() {
        mResource = mContext.getResources();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mWellnessDailyAnimView = inflater.inflate(R.layout.wellness_daily_anim_main, null);
        addView(mWellnessDailyAnimView);
    }
}

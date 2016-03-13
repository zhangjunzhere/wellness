package com.asus.wellness.cm;

import android.preference.PreferenceActivity;

import com.cmcm.common.statistics.CMAgent;

/**
 * Created by waylen_wang on 2015/8/25.
 */
public class CmBasePreferenceActivity extends PreferenceActivity {
    @Override
    protected void onResume(){
        super.onResume();
        CMAgent.onPageStart(getPageName());
    }

    @Override
    protected void onPause(){
        super.onPause();
        CMAgent.onPageEnd(getPageName());
    }

    public  String getPageName(){
        return  CmBasePreferenceActivity.class.getSimpleName();
    };
}

package com.asus.wellness.coach;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.coach.setup.CoachPagerAdapter;
import com.asus.wellness.coach.setup.CustomViewPager;
import com.asus.wellness.utils.CoachWorkoutHelper;
import com.asus.wellness.utils.Constant;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.Utility;
import com.viewpagerindicator.IconPageIndicator;

import java.util.List;

import de.greenrobot.event.EventBus;

public class CoachSetupActivity extends FragmentActivity{

    private CoachPagerAdapter mAdapter;
    private CustomViewPager mPager;
    private IconPageIndicator mIndicator;
    private boolean voiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pni_activity_coach);
        findViewById(R.id.line).setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        if(intent != null && Constant.INTENT_MIME_RUNNING.equals(intent.getStringExtra(Constant.KEY_MIME_TYPE)) ){
            voiceRunning = true;
        }
        //reset previous data
        WApplication.getInstance().getCoachDataModel().resetData();
        //init fragment
        List<Class<?>> fragments =CoachWorkoutHelper.getFragmentClazzByGoal(WApplication.getInstance().getCoachDataModel().getGoal(), voiceRunning,this);
        mAdapter = new CoachPagerAdapter(getSupportFragmentManager(),this,fragments);

        mPager = (CustomViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (IconPageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }

    public boolean getVoiceRunning(){
        return voiceRunning;
    }


//    @Override
    public void changePageList(List<Class<?>> fragmentClazz) {
        mAdapter.changePageList(fragmentClazz);
        mIndicator.notifyDataSetChanged();
   }

    public void enablePageScroll(boolean b) {
        mPager.setPagingEnabled(b);
    }

    @Override
    public void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

//    @Override
//    public void onDestroy(){
//        super.onDestroy();
//        if(mPager.getPagingEnabled()){
//            Intent intent = new Intent(this, WellnessMicroAppMain.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }
//    }

    public void onEventMainThread(EBCommand ebCommand){
        if(ebCommand.receiver.equals(this.getClass().getName())) {
            Log.d(Utility.TAG, ebCommand.toString());
            if(EBCommand.COMMAND_NEXT_PAGE.equals(ebCommand.command)){
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }
        }
    }
}

package com.asus.wellness.sleep;

import android.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.Utility;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/8/27.
 */
public class SleepRecordingFragment extends WearableSleepFragment {
    private final String TAG  = "SleepRecordingFragment";
    private Animation mAlphaAnimation;
    private ImageView mAnimationImage;
    private ThreeDotsLoader mThreeDots;
    private ScrollView ll_sleep_tracking;
    private int pos = 0;
    private int dest = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pni_sleep_fragment_start,container,false);
        mAnimationImage = (ImageView)v.findViewById(R.id.sleepImageView);
        ll_sleep_tracking = (ScrollView) v.findViewById(R.id.ll_sleep_tracking);

        mThreeDots = (ThreeDotsLoader)v.findViewById(R.id.threeDots);

        initAnimation();
        //startFlickerAninmation();
        return  v;
    }

    private void initAnimation(){
        mAlphaAnimation = new AlphaAnimation(0.2f, 1.0f);
        mAlphaAnimation.setDuration(1000);
        mAlphaAnimation.setRepeatCount(Animation.INFINITE);
        mAlphaAnimation.setRepeatMode(Animation.REVERSE);
    }
    public void startFlickerAninmation() {
        if(isAmbient())
        {
            Log.i("smile","startFlickerAninmation ambient return");
            return;
        }
        Log.i("smile","startFlickerAninmation");
        mAlphaAnimation.cancel();
        mAlphaAnimation.start();
        mAnimationImage.setImageResource(R.drawable.asus_wellness_ic_sleep_recording_b);
        mAnimationImage.startAnimation(mAlphaAnimation);
        mThreeDots.stopLoading();
        mThreeDots.startLoading();
     }

    public void stopFlickerAninmation(){
        mAlphaAnimation.cancel();
        mAnimationImage.setImageResource(R.drawable.asus_wellness_ic_sleep_recording);
        mAnimationImage.clearAnimation();
        mThreeDots.stopLoading();
    }

    @Override
    public void onResume(){
        super.onResume();
        startFlickerAninmation();
    }

    @Override
    public void onPause(){
        super.onPause();
        stopFlickerAninmation();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopFlickerAninmation();
    }

    @Override
    public void onEnterAmbient() {
	 ll_sleep_tracking.removeCallbacks(scrollRunnable);
        ll_sleep_tracking.scrollTo(0, 0);
        stopFlickerAninmation();
    }

    @Override
    public void onExitAmbient() {
        startFlickerAninmation();

        initScroll();
        ll_sleep_tracking.postDelayed(scrollRunnable, 500);
    }

    private void initScroll(){
        pos = 0;
        dest = Utility.getScreenHeight(getActivity());
    }

    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            ll_sleep_tracking.smoothScrollTo(0, pos);
            pos += 30;
            if(pos < dest){
                ll_sleep_tracking.postDelayed(this,10);
            }
        }
    };
}

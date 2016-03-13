package com.asus.wellness.Profile.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.asus.wellness.Profile.ProfileSwipeAdapter;
import com.asus.wellness.R;
import com.asus.wellness.coach.setup.CustomViewPager;
import com.nineoldandroids.view.ViewHelper;
import com.viewpagerindicator.IconPageIndicator;

/**
 * Created by smile_gao on 2015/5/25.
 */
public class ProfileView extends LinearLayout {
    CustomViewPager mPager = null;
    ProfileSwipeAdapter mAdapter=null;
    int SCALE_MAX =1 ;

    private float mLeftScale;
    private float mRightScale;

    private ViewBase mLeft;

    private ViewBase mRight;
    IconPageIndicator mIndicator;
    public ProfileView(Context context)
    {
        super(context);
        View.inflate(context, R.layout.pni_profile, this);

        mPager = (CustomViewPager)findViewById(R.id.profilepager);
        // AsusLog.LogI("smile",mSmallSize+" "+mBigSize);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
             //   Log.i("onPageScrolled",position+" offset "+positionOffset+" "+positionOffsetPixels);
                float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;

                mLeft = mAdapter.findViewFromObject(position);

                mRight = mAdapter.findViewFromObject(position + 1);
                animateStack(mLeft, mRight, effectOffset, positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mIndicator =new IconPageIndicator(context);
        mIndicator = (IconPageIndicator)findViewById(R.id.indicator);

    }
    private boolean isSmall(float positionOffset)
    {
        return Math.abs(positionOffset) < 0.0001;
    }
    protected void animateStack(ViewBase left, ViewBase right, float effectOffset,
                                int positionOffsetPixels)
    {
        if (right != null)
        {

            mRightScale = (2 - SCALE_MAX) * effectOffset + SCALE_MAX;

//            mTrans = -getWidth() - mPager.getPageMargin() + positionOffsetPixels;
//            ViewHelper.setScaleX(right, mScale);
            View lineView = right.getLineView();
            if(lineView==null)
            {
                return;
            }
            ViewHelper.setScaleY(lineView, mRightScale);
     //       ViewHelper.setTranslationX(right, mTrans);
        }
        if (left != null)
        {
            mLeftScale =SCALE_MAX*2 - (2 - SCALE_MAX) * effectOffset ;
            View lineView = left.getLineView();
            if(lineView==null)
            {
                return;
            }
            ViewHelper.setScaleY(lineView, mLeftScale);
        }
       // Log.i("smile","left: "+mLeftScale+" right: "+mRightScale+" effort: "+effectOffset);
    }
    public  void setAdapter(ProfileSwipeAdapter adapter)
    {
   //     mPager = (ViewPager) findViewById(R.id.pager);

        mAdapter= adapter;
        mPager.setAdapter(adapter);
        mIndicator.setViewPager(mPager);
        int visible = mAdapter.getCount()>1? View.VISIBLE: View.GONE;
        mIndicator.setVisibility(visible);
    }
    public int getCurrentPageIndex()
    {
         return mPager.getCurrentItem();

    }
    public void scrollToNextPage()
    {
        int current = getCurrentPageIndex();
        current++;
        if(current>=mAdapter.getCount())
        {
            return;
        }
        mPager.setCurrentItem(current,true);

    }
    public void setViewPagerScroll(boolean enable)
    {
        mPager.setPagingEnabled(enable);
    }
}

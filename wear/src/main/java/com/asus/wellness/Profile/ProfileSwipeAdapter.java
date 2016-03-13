package com.asus.wellness.Profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.view.ViewBase;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.HashMap;

/**
 * 类描述：左右滑动手表屏幕，下方有indicator代表当前页面的特效
 * Created by Kim_Bai on 5/14/2015.
 */
public class ProfileSwipeAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

    ProfileController profileController = null;
    FragmentManager mFragment = null;
    HashMap<Integer, ViewBase>  viewBaseHashMap;
    private int adaptercount = 0;
    public ProfileSwipeAdapter(FragmentManager fm,ProfileController pm) {
        super(fm);
        profileController = pm;
        mFragment = fm;
        viewBaseHashMap = new HashMap<>();
    }
    public void resetAdaptercount()
    {
        adaptercount = 0;
    }
    public void setCustomAdaptercount(int count)
    {
        adaptercount = count;
    }
    @Override
    public Fragment getItem(int position) {
        AsusLog.i("smile", String.valueOf(position) + " getItem");
//       Fragment f= mFragment.findFragmentByTag("viewbase" + position);
//        if(f !=null)
//        {
//            return  f;
//        }
        ViewBase vb = ViewBase.getView(position, profileController);
        if(viewBaseHashMap.containsKey(position))
        {
            return  viewBaseHashMap.get(position);
        }
        else
        {
            viewBaseHashMap.put(position, vb);
        }
        return vb;
       // return ArrayListFragment.newInstance(position);
      // return  new GenderView(); //profileModel
    }
    public ViewBase findViewFromObject(int position)
    {
        return viewBaseHashMap.get(position);
    }
//    @Override
//    public float getPageWidth(int position) {
//        if(position == getCount()-1)
//        {
//            return  1f;
//        }
//        return 0.85f;
//    }

    @Override
    public int getIconResId(int index) {
        return R.drawable.pni_perm_group_dot;
    }

    @Override
    public int getCount() {
        if(adaptercount >0)
        {
            return  adaptercount;
        }
        return ViewBase.profileClasses.length;
    }
}

package com.asus.wellness.coach.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.asus.wellness.R;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoachPagerAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter {

    public final ArrayList<Fragment> screens = new ArrayList<Fragment>();

    private Context context;

    public CoachPagerAdapter(FragmentManager fm, Context context, List<Class<?>> screens) {
        super(fm);
        this.context = context;

        for(Class<?> screen : screens) {
            addScreen(screen, null);
        }
    }

    public CoachPagerAdapter(FragmentManager fm, Context context, Map<Class<?>, Bundle> screens) {
        super(fm);
        this.context = context;

        for(Class<?> screen : screens.keySet())
            addScreen(screen, screens.get(screen));
    }

    private void addScreen(Class<?> clazz, Bundle args) {
        screens.add(Fragment.instantiate(context, clazz.getName(), args));
    }

    @Override
    public int getIconResId(int index) {
        return R.drawable.pni_perm_group_dot;
    }

    @Override
    public int getCount() {
        return screens.size();
    }

    @Override
    public Fragment getItem(int position) {
        return screens.get(position);
    }

//    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE; // To make notifyDataSetChanged() do something
    }



    public void changePageList(List<Class<?>> fragmentClazz){
        screens.clear();
        for(Class<?> clazz : fragmentClazz){
            addScreen(clazz,null);
        }
        notifyDataSetChanged();
    }
}

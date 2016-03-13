package com.asus.wellness.adapter;

import com.asus.wellness.R;
import com.asus.wellness.fragment.StartMeasureFragment;
import com.asus.wellness.fragment.TargetStatusFragment;
import com.viewpagerindicator.IconPagerAdapter;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SwipeAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
	protected static final int[] ICONS = new int[] {
			R.drawable.pni_perm_group_dot,
			R.drawable.pni_perm_group_dot,
			R.drawable.pni_perm_group_dot,
			R.drawable.pni_perm_group_dot
	};
	Intent mIntent;
	private List<Fragment> mFragmentList = new ArrayList<>();
	final  int SIZE;
	//default for robin
	public SwipeAdapter(FragmentManager fm, Intent intent) {
		super(fm);
		mIntent=intent;
		mFragmentList.add(TargetStatusFragment.newInstance(mIntent));
		mFragmentList.add(new StartMeasureFragment());
		SIZE = mFragmentList.size();
		// TODO Auto-generated constructor stub
	}
	public SwipeAdapter(FragmentManager fm, List<Fragment> fragmentList) {
		super(fm);
		mFragmentList = fragmentList;
		SIZE = mFragmentList.size();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
//		Fragment fragment=null;
//		switch(arg0){
//			case 0:
//				fragment=TargetStatusFragment.newInstance(mIntent);
//				break;
//			case 1:
//				fragment=new StartMeasureFragment();
//				break;
//		}
		return mFragmentList.get(arg0);
	}

	@Override
	public int getIconResId(int index) {
		return ICONS[index % ICONS.length];
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return SIZE;
	}

	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return POSITION_NONE;
	}
}

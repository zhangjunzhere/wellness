package com.asus.wellness.Profile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/5/15.
 */
public class AgeFragment extends ViewBase{
    WearableListView listView;
    private static  final int  MAX = 100;
    private  List<String> listItems;
    MyAdapter myAdapter;
    public AgeFragment()
    {
        super();
        listItems = new ArrayList<String>();
        for (int i=1;i<MAX;i++)
        {
            listItems.add(String.valueOf(i));
        }
    }

    @Override
    public String getTitle()
    {
        return getResources().getString(R.string.age)+"("+getResources().getString(R.string.yearsold)+")";
    }
    /**
     * get current Fragment layout res
     * @return layout res id
     */
    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_age;
    }

    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();
        AsusLog.i("smile","age "+pm.getAge());
        if(pm.getAge()>0 && pm.getAge()<MAX)
        {
            listView.scrollToPosition(pm.getAge()-1);

        }

    }
    @Override
    public void onItemClick(int listviewindex,String str,int position)
    {
      //  Log.i("smile","onItemClick "+str+" pos: "+position);
        int clicknum = Integer.parseInt(str);
        if(clicknum == mProfileController.getmProfileModel().getAge())
        {
            mProfileController.goNextPage();
        }
        else
        {
            listView.smoothScrollToPosition(clicknum-1);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=super.onCreateView(inflater, container, savedInstanceState);
        listView = (WearableListView) v.findViewById(R.id.list);
        myAdapter = new MyAdapter(listItems,this);
        listView.setAdapter(myAdapter);

        listView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {

            }

            @Override
            public void onAbsoluteScrollChange(int i) {

            }

            @Override
            public void onScrollStateChanged(int i) {

            }

            @Override
            public void onCentralPositionChanged(int i) {

                 AsusLog.i("smile","log  "+i);
                 mProfileController.onAgeChange(i+1);
            }
        });

        return  v;
    }

}

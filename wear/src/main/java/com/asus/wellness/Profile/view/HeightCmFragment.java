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
 * Created by smile_gao on 2015/5/19.
 */
public class HeightCmFragment extends HeightFragment {
    WearableListView listView;
    MyAdapter myAdapter;
    List<String> listItems;
    private static  final  int MAX = 300;

    public  static   String Tag = "HeightCmFragment";

    public HeightCmFragment() {
        super();
        listItems = new ArrayList<String>();
        for (int i=1;i<MAX;i++)
        {
            listItems.add(String.valueOf(i));
        }
    }
    @Override
    public void onItemClick(int listviewindex, String str,int position)
    {
       // Log.i("smile", "height onItemClick " + str);
        int clicknum = Integer.parseInt(str);
        if(clicknum == mProfileController.getmProfileModel().getHeight())
        {
            mProfileController.goNextPage();
        }
        else
        {
            listView.smoothScrollToPosition(position);
        }
    }
    @Override
    public String getMyTag()
    {
        return  Tag;
    }
    @Override
    public String getTitle()
    {
        return getResources().getString(R.string.height)+"("+getResources().getString(R.string.cm)+")";
    }
    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_height_cm;
    }
    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();
        int height = pm.getHeight();
        if(height>0 && height<MAX)
        {
            listView.scrollToPosition(height-1);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=super.onCreateView(inflater,container,savedInstanceState);
        listView = (WearableListView) v.findViewById(R.id.list);
        myAdapter = new MyAdapter(listItems, this);
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

                AsusLog.i("smile", "cm onCentralPositionChanged  " + (i+1));
                mProfileController.onHeightChange(i+1);
            }
        });
        listItems = new ArrayList<String>();
        for (int i=1 ;i <MAX;i++)
        {
            listItems.add(String.valueOf(i));
        }
        return  v;
    }
}

package com.asus.wellness.Profile.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.Profile.EventCmd;
import com.asus.wellness.Profile.ProfileEvent;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/5/20.
 */
public class WeightUnitFragment extends ViewBase {
    WearableListView listView;
    MyAdapter weightunitAdapter;
    int mCurrentPosition = 0;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        final String kg = getResources().getString(R.string.kg);
        final String lbs = getResources().getString(R.string.lbs);
        listView = (WearableListView)view.findViewById(R.id.listview);
        List<String> list = new ArrayList<String>();//{male,female,skip}
        list.add(kg);
        list.add(lbs);
        weightunitAdapter = new MyAdapter(list,this);
        listView.setAdapter(weightunitAdapter);


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
                mCurrentPosition = i;
                mProfileController.onWeightUnitChange(i);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public String getTitle()
    {
        return getResources().getString(R.string.weightunit);
    }

    @Override
    public void onItemClick(int listviewindex,String str,int position)
    {
        Log.i("smile", "onItemClick " + str + " pos: ");
        //  int clicknum = Integer.parseInt(str);
        if( position != mCurrentPosition)
        {
            listView.smoothScrollToPosition(position);
        }
        else if(position < 2)
        {
            if(mCurrentPosition == position)
            {
                mProfileController.goNextPage();
            }
        }

    }
    @Override
    public int getListItemGravity()
    {
        return Gravity.LEFT | Gravity.CENTER_VERTICAL;
    }
    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_weight_unit;
    }

    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();

        if(pm.getWeightUnit()== ProfileTable.WEIGHT_UNIT_KG)
        {
            listView.smoothScrollToPosition(0);
        }
        else
        {
            listView.smoothScrollToPosition(1);
        }
        EventBus.getDefault().post(new ProfileEvent(EventCmd.UPDATE_WEIGHT_VIEW));
    }
}

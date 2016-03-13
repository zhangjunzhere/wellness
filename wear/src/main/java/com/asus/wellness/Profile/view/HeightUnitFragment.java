package com.asus.wellness.Profile.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

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
 * Created by smile_gao on 2015/5/19.
 */
public class HeightUnitFragment extends  ViewBase {
//    Button cmbtn ;
//    Button ftinbtn;
    WearableListView listView;
    MyAdapter heightunitAdapter;
    int mCurrentPosition = 0;

    @Override
    public String getTitle()
    {
        return getResources().getString(R.string.heightunit);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

//        cmbtn = (Button)view.findViewById(R.id.cm);
//        ftinbtn = (Button)view.findViewById(R.id.ftin);
//        cmbtn.setOnClickListener(mProfileController);
//        ftinbtn.setOnClickListener(mProfileController);
        final String cm = getResources().getString(R.string.cm);
        final String ft = getResources().getString(R.string.ft);
        listView = (WearableListView)view.findViewById(R.id.listview);
        List<String> list = new ArrayList<String>();//{male,female,skip}
        list.add(cm);
        list.add(ft);
        heightunitAdapter = new MyAdapter(list,this);
        listView.setAdapter(heightunitAdapter);


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
                mProfileController.onHeightUnitChange(i);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void onItemClick(int listviewindex,String str,int position)
    {
        Log.i("smile", "onItemClick " + str + " pos: " );
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
        return R.layout.pni_profile_fragment_heightunit;
    }

    /**
     * update ui , send msg to HeightFragment
     */
    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();

        if(pm.getHeightUnit()== ProfileTable.HEIGHT_UNIT_FT)
        {
            listView.smoothScrollToPosition(1);
        }
        else
        {
            listView.smoothScrollToPosition(0);

        }
        EventBus.getDefault().post(new ProfileEvent(EventCmd.UPDATE_HEIGHT_VIEW));
    }
}

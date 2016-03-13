package com.asus.wellness.Profile.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/5/14.
 */
public class GenderFragment extends ViewBase {
//    Button malebtn ;
//    Button femalebtn;
//    Button skipbtn;
    WearableListView listView;
    MyAdapter genderAdapter;
    int mCurrentPosition = 0;

    public int getLayout() {
        return R.layout.pni_profile_fragment_gender;
    }
    public GenderFragment()
    {
        super();
    }
 @Override
 public int getListItemGravity()
 {
     return Gravity.LEFT | Gravity.CENTER_VERTICAL;
 }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

//        malebtn =(Button) view.findViewById(R.id.male);
//        femalebtn =(Button) view.findViewById(R.id.female);
//       Button skipbtn =(Button) view.findViewById(R.id.skip);
//        malebtn.setOnClickListener(mProfileController);
//        femalebtn.setOnClickListener(mProfileController);
//        skipbtn.setVisibility(View.VISIBLE);
//        skipbtn.setOnClickListener(mProfileController);


        listView = (WearableListView)view.findViewById(R.id.genderlist);
       final String male = getResources().getString(R.string.male);
       final String female = getResources().getString(R.string.female);
       final String skip = getResources().getString(R.string.skip);
        List<String> list = new ArrayList<String>();//{male,female,skip}
        list.add(male);
        list.add(female);
        if(mProfileController.getFirstUse())
        {
            list.add(skip);
        }
        genderAdapter = new MyAdapter(list,this);
        listView.setAdapter(genderAdapter);


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
                mProfileController.onGenderChange(i);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public String getTitle()
    {
        return getResources().getString(R.string.setupyourprofile);
    }

    @Override
    public void onItemClick(int listviewindex,String str,int position)
    {
      //  Log.i("smile", "onItemClick " + str + " pos: " );
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
        else {
            mProfileController.goSkipActivity();
        }
    }
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();
       // AsusLog.i("Gender:",pm.getGender());
        if(pm.getGender()== ProfileTable.MALE)
        {
            listView.smoothScrollToPosition(0);
        }else
        {
            listView.smoothScrollToPosition(1);
        }
    }
}

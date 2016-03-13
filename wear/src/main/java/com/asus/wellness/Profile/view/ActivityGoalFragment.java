package com.asus.wellness.Profile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ActivityGoalDataItem;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/5/20.
 */
public class ActivityGoalFragment extends  ViewBase {
  //  ScrollView mScollview ;
    int[][] content = new int[][]{
            {R.string.k05, R.string.sedentaryliftstyle},
            {R.string.k57, R.string.lowactive},
            {R.string.k710,R.string.somewhatactive},
            {R.string.k1012,R.string.active},
     //       {R.string.k12more,R.string.highlyactive}
    };
    WearableListView mlist_goal;
    ActvityGoalAdatper myAdapter;
    List<ActivityGoalDataItem> itemList;
    protected int mCurrentPosition=0;
  //  ActivityGoalItemView[] itemviews = new ActivityGoalItemView[4];

    @Override
    public void onItemClick(int listviewindex, String str, int position)
    {
        Log.i("smile","AcitityGoal onItemClick");
        if( position != mCurrentPosition)
        {
            mlist_goal.smoothScrollToPosition(position);
        }
        else
        {
            if(mCurrentPosition == position)
            {
                mProfileController.goNextPage();
            }
        }

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=super.onCreateView(inflater, container, savedInstanceState);
        fileContent();
        mlist_goal = (WearableListView) v.findViewById(R.id.list_goal);
        myAdapter = new ActvityGoalAdatper(itemList,this);
        mlist_goal.setAdapter(myAdapter);
        mlist_goal.addOnScrollListener(new WearableListView.OnScrollListener() {
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
                mProfileController.onGoalChange(i);
            }
        });
        fileContent();
        return  v;
    }
    void fileContent()
    {
        itemList = new ArrayList<>();
        for (int i =0 ;i < 4; i++)
        {
            //itemviews[i].setOnClickListener(mProfileController);
            //itemviews[i].setContent(content[i][0],content[i][1]);
            itemList.add(new ActivityGoalDataItem(getResStr(content[i][0]),getResStr(content[i][1])));
        }
    }
    String getResStr(int id)
    {
        return  getResources().getString(id);
    }
    @Override
    public View getListItemView()
    {
        return  new ActivityGoalItemView(getActivity());
    }/**
     * get current Fragment layout res
     * @return layout res id
     */
    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_activity_goal;
    }

    /**
     * udpate current ui
     */
    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();
        long goal = pm.getStepgoal();
        int target =  pm.getStepgoalTargetIndex();
        mlist_goal.scrollToPosition(target);
//        for (int i =0 ;i <itemviews.length; i++)
//        {
//            itemviews[i].unSelect();
//        }
//        itemviews[pm.getStepgoalTargetIndex()].select();
    }

    class ActvityGoalAdatper extends  WearableListView.Adapter{
        private List<ActivityGoalDataItem> mList;
        private ViewBase mViewBase;
        private int mIndex =0;
        public ActvityGoalAdatper(List<ActivityGoalDataItem> list, ViewBase v) {
            super();
            mList= list;
            mViewBase = v;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(mViewBase.getListItemView());
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, final int position) {
           // super.onBindViewHolder(holder, position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewBase.onItemClick(mIndex, "", position);
                }
            });
            TextView tvgoal  = (TextView)holder.itemView.findViewById(R.id.goal);
            tvgoal.setText(mList.get(position).getGoal());
            TextView tvdes  = (TextView)holder.itemView.findViewById(R.id.describe);
            tvdes.setText(mList.get(position).getDes());

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }


}

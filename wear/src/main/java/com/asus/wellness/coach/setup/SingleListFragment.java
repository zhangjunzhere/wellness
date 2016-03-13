package com.asus.wellness.coach.setup;

import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim_Bai on 5/14/2015.
 */
public abstract class SingleListFragment extends AbsWorkoutFragment {

    protected WearableListView mWearableListView ;
    protected TextView mPageTitle ;
    protected WearableListViewAdapter mWearableListViewAdapter ;
    protected WearableListViewAdapter.OnScrollListener mOnScrollListener;

    protected List<String> mModelArray = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pni_coach_fragment_workout_insert_data, container, false);

        mWearableListView = (WearableListView)v.findViewById(R.id.list0);
        mModelArray = getModelArray();

        Bundle textSizes = createAdapterArgs();
        mWearableListViewAdapter= new WearableListViewAdapter(this.getActivity(), mModelArray,textSizes);

        mOnScrollListener = getOnScrollListener();
        mWearableListView.setAdapter(mWearableListViewAdapter);
        mWearableListView.addOnScrollListener(mOnScrollListener);
        mWearableListView.setClickListener(mWearableListViewAdapter);


        mPageTitle = (TextView) v.findViewById(R.id.tv_page_title);
        mPageTitle.setText(getPageTitle());
   //     Utility.adjustTextSize(mPageTitle, getResources().getDimension(R.dimen.page_title_text_size), getResources().getDimension(R.dimen.margin_left) );

        View line = v.findViewById(R.id.line);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)line.getLayoutParams();
        params.leftMargin = lineLeftMargin();// getActivity().getResources().getDimensionPixelOffset(R.dimen.margin_left);
        params.rightMargin = lineRightMargin();// getActivity().getResources().getDimensionPixelOffset(R.dimen.margin_left);
        line.setLayoutParams(params);
        return v;
    }




    @Override
    public void onDestroy(){
        mWearableListView.removeOnScrollListener(mOnScrollListener);
        super.onDestroy();
    }

    protected   Bundle createAdapterArgs() {return  null;}

    protected   int  lineLeftMargin() {return  0;} //getActivity().getResources().getDimensionPixelOffset(R.dimen.margin_left);
    protected   int  lineRightMargin() {return  0;}


    protected abstract WearableListViewAdapter.OnScrollListener getOnScrollListener();
    protected abstract List<String> getModelArray();
    protected abstract String getPageTitle();
}


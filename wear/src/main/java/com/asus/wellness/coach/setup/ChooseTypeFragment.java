package com.asus.wellness.coach.setup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asus.wellness.R;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.utils.EBCommand;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Kim_Bai on 5/14/2015.
 */
public class ChooseTypeFragment extends SingleListFragment {

    private String TAG = "ChooseTypeFragment";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater,container,savedInstanceState);

        mWearableListView.scrollToPosition(mCoachDataModel.getType().ordinal());
        return rootView;
    }

    @Override
    protected WearableListViewAdapter.OnScrollListener getOnScrollListener(){
        return  new WearableListViewAdapter.OnScrollListener(){
            @Override
            public void onCentralPositionChanged(int i) {
                Log.d("ChooseTypeFragment", "onCentralPositionChanged position:" + i);

                CoachDataModel.eType preType = mCoachDataModel.getType();
                CoachDataModel.eType curType = CoachDataModel.eType.valueOf(i);

                Log.d("ChooseTypeFragment", "preType :" +preType + " curType:" + curType);

                if(preType != curType){
                    mCoachDataModel.setType(curType);
                    EBCommand ebCommand = new EBCommand(this.getClass().getName(),ChooseGoalFragment.class.getName(), EBCommand.COMMAND_COACH_GOAL_CHANGED, null);
                    EventBus.getDefault().post(ebCommand);
                }
            }
        };
    }


    @Override
    protected List<String> getModelArray(){
        mModelArray.clear();
        String[] arr =  getActivity().getResources().getStringArray(R.array.workout_types);
        for(String item : arr){
            mModelArray.add(item);
        }
        return mModelArray;
    }

    @Override
    protected  String getPageTitle(){
        return getString(R.string.title_workout_choose) ;
    }


    @Override
    protected   int  lineLeftMargin() {return  getActivity().getResources().getDimensionPixelOffset(R.dimen.margin_left);}
}

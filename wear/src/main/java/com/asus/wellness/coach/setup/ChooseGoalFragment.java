package com.asus.wellness.coach.setup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.asus.wellness.R;
import com.asus.wellness.coach.CoachSetupActivity;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.utils.CoachWorkoutHelper;
import com.asus.wellness.utils.EBCommand;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Kim_Bai on 5/14/2015.
 */
public class ChooseGoalFragment extends SingleListFragment {
    private final String TAG = "ChooseGoalFragment";
    private int scrollPosition = 0;
    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater,container,savedInstanceState);
        int position = mCoachDataModel.getGoal().getValue();
        mWearableListView.scrollToPosition(position);

        //if not do this ,dynamicChangPageList will response slowly
        mWearableListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                    dynamicChangPageList(scrollPosition);
                }
                return false;
            }
        });
        return rootView;
    }


    public  void onEvent(EBCommand cmdMsg){
        String receiver = cmdMsg.receiver;
        if(this.getClass().getName().equals(receiver)){
            Log.d(TAG, "onEvent :" + cmdMsg);
            mWearableListViewAdapter.setContent(getModelArray(),null);
            dynamicChangPageList(scrollPosition);
        }
    }

     @Override
    protected WearableListViewAdapter.OnScrollListener getOnScrollListener(){
        return new WearableListViewAdapter.OnScrollListener(){
            @Override
            public void onCentralPositionChanged(int i) {
                Log.d("ChooseGoalFragment", "onCentralPositionChanged position:" + i);
                scrollPosition = i;

            }
        };
    }

    @Override
    protected List<String> getModelArray() {
        mModelArray.clear();
        int resId = R.array.workout_goal_distance;
        switch (mCoachDataModel.getType()) {
            case PUSHUP:
            case SITUP:
                resId = R.array.workout_goal_quantity;
                break;
            case RUN:
            case BIKE:
            default:
                break;
        }
        String[] arr =  getActivity().getResources().getStringArray(resId);
        for(String item : arr){
            mModelArray.add(item);
        }
        return mModelArray;
    }


    private void dynamicChangPageList(final int position){
        CoachDataModel.eGoal preGoal = mCoachDataModel.getGoal();
        CoachDataModel.eGoal curGoal = CoachDataModel.eGoal.valueOf(position, mCoachDataModel.getType());

        Log.d(TAG, "preGoal = " + preGoal + "  curGoal=" + curGoal);

        if (preGoal != curGoal) {
            mCoachDataModel.setGoal(curGoal);
            if(preGoal == CoachDataModel.eGoal.NOGOAL || curGoal == CoachDataModel.eGoal.NOGOAL) {
                boolean voiceRunning = ((CoachSetupActivity)getActivity()).getVoiceRunning();
                List<Class<?>> fragmentListAll = CoachWorkoutHelper.getFragmentClazzByGoal(mCoachDataModel.getGoal(),voiceRunning,getActivity());
                ((CoachSetupActivity) getActivity()).changePageList(fragmentListAll);
            }else{
                EBCommand ebCommand = new EBCommand(getClass().getName(),InsertDataFragment.class.getName(),EBCommand.COMMAND_COACH_TYPE_CHANGED,null);
                EventBus.getDefault().post(ebCommand);
            }
        }
    }

    @Override
    protected  String getPageTitle(){
        return getString(R.string.title_workout_goal);
    }

}


package com.asus.wellness.coach;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.coach.setup.AbsWorkoutFragment;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.Utility;


public class WorkoutInfoFragment extends AbsWorkoutFragment {
    private View mRootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.pni_coach_fragment_workout_info, container, false);
        updateUI();
        return mRootView;
    }

    private void updateUI(){
        TextView tv_timer = (TextView)mRootView.findViewById(R.id.tv_timer);
        String time = Utility.formatTime(mCoachDataModel.getTotalTime());
        tv_timer.setText(time);

        TextView tv_calories = (TextView)mRootView.findViewById(R.id.tv_calories);
        tv_calories.setText(String.valueOf(mCoachDataModel.getTotalCalories()));

        TextView tv_target_distance = (TextView)mRootView.findViewById(R.id.tv_target_distance);
        tv_target_distance.setText(String.valueOf(mCoachDataModel.getDistanceTargetWithUnitString(getActivity())));

        TextView tv_workout = (TextView)mRootView.findViewById(R.id.tv_workout);

        ImageView iv_workout_icon = (ImageView) mRootView.findViewById(R.id.iv_workout_icon);

        int visibility = View.GONE;
        int workoutIconId = R.drawable.pni_asus_wellness_ic_walk;
        String workoutValue = String.valueOf((long)mCoachDataModel.getValue());

        switch (mCoachDataModel.getType()){
            case RUN:
                visibility = View.VISIBLE;
                workoutIconId = R.drawable.pni_asus_wellness_ic_walk_b;
                workoutValue =  mCoachDataModel.getDistanceValueString();
                break;
            case SITUP:
                workoutIconId = R.drawable.asus_wellness_ic_situp;
                break;
            case PUSHUP:
                workoutIconId = R.drawable.asus_wellness_ic_pushup;
                break;
        }
        iv_workout_icon.setImageResource(workoutIconId);
        tv_target_distance.setVisibility(visibility);
        tv_workout.setText(workoutValue);
    }

    //for evenetbus ,receive command from WorkoutControllerLayout
    @Override
    public void onEventMainThread(EBCommand ebCommand){
        if((StartWorkoutActivity.class.getName().equals(ebCommand.receiver))){
               updateUI();
        }
    }

}

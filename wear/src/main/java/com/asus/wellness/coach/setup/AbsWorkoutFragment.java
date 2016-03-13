package com.asus.wellness.coach.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.utils.EBCommand;

import de.greenrobot.event.EventBus;

/**
 * Created by Kim_Bai on 5/14/2015.
 */
public abstract class AbsWorkoutFragment extends Fragment {

//    protected  String mTag  ;
    protected CoachDataModel mCoachDataModel;

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoachDataModel = ((WApplication)getActivity().getApplication()).getCoachDataModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public    void onEventMainThread(EBCommand cmdMsg){

    }

}

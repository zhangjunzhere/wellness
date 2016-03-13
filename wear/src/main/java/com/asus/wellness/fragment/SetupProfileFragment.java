package com.asus.wellness.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asus.wellness.Profile.EventCmd;
import com.asus.wellness.Profile.ProfileEvent;
import com.asus.wellness.Profile.controller.SetupProfileController;
import com.asus.wellness.Profile.view.ProfileReviewFragment;
import com.asus.wellness.Profile.view.RealSetupProfileFragment;
import com.asus.wellness.Profile.view.ViewBase;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

import de.greenrobot.event.EventBus;

/**
 * 第四个Fragment，显示 Tap to setup your profile
 * Created by Kim_Bai on 5/14/2015.
 */
public class SetupProfileFragment extends Fragment {

    SetupProfileController mController;
    /**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */
    public static SetupProfileFragment newInstance() {
        SetupProfileFragment f = new SetupProfileFragment();
        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController  = new SetupProfileController(getActivity());
        mController.init(false);
    }
    /**
     * receive msg to update ui
     * @param msg   UPDATE_MAIN_PROFILE_VIEW
     */
    public void onEvent(ProfileEvent msg)
    {
        AsusLog.i("smile", "onevent " + msg);
        if(msg.getEventCmd().equals(EventCmd.UPDATE_MAIN_PROFILE_VIEW))
        {
            updateUi();
        }
    }
    /**
     * register eventbus msg
     */
    @Override
    public void onResume() {
        super.onResume();
        AsusLog.i("smile", "profile onresume ");
        EventBus.getDefault().register(this);
    }

    /**
     * unresgiter eventbus msg
     */
    @Override
    public void onPause() {
        super.onPause();
        AsusLog.i("smile", "profile onpause ");
        EventBus.getDefault().unregister(this);
    }
    /**
     * according to isprofileset in db , determine which view to show
     */
    private void updateUi()
    {
        ViewBase vb = null;
        boolean needshow = false;
       if( !mController.getmProfileModel().getIsProfileSet())
       {
           vb = (ViewBase) getChildFragmentManager().findFragmentByTag(RealSetupProfileFragment.Tag);
           if(vb == null)
           {
              // vb = new RealSetupProfileFragment(mController);
           }
           else{
               needshow =true;
           }

       }
        else
       {
           vb = (ViewBase) getChildFragmentManager().findFragmentByTag(ProfileReviewFragment.Tag);
           if(vb == null)
           {
               vb = new ProfileReviewFragment();
               Bundle bd = new Bundle();
               bd.putBoolean(ProfileReviewFragment.BundleKey,true);
               vb.setArguments(bd);
           }
           else
           {
               needshow =true;
           }
       }
        if(needshow)
        {
            getChildFragmentManager().beginTransaction().show(vb).commit();

        }
        else {
            getChildFragmentManager().beginTransaction().replace(R.id.setup_profile_container, vb, vb.getMyTag()).commit();
        }
   //     getChildFragmentManager().beginTransaction().addToBackStack(vb.getMyTag()).commit();
    }
    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pni_main_fragment_setup_profile, container, false);
    //    mContainer = (FrameLayout)v.findViewById(R.id.setup_profile_container);
        updateUi();
        return v;
    }
}

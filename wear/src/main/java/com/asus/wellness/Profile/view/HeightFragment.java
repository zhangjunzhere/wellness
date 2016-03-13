package com.asus.wellness.Profile.view;

import android.util.Log;

import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.Profile.EventCmd;
import com.asus.wellness.Profile.ProfileEvent;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/5/19.
 */
public class HeightFragment extends ViewBase {
    HashMap<Integer,ViewBase> fragmentmap = new HashMap<>();


    /**
     * recevie height unit change msg
     * @param msg EventCmd.UPDATE_HEIGHT_VIEW
     */
    public void onEvent(ProfileEvent msg)
    {
        if(msg.getEventCmd().equals(EventCmd.UPDATE_HEIGHT_VIEW))
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
       // AsusLog.i("smile", "onresume ");
        EventBus.getDefault().register(this);
    }

    /**
     * unresgiter eventbus msg
     */
    @Override
    public void onPause() {
        super.onPause();
       // AsusLog.i("smile", "onpause ");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_height;
    }

    @Override
    public void updateUi() {
        AsusLog.i("smile","HeightView updateUI");
        ViewBase vb = null;
        Boolean needreplace =false;
        if(mProfileController.getmProfileModel().getHeightUnit()==ProfileTable.HEIGHT_UNIT_CM)
        {
            vb = (ViewBase)getChildFragmentManager().findFragmentByTag(HeightCmFragment.Tag);
            if(vb == null)
            {
                needreplace = true;
                vb = new HeightCmFragment();
            }
        }
        else
        {
            vb = (ViewBase)getChildFragmentManager().findFragmentByTag(HeightFtFragment.Tag);
            if(vb == null)
            {
                needreplace = true;
                vb = new HeightFtFragment();
            }
          //  vb = new HeightFtFragment(mProfileController);
        }

        if(needreplace)
        {
            Log.i("smile", "Fragment height null");
            getChildFragmentManager().beginTransaction().replace(R.id.height_container, vb, vb.getMyTag()).commit();

        }
        else
        {
            Log.i("smile","Fragment height show");
            getChildFragmentManager().beginTransaction().show(vb).commit();
        }

//        android.support.v4.app.FragmentTransaction transaction = mProfileController.getFragmentManager().beginTransaction();
//        if(vb.isAdded())
//        {
//            mProfileController.getFragmentManager().beginTransaction().remove(vb);
//        }
//       transaction.replace(R.id.container, vb);
//        transaction.commit();
    }
}

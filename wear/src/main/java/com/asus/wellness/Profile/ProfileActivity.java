package com.asus.wellness.Profile;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.system.SystemModel;

/**
 * Created by smile_gao on 2015/5/15.
 */
public class ProfileActivity extends FragmentActivity implements IController {
    ProfileController mController = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.profile);

//        int pageindex = 0;
//        if(savedInstanceState != null)
//        {
//            savedInstanceState.getInt("index",0);
//        }
        mController = new ProfileController(this);
        //smile_gao add for first use
        if(SystemModel.getIntance().isFirstUse())
        {
            SystemModel.getIntance().setFirstUse(false);
            mController.setFirstUse(true);
        }
        //end smile
        mController.init(true);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("smile","ProfileActivity onDestory");
    }

    public ProfileController getController()
    {
        return mController;
    }
}

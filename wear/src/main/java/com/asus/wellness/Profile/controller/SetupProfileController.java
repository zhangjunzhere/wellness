package com.asus.wellness.Profile.controller;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.asus.wellness.Profile.ProfileActivity;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

/**
 * Created by smile_gao on 2015/5/22.
 */
public class SetupProfileController extends  ProfileController {
    public SetupProfileController(FragmentActivity context) {
        super(context);
    }

    /**
     *  click event
     * @param view, event sender
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.setupbtn:
                goProfileActivity();
                mContext.finish();
                break;
            case R.id.editprofilebtn:
                goProfileActivity();
                mContext.finish();
                break;

            //end heightunit view
        }
    }
    void goProfileActivity()
    {
        startActivity(ProfileActivity.class);
    }

    @Override
    /**
     * update current Fragment ui
     */
    public void updateUi() {
        AsusLog.i("smile","SetupProfileController updateui");
    }
}

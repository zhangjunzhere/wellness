package com.asus.wellness.Profile;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.controller.SetupProfileController;
import com.asus.wellness.Profile.view.ProfileReviewFragment;
import com.asus.wellness.R;

public class ProfileEditActivity extends FragmentActivity implements IController {
    SetupProfileController mController;
    FrameLayout rootView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        mController  = new SetupProfileController(this);
        mController.init(false);
        ProfileReviewFragment profileReviewFragment = new ProfileReviewFragment();
        Bundle bd = new Bundle();
        bd.putBoolean(ProfileReviewFragment.BundleKey,true);
        profileReviewFragment.setArguments(bd);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,profileReviewFragment).commit();
    }

    @Override
    public ProfileController getController() {
        return mController;
    }
}

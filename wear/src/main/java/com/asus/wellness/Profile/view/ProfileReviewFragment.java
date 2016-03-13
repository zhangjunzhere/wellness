package com.asus.wellness.Profile.view;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.fragment.DrawerUserHeadImageView;
import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.Profile.EventCmd;
import com.asus.wellness.Profile.ProfileEvent;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/5/21.
 */
public class ProfileReviewFragment extends  ViewBase {
    TextView name ;
    TextView gender ;
    TextView age;
    TextView height;
    TextView weight;
    TextView stepsgoal;
    TextView lifestyle;
    TextView distanunit;
    TextView saveprofile;
    TextView editprofile;
  //  DrawerUserHeadImageView head;
    boolean mEditProfile =false;
    public final static String  Tag = "ProfileReviewFragment";
    public final static String BundleKey= "editable";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bd =  getArguments();
        if(bd!=null)
        {
            mEditProfile = bd.getBoolean(ProfileReviewFragment.BundleKey,false);
            Log.i("smile","ProfileReviewFragment mEditProfile "+mEditProfile);
        }
        else {
            Log.i("smile", "ProfileReviewFragment bd null ");
        }
    }
    //
//    public ProfileReviewFragment(boolean editprofile) {
//        super();
//        mEditProfile = editprofile;
//    }

    @Override
    public String getMyTag() {
        return Tag;
    }

    /**
     * receive msg to update ui
     * @param msg   UPDATE_PROFILE_VIEW
     */
    public void onEvent(ProfileEvent msg)
    {
       // AsusLog.i("smile", "onevent " + msg);
        if(msg.getEventCmd().equals(EventCmd.UPDATE_PROFILE_VIEW))
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
       // AsusLog.i("smile", "profile onresume ");
        EventBus.getDefault().register(this);
    }

    /**
     * unresgiter eventbus msg
     */
    @Override
    public void onPause() {
        super.onPause();
       // AsusLog.i("smile", "profile onpause ");
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        name = (TextView) view.findViewById(R.id.name);
        gender = (TextView) view.findViewById(R.id.gender);
        age = (TextView) view.findViewById(R.id.age);
        height = (TextView) view.findViewById(R.id.height);
        weight = (TextView) view.findViewById(R.id.weight);
        stepsgoal = (TextView) view.findViewById(R.id.stepgoal);
        lifestyle = (TextView) view.findViewById(R.id.lifestyle);
        distanunit = (TextView) view.findViewById(R.id.distanceunit);
        saveprofile = (TextView) view.findViewById(R.id.saveprofilebtn);
        saveprofile.setOnClickListener(mProfileController);
        int size = getResources().getDimensionPixelSize(R.dimen.btn_select_font_size);
        Utility.fitFontSizeForView(saveprofile, size, Utility.getScreenWidth(getActivity()));
        editprofile = (TextView) view.findViewById(R.id.editprofilebtn);
        editprofile.setOnClickListener(mProfileController);
        Utility.fitFontSizeForView(editprofile, size, Utility.getScreenWidth(getActivity()));


        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView titleLine = (ImageView) view.findViewById(R.id.profile_title_line);
        if(mEditProfile)
        {
            saveprofile.setVisibility(View.GONE);
            editprofile.setVisibility(View.VISIBLE);
            title.setVisibility(View.GONE);
            titleLine.setVisibility(View.GONE);
            name.setVisibility(View.VISIBLE);
            Profile p =  ProfileHelper.getStandardProfile();
            name.setText(p.getName());
        }
        else
        {
            saveprofile.setVisibility(View.VISIBLE);
            editprofile.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
            titleLine.setVisibility(View.VISIBLE);
            name.setVisibility(View.GONE);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_profile_review;
    }

    @Override
    public void updateUi() {
        if(mProfileController == null)
        {
            Log.i("smile","profile null");
        }
        ProfileModel pm = mProfileController.getmProfileModel();

        gender.setText(getAgeStr(pm));
        age.setText(String.valueOf(pm.getAge()));

        height.setText(getHeightStr(pm));

        weight.setText(getWeightStr(pm));

        stepsgoal.setText(getSteps(pm));

        lifestyle.setText(getLiftStyle(pm));

        distanunit.setText(getDistanceUnit(pm));


    }

    String getDistanceUnit(ProfileModel pm)
    {
       int resid = pm.getDistanceUnit()== ProfileTable.DISTANTCE_UNIT_KM? R.string.distance_unit : R.string.miles;
        return  getResStr(R.string.distanceunit)+" (" +getResStr(resid).toLowerCase()+")";
    }
    String getLiftStyle(ProfileModel pm)
    {
        int resid =R.string.sedentaryliftstyle;
        switch (pm.getStepgoalTargetIndex())
        {
            case 1:  resid = R.string.lowactive; break;
            case 2:  resid = R.string.somewhatactive; break;
            case 3:  resid = R.string.active; break;
            case 4: resid = R.string.highlyactive; break;
            default:
                resid =R.string.sedentaryliftstyle;
                break;
        }

        return getResStr(resid);
    }
    String getSteps(ProfileModel pm)
    {
        long steps = pm.getStepgoal();
//        int steps = 5000;
//        switch (pm.getStepgoalTargetIndex())
//        {
//            case 1:  steps = 7500; break;
//            case 2:  steps = 10000; break;
//            case 3:  steps = 12500; break;
//            case 4: steps = 13000; break;
//            default:
//                    steps=5000;
//                break;
//        }

        String stepstr = steps<1000 ? String.valueOf(steps) : new java.text.DecimalFormat("0,000").format(steps);
        return stepstr;
    }
    String getAgeStr(ProfileModel pm )
    {
        String genderstr = pm.getGender()== ProfileTable.MALE?getResStr(R.string.male) : getResStr(R.string.female);
        return  genderstr;
    }
    String getHeightStr(ProfileModel pm)
    {
        String heightstr = String.valueOf((int) Math.round(pm.getHeight())) +" "+ getResources().getString(R.string.cm).toLowerCase();
        if(pm.getHeightUnit()==ProfileTable.HEIGHT_UNIT_FT)
        {
            float height = Utility.InchToFt(pm.getHeight());
            int ft_pos = (int)height;
            int in_pos = (int)(Math.round((height - ft_pos)*12));
            heightstr = ft_pos+"'"+in_pos+"\"";
        }
        return  heightstr;
    }
    String getWeightStr(ProfileModel pm)
    {
        String weightstr = pm.getWeight()+" "+getResStr(R.string.kg).toLowerCase();;
        if(pm.getWeightUnit()==ProfileTable.WEIGHT_UNIT_LBS)
        {
            weightstr = pm.getWeight()+" "+getResStr(R.string.lbs).toLowerCase();
        }
        return  weightstr;
    }
    private String getResStr(int id)
    {
        return  getResources().getString(id);
    }
}

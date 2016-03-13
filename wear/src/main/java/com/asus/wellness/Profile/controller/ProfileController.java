package com.asus.wellness.Profile.controller;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.Profile.EventCmd;
import com.asus.wellness.Profile.ProfileActivity;
import com.asus.wellness.Profile.ProfileEvent;
import com.asus.wellness.Profile.ProfileSwipeAdapter;
import com.asus.wellness.Profile.SkipProfileDialog;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.Profile.view.ProfileView;
import com.asus.wellness.Profile.view.ViewBase;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;
import com.asus.wellness.utils.Utility;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/5/14.
 */
public class ProfileController implements View.OnClickListener {
    ProfileModel mProfileModel = null;
    ProfileSwipeAdapter mAdapter = null;
    ProfileView mProfileView=null;
    FragmentManager mFragmentManager;
    FragmentActivity mContext;
    DataLayerManager dataLayerManager;
    boolean isFirstUse=false;
    public ProfileController(FragmentActivity context) {
        mContext = context;
        connectToPlayService();
    }
    public void setFirstUse(boolean first)
    {
        isFirstUse = first;
    }
    public boolean getFirstUse()
    {
        return isFirstUse;
    }

    public void init(boolean bindview)
    {

        mProfileModel = new ProfileModel();
        mProfileModel.reset();
        mFragmentManager = mContext.getSupportFragmentManager();
        if(bindview)
            bindViewPage(mContext);
    }
    private void connectToPlayService()
    {
//        if(dataLayerManager == null) {
            Log.d("smile", "dataLayerManager null ");
            dataLayerManager =  DataLayerManager.getInstance(mContext);
            dataLayerManager.connectGoogleApiClient(null);
//        }
//        else if(!dataLayerManager.isConnected())
//        {
//            Log.d("smile", "dataLayerManager not null ,but not connected");
//            dataLayerManager.connectGoogleApiClient(null);
//        }
//        else
//        {
//            Log.d("smile", "dataLayerManager not null,Connected");
//        }
    }
    /**
     * get FragmentManager
     *
     * @return
     */
    public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }

    /**
     * bind view page, set adapter
     *
     * @param context activity context ,for get view
     */
    private void bindViewPage(FragmentActivity context) {

        mProfileView = new ProfileView(context);
        context.setContentView(mProfileView);
        mAdapter = new ProfileSwipeAdapter(mFragmentManager, this);
        mProfileView.setAdapter(mAdapter);



    }

    /**
     * get Pofile Model, which attach to db Profile
     * @return ProfileModel
     */
    public ProfileModel getmProfileModel() {
        return mProfileModel;
    }

    /**
     * update current Fragment ui
     */
   public void updateUi() {

//       ViewBase vb1 =(ViewBase)mAdapter.getItem(i);
//        if(vb1!=null)
//        {
//            vb1.updateUi();
//        }
       int index = mProfileView.getCurrentPageIndex();
        Fragment fragment =
                mFragmentManager.findFragmentByTag(
                        "android:switcher:" + R.id.profilepager + ":" + index);
        if (fragment != null) {
            AsusLog.i("smile", "fragment not null " + fragment.toString());
            ViewBase vb = (ViewBase) fragment;
            vb.updateUi();
        }

    }

    /**
     *  click event
     * @param view, event sender
     */
    @Override
    public void onClick(View view) {
        boolean gonextpage = false;
        switch (view.getId()) {
//            case R.id.male:
//                setGender(ProfileTable.MALE);
//                gonextpage = true;
//                break;
//            case R.id.female:
//                setGender(ProfileTable.FEMALE);
//                gonextpage = true;
//                break;
//            case R.id.skip:
//                goSkipActivity();
//                break;
            //heightunit view
//            case R.id.cm:
//                setHeightUnit(ProfileTable.HEIGHT_UNIT_CM);
//                gonextpage = true;
//                break;
//            case R.id.ftin:
//                setHeightUnit(ProfileTable.HEIGHT_UNIT_FT);
//                gonextpage = true;
//                break;
//            case R.id.kgbtn:
//                setWeightUnit(ProfileTable.WEIGHT_UNIT_KG);
//                gonextpage = true;
//                break;
//            case R.id.lbsbtn:
//                setWeightUnit(ProfileTable.WEIGHT_UNIT_LBS);
//                gonextpage = true;
//                break;
//            case R.id.goal1:
//            case R.id.goal2:
//            case R.id.goal3:
//            case R.id.goal4:
 //           case R.id.goal5:
//                onGoalChange(view);
//                gonextpage = true;
//                break;
//            case R.id.kmbtn:
//                setDistanceUnit(ProfileTable.DISTANTCE_UNIT_KM);
//                gonextpage = true;
//                break;
//            case R.id.milesbtn:
//                setDistanceUnit(ProfileTable.DISTANTCE_UNIT_MILES);
//                gonextpage = true;
//                break;
            case R.id.saveprofilebtn:
                saveProfile();
                break;


            //end heightunit view
        }
        updateUi();
        notifyProfileReviewChange();
        if(gonextpage)
        {
            goNextPage();
        }
    }

    private void updateGenderView() {
        mAdapter.resetAdaptercount();
        mAdapter.notifyDataSetChanged();
        EventBus.getDefault().post(new ProfileEvent(EventCmd.UPDATE_AGE_SKIP_VIEW, "gender"));
    }

    public void goNextPage()
    {
        Log.i("smile", "goNextPage");
        mProfileView.scrollToNextPage();
    }

    /**
     *  when ui update ,last Fragment profile need change
     */
    void notifyProfileReviewChange() {
        EventBus.getDefault().post(new ProfileEvent(EventCmd.UPDATE_PROFILE_VIEW));
    }

    /**
     * lunch profile activity
     */
    void goProfileActivity()
    {
        startActivity(ProfileActivity.class);
    }
    /**
     * launch skip activity
     */
   public void goSkipActivity() {
       SkipProfileDialog skipProfileActivity = new SkipProfileDialog();
       skipProfileActivity.setFirstUse(isFirstUse);
       skipProfileActivity.show(mContext.getFragmentManager(), "SkipProfileActivity");
//        startActivity(SkipProfileActivity.class);
//        mAdapter.setCustomAdaptercount(2);
//        mAdapter.notifyDataSetChanged();
//        EventBus.getDefault().post(new ProfileEvent(EventCmd.UPDATE_AGE_SKIP_VIEW, "skip"));
    }

    /**
     * launch skip main activity
     */
    void saveProfile() {
        mProfileModel.setIsProfileSet(true);
        mProfileModel.update();
        Log.i("smile","dis unit:"+mProfileModel.getDistanceUnit());
        mContext.finish();
        EventBus.getDefault().post(new ProfileEvent(EventCmd.UPDATE_MAIN_PROFILE_VIEW));
        dataLayerManager.sendProfileToPhone();
     //   Intent intent = new Intent(this, WellnessMicroAppMain.class);

     //   startActivity(WellnessMicroAppMain.class);
    }

    /**
     * luanch activity
     * @param c  , des Activity to launch
     */
    void startActivity(Class c) {
        Intent intent = new Intent(mContext, c);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }

    /**
     * set distance unit
     * @param unit , KM or Miles
     */
    void setDistanceUnit(int unit) {
        mProfileModel.setDistanceUnit(unit);
      //  mProfileModel.update();
    }

    /**
     * set goal, by view tag
     * @param v, goal item v
     */
    public  void onGoalChange(int selectIndex) {
//        String tag = v.getTag().toString();
//        AsusLog.i("smile", "tag: " + tag);
//        int selectIndex = 0;
//        try {
//            selectIndex = Integer.parseInt(tag);
//        } catch (Exception e) {
//
//        }
        int stepgoal= 5000;
        switch (selectIndex)
        {
            case 1: stepgoal =7500; break;
            case 2: stepgoal = 10000; break;
            case 3: stepgoal = 12500; break;
            case 4: stepgoal = 15000; break;
            default: stepgoal = 5000; break;
        }
        mProfileModel.setStepgoal(stepgoal);
        notifyProfileReviewChange();
    }

    /**
     * set weight unit
     * @param weightUnit,  LBs or Kg
     */
    void setWeightUnit(int weightUnit) {
        if(weightUnit != mProfileModel.getWeightUnit())
        {
            if(mProfileModel.getWeightUnit() == ProfileTable.WEIGHT_UNIT_KG){
                float weight = Utility.kgToLbs(mProfileModel.getWeight());
                Log.i("smile","weight kg: "+weight);
                mProfileModel.setWeight(Math.round(weight));
            }
            else if(mProfileModel.getWeightUnit() == ProfileTable.WEIGHT_UNIT_LBS){
                float weight = Utility.LbsToKg(mProfileModel.getWeight());
                Log.i("smile","weight lbs: "+weight);
                mProfileModel.setWeight(Math.round(weight));
            }
        }
        mProfileModel.setWeightunit(weightUnit);
       // mProfileModel.update();
    }

    /**
     * set height unit
     * @param heightUnit cm  or 1'1"
     */
    void setHeightUnit(int heightUnit) {
        if(heightUnit!= mProfileModel.getHeightUnit()) {
            if (mProfileModel.getHeightUnit() == ProfileTable.HEIGHT_UNIT_CM) {
                //convert to ft
                float height = Utility.cmToFt(mProfileModel.getHeight());
                mProfileModel.setHeight(Math.round(Utility.ftToInch(height)));


            }
            else if (mProfileModel.getHeightUnit() == ProfileTable.HEIGHT_UNIT_FT)
            {
                //convert to cm
                float heighcm = Utility.ftToCm(Utility.InchToFt(mProfileModel.getHeight()));
                mProfileModel.setHeight(Math.round(heighcm));
            }
        }
        mProfileModel.setHeightunit(heightUnit);
      //  mProfileModel.update();
    }

    /**
     * set weight
     * @param weight
     */
    public void onWeightChange(int weight) {
        if (Math.abs(weight - mProfileModel.getWeight()) < 0.01) {
            //  AsusLog.i("smile","same age");
            return;
        }
        AsusLog.i("smile", "set  weight " + weight);
        mProfileModel.setWeight(weight);
        notifyProfileReviewChange();
       // mProfileModel.update();
    }

    /**
     * set height
     * @param height
     */
    public void onHeightChange(int height) {
        if (Math.abs(height - mProfileModel.getHeight()) < 0.01) {
            //  AsusLog.i("smile","same age");
            return;
        }
        AsusLog.i("smile", "set  height " + height);
        mProfileModel.setHeight(height);
        notifyProfileReviewChange();
      //  mProfileModel.update();
    }

    /**
     * set age
     * @param age
     */
    public void onAgeChange(int age) {
        if (age == mProfileModel.getAge()) {
            //  AsusLog.i("smile","same age");
            return;
        }
        AsusLog.i("smile", "set  age " + age);
        mProfileModel.setAge(age);
        notifyProfileReviewChange();
       // mProfileModel.update();
    }
    /**
     * set height unit
     * @param gender
     */
    public void onWeightUnitChange(int unit) {
        if (unit == mProfileModel.getWeightUnit()) {
            //  AsusLog.i("smile","same age");
            return;
        }
        AsusLog.i("smile", "set  w unit " + unit);
       setWeightUnit(unit);
        updateUi();
        notifyProfileReviewChange();
        // mProfileModel.update();
    }
    /**
     * set height unit
     * @param gender
     */
    public void onDistanceUnitChange(int unit) {
        if (unit == mProfileModel.getDistanceUnit()) {
            //  AsusLog.i("smile","same age");
            return;
        }
        AsusLog.i("smile", "set dis  unit " + unit);
        setDistanceUnit(unit);
        notifyProfileReviewChange();
        // mProfileModel.update();
    }
    /**
     * set height unit
     * @param gender
     */
    public void onHeightUnitChange(int unit) {
        if (unit == mProfileModel.getHeightUnit()) {
            //  AsusLog.i("smile","same age");
            return;
        }
        AsusLog.i("smile", "set  unit " + unit);
        setHeightUnit(unit);
        updateUi();
        notifyProfileReviewChange();
        // mProfileModel.update();
    }
    /**
     * set age
     * @param gender
     */
    public void onGenderChange(int gender) {
        if (gender == mProfileModel.getGender()) {
            //  AsusLog.i("smile","same age");
            return;
        }
        if(gender>1)
        {
            mAdapter.setCustomAdaptercount(1);
            mProfileView.setViewPagerScroll(false);
        }
        else
        {
            mProfileView.setViewPagerScroll(true);
            mAdapter.resetAdaptercount();
        }
        mAdapter.notifyDataSetChanged();
        AsusLog.i("smile", "set  gender " + gender);
        mProfileModel.setGender(gender);
        notifyProfileReviewChange();
        // mProfileModel.update();
    }
    /**
     * set gender
     * @param gender  male or female
     */
    void setGender(int gender) {
        mProfileModel.setGender(gender);
       // mProfileModel.update();
    }
//
//    void setGenderFemale() {
//        mProfileModel.setGender("female");
//        mProfileModel.update();
//    }

}

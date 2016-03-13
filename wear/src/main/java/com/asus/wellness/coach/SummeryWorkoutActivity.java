package com.asus.wellness.coach;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.utils.Utility;
import java.util.ArrayList;
import java.util.List;

public class SummeryWorkoutActivity extends WearableActivity {

    CoachDataModel mCoachDataModel;
    private  List<SummeryItem> mSummeryItems;
    ScrollView mRootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pni_coach_activity_workout_summery);
        mRootView = (ScrollView)findViewById(R.id.ll_rootView);
        mCoachDataModel = ((WApplication)getApplication()).getCoachDataModel();
        //EBCommandUtils.showCoachNotification(this.getClass().getName(), false);
        stopService(new Intent(this, WorkoutDataService.class));

        setAmbientEnabled();

        List<SummeryItem> summeryItems  = calculateSummery();
        showSummeryUI(summeryItems);
    }

    @Override
    public void onResume(){
        super.onResume();
        setLayoutGravity();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCoachDataModel.resetData();
    }


    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        mRootView.setBackground(null);
        findViewById(R.id.tv_clock).setVisibility(View.VISIBLE);
        mRootView.scrollTo(0, 0);
        setLayoutGravity();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        findViewById(R.id.ll_rootView).setBackgroundResource(R.drawable.pni_background_gradient);
        findViewById(R.id.tv_clock).setVisibility(View.GONE);
        setLayoutGravity();

//        showSummeryUI(mSummeryItems);
    }


    private class SummeryItem{
        public int iconId;
        public String title;
        public String value;
        public String unit;

        public SummeryItem(int iconId,int titleId,String value,String unit){
            this.iconId = iconId;
            this.title = getString(titleId);
            this.value = value;
            this.unit = unit;
        }
    }

    public List<SummeryItem> calculateSummery(){
        List<SummeryItem> summeryItems = new ArrayList<SummeryItem>();

        summeryItems.add(
                new SummeryItem(
                        R.drawable.asus_wellness_ic_target,
                        R.string.title_workout_target_value,
                        mCoachDataModel.getTargetString(this),
                        mCoachDataModel.getTargetUnitString(this)
                )
        );

        SummeryItem coachItem =   new SummeryItem(
                R.drawable.pni_asus_wellness_ic_distance,
                R.string.total_quantity,
                String.valueOf(mCoachDataModel.getTotalQuantity()),
                ""
        );
        switch (mCoachDataModel.getType()){
            case RUN:
                coachItem =   new SummeryItem(
                        R.drawable.pni_asus_wellness_ic_distance,
                        R.string.total_distance,
                        mCoachDataModel.getDistanceValueString(),
                        mCoachDataModel.getUnit() == CoachDataModel.eUnit.KM ? getString(R.string.distance_unit) : getString(R.string.miles)
                );
                break;
            case PUSHUP:
                coachItem.iconId = R.drawable.asus_wellness_ic_pushup;
                break;
            case SITUP:
                coachItem.iconId = R.drawable.asus_wellness_ic_situp;
                break;
            default:
                break;
        }

        summeryItems.add(coachItem);
        summeryItems.add(
                new SummeryItem(
                        R.drawable.pni_asus_wellness_ic_time,
                        R.string.total_time,
                        Utility.formatTime(mCoachDataModel.getTotalTime()),
                        ""
                )
        );
        summeryItems.add(
                new SummeryItem(
                        R.drawable.pni_asus_wellness_ic_calories,
                        R.string.calories_burned,
                        String.valueOf(mCoachDataModel.getTotalCalories()) ,
                        getString(R.string.calories_unit)
                )
        );

        return summeryItems;
    }

    public void showSummeryUI(List<SummeryItem> summeryItems ){
        ViewGroup rl_calories = (ViewGroup) findViewById(R.id.rl_calories);


        int[] layoutIds = new int[]{
                R.id.rl_target,
                R.id.rl_distance,
                R.id.rl_time,
                R.id.rl_calories
        };

        for(int i = 0; i < layoutIds.length; i++ ){
            ViewGroup layout = (ViewGroup) findViewById(layoutIds[i]);
            ImageView iv_icon = (ImageView) layout.findViewById(R.id.iv_icon);
            TextView tv_name = (TextView) layout.findViewById(R.id.tv_name);
            TextView tv_value = (TextView) layout.findViewById(R.id.tv_value);
            TextView tv_unit = (TextView) layout.findViewById(R.id.tv_unit);

            SummeryItem item = summeryItems.get(i);
            iv_icon.setImageResource(item.iconId);
            tv_name.setText(item.title);
            tv_value.setText(item.value);
            tv_unit.setText(item.unit);
        }

        switch(mCoachDataModel.getType() ){
            case RUN:
                rl_calories.setVisibility(View.VISIBLE);
                break;
            case PUSHUP:
            case SITUP:
                rl_calories.setVisibility(View.GONE);
                break;
        }
    }

    //add by emily
    private void setLayoutGravity(){
        View view = findViewById(R.id.coach_activity_workout_summery_view);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        if(view.getMeasuredHeight() < Utility.getScreenHeight(this)){
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
        }
        else {
            layoutParams.gravity = Gravity.TOP;
        }
        view.setLayoutParams(layoutParams);
    }
}

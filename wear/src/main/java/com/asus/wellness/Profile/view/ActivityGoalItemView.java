package com.asus.wellness.Profile.view;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.wellness.R;

/**
 * Created by smile_gao on 2015/5/21.
 */
public class ActivityGoalItemView extends LinearLayout implements WearableListView.OnCenterProximityListener {

    TextView goal =null;
    TextView step = null;
    TextView describtion=null;
    int bigfontlength = 0;
    public ActivityGoalItemView(Context context) {
        super(context);
        init(context);
    }

    public ActivityGoalItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    void init(Context context)
    {
        LayoutInflater.from(context).inflate(R.layout.pni_profile_activity_goal_list_item, this);
        goal = (TextView) findViewById(R.id.goal);
        step = (TextView) findViewById(R.id.goalstep);
        describtion = (TextView) findViewById(R.id.describe);
       // getBigsizeTextleght(context);
    }
    void getBigsizeTextleght(Context context)
    {
        goal.setText(context.getResources().getString(R.string.k1012));
        goal.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.goal_big_list_item_size));
        goal.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        bigfontlength = goal.getMeasuredWidth();
        Log.i("smile","bigfontlength: "+bigfontlength);
    }

    /**
     * set selected ui
     */
    public void select()
    {
        if(goal!=null)
        {
            goal.setTextColor(getResources().getColor(R.color.activitygoalselectcolor));
            goal.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.goal_big_list_item_size));
        }
        if(step !=null)
        {
            step.setTextColor(getResources().getColor(R.color.activitygoalselectcolor));
        }
        if(describtion !=null)
        {
            describtion.setTextColor(getResources().getColor(R.color.activitygoalselectcolor));
            goal.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.describe_big_list_item_size));
        }
    }

    /**
     * set unselect ui
     */
    public  void unSelect()
    {
        if(goal!=null)
        {
            goal.setTextColor(getResources().getColor(R.color.white));
            goal.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.goal_small_list_item_size));
        }
        if(step !=null)
        {
            step.setTextColor(getResources().getColor(R.color.white));
        }
        if(describtion !=null)
        {
            describtion.setTextColor(getResources().getColor(R.color.white));
            goal.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.describe_small_list_item_size));
        }
    }

    /**
     * set content and goal
     * @param goalresid   goal res id ,0-5k,5k-7.5k ...
     * @param describereresid  describe content
     */
    public  void setContent(int goalresid, int describereresid)
    {
        if(goal!=null)
            goal.setText(getResources().getString(goalresid));
        if(describtion !=null)
            describtion.setText(getResources().getString(describereresid));
    }


    @Override
    public void onCenterPosition(boolean b) {
        select();
    }

    @Override
    public void onNonCenterPosition(boolean b) {
      unSelect();
    }
}

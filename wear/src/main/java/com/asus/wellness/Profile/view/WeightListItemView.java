package com.asus.wellness.Profile.view;

import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.WearableListView;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.wellness.Profile.EventCmd;
import com.asus.wellness.Profile.ProfileEvent;
import com.asus.wellness.R;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/5/26.
 */
public class WeightListItemView extends LinearLayout implements WearableListView.OnCenterProximityListener {
     TextView txtView;
 //    TextView dotView;
    private  float mSmallSize;
    private  float mBigSize;
    private final int mSelectColor;
    public WeightListItemView(Context context)
    {
        super(context);
        mSelectColor = getResources().getColor(R.color.activitygoalselectcolor);
        init(context,R.dimen.age_small_list_item_size , R.dimen.age_big_list_item_size);
//        View.inflate(context, R.layout.profile_listviewitem, this);
//        txtView = (TextView)findViewById(R.id.name);
//        dotView  = (TextView)findViewById(R.id.weightdot);
//        mSmallSize = getResources().getDimensionPixelSize(R.dimen.age_small_list_item_size);
//        mBigSize = getResources().getDimensionPixelSize(R.dimen.age_big_list_item_size);
        // AsusLog.LogI("smile",mSmallSize+" "+mBigSize);
    }

    public WeightListItemView(Context context,int smallsizeres, int bigsizeres)
    {
        super(context);
        mSelectColor = getResources().getColor(R.color.activitygoalselectcolor);
        init(context,smallsizeres,bigsizeres);
        // AsusLog.LogI("smile",mSmallSize+" "+mBigSize);
    }
    void init(Context context,int smallsize, int bigsize)
    {
        this.setOrientation(HORIZONTAL);
       View v= View.inflate(context, R.layout.pni_profile_weight_listviewitem, this);
        txtView = (TextView)findViewById(R.id.name);
    //    dotView  = (TextView)findViewById(R.id.weightdot);
        mSmallSize =getResources().getDimensionPixelSize(smallsize);
        mBigSize = getResources().getDimensionPixelSize(bigsize);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(lp);
    //    this.addView(v,lp);
        this.requestLayout();
    }


    @Override
    public void onCenterPosition(boolean b) {

        txtView.setTextColor(mSelectColor);
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mBigSize);
    //    dotView.setVisibility(View.VISIBLE);
        invalidateUi();
        //   txtView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

    }

    @Override
    public void onNonCenterPosition(boolean b) {
       // AsusLog.i("smile", String.valueOf(b));
        txtView.setTextColor(Color.WHITE);
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallSize);
     //   dotView.setVisibility(View.INVISIBLE);

      //  invalidateUi();
        //   txtView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
    }
    static int  itemlength = 0;
    void invalidateUi()
    {
        CharSequence cs = txtView.getText();
//       // LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//        int width = 70;
       if(cs.length()!=itemlength)
       {
           itemlength = cs.length();
           EventBus.getDefault().post(new ProfileEvent(EventCmd.UPDATE_PROFILE_WEIGHT_ITEM_VIEW,String.valueOf(itemlength)));
       }
//        if(num>99)
//        {
//            width = 110;
//        }
//        else if(num<10)
//        {
//            width = 35;
//        }
//
//        txtView.setMinWidth(width);
//        txtView.setHeight(82);
//        this.requestLayout();
//        this.invalidate();
    }
}

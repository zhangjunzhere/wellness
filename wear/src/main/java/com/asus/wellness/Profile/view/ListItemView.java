package com.asus.wellness.Profile.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.WearableListView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2015/5/20.
 */
public class ListItemView  extends LinearLayout implements WearableListView.OnCenterProximityListener {
    final TextView txtView;
    private final float mSmallSize;
    private final float mBigSize;
    private final int mSelectColor;
    private final boolean mMeasure;
    private final int mGravity;
    int textWidth;
    public ListItemView(Context context)
    {
        super(context);
        mMeasure= true;
        View.inflate(context, R.layout.pni_profile_listviewitem, this);
        txtView = (TextView)findViewById(R.id.name);
        mSmallSize = getResources().getDimensionPixelSize(R.dimen.age_small_list_item_size);
        mBigSize = getResources().getDimensionPixelSize(R.dimen.age_big_list_item_size);
        mSelectColor = getResources().getColor(R.color.activitygoalselectcolor);
        // AsusLog.LogI("smile",mSmallSize+" "+mBigSize);
        mGravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        textWidth = Utility.getScreenWidth((Activity)getContext());
        //int width = Utility.getScreenWidth();
    }

    public ListItemView(Context context,int smallsizeres, int bigsizeres, boolean measure,int gravity)
    {
        super(context);
        mMeasure = measure;
        View.inflate(context, R.layout.pni_profile_listviewitem, this);
        txtView = (TextView)findViewById(R.id.name);
        mGravity = gravity;
        txtView.setGravity(gravity);;
        mSmallSize =getResources().getDimensionPixelSize(smallsizeres);
        mBigSize = getResources().getDimensionPixelSize(bigsizeres);
        mSelectColor = getResources().getColor(R.color.activitygoalselectcolor);
        textWidth = Utility.getScreenWidth((Activity) getContext());
        // AsusLog.LogI("smile",mSmallSize+" "+mBigSize);
    }

    @Override
    public void onCenterPosition(boolean b) {
        txtView.setTextColor(mSelectColor);
     //   txtView.setGravity(Gravity.CENTER);
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mBigSize);
        // mTitleTv.setText(title);  //getResources().getString(R.string.heightunit)

        if(mGravity != Gravity.CENTER)
        {
            txtView.setPadding(0, 0, 0, 0);
        }
        if(mMeasure)
          txtView.measure(MeasureSpec.UNSPECIFIED,MeasureSpec.UNSPECIFIED);

        if(txtView.getMeasuredWidth()>0 && txtView.getMeasuredWidth()*1.1 > textWidth)
        {
            Log.i("smile","txtView width "+txtView.getMeasuredWidth()+ "> screen width: "+txtView.getMeasuredWidth()+"  "+txtView.getText());
            Utility.adjustTextSize(txtView, mBigSize, (int)(textWidth*0.1));
        }
     //   txtView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

    }

    @Override
    public void onNonCenterPosition(boolean b) {
        txtView.setTextColor(Color.WHITE);
        txtView.setGravity(mGravity);
        if(mGravity != Gravity.CENTER)
        {
            txtView.setPadding(10, 0, 0, 0);
        }
        txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mSmallSize);
     //   txtView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
    }
}

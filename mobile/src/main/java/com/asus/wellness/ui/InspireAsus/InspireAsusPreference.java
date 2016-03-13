package com.asus.wellness.ui.InspireAsus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.TwoStatePreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.ui.setting.SettingActivity;

/**
 * Created by Kim_Bai on 6/1/2015.
 */
public class InspireAsusPreference extends CheckBoxPreference {
    private Context mContext;
    private TextView mTextView;
    private CheckBox mCheckBox;
    View.OnClickListener mClickListener;
    public InspireAsusPreference(Context context) {
        this(context, null, 0);
    }

    public InspireAsusPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public InspireAsusPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    public void setClickListener(View.OnClickListener listener)
    {
        mClickListener = listener;
    }
//    @Override
//    protected View onCreateView(ViewGroup parent) {
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View myView = inflater.inflate(R.layout.inspire_asus_preference, null);
//        return myView;
//    }
    @Override
    protected void onClick() {
        Log.i("smile","Inspire pre onclick");
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
//        if(mTextView!=null && mCheckBox!=null)
//        {
//            return;
//        }
        mTextView = (TextView) view.findViewById(android.R.id.summary);
        mCheckBox = (CheckBox) view.findViewById(android.R.id.checkbox);
        SharedPreferences mPre=mContext.getSharedPreferences(SettingActivity.KEY_GA, Context.MODE_PRIVATE);
        if (mCheckBox != null) {
            if (mClickListener != null) {
                Log.i("smile", "Inspire mClickListener not null");
                mCheckBox.setOnClickListener(mClickListener);
            }
            mCheckBox.setChecked(mPre.getBoolean(SettingActivity.IS_APP_OPT_OUT, true));
        }
    }

    public TextView getmTextView() {
        return mTextView;
    }

    public CheckBox getmCheckBox() {
        return mCheckBox;
    }

}

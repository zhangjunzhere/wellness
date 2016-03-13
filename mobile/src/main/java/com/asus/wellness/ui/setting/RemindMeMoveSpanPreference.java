package com.asus.wellness.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.asus.sharedata.SyncIdleAlarm;
import com.asus.wellness.R;
import com.asus.wellness.utils.Utility;

/**
 * Created by Kim_Bai on 12/27/2014.
 */
public class RemindMeMoveSpanPreference extends CheckBoxPreference {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private TextView mFromTv;
    private TextView mToTv;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    public RemindMeMoveSpanPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = mSharedPreferences.edit();
    }

    public RemindMeMoveSpanPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemindMeMoveSpanPreference(Context context) {
        this(context, null, 0);
    }

    public void setmOnClickListener(OnClickListener mOnClickListener){
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myView = inflater.inflate(R.layout.remind_me_move_span_preference_activity, null);
        return myView;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mFromTv = (TextView) view.findViewById(R.id.from_tv);
        mToTv = (TextView) view.findViewById(R.id.to_tv);

        mFromTv.setText(Utility.TimeFormat(mSharedPreferences.getInt(Utility.HOUR_OF_DAY_FROM, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_FROM), mSharedPreferences.getInt(Utility.MINUTE_FROM, SyncIdleAlarm.DEFAULT_MINUTE_FROM), false));
        mToTv.setText(Utility.TimeFormat(mSharedPreferences.getInt(Utility.HOUR_OF_DAY_TO, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_TO), mSharedPreferences.getInt(Utility.MINUTE_TO, SyncIdleAlarm.DEFAULT_MINUTE_TO), false));
    }

    // Getters

    public TextView getmFromTv() {
        return mFromTv;
    }

    public TextView getmToTv() {
        return mToTv;
    }
}

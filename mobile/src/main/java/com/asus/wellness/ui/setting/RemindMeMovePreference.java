package com.asus.wellness.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.asus.sharedata.SyncIdleAlarm;
import com.asus.wellness.R;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.ui.setting.SettingIdleAlarmActivity;
import com.asus.wellness.utils.Utility;
import com.cmcm.common.statistics.CMAgent;

import java.util.HashMap;

/**
 * Created by larrylf_lin on 2015/8/4.
 */
public class RemindMeMovePreference extends CheckBoxPreference {
    private  CheckBox mRemindCheckBox;
    private TextView mSummerTextview;
    private  Context mContext;
    HashMap<String,String> data=new HashMap<>();
    View.OnClickListener mClickListener;
    public RemindMeMovePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public RemindMeMovePreference(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.mContext = context;
    }

    public RemindMeMovePreference(Context context) {
        this(context, null, 0);
    }

//   @Override
//    protected View onCreateView(ViewGroup parent) {
////       setLayoutResource(R.layout.remind_me_move_preference);
////       setWidgetLayoutResource(R.layout.asus_checkbox);
////       super.onCreateView(parent);
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View myView = inflater.inflate(R.layout.remind_me_move_preference, null);
////       if(myView != null){
////           if (getItemHeight() != 0){
////               int height = (int)(getItemHeight() * 1.2);
////               myView.setMinimumHeight(height);
////           }
////       }
//       return myView;
//
//    }
    public float getItemHeight() {
        try {
            TypedValue value = new TypedValue();
            DisplayMetrics metrics = new DisplayMetrics();

            getContext().getTheme().resolveAttribute(
                    android.R.attr.listPreferredItemHeight, value, true);
            ((WindowManager) (getContext().getSystemService(Context.WINDOW_SERVICE)))
                    .getDefaultDisplay().getMetrics(metrics);

            return TypedValue.complexToDimension(value.data, metrics);
        }catch (Exception e){
            e.printStackTrace();
            return  0;
        }
    }

   /* @Override
    public View getView(final View convertView, final ViewGroup parent) {
        final View v = super.getView(convertView, parent);
        final int hieght = 300; //android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        final int width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        final LayoutParams params = new LayoutParams(hieght, width);
        v.setLayoutParams(params );
        return v;
    }
   */
    public void setClickListener(View.OnClickListener listener)
    {
        mClickListener = listener;
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
       Log.i("larry", "onBindView check: "+Utility.getRemindMeMoveChecbox());
//        if(mSummerTextview!=null && mRemindCheckBox!=null)
//        {
//            return;
//        }
        mSummerTextview  = (TextView) view.findViewById(android.R.id.summary);
        mRemindCheckBox  = (CheckBox) view.findViewById(android.R.id.checkbox);

        mRemindCheckBox.setChecked(Utility.getRemindMeMoveChecbox());
        this.setSummerText(Utility.getRemindMeMoveChecbox());
        if (mRemindCheckBox != null){
            if(mClickListener!=null)
            {
                Log.i("smile","remind mClickListener not null");
                mRemindCheckBox.setOnClickListener(mClickListener);
            }

            mRemindCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //Log.i("larry", "remind_asus_checkbox onCheckedChanged, isChecked: " + mRemindCheckBox.isChecked());
                    //mCallBack.updateSummer(mRemindCheckBox.isChecked());
                    Log.i("smile","oncheck change");
                    SharedPreferences sp;
                    sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putBoolean(mContext.getString(R.string.pref_key_idle_alarm_switch), mRemindCheckBox.isChecked());
                    editor.commit();
                    if (mRemindCheckBox.isChecked()) {
                        int interval = sp.getInt(getContext().getString(R.string.pref_key_idle_alarm_options_interval), SettingIdleAlarmActivity.DEFAULT_TIME_INTERVAL);

                        Intent intent = new Intent(SettingIdleAlarmActivity.ACTION_ALARM);
                        intent.putExtra(SettingIdleAlarmActivity.KEY_CHANGE_ALARM_INTERVAL, interval);
                        getContext().sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(SettingIdleAlarmActivity.ACTION_CANCEL_ALARM);
                        getContext().sendBroadcast(intent);
                    }
                    data.clear();
                    String remind=mRemindCheckBox.isChecked()?"1":"2";
                    String[] timeInfo= CmHelper.getRemindTime(sp, getContext());
                    data.put("remind_me",remind);
                    data.put("remind_fromtime",timeInfo[0]);
                    data.put("remind_totime",timeInfo[1]);
                    data.put("remind_duration", timeInfo[2]);
                    CMAgent.onEvent(CmHelper.PROFILE_MSG_ID, data);
                }
            });

        }
    }

    @Override
    protected void onClick() {
        Log.i("smile","RemindMe pre onclick");
    }

    public CheckBox getmCheckBox() {
        return mRemindCheckBox;
    }

    public void setCheckBoxCheced(Boolean isCheced) {
        if(mRemindCheckBox != null){
            mRemindCheckBox.setChecked(isCheced);
        }

    }

    public void setSummerText(Boolean isChecked) {
        if ( mSummerTextview != null)
        {
            if (isChecked) {
                mSummerTextview.setText(getSummerString());
            }else {
                mSummerTextview.setText(getContext().getString(R.string.setting_idle_alarm_description));
            }
        }
    }

    private String getSummerString(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        StringBuilder builder = new StringBuilder();
        String fromContent = Utility.TimeFormat(sp.getInt(Utility.HOUR_OF_DAY_FROM, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_FROM), sp.getInt(Utility.MINUTE_FROM, SyncIdleAlarm.DEFAULT_MINUTE_FROM), false);
        String toContent = Utility.TimeFormat(sp.getInt(Utility.HOUR_OF_DAY_TO, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_TO), sp.getInt(Utility.MINUTE_TO, SyncIdleAlarm.DEFAULT_MINUTE_TO), false);
        builder.append( getContext().getString(R.string.from));
        builder.append(" ");
        builder.append(fromContent);
        builder.append(" ");
        builder.append( getContext().getString(R.string.to));
        builder.append(" ");
        builder.append(toContent);
        builder.append(", ");
        int entryId =sp.getInt(getContext().getString(R.string.pref_key_idle_alarm_duration_summary),2);
        String[] durationEntries = getContext().getResources().getStringArray(R.array.remind_duration_option);
        String duration = durationEntries[entryId];
        builder.append(duration);
        return builder.toString();
    }

}
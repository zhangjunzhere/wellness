package com.asus.wellness.ui.setting;

import com.asus.wellness.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class WellnessSwitchPreference extends SwitchPreference {

	public WellnessSwitchPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public WellnessSwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public WellnessSwitchPreference(Context context) {
		super(context);
	}

	@Override
	protected void onClick() {
		// TODO Auto-generated method stub
	}
	
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        
        ViewGroup viewGroup = (ViewGroup) view;
        final Switch sw = (Switch) viewGroup.findViewById(R.id.asus_switch);
        if (sw != null) {
            SharedPreferences sp = getSharedPreferences();
            sw.setChecked(sp.getBoolean(getKey(), false));
            sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                
                @Override
                public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                    if (mOnSwitchCheckedChangedListener != null) {
                        mOnSwitchCheckedChangedListener.onSwitchCheckedChanged(isChecked);
                    }
                }
            });
        }
    }
    
    private OnSwitchCheckedChangedListener mOnSwitchCheckedChangedListener;
    
    public interface OnSwitchCheckedChangedListener {
        public void onSwitchCheckedChanged(boolean isChecked);
    }
    
    public void setOnSwitchCheckedChangedListener(OnSwitchCheckedChangedListener listener) {
        mOnSwitchCheckedChangedListener = listener;
    }
    
}

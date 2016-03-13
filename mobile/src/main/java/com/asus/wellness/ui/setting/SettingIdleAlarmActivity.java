package com.asus.wellness.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.asus.commonui.datetimepicker.time.RadialPickerLayout;
import com.asus.commonui.datetimepicker.time.TimePickerDialog;
import com.asus.sharedata.SyncIdleAlarm;
import com.asus.wellness.DataLayerManager;
import com.asus.wellness.R;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.cmcm.common.statistics.CMAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

public class SettingIdleAlarmActivity extends PreferenceActivity {
    private RemindMeMoveSpanPreference myRemindMeMoveSpanPreference;
    private EditTextPreference mEditTextPreference;
    private static final String EDIT_STRING = "edit_string";

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

	public static final String ACTION_ALARM="action_alarm";
	public static final String ACTION_CANCEL_ALARM="action_cancel_alarm";
	public static final String KEY_CHANGE_ALARM_INTERVAL="change_alarm_interval";
    public static int DEFAULT_TIME_INTERVAL = 3600000;  // fix viberating always bug.
	DataLayerManager dataLayer;
    private WellnessSwitchPreference switchPre;
    private String[] durationEntries;
    HashMap<String,String> data=new HashMap<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

        //emily++++  remove padding for PreferenceActivity
        setContentView(R.layout.setting_layout);
        setPreferenceScreen(null);
        //emily----

        addPreferencesFromResource(R.xml.remind_me_move_preference);
		//setContentView(R.layout.setting_idle_alarm_layout);

		//settingActionBar();   // For switch
		//settingIdleAlarmOptions();

		setTitle(R.string.setting_title_set_idle_alarm);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

        if (getResources().getConfiguration().smallestScreenWidthDp < 800) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
		
		dataLayer=new DataLayerManager(this);
		dataLayer.connectGoogleApiClient();

        init();
        Utility.trackerScreennView(getApplicationContext(), "Goto SettingIdleAlarm");
	}
    public void onEventMainThread(IdleAlarmEvent event)
    {
        Log.i("smile","SettingIdleAlarmActivity onEventMainThread");
        init();
    }
    @Override
    protected void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
    // kim_bai
    private void init() {
        // Initialize SharedPreferences

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        myRemindMeMoveSpanPreference = (RemindMeMoveSpanPreference) findPreference("remind_me_move_span_preference");
        editor = sp.edit();

        // Switch Preference
        switchPre = (WellnessSwitchPreference) findPreference(getString(R.string.pref_key_idle_alarm_switch));
        switchPre.setOnSwitchCheckedChangedListener(new WellnessSwitchPreference.OnSwitchCheckedChangedListener() {

            @Override
            public void onSwitchCheckedChanged(boolean isChecked) {
                boolean value = sp.getBoolean(getString(R.string.pref_key_idle_alarm_switch), false);
                //Log.i("larry", "setOnSwitchCheckedChangedListener isChecked: " + isChecked);
                switchPre.setChecked(isChecked);
                dataLayer.sendIdleAlarmSetting();

                SharedPreferences mSharedPref = getSharedPreferences(SettingActivity.KEY_GA, MODE_PRIVATE);
                SharedPreferences.Editor remindEditor = mSharedPref.edit();
                remindEditor.putBoolean(SettingActivity.IS_REMIND_OPT_OUT,  !isChecked);
                remindEditor.commit();

                changeRemindMeDetailText();
                if (isChecked) {
                    // Get tracker.
                    Tracker t = GAApplication.getInstance().getTracker(getApplicationContext());
                    // Build and send an Event.
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("Settings")
                            .setAction("Remind me to move")
                            .setLabel("Checked")
                            .build());
                }
            }
        });
        switchPre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                // TODO Auto-generated method stub
                boolean enableAlarm = (Boolean) newValue;
                if (enableAlarm) {
                    int interval = sp.getInt(getString(R.string.pref_key_idle_alarm_options_interval), DEFAULT_TIME_INTERVAL);

                    Intent intent = new Intent(ACTION_ALARM);
                    intent.putExtra(KEY_CHANGE_ALARM_INTERVAL, interval);
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(ACTION_CANCEL_ALARM);
                    sendBroadcast(intent);
                }
                return true;
            }

        });
        // List Preference Duration
        durationEntries = getResources().getStringArray(R.array.remind_duration_option);
        final String[] durationValues = getResources().getStringArray(R.array.remind_duration_option_values);
        final ListPreference listPreference = (ListPreference) findPreference(getString(R.string.pref_remind_duration));
        //emily++++
        //listPreference.setSummary(sp.getString(getString(R.string.pref_key_idle_alarm_duration_summary), getString(R.string.setting_idle_alarm_option_1hour)));
        int entryId =sp.getInt(getString(R.string.pref_key_idle_alarm_duration_summary),2);
        listPreference.setSummary(durationEntries[entryId]);
        listPreference.setValueIndex(entryId);
        //emily----

        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Set Duration Summary with Entry
                for (int i = 0; i < durationValues.length; i++) {
                    if (newValue.equals(durationValues[i])) {
                        listPreference.setSummary(durationEntries[i]);
                        //editor.putString(getString(R.string.pref_key_idle_alarm_duration_summary), durationEntries[i]);
                        editor.putInt(getString(R.string.pref_key_idle_alarm_duration_summary), i);//emily
                    }
                }

                int interval = Integer.parseInt(newValue.toString());
                editor.putInt(getString(R.string.pref_key_idle_alarm_options_interval), interval);
                editor.commit();

                Intent intent = new Intent(ACTION_ALARM);
                intent.putExtra(KEY_CHANGE_ALARM_INTERVAL, interval);
                sendBroadcast(intent);

                dataLayer.sendIdleAlarmSetting();
                changeRemindMeDetailText();
                return true;
            }
        });
        initSwitchButton();

    }

    private void  initSwitchButton(){
        Boolean isChecked = Utility.getRemindMeMoveChecbox();
        switchPre.setChecked(isChecked);
        changeRemindMeDetailText();

    }

    TimePickerDialog tp;

    /**
     * Time Picker From
     * @param v
     */
    public void startTimePickerFrom(View v) {

        if (tp != null)
            tp.dismiss();
        Calendar c = Calendar.getInstance();
        tp = new TimePickerDialog();

        tp.initialize(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
                myRemindMeMoveSpanPreference.getmFromTv().setText(Utility.TimeFormat(hourOfDay, minute, false));

                editor.putInt(Utility.HOUR_OF_DAY_FROM, hourOfDay);
                editor.putInt(Utility.MINUTE_FROM, minute);
                editor.commit();
                changeRemindMeDetailText();
                dataLayer.sendIdleAlarmSetting();
            }
        }, sp.getInt(Utility.HOUR_OF_DAY_FROM, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_FROM), sp.getInt(Utility.MINUTE_FROM, SyncIdleAlarm.DEFAULT_MINUTE_FROM),DateFormat.is24HourFormat(this),false,0);

        tp.show(getFragmentManager(), "Set");
    }

    /**
     * Time Picker To
     * @param v
     */
    public void startTimePickerTo(View v) {

        if (tp != null)
            tp.dismiss();
        Calendar c = Calendar.getInstance();
        tp = new TimePickerDialog();
        tp.initialize(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
                myRemindMeMoveSpanPreference.getmToTv().setText(Utility.TimeFormat(hourOfDay, minute, false));

                editor.putInt(Utility.HOUR_OF_DAY_TO, hourOfDay);
                editor.putInt(Utility.MINUTE_TO, minute);
                editor.commit();

                dataLayer.sendIdleAlarmSetting();
                changeRemindMeDetailText();
            }
        }, sp.getInt(Utility.HOUR_OF_DAY_TO, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_TO), sp.getInt(Utility.MINUTE_TO, SyncIdleAlarm.DEFAULT_MINUTE_TO), DateFormat.is24HourFormat(this),false,0);

        tp.show(getFragmentManager(), "Set");
    }
    // end

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dataLayer.disConnectGoogleApiClient();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeRemindMeDetailText(){
        if(switchPre != null){
            //Log.i("banner", "isChecked: " + switchPre.isChecked());
            if ( switchPre.isChecked()){
                StringBuilder builder = new StringBuilder();
                String fromContent = Utility.TimeFormat(sp.getInt(Utility.HOUR_OF_DAY_FROM, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_FROM), sp.getInt(Utility.MINUTE_FROM, SyncIdleAlarm.DEFAULT_MINUTE_FROM), false);
                String toContent = Utility.TimeFormat(sp.getInt(Utility.HOUR_OF_DAY_TO, SyncIdleAlarm.DEFAULT_HOUR_OF_DAY_TO), sp.getInt(Utility.MINUTE_TO, SyncIdleAlarm.DEFAULT_MINUTE_TO), false);
                builder.append( getString(R.string.from));
                builder.append(" ");
                builder.append(fromContent);
                builder.append(" ");
                builder.append( getString(R.string.to));
                builder.append(" ");
                builder.append(toContent);
                builder.append(", ");
                int entryId =sp.getInt(getString(R.string.pref_key_idle_alarm_duration_summary),2);
                String duration = durationEntries[entryId];
                builder.append(duration);
                switchPre.setSummary(builder.toString());
            }else {
                switchPre.setSummary(getString(R.string.setting_idle_alarm_description));
            }
            data.clear();
            String remind=switchPre.isChecked()?"1":"2";
            String[] timeInfo= CmHelper.getRemindTime(sp, this);
            data.put("remind_me",remind);
            data.put("remind_fromtime",timeInfo[0]);
            data.put("remind_totime",timeInfo[1]);
            data.put("remind_duration",timeInfo[2]);
            CMAgent.onEvent(CmHelper.PROFILE_MSG_ID, data);

        }
    }
}

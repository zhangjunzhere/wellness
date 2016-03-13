package com.asus.wellness.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.sharedata.SyncIdleAlarm;
import com.asus.wellness.R;
import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.microprovider.ActivityStatusTable;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.DataEventHelper;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.common.ConnectionResult;

/**
 * Created by smile_gao on 2015/8/27.
 */
public class IdleAlarmFragment extends Fragment implements View.OnClickListener{
    ImageView idle_alarm;
    TextView onoffTv;
    SharedPreferences sp;
    DataLayerManager dataLayerManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataLayerManager =  DataLayerManager.getInstance(getActivity());
        dataLayerManager.connectGoogleApiClient(null);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CollectStepCountService.ACTION_ALARM);
        intentFilter.addAction(CollectStepCountService.ACTION_CANCEL_ALARM);
//        intentFilter.addAction(ACTION_NEW_DAY);
//        intentFilter.addAction(ACTION_NEW_DAY_CANCEL);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        getActivity().registerReceiver(alarmBroadcastReceiver, intentFilter);
    }

    public static IdleAlarmFragment newInstance()
    {
        return  new IdleAlarmFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean on = sp.getBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, false);
        toggleAlarmText(on);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pni_idle_alarm_layout,container,false);
        idle_alarm = (ImageView)v.findViewById(R.id.ic_alarm);
        idle_alarm.setOnClickListener(this);
        onoffTv = (TextView)v.findViewById(R.id.idle_text_on_off);
        String notice = getResources().getString(R.string.idle_alarm_text);
        String str = getResources().getString(R.string.idle_alarm_off); ;
        onoffTv.setText(notice + " " + str);
//        int size = getResources().getDimensionPixelSize(R.dimen.tap_text_size);
//        Utility.fitFontSizeForView(onoffTv, size, Utility.getScreenWidth(getActivity()));
        return  v;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(alarmBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        boolean on = sp.getBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, false);
        on = !on;
        toggleAlarmText(on);
        setPreference(on);
        syncToPhone(on);
        toggleAlarm(on);

    }
    private void syncToPhone(final boolean alarmon)
    {

        if(!dataLayerManager.isConnected())
        {
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    ConnectionResult cr = dataLayerManager.connectBlocking(200);
                    if(cr.isSuccess())
                    {
                        dataLayerManager.sendIdleAlram(alarmon);
                    }
                    else
                    {
                        Log.i("IdleAlarmFragment"," not conect to play service");
                    }
                    return null;
                }
            };
            task.execute();
            Log.i("IdleAlarmFragment", " not conect to play service " + Thread.currentThread().getId());
            return;
        }
        dataLayerManager.sendIdleAlram(alarmon);
    }

    private void toggleAlarm(boolean alarmon)
    {
        DataEventHelper.toggleIdleAlarm(getActivity(),alarmon);
    }
    private void setPreference(boolean alarmon)
    {
        SharedPreferences.Editor editor = sp.edit();

        editor.putBoolean(SyncIdleAlarm.KEY_IDLE_ALARM_SWITCH, alarmon);
        if(alarmon) {
            editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL, sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_INTERVAL,SyncIdleAlarm.DEFAULT_IDLE_ALARM_INTERVAL));
            editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_FROM, sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_FROM,SyncIdleAlarm.DEFAULT_IDLE_ALARM_HOUR_OF_DAY_FROM));
            editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_FROM, sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_FROM , SyncIdleAlarm.DEFAULT_IDLE_ALARM_MINUTE_FROM));
            editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_TO, sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_HOUR_OF_DAY_TO,SyncIdleAlarm.DEFAULT_IDLE_ALARM_HOUR_OF_DAY_TO));
            editor.putInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_TO, sp.getInt(SyncIdleAlarm.KEY_IDLE_ALARM_MINUTE_TO, SyncIdleAlarm.DEFAULT_IDLE_ALARM_MINUTE_TO));
        }
        editor.commit();
    }

    void toggleAlarmText(boolean on)
    {
        if(!isVisible())
        {
            Log.i("smile","idlealramfragment toggleAlarmText not attach to activity ");
            return;
        }
        String notice = getResources().getString(R.string.idle_alarm_text);
        String str = on ? getResources().getString(R.string.idle_alarm_on) : getResources().getString(R.string.idle_alarm_off); ;
        onoffTv.setText(notice+" "+str);
//        int size = getResources().getDimensionPixelSize(R.dimen.tap_text_size);
//        Utility.fitFontSizeForView(onoffTv, size, Utility.getScreenWidth(getActivity()));
    }
    public BroadcastReceiver alarmBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("IdleFragment",action);
            if (action.matches(CollectStepCountService.ACTION_ALARM)) {
                IdleAlarmFragment.this.toggleAlarmText(true);
            }
            else
            {
                IdleAlarmFragment.this.toggleAlarmText(false);
            }
        }
    };

}

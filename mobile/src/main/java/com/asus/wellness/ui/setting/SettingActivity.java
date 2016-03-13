package com.asus.wellness.ui.setting;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.asus.sharedata.ShareUtils;
import com.asus.wellness.CollectInfoService;
import com.asus.wellness.DataLayerManager;
import com.asus.wellness.R;
import com.asus.wellness.WellnessLocationManager;
import com.asus.wellness.cm.CmBasePreferenceActivity;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.provider.LocationChangeTable;
import com.asus.wellness.provider.StepGoalTable;
//import com.asus.wellness.sleep.SleepTrackingNotification;
import com.asus.wellness.sync.SyncDbEvent;
import com.asus.wellness.ui.InspireAsus.InspireAsusActivity;
import com.asus.wellness.ui.InspireAsus.InspireAsusPreference;
import com.asus.wellness.ui.StepGoalTableObserver;
import com.asus.wellness.ui.permission.GrantPermissionActivity;
import com.asus.wellness.ui.permission.PermissionDialog;
import com.asus.wellness.ui.permission.PermissionHelper;
import com.asus.wellness.utils.HudToastAnimation;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.GoogleAnalytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;

public class SettingActivity extends CmBasePreferenceActivity {
    public static final String FEEDBACK_ACTION = "com.asus.wellness.intent.action.USERFEEDBACK";

    private static final int REQUEST_BACKUP_SETTINGS = 1, REQUEST_LOCATION_SERVICE = 2;
    private CheckBoxPreference mBackupRestore, mLocationService;
    DataLayerManager dataLayer;

    //emily add, 2014.12.05
    private static final String URL_ABOUT_ASUS_WEBSITE = "http://www.asus.com";
    private AlertDialog mAboutAlertDialog;
    //emily end, 2014.12.05

    private SharedPreferences mSharedPref;
    public static final String KEY_GA = "key_ga";
    public static final String IS_APP_OPT_OUT = "is_app_opt_out";
    public static final String IS_REMIND_OPT_OUT = "is_remind_opt_out";

    private StepGoalTableObserver mStepGoalTableObserver;
    private RemindMeMovePreference remindMePre;

    private long mNeverShowTime = 0;
    private boolean mFromSettingLocation= false;
    PermissionDialog mPermissoinDialog ;
    @Override
    public String getPageName(){
        return SettingActivity.class.getSimpleName();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_layout);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.setting_preference);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if (Utility.isPadDevice(this)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        dataLayer = new DataLayerManager(this);
        dataLayer.connectGoogleApiClient();


        // Register ContentObserver
        mStepGoalTableObserver = new StepGoalTableObserver(mHandler);
        getContentResolver().registerContentObserver(StepGoalTable.TABLE_URI, true, mStepGoalTableObserver);

        mSharedPref = getSharedPreferences(KEY_GA, MODE_PRIVATE);
        Utility.trackerScreennView(getApplicationContext(), "Goto Setting");
        initLocationSetting();
        remindMePre = (RemindMeMovePreference)findPreference(getString(R.string.pref_key_idle_alarm_switch_settings));
        remindMePre.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRemindCheckbox(v);
            }
        });
        InspireAsusPreference pre = (InspireAsusPreference) findPreference(getString(R.string.pref_key_inspire_asus));
        pre.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickInspireAsusCheckbox(v);
            }
        });
    }



    private void initLocationSetting()
    {
        mLocationService = (CheckBoxPreference) findPreference(getString(R.string.pref_key_location));

        mLocationService.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Log.i("smile","onPreferenceChange");
                boolean locationServiceEnabled = checkLocationServiceEnable(SettingActivity.this);
                boolean goingEnable = (Boolean) newValue;
                if (goingEnable && !locationServiceEnabled) {
                    startLocationSettings();
                    return false;
                } else {
                    return startTrackLocationWithPermission(goingEnable);
                }


            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == StepGoalTableObserver.SEND_NEXT_GOAL) {
                setActivityGoal();
            }
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        dataLayer.disConnectGoogleApiClient();

        // Unregister ContentObserver
        getContentResolver().unregisterContentObserver(mStepGoalTableObserver);
        if (mAttachmentTask!=null){
            mAttachmentTask.cancel(true);
        }

    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        setBugReport();
        setBackupRestore();
        setLocationService();
        setActivityGoal();
        setEncourageUs();
        setUserFeedback();
        //setAppVersion();
        setAbout();//add by emily,2014.12.05
        BackupManager mbm = new BackupManager(this);
        mbm.dataChanged();

        setTitle(R.string.setting_title);
        //smile add for idle
        EventBus.getDefault().register(this);
    }
    public void onEventMainThread(SyncDbEvent event)
    {
        Log.i("smile", "SettingActivity sendmail");
        sendMail(true, null);
    }
    public void onEventMainThread(IdleAlarmEvent event)
    {
        Log.i("smile", "SettingActivity onEventMainThread");
        setRemindMeRemoveAsus(false);
    }
    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            setInspireAsus();
            setRemindMeRemoveAsus(true);
        }
    }

    public void goSettingIdleAlarmActivity(View view){
        Intent intent = new Intent(SettingActivity.this, SettingIdleAlarmActivity.class);
        startActivity(intent);
    }

    public void  clickRemindCheckbox(View view){
        boolean isChecked = ((CheckBox) view).isChecked();
        boolean isRemindOptOut = !isChecked;
        Log.i("smile", "clickRemindCheckbox: " + isChecked);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(IS_REMIND_OPT_OUT, isRemindOptOut);
        editor.commit();
        RemindMeMovePreference pre = (RemindMeMovePreference) findPreference(getString(R.string.pref_key_idle_alarm_switch_settings));
        pre.setCheckBoxCheced(isChecked);
        pre.setSummerText(isChecked);
        Utility.setRemindMeMoveChecbox(isChecked);
        dataLayer.sendIdleAlarmSetting();

    }

    private void setRemindMeRemoveAsus(boolean sendToWear){
        RemindMeMovePreference pre = (RemindMeMovePreference) findPreference(getString(R.string.pref_key_idle_alarm_switch_settings));
        boolean isChecked = !mSharedPref.getBoolean(IS_REMIND_OPT_OUT, true);
        //Log.i("larry","setRemindMeRemoveAsus isCheced: " + isChecked);
        if(pre.getmCheckBox() != null) {
            pre.getmCheckBox().setChecked(isChecked);
            pre.setSummerText(isChecked);
        }
        Utility.setRemindMeMoveChecbox(isChecked);
        if(sendToWear)
          dataLayer.sendIdleAlarmSetting();
    }

    private void setAppVersion() {
        Preference pre = findPreference(getString(R.string.pref_key_app_ver));
        pre.setSummary(getCurrentVersionName());
    }

    private void setBackupRestore() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean backupEnabled = Settings.Secure.getInt(getContentResolver(), "backup_enabled", 0) != 0;

        //boolean restoreEnable = Settings.Secure.getInt(getContentResolver(), "backup_auto_restore", 1) != 0;

        boolean enable = sp.getBoolean(getString(R.string.pref_key_backup_restore), getResources().getBoolean(R.bool.default_backup_restore));

        mBackupRestore = (CheckBoxPreference) findPreference(getString(R.string.pref_key_backup_restore));

        boolean  ischecked = (backupEnabled && enable);
        mBackupRestore.setChecked(ischecked);
        mBackupRestore.setWidgetLayoutResource(ischecked ? R.layout.asus_checkbox:R.layout.asus_checkbox_unchecked);
        mBackupRestore.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean backupEnabled = Settings.Secure.getInt(getContentResolver(), "backup_enabled", 0) != 0;
                boolean goingEnable = (Boolean) newValue;

                if (goingEnable && !backupEnabled) {
                    //Show Dialog
                    DialogFragment dialog = new BackupSettingsDialog();
                    dialog.show(getFragmentManager(), "backup_setting_dialog");
                    return false;
                }
                else if(goingEnable == false){
                    showDisableBackupDialog(goingEnable);
                    return false;
                } else {
                    String backup = goingEnable ? "1" : "2";
                    CmHelper.uploadSettingData(backup, null, null, null, false);
                }

                mBackupRestore.setWidgetLayoutResource(goingEnable ? R.layout.asus_checkbox:R.layout.asus_checkbox_unchecked);
                return true;

            }
        });

    }

    private void showDisableBackupDialog(final Boolean goingEnable){
        AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this)
                //.setTitle("mTitle")
                .setMessage(getResources().getString(R.string.settings_disable_backup))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mBackupRestore.setChecked(false);
                                mBackupRestore.setWidgetLayoutResource(R.layout.asus_checkbox_unchecked);
                                String backup = goingEnable ? "1" : "2";
                                CmHelper.uploadSettingData(backup, null, null, null, false);
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mBackupRestore.setChecked(true);
                                mBackupRestore.setWidgetLayoutResource(R.layout.asus_checkbox);
                            }
                        }
                )
                .create();
        dialog.show();;
    }
   long startLocationTime =0;
    private void setLocationService() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean locationServiceEnabled = checkLocationServiceEnable(this);
        boolean locationPermissionGranted =PermissionHelper.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, this);
        if(mFromSettingLocation && !locationPermissionGranted )
        {
            mFromSettingLocation = false;
            mNeverShowTime = System.currentTimeMillis();
            PermissionHelper.grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION, this, GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_LOCATION);
            return ;
        }

        //boolean restoreEnable = Settings.Secure.getInt(getContentResolver(), "backup_auto_restore", 1) != 0;

        boolean enable = sp.getBoolean(getString(R.string.pref_key_location), getResources().getBoolean(R.bool.default_location));

        doStartTrack(locationServiceEnabled && enable && locationPermissionGranted);
        mLocationService.setChecked((locationServiceEnabled && enable && locationPermissionGranted));


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_LOCATION:
            {
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.i("smile","onRequestPermissionsResult START_ACTIVITY_REQUEST_CODE_LOCATION true "+mLocationService.isChecked());
                    mLocationService.setChecked(true);
                    doStartTrack(true);
                }
            }
            break;
        case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_WRITE:
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.i("smile","onRequestPermissionsResult START_ACTIVITY_REQUEST_CODE_STORAGE_WRITE  true");
                startMailTask();
            }
            else
            {
                Log.i("smile","onRequestPermissionsResult START_ACTIVITY_REQUEST_CODE_STORAGE_WRITE  false");
            }
            break;
        }
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_DENIED && mNeverShowTime>0)
        {
            long span = System.currentTimeMillis()-mNeverShowTime;
            Log.i("smile","onRequestPermissionsResult denied span "+span);
            if(span < GrantPermissionActivity.NEVER_SHOW_TIME)
            {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fragment = getFragmentManager().findFragmentByTag(PermissionDialog.TAG);
                if(fragment!=null)
                {
                    ft.remove(fragment);
                }
                mPermissoinDialog =new PermissionDialog();
                Bundle bd = new Bundle();
                bd.putString(PermissionDialog.TITLE_KEY,getString(R.string.allow_track_location_title));
                bd.putString(PermissionDialog.CONTENT_KEY,getString(R.string.allow_track_location_content));
                bd.putString(PermissionDialog.PACKAGENAME_KEY,getPackageName());
                mPermissoinDialog.setArguments(bd);
                mPermissoinDialog.show(ft,PermissionDialog.TAG);
              //  Utility.openSettingAppInfo(getPackageName(),this);
            }
        }
        mNeverShowTime =0;
        Log.i("smile","onRequestPermissionsResult over ");
//            long timeSpan = System.currentTimeMillis()- startLocationTime;
//            Log.i("smile","time span: "+timeSpan);
//            if(timeSpan<200)
//            {
//                Intent i = new Intent();
//                i.setAction("android.intent.action.MANAGE_APP_PERMISSIONS");
//                i.setComponent(new ComponentName("com.android.packageinstaller","com.android.packageinstaller.permission.ui.ManagePermissionsActivity"));
//                i.putExtra("android.intent.extra.PACKAGE_NAME","com.asus.wellness");
//                startActivity(i);
//            }

    }

    boolean startTrackLocationWithPermission(boolean goingEnable)
    {
        startLocationTime = System.currentTimeMillis();
        if(!PermissionHelper.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, this))
        {
            mNeverShowTime = System.currentTimeMillis();
            PermissionHelper.grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION, this, GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_LOCATION);
            return  false;
        }
//        if(!PermissionHelper.checkLocationPermission(SettingActivity.this,))
//        {
//            Log.i("smile","startTrackLocationWithPermission return");
//            return false;
//        }
        Log.i("smile","startTrackLocationWithPermission true");
        doStartTrack(goingEnable);
        return true;
    }
    void doStartTrack(boolean goingEnable)
    {
        startTracking(goingEnable);
        String track=goingEnable?"1":"2";
        CmHelper.uploadSettingData(null, track, null, null, false);
    }
    private void startTracking(boolean isTracking) {
        Intent intent = new Intent(SettingActivity.this, CollectInfoService.class);
        if (isTracking) {
            Log.d("smile", "[setting]start service");
            startService(intent);
        } else {
            Log.d("smile", "[setting]stop service");
            stopService(intent);
            //Insert 360,360 to LocationChangeTable to indicate off positioning.
            ContentValues cv = new ContentValues();
            cv.put(LocationChangeTable.COLUMN_GET_LOCATION_TIME, System.currentTimeMillis());
            cv.put(LocationChangeTable.COLUMN_LOCATION_LATITUDE, 360);
            cv.put(LocationChangeTable.COLUMN_LOCATION_LONGITUDE, 360);
            cv.put(LocationChangeTable.COLUMN_DISTRICT, WellnessLocationManager.OFF_POSITIONING);
            getContentResolver().insert(LocationChangeTable.TABLE_URI, cv);
        }
    }

    private void setActivityGoal() {
        Cursor cursor = Utility.getStepGoalCursor(getApplicationContext());
        Cursor cursorPre = Utility.getPreStepGoalCursor(getApplicationContext());

        Preference pre = findPreference(getString(R.string.pref_key_activity_goal_preference));

        int stepGoalCur = SettingStepGoalActivity.DEFAULT_STEP_GOAL;
        if (cursor.moveToFirst()) {
            stepGoalCur = cursor.getInt(cursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
        } else if(cursorPre.moveToFirst()) {
            stepGoalCur = cursorPre.getInt(cursorPre.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
        }
        String stepGoal = String.format(getString(R.string.step_counts_message_preference), Utility.commaNumber(stepGoalCur)) + " " + Utility.getIntensityString(this, stepGoalCur);
        cursor.close();
        cursorPre.close();

        Cursor cursorNext = Utility.getNextStepGoalCursor(getApplicationContext());
        if(cursorNext.moveToFirst()) {
            stepGoal += "\n" + getString(R.string.tomorrow_goal) + " " + cursorNext.getInt(cursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL)) + " " + getString(R.string.steps);
        }
        pre.setSummary(stepGoal);
        cursorNext.close();

       // changeRemindMeDetailTextInSetting();
       /* WellnessSwitchPreference switchPre = (WellnessSwitchPreference) findPreference(getString(R.string.pref_key_idle_alarm_switch));
        switchPre.setOnSwitchCheckedChangedListener(new OnSwitchCheckedChangedListener() {

            @Override
            public void onSwitchCheckedChanged(boolean isChecked) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
                boolean value = sp.getBoolean(getString(R.string.pref_key_idle_alarm_switch), false);
                if (value != isChecked) {
                    sp.edit().putBoolean(getString(R.string.pref_key_idle_alarm_switch), isChecked).commit();
                }
                dataLayer.sendIdleAlarmSetting();
            }
        });
        switchPre.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                // TODO Auto-generated method stub
                boolean enableAlarm = (Boolean) newValue;
                if (enableAlarm) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
                    int interval = sp.getInt(getString(R.string.pref_key_idle_alarm_options_interval), SettingIdleAlarmActivity.DEFAULT_TIME_INTERVAL);

                    Intent intent = new Intent(SettingIdleAlarmActivity.ACTION_ALARM);
                    intent.putExtra(SettingIdleAlarmActivity.KEY_CHANGE_ALARM_INTERVAL, interval);
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(SettingIdleAlarmActivity.ACTION_CANCEL_ALARM);
                    sendBroadcast(intent);
                }
                return true;
            }

        });*/
    }

    public void clickInspireAsusLl(View v) {
        Intent intent = new Intent(this, InspireAsusActivity.class);
        startActivity(intent);
    }

    public void clickInspireAsusCheckbox(View v) {
        boolean isAppOptOut = ((CheckBox) v).isChecked();
        GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(isAppOptOut);

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(IS_APP_OPT_OUT, isAppOptOut);
        editor.commit();

        String inspire=isAppOptOut?"1":"2";
        CmHelper.uploadSettingData(null, null, inspire, null, false);
    }

    private void setInspireAsus() {
        InspireAsusPreference pre = (InspireAsusPreference) findPreference(getString(R.string.pref_key_inspire_asus));
        if(pre.getmCheckBox() != null) {
            pre.getmCheckBox().setChecked(mSharedPref.getBoolean(IS_APP_OPT_OUT, true));
        }
    }

    private void setEncourageUs() {
        Preference pre = findPreference(getString(R.string.pref_key_encourage_us));
        pre.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                createEncourageUSDialog();
                return false;
            }
        });
    }

    private void createEncourageUSDialog(){
        View encourage_us_layout = View.inflate(this, R.layout.dialog_encourage_us, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.encourage_title));
        builder.setView(encourage_us_layout)
        // .setMessage(getString(R.string.toolbar_encourage_us_text))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.toolbar_encourage_dialog_rate_now), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        goMarket();
                        String encourage="1";
                        CmHelper.uploadSettingData(null, null, null, encourage, false);
                    }
                })
                .setNegativeButton(getString(R.string.toolbar_encourage_us_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
        //SleepTrackingNotification.getInstance().showRemoteViewNotification(this);
    }

    private void goMarket(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
        startActivity(intent);
        mHandler.postDelayed(new HudToastAnimation(SettingActivity.this), 1700);

    }

    private void setUserFeedback() {
        Preference pre = findPreference(getString(R.string.pref_key_userfeedback));
        pre.setTitle(R.string.uf_sdk_feedback_and_help);//smile_gao modify to new string id 2014/12/4
        pre.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sendBroadcast(new Intent(FEEDBACK_ACTION));
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_BACKUP_SETTINGS:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                boolean backupEnabled = Settings.Secure.getInt(getContentResolver(), "backup_enabled", 0) != 0;
                if (backupEnabled) {
                    sp.edit().putBoolean(getString(R.string.pref_key_backup_restore), backupEnabled).commit();
                    String backup = backupEnabled ? "1" : "2";
                    CmHelper.uploadSettingData(backup, null, null, null, false);
                }
                break;
            case REQUEST_LOCATION_SERVICE:
                mFromSettingLocation = true;
                Log.i("smile","REQUEST_LOCATION_SERVICE: "+resultCode);
                sp = PreferenceManager.getDefaultSharedPreferences(this);
                boolean locationServiceEnabled = checkLocationServiceEnable(this);
                startTracking(locationServiceEnabled);
                if (locationServiceEnabled) {
                    sp.edit().putBoolean(getString(R.string.pref_key_location), locationServiceEnabled).commit();
                    String track=locationServiceEnabled?"1":"2";
                    CmHelper.uploadSettingData(null, track, null, null, false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class BackupSettingsDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.setting_dialog_backup_restore_title)
                    .setMessage(R.string.setting_dialog_backup_restore_message)
                    .setPositiveButton(R.string.action_settings, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
                            getActivity().startActivityForResult(intent, SettingActivity.REQUEST_BACKUP_SETTINGS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();
        }
    }

    public static boolean checkLocationServiceEnable(Context context) {
        try {
            int locationMode = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.LOCATION_MODE);
            switch (locationMode) {
                case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                    return true;
                case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                case Settings.Secure.LOCATION_MODE_OFF:
                    return false;
            }
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private void startLocationSettings() {
        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(myIntent, REQUEST_LOCATION_SERVICE);
    }

    private String getCurrentVersionName() {
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();

        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.versionName;
        } catch (NameNotFoundException e) {
            return "1.0";
        }
    }

    //emily add,2014.12.05
    private void setAbout() {
        Preference pre = findPreference("pref_key_about");
        pre.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mAboutAlertDialog == null) {
                    mAboutAlertDialog = createAboutDialog();
                }

                mAboutAlertDialog.show();
                return false;
            }
        });
    }

//    private void setExportDb()
//    {
//
//        int isDevelopment = Settings.Global.getInt(getContentResolver(),
//                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
//        Log.v("smile", "resume Development = " + isDevelopment);
//        if(isDevelopment == 1)
//        {
//            if(pre == null)
//            {
//                getPreferenceScreen().addPreference(pre);
//
//            }
//            pre.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//
//                }
//            });
//        }
//        else
//        {
//            if(pre != null)
//                getPreferenceScreen().removePreference(pre);
//        }
//
//    }

    private AlertDialog createAboutDialog() {
        LayoutInflater inflater = LayoutInflater.from(SettingActivity.this);
        final View view = inflater.inflate(R.layout.about_dialog, null);

        TextView versionTextView = (TextView) (view.findViewById(R.id.versionTextView));
        String versionStr = getBaseContext().getResources().getString(R.string.about_app_version) + ": " + getVersionName(getBaseContext());
        versionTextView.setText(versionStr);

        TextView websiteTextView = (TextView) (view.findViewById(R.id.websiteTextView));
        websiteTextView.setText(URL_ABOUT_ASUS_WEBSITE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle(R.string.app_name);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mAboutAlertDialog.dismiss();
                    }
                });
        return builder.create();
    }

    private String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);// getPackageName()æ˜¯ä½ å½“å‰�ç±»çš„åŒ…å��ï¼Œ0ä»£è¡¨æ˜¯èŽ·å�–ç‰ˆæœ¬ä¿¡æ�¯
            versionName = packInfo.versionName;
        } catch (Exception ex) {
        }
        return versionName;
    }
    //emily end,2014.12.05

    private void setBugReport() {
        Preference pre = findPreference("pref_key_report_bugs");
        int isDevelopment = Settings.Global.getInt(getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);

        if(isDevelopment != 1){
            if(pre!=null){
                getPreferenceScreen().removePreference(pre);
            }
            return;
        }
        if(pre == null){
            pre = new Preference(this);
            pre.setKey("pref_key_report_bugs");
            getPreferenceScreen().addPreference(pre);
        }
        pre.setTitle(getString(R.string.bug_report_preference_title));
        pre.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(!PermissionHelper.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, SettingActivity.this))
                {
                    mNeverShowTime = System.currentTimeMillis();
                    PermissionHelper.grantPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,SettingActivity.this,GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_WRITE);
                }
                else
                {
                    startMailTask();
                }
                //Toast.makeText(getApplicationContext(), getString(R.string.bug_report_toast), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private AsyncTask mAttachmentTask;
    public static String[] EMAIL_ADDRESS = {"junzheng_zhang@asus.com","smile_gao@asus.com","emily18_zheng@asus.com"};
    public int number_postion = 7;
    public static String filePre = "AsusWellness-bugreport-";
    public static String fileSuffix = ".zip";

    private void startMailTask()
    {
        //Log.i("emily", "send mail");
        if (mAttachmentTask != null) {
            mAttachmentTask.cancel(true);
        }
        mAttachmentTask = new AttachmentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private class AttachmentTask extends AsyncTask<Void, Void, File>{
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(Void... params) {
            // TODO Auto-generated method stub
            return getAttachmentFile();
        }

        @Override
        protected void onPostExecute(File result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            boolean hasWearDB  = dataLayer.syncWearDb();
            if(!hasWearDB){
                sendMail(hasWearDB,result);
            }
        }
    }

    private File getAttachmentFile(){
        String path = saveWellnessdb();
        if(path.equals("")){
            return null;
        }
        File dbFile = new File(path);
        //Log.i("emily", "dbFile " + path + ", " + (dbFile.exists() ? "exists" : "not exists"));
        return dbFile;
    }

   public static final String Extension=".db";
    private void sendMail(boolean hasWearDB, File file) {
        Log.i("emily","sendMail----");
        Intent intent = new Intent(Intent.ACTION_SEND);
        if(!hasWearDB){
            if (file == null){
                return;
            }
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        }
        else{
            File root =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//+"/wellness/";
            String path = root.getAbsolutePath()+"//wellness//";
            root = new File(path);

            ArrayList<Uri> lstFile = new ArrayList<Uri>();
            if (root.exists() && root.isDirectory()) {
                File[] files = root.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.isFile()) {
                        if(f.getName().endsWith(Extension)){
                            lstFile.add(Uri.fromFile(f));
                        }
                    }
                }
            }
            if(lstFile==null || lstFile.size()==0){
                return;
            }
            if (lstFile.size()>1){
                intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_STREAM, lstFile);
            }
            else{
                intent.putExtra(Intent.EXTRA_STREAM, lstFile.get(0));
            }
        }

        String[] tos = EMAIL_ADDRESS;
        intent.putExtra(Intent.EXTRA_EMAIL, tos);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.bug_report_email_body));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report_email_subject));
        intent.setType("application/octet-stream");//octet-stream

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.but_report_share)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public File createZipFile() {
        File file = Environment.getExternalStorageDirectory();
        if (!file.canWrite()) {
            return null;
        }
        String path = file.getAbsolutePath() + "//download//";

        File wellnessFile = new File(path);
        if (!wellnessFile.exists() || !wellnessFile.isFile()) {
            boolean ismk = wellnessFile.mkdir();
        }

        String zipName = getZipFileName(path);
        path = path + zipName;
        File f = new File(path);
        //setFileChmod(f);

        if (f.exists()) {
            //Log.i("emily", "delete = " + path);
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private String getZipFileName(String path) {
        File file = new File(path);
        if (!file.exists()) {
            boolean ismk = file.mkdir();
            return getFilePre() + "-" + String.valueOf(1) + fileSuffix;
        }

        File[] files = new File(path).listFiles();
        File f;
        int number = 0;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                f = files[i];
                String fileName = f.getName();
                if (fileName.contains(getFileCreateDate())) {
                    int dot = fileName.lastIndexOf(".");
                    if ((dot > -1) && (dot < (fileName.length() - 1))) {
                        fileName = fileName.substring(0, dot);
                    }
                    String[] array = fileName.split("-");
                    if (array != null && array.length > number_postion) {
                        //Log.i("emily","arrayList = " + array.length+", "+array[number_postion]+", fileName = " + fileName);
                        int tempNum = 0;
                        try {
                            tempNum = Integer.valueOf(array[number_postion]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if (tempNum > number) {
                            number = tempNum;
                        }
                    }
                }
            }
            for (int i = 0; i < files.length; i++) {
                f = files[i];
                if (f.exists() && f.getName().contains(filePre)) {
                    f.delete();
                }
            }
        }
        number++;
        String name = getFilePre() + "-" + String.valueOf(number) + fileSuffix;
       // Log.i("emily", "getZipFileName = " + name);
        return name;
    }

    private void setFileChmod(File destFile) {
        try {
            String command = "chmod 777 " + destFile.getAbsolutePath();
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFileCreateDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String pre = filePre + format.format(new Date(System.currentTimeMillis()));
        return pre;//AsusWellness-bugreport-2015-12-30
    }

    public static String getFilePre() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String pre = filePre + format.format(new Date(System.currentTimeMillis()));
        return pre; //AsusWellness-bugreport-2015-12-30-12-45
    }

    public String saveWellnessdb() {
        try {
            File backupDB = ShareUtils.getBakcupDbFile(null,true, ShareUtils.getVersionCode(getBaseContext()));
            if (backupDB == null) {

                return "";
            }
            String currentDBPath = Environment.getDataDirectory() + "//data//" + getPackageName() + "//databases//asus_wellness.db";
            File currentDB = new File(currentDBPath);
            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return backupDB.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}

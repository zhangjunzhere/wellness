package com.asus.wellness.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.wellness.EcgInfo;
import com.asus.wellness.R;
import com.asus.commonui.colorful.ColorfulLinearLayout;
import com.asus.commonui.drawerlayout.ActionBarDrawerToggle;
import com.asus.commonui.drawerlayout.DrawerLayout;
import com.asus.wellness.ga.TaskWatcherService;
import com.asus.wellness.WApplication;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.receiver.AlarmReceiver;
import com.asus.wellness.CollectInfoService;
import com.asus.wellness.DataLayerManager;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.Day;
import com.asus.wellness.ParseDataManager.ProfileData;
import com.asus.wellness.ParseDataManager.RelaxInfo;
import com.asus.wellness.ParseDataManager.StepInfo;
import com.asus.wellness.ParseDataManager.StressInfo;
import com.asus.wellness.adapter.DrawerAdapter;
import com.asus.wellness.adapter.SlidePagerAdapter;
import com.asus.wellness.provider.ActivityStateTable;
import com.asus.wellness.provider.EcgTable;
import com.asus.wellness.sleep.SleepHelper;
import com.asus.wellness.sync.SyncHelper;
import com.asus.wellness.ui.daily.DailyDetailSleepActivity;
import com.asus.wellness.ui.daily.WellnessDailyAnimUIManager;
import com.asus.wellness.ui.daily.WellnessDailyAnimUIManager.EventNotify;
import com.asus.wellness.ui.daily.WellnessDailyAnimUIManager.MOVING_TYPE;
import com.asus.wellness.ui.daily.DailyDetailActivityActivity;
import com.asus.wellness.ui.daily.DailyDetailEnergyActivity;
import com.asus.wellness.ui.daily.DailyPageFragment;
import com.asus.wellness.ui.permission.GrantPermissionActivity;
import com.asus.wellness.ui.permission.PermissionDialog;
import com.asus.wellness.ui.permission.PermissionHelper;
import com.asus.wellness.ui.profile.SetupProfileActivity;
import com.asus.wellness.ui.setting.SettingActivity;
import com.asus.wellness.utils.DeviceHelper;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;
import com.cmcm.common.statistics.CMAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import de.greenrobot.event.EventBus;

public class MainWellness extends MainBaseActivity implements View.OnTouchListener{
    private final String TAG = "Wellness-MainWellness";
    private boolean isDestroy=false;

	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    public static final int TYPE_DAILY_VIEW=0;
    public static final int TYPE_WEEKLY_VIEW=1;

    private final String SHOW_HEART = "show_heart_rate";
    private final String HIDE_HEART = "hide_heart_rate";

    public static final Object sLockObject = new Object();
    //Private shared preference will NOT be backup and restore
    public static final String PREFERENCE_PRIVATE = "private";

    public ViewPager mViewPager;
    SlidePagerAdapter mSlidePagerAdapter;
    HashMap<String, WellnessDailyAnimUIManager> mDailyAnimViewUIMap = new HashMap<String, WellnessDailyAnimUIManager>();

    WellnessDailyAnimUIManager mDailyAnimUIManager = null;
    //WeekTitleView mWeekTitleView = null;

    private int mViewType = TYPE_DAILY_VIEW;

    private long nowTime = System.currentTimeMillis();
    private long todayTime = nowTime;
    private long startTime = 0;
    private DBObserver dbObserver=new DBObserver(new Handler());

    public static final int NONE = 0;
    public static final int MSG_UPDATE_BODYINFO = 1;
    public static final int MSG_UPDATE_MINDINFO = 2;
    public static final int INFO_TYPE_BODY = 3;
    public static final int INFO_TYPE_MIND = 4;
    public static final int MSG_UPDATE_SLEEPINFO = 5;
    public static final int INFO_TYPE_SLEEP = 6;

    protected Context mContext;
    private ScheduledExecutorService mGetInfoExecutor = null;
    private GetInfoRunnable mGetInfoRunnable = null;
    private Handler mMainHandler = null;
    private long mFocusLateTime = 0;
    private Map<Long, Integer> mBodyInfoMap = Collections.synchronizedMap(new HashMap<Long,Integer>());
    private Map<Long, Integer> mMindInfoMap = Collections.synchronizedMap(new HashMap<Long, Integer>());
    private Map<Long, Integer> mSleepInfoMap = Collections.synchronizedMap(new HashMap<Long, Integer>());

    private DailyPageFragment dailyPageTimelineFragment;
    public static final String ACTION_UPDATE_TIME="action_update_time";
    public static final String ACTION_UPDATE_DATE="action_update_date";
    DataLayerManager dl;
    //calabash
    private ProgressDialog ringProgressDialog;

    private static final String VERSION_KEY = "version.key";
    private long mCreateTime;
    private AlarmReceiver alarmReceiver;

    //emily++++
    private Spinner mSpinner;
    private DeviceSpinnerAdapter mAdapter;
    private TextView mDeviceName;
    public static final int MSG_UPDATE_DEVICETYPE = 101;
    //emily----
    public static final int MSG_UPDATE_DATE_AND_DAILY_ANIM_UI = 102;
    public static final int MSG_UPDATE_DAILY_UI = 103;

    HashMap<String,String> data=new HashMap<>();
    ChangeDailyRunnable mChangeDailyRunable;
    private long mNeverShowTime = 0;
    @Override
    public  String getPageName(){
        return  MainWellness.class.getSimpleName();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mMainHandler = new MainHandler(this);
		dl=new DataLayerManager(this);


        mContext = this.getBaseContext();
        mChangeDailyRunable = new ChangeDailyRunnable();

		setContentView(R.layout.wellness_main_layout);
		setDrawerInfo();
		setInitialView();

		getContentResolver().registerContentObserver(ActivityStateTable.TABLE_URI, true, dbObserver);
		getContentResolver().registerContentObserver(EcgTable.TABLE_URI, true, dbObserver);

        mContext = this.getBaseContext();

//        if (Utility.isPadDevice(mContext)) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//        }

        ParseDataManager pdm = ParseDataManager.getInstance();
        ProfileData pfd = pdm.getProfileData(mContext);
        startTime = pfd.start_time;

        mGetInfoRunnable = null;


        IntentFilter intentF=new IntentFilter();
        intentF.addAction(ACTION_UPDATE_TIME);
        intentF.addAction(ACTION_UPDATE_DATE);
        intentF.addAction(Intent.ACTION_TIME_CHANGED);
        intentF.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(drawerBr, intentF);
/*        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo("com.asus.wellness", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int currentVersion = info.versionCode;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int lastVersion = prefs.getInt(VERSION_KEY, 0);
        if (currentVersion > lastVersion) {
            //如果当前版本大于上次版本，该版本属于第一次启�?

            //将当前版本写入preference中，则下次启动的时�?，据此判断，不再为首次启�?
            prefs.edit().putInt(VERSION_KEY,currentVersion).commit();
        }*/

        mCreateTime = System.currentTimeMillis();
        SharedPreferences mSharedPref = getSharedPreferences(SettingActivity.KEY_GA, MODE_PRIVATE);
        GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(mSharedPref.getBoolean(SettingActivity.IS_APP_OPT_OUT, true));

        alarmReceiver = new AlarmReceiver();
        alarmReceiver.startAlarm(this);

        EventBus.getDefault().postSticky(WApplication.getInstance().getConnectedDevice());

//        Intent serviceIntent=new Intent(this, SyncService.class);
//        getApplicationContext().startService(serviceIntent);
 //       insertFakeData("wellnesscalabash");
        startIntentService();
    }

    private boolean isDailyTipsShowed() {
        boolean isShowed = false;
        SharedPreferences sp = getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
        isShowed = sp.getBoolean(getString(R.string.pref_key_tips_daily_showed), false);
        return isShowed;
    }

    private void showDailyTips() {
        TipsDialog dialog = new TipsDialog();
        dialog.show(getSupportFragmentManager(), "TutorialDialog");
        dialog.setOnDismissListener(new TipsDialog.OnDismissListener() {
            @Override
            public void onDismiss(Boolean tipsDone) {
                if(!tipsDone){
                    finish();
                }
            }
        });
    }

	private BroadcastReceiver drawerBr=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
          //  Log.d("RECEIVER", " : " + intent.getAction());
            String action = intent.getAction();
            if(action.equals(ACTION_UPDATE_TIME)) {
                updateDrawer();
            } else if(action.equals(ACTION_UPDATE_DATE)) {
                nowTime = System.currentTimeMillis();
                Log.i(TAG,"ACTION_UPDATE_DATE");
                todayTime = nowTime;
                updateDate();
            } else if(action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                alarmReceiver.cancelAlarm(context);
                alarmReceiver.startAlarm(context);
                if(action.equals(Intent.ACTION_TIME_CHANGED)){
                    nowTime = System.currentTimeMillis();
                    MainWellness.this.changeDailyFragment(nowTime,false);
                }
            }
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isDestroy=true;
		getContentResolver().unregisterContentObserver(dbObserver);
//        if(mBodyInfoMap != null) {
//            synchronized (mBodyInfoMap) {
//                if (mBodyInfoMap != null) {
//                    mBodyInfoMap.clear();
//                    mBodyInfoMap = null;
//                }
//            }
//        }
//
//        if(mMindInfoMap != null) {
//            synchronized (mMindInfoMap) {
//                if (mMindInfoMap != null) {
//                    mMindInfoMap.clear();
//                    mMindInfoMap = null;
//                }
//            }
//        }
        mGetInfoRunnable = null;
        if(mMainHandler!=null)
        {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        unregisterReceiver(drawerBr);
        if(dailyPageTimelineFragment!=null)
          dailyPageTimelineFragment.cancelLoadTimelineTask();
        dl.disConnectGoogleApiClient();

        // Get tracker.
        Tracker t = GAApplication.getInstance().getTracker(getApplicationContext());

        // Build and send timing.
        t.send(new HitBuilders.TimingBuilder()
                .setCategory("Timing")
                .setValue(System.currentTimeMillis() - mCreateTime)
                .setVariable("App remain timing")
                .setLabel("From create to destroy")
                .build());

        alarmReceiver.cancelAlarm(this);
	}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged()");

        ParseDataManager pdm = ParseDataManager.getInstance();
        ProfileData pfd = pdm.getProfileData(getBaseContext());
        long startTime = pfd.start_time;

        if(mDailyAnimUIManager != null) {
            //updateDailyAnimUI();
            if (mMainHandler != null) {
                Message sendMsg = Message.obtain();
                sendMsg.what = MSG_UPDATE_DAILY_UI;
                mMainHandler.removeMessages(MSG_UPDATE_DAILY_UI);
                mMainHandler.sendMessage(sendMsg);
            }
            return;
        }
        // Update Date at 24:00:00
        //updateDate();
        if (mMainHandler != null) {
            Message sendMsg = Message.obtain();
            sendMsg.what = MSG_UPDATE_DATE_AND_DAILY_ANIM_UI;
            mMainHandler.removeMessages(MSG_UPDATE_DATE_AND_DAILY_ANIM_UI);
            mMainHandler.sendMessage(sendMsg);
        }
    }

    private void updateDate() {
        Device device = WApplication.getInstance().getConnectedDevice();
        String showHeart = device.getIsRobin()?  SHOW_HEART:HIDE_HEART;

        if(mDailyAnimViewUIMap.containsKey(showHeart)) {
            mDailyAnimUIManager = mDailyAnimViewUIMap.get(showHeart);
            mDailyAnimUIManager.setFirstUseAndNowTime(nowTime, startTime);
            mDailyAnimUIManager.checkAndMatchPosition();
        }else{
            mDailyAnimUIManager = createDailyAnimUIManager(nowTime,startTime);
            mDailyAnimViewUIMap.put(showHeart,mDailyAnimUIManager);
        }
    }


    private WellnessDailyAnimUIManager createDailyAnimUIManager(final long nowTime, long startTime ){
        return new WellnessDailyAnimUIManager(this, new EventNotify() {
            @Override
            public void handler(WellnessDailyAnimUIManager self, MOVING_TYPE curMoving_type, long dateTime) {
          /*  Log.d(TAG, "Notify: " + curMoving_type.toString() + " , dateTime = " + dateTime
                    + " , todayTime = " + todayTime + " , nowTime = " + nowTime);*/
                Date date = new Date(dateTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                Date date2 = new Date(todayTime);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(date2);

                Date date3 = new Date(nowTime);
                Calendar cal3 = Calendar.getInstance();
                cal3.setTime(date3);
                Log.d("Time_", "dateTime: " + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + ", todayTime: " + cal2.get(Calendar.DAY_OF_MONTH) + "/" + cal2.get(Calendar.MONTH) + ", nowTime: " + cal3.get(Calendar.DAY_OF_MONTH) + "/" + cal3.get(Calendar.MONTH));

                long timeEarly = dateTime - Utility.ONE_DAY_MS;
                long timeLate = dateTime;

                if (MOVING_TYPE.FORWARD_START.equals(curMoving_type) ||
                        MOVING_TYPE.FORWARD_END.equals(curMoving_type)) {
                    updateDailyAnimUI(timeEarly, timeLate);
                    getBodyMindInfo(timeEarly, timeLate);
                } else if (MOVING_TYPE.BACK_START.equals(curMoving_type) ||
                        MOVING_TYPE.BACK_END.equals(curMoving_type)) {
                    updateDailyAnimUI(timeEarly, timeLate);
                    getBodyMindInfo(timeEarly, timeLate);
                } else if (MOVING_TYPE.ORIGINAL_POS.equals(curMoving_type)) {
                    goToDate(dateTime);
                    updateDailyAnimUI();
                }
            }
        }, nowTime, startTime);
    }

    public void updateDailyAnimUI() {
        long timeEarly = nowTime - Utility.ONE_DAY_MS;
        long timeLate = nowTime;
        updateDailyAnimUI(timeEarly, timeLate);
        getBodyMindInfo(timeEarly, timeLate);
    }

    public void updateDailyAnimUI(long timeEarly, long timeLate) {
        int bodyEarlyData = -2, bodyLateData = -2;
        int mindEarlyData = -2, mindLateData = -2;
        int sleepEarlyData = -2, sleepLateData = -2;

        if(mBodyInfoMap != null) {
            synchronized (mBodyInfoMap) {
                if (mBodyInfoMap != null && !mBodyInfoMap.isEmpty()) {
                    if (mBodyInfoMap.get(timeEarly) != null) {
                        bodyEarlyData = mBodyInfoMap.get(timeEarly);
                    }
                    if (mBodyInfoMap.get(timeLate) != null) {
                        bodyLateData = mBodyInfoMap.get(timeLate);
                    }
                    if (bodyEarlyData != -2 || bodyLateData != -2) {
                        setDailyAnimUiInfo(MSG_UPDATE_BODYINFO, bodyEarlyData, bodyLateData);
                    }
                }
            }
        }

        if(WApplication.getInstance().getConnectedDevice().getIsRobin()){
            if(mMindInfoMap != null) {
                synchronized (mMindInfoMap) {
                    if (mMindInfoMap != null && !mMindInfoMap.isEmpty()) {
                        if (mMindInfoMap.get(timeEarly) != null) {
                            mindEarlyData = mMindInfoMap.get(timeEarly);
                        }
                        if (mMindInfoMap.get(timeLate) != null) {
                            mindLateData = mMindInfoMap.get(timeLate);
                        }
                        if (mindEarlyData != -2 || mindLateData != -2) {
                            setDailyAnimUiInfo(MSG_UPDATE_MINDINFO, mindEarlyData, mindLateData);
                        }
                    }
                }
            }
        }
        else {
            if(mSleepInfoMap != null) {
                synchronized (mSleepInfoMap) {
                    if (mSleepInfoMap != null && !mSleepInfoMap.isEmpty()) {
                        if (mSleepInfoMap.get(timeEarly) != null) {
                            sleepEarlyData = mSleepInfoMap.get(timeEarly);
                        }
                        if (mSleepInfoMap.get(timeLate) != null) {
                            sleepLateData = mSleepInfoMap.get(timeLate);
                        }
                        if (sleepEarlyData != -2 || sleepLateData != -2) {
                            setDailyAnimUiInfo(MSG_UPDATE_SLEEPINFO, sleepEarlyData, sleepLateData);
                        }
                    }
                }
            }
        }
    }

    public void getBodyMindInfo(long timeEarly, long timeLate) {
        getBodyInfo(timeEarly, timeLate, timeLate + Utility.ONE_DAY_MS);
        if(WApplication.getInstance().getConnectedDevice().getIsRobin()){
            getMindInfo(timeEarly, timeLate, timeLate + Utility.ONE_DAY_MS);
        } else{
            getSleepInfo(timeEarly, timeLate, timeLate + Utility.ONE_DAY_MS);
        }
    }

    public void setDailyAnimUiInfo(int type, int earlyData, int lateData) {
        if(mDailyAnimUIManager == null) return;
        String earlyDataStr = "", lateDataStr = "";
        switch(type) {
            case MSG_UPDATE_BODYINFO:
                earlyDataStr = (earlyData >= 0) ? String.valueOf(earlyData)+"%" : "";
                lateDataStr = (lateData >= 0) ? String.valueOf(lateData)+"%" : "0%";
                mDailyAnimUIManager.setActivityText(lateDataStr, earlyDataStr);
                break;
            case MSG_UPDATE_MINDINFO:
                earlyDataStr = (earlyData >= 0) ? String.valueOf(earlyData) : "";
                lateDataStr = (lateData >= 0) ? String.valueOf(lateData) : "0";
                mDailyAnimUIManager.setEnergyText(lateDataStr, earlyDataStr);
                break;
            case MSG_UPDATE_SLEEPINFO:
                earlyDataStr = (earlyData >= 0) ? String.valueOf(earlyData) : "";
                lateDataStr = (lateData >= 0) ? String.valueOf(lateData) : "0";
                mDailyAnimUIManager.setEnergyText(lateDataStr, earlyDataStr);
                break;
            default:
                break;
        }
        setCircleData();
    }

	class DBObserver extends ContentObserver{

		public DBObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
            //smile_gao add for startTime
            ParseDataManager pdm = ParseDataManager.getInstance();
            ProfileData pfd = pdm.getProfileData(getBaseContext());
            long dbStartTime = pfd.start_time;
            nowTime = System.currentTimeMillis();
            if (dbStartTime != startTime){
                startTime = dbStartTime;
                updateDate();
            }
            else {
                changeDailyFragment(nowTime,false);
                updateDailyAnimUI();
            }
		}
	}

	private void setActionBar(){
		setActionBarTitle();
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));

		//getActionBar().setIcon(R.drawable.asus_wellness_btn_menu);

//		int actionbarTitleId=getResources().getIdentifier("action_bar_title", "id", "android");
//		TextView abTitle = (TextView)findViewById(actionbarTitleId);
//		abTitle.setTextColor(0xff4c4c4c);
//		abTitle.setTextSize(getResources().getDimensionPixelSize(R.dimen.daily_actionbar_title_size));

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
	}


    private void setActionBarTitle(){
        //emily++++
        View actionbarLayout = LayoutInflater.from(this).inflate(R.layout.actionbar_title_layout, null);
        TextView titleTv = (TextView) actionbarLayout.findViewById(R.id.actionbar_title);
        mSpinner = (Spinner) actionbarLayout.findViewById(R.id.actionbar_spinner);
        mDeviceName = (TextView) actionbarLayout.findViewById(R.id.actionbar_device_name);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(actionbarLayout);

        mAdapter= new DeviceSpinnerAdapter(this);
        mSpinner.setAdapter(mAdapter);
//        mSpinner.setSelection(WApplication.getInstance().isZenWatchRobin() ? 0 : 1);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long selectedId = mSpinner.getSelectedItemId();
                Device deviceNew = mAdapter.getItem(position);
                WApplication application = WApplication.getInstance();
                Device deviceOld = application.getConnectedDevice();
                application.setConnectedDevice(deviceNew);
                if (!deviceNew.getName().equals(deviceOld.getName())) {
                    EventBus.getDefault().postSticky(deviceNew);
                }
                DeviceHelper.updateDeviceConnectTime(deviceNew);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        updateSpinner();

        //emily----

        ParseDataManager parseDataManager=ParseDataManager.getInstance();
        ProfileData profile=parseDataManager.getProfileData(this);
        if (profile != null){
            String title=String.format(getString(R.string.daily_actionbar_title), profile.name);
            if (title == null){
                title = "unknown";
            }
            titleTv.setText(title);
        }
    }

    private void updateSpinner(){
        DeviceSpinnerAdapter adapter = (DeviceSpinnerAdapter)mSpinner.getAdapter();
        adapter.notifyDataSetChanged();

        int count = mSpinner.getAdapter().getCount();
        switch(count){
            case 0:
                mSpinner.setVisibility(View.GONE);
                mDeviceName.setVisibility(View.VISIBLE);
                mDeviceName.setText("ASUS ZenWatch");
                break;
            case 1: {
                mSpinner.setVisibility(View.GONE);
                mDeviceName.setVisibility(View.VISIBLE);
                Device device = adapter.getItem(0);
                mDeviceName.setText(device.getName());
            }
            break;
            default:
                mSpinner.setVisibility(View.VISIBLE);
                mDeviceName.setVisibility(View.GONE);
                break;
        }

        final int spinnerVerticalOffset = (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT +1)? 70 : 0;
        mSpinner.setDropDownVerticalOffset(spinnerVerticalOffset);
        mSpinner.setOnTouchListener(mIsShowingTips ? this : null);
    }


    public void setInitialView(){
        if (!isDailyTipsShowed() ){//&& !Utility.isPadDevice(mContext)) {
//            getActionBar().hide();
            //showDailyTips();
//            return;
        }else {
            //history data for cm
            CmHelper.uploadHistoryData(mContext);
        }
        CMAgent.onServiceActive();
        setActionBar();
        getActionBar().show();
        dailyView(null);
        this.changeDailyFragment(nowTime, false);
    }
    private void doSync()
    {
        Log.i("MainWellness", "request sync data");
        if(dl.isConnected())
        {
            dl.requestSyncData();
        }
        else {
            dl.connectGoogleApiClient(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {

                    dl.requestSyncData();
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_PICK:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Utility.pickImage(MainWellness.this);
                }
                break;
            case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_TAKEPHOTO:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Utility.takePhoto(MainWellness.this);
                }
                break;
        }
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_DENIED && mNeverShowTime>0)
        {
            long span = System.currentTimeMillis()-mNeverShowTime;
            Log.i("smile","onRequestPermissionsResult denied span "+span);
            if(span < GrantPermissionActivity.NEVER_SHOW_TIME)
            {
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fragment = getFragmentManager().findFragmentByTag(PermissionDialog.TAG);
                if(fragment!=null)
                {
                    ft.remove(fragment);
                }
                PermissionDialog mPermissoinDialog =new PermissionDialog();
                Bundle bd = new Bundle();
                bd.putString(PermissionDialog.TITLE_KEY,getString(R.string.allow_access_photos_title));
                bd.putString(PermissionDialog.CONTENT_KEY,getString(R.string.allow_access_photos_content));
                bd.putString(PermissionDialog.PACKAGENAME_KEY,getPackageName());
                mPermissoinDialog.setArguments(bd);
                mPermissoinDialog.show(ft,PermissionDialog.TAG);
                //  Utility.openSettingAppInfo(getPackageName(),this);
            }
        }
        mNeverShowTime =0;
        Log.i("smile", "onRequestPermissionsResult " + requestCode);
    }


    void checkPermission()
    {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return;
//        }
        if(!PermissionHelper.checkPermission(Manifest.permission.BODY_SENSORS,this))
        {
            Intent intent = new Intent(this, GrantPermissionActivity.class);
            intent.putExtra(GrantPermissionActivity.TYPEKEY,GrantPermissionActivity.TYPE_BODY_SENSOR);
            startActivityForResult(intent,GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_BODYSENSOR);
 //           PermissionHelper.grantPermission(Manifest.permission.BODY_SENSORS, this, GrantPermissionActivity.TYPE_BODY_SENSOR);
        }
//        String[] permissons = new String[]{
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.BODY_SENSORS,
//              //  Manifest.permission.GET_ACCOUNTS,
//              //  "com.google.android.apps.photos.permission.GOOGLE_PHOTOS"
//        };
//        List<String> needGrantPermissionList = new ArrayList<>();
//        for(String p : permissons)
//        {
//            if(!PermissionHelper.checkPermission(p, this))
//            {
//                needGrantPermissionList.add(p);
//            }
//        }
//        if(needGrantPermissionList.size()>0)
//        {
//            PermissionHelper.grantPermission(needGrantPermissionList, this, 111);
//        }
//        else
//        {
//            Log.i("smile", " needGrantPermissionList size =0 ");
//        }
//
//        checkPermission( Manifest.permission.READ_EXTERNAL_STORAGE);
//        checkPermission( Manifest.permission.ACCESS_COARSE_LOCATION);
//        checkPermission(Manifest.permission.BODY_SENSORS);
//        checkPermission("com.google.android.apps.photos.permission.GOOGLE_PHOTOS");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isDailyTipsShowed()) {
            checkPermission();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        /* checkPermission();*/
        doSync();
		dl.isConnected(new ResultCallback<GetConnectedNodesResult>(){
			@Override
			public void onResult(GetConnectedNodesResult arg0) {
				// TODO Auto-gener ated method stub
				List<Node> listNode=arg0.getNodes();
				Log.d("circle","onresume check");
				if(listNode.size()==0){
					Toast.makeText(MainWellness.this, getString(R.string.not_connected_to_wellness),1000).show();
				}
			}
		});
//        dl.retrieveStepFromWatch();

		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
		boolean isLocationEnable=SettingActivity.checkLocationServiceEnable(this);
		boolean isActivityTrackingEnable = sp.getBoolean(getString(R.string.pref_key_location), getResources().getBoolean(R.bool.default_location));
		if(isLocationEnable && isActivityTrackingEnable){
			Intent intent=new Intent(this, CollectInfoService.class);
			startService(intent);
		}
		updateDrawer();
		setActionBarTitle();

        //

	}
    public void onEventMainThread(Device device)
    {
     //   Log.i("smile", "onEvent " + device.getName()+" isrobin: "+device.getIsRobin());
        goToDate(nowTime);

        int visibility = !DeviceHelper.isDeviceValid(device) || device.getIsRobin() ? View.VISIBLE: View.GONE;
        Log.i("smile", "onEvent " + device.getName()+" isrobin: "+device.getIsRobin()+" visible: "+visibility);
        //findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.R_mind).setVisibility(visibility);
//        Handler handler = new Handler();
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
  //      DeviceSpinnerAdapter adapter = (DeviceSpinnerAdapter)mSpinner.getAdapter();
        /*mAdapter.notifyDataSetChanged();
        mSpinner.setSelection(0);*/
//        if(device == )
//        adapter.notifyDataSetChanged();
//
//        int count = mSpinner.getAdapter().getCount();
//        switch(count){
//            case 0:
//                mSpinner.setVisibility(View.GONE);
//                mDeviceName.setVisibility(View.VISIBLE);
//                mDeviceName.setText("ASUS ZenWatch");
//                break;
//            case 1: {
//                mSpinner.setVisibility(View.GONE);
//                mDeviceName.setVisibility(View.VISIBLE);
//                mDeviceName.setText(device.getName());
//            }
//                break;
//            default:
//                mSpinner.setVisibility(View.VISIBLE);
//                mDeviceName.setVisibility(View.GONE);
//                break;
//        }

            //emily++++
        if (mDailyAnimUIManager != null) {
            if (mMainHandler != null) {
                Message sendMsg = Message.obtain();
                sendMsg.what = MSG_UPDATE_DEVICETYPE;
                mMainHandler.removeMessages(MSG_UPDATE_DEVICETYPE);
                mMainHandler.sendMessage(sendMsg);
            }
        }
        //emily----

        if(mViewType == TYPE_WEEKLY_VIEW) {
            weeklyView(null);
        }
    }


    private void setButton(int type) {
        Button daily = (Button) findViewById(R.id.daily_button);
        Button weekly=(Button) findViewById(R.id.weekly_button);
		switch(type){
		case TYPE_DAILY_VIEW:
			daily.setBackgroundResource(R.drawable.asus_wellness_tab_bottom_p);
			daily.setTextColor(0xffffffff);
			weekly.setBackgroundResource(R.drawable.asus_wellness_tab_bottom);
			weekly.setTextColor(0xff4e4e4e);
			break;
		case TYPE_WEEKLY_VIEW:
			weekly.setBackgroundResource(R.drawable.asus_wellness_tab_bottom_p);
			weekly.setTextColor(0xffffffff);
			daily.setBackgroundResource(R.drawable.asus_wellness_tab_bottom);
			daily.setTextColor(0xff4e4e4e);
			break;
		}
		daily.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED)
                , MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
		weekly.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED)
                , MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
		if(daily.getMeasuredWidth()>=weekly.getMeasuredWidth()){
			LayoutParams params=weekly.getLayoutParams();
			params.width=daily.getMeasuredWidth();
			weekly.setLayoutParams(params);
		}
		else{
			LayoutParams params=daily.getLayoutParams();
			params.width=weekly.getMeasuredWidth();
			daily.setLayoutParams(params);
		}
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		switch(item.getItemId()){
			case R.id.setting:
				Intent intent=new Intent(this, SettingActivity.class);
				startActivity(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
    }

	private void setDrawerInfo(){
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerToggle = new ActionBarDrawerToggle(
		        this,                  /* host Activity */
		    mDrawerLayout,         /* DrawerLayout object */
		    R.drawable.asus_wellness_btn_menu,  /* nav drawer image to replace 'Up' caret */
		    R.string.weekly_info_activity_title,  /* "open drawer" description for accessibility */
		    R.string.weekly_info_activity_title  /* "close drawer" description for accessibility */
		            ) {
		        public void onDrawerClosed(View view) {
		        }
		        public void onDrawerOpened(View drawerView) {
		        }
		        public void onDrawerStateChanged(int newState) {
		            if (newState == DrawerLayout.STATE_IDLE) {
		                if (mViewType == TYPE_DAILY_VIEW && mDailyAnimUIManager != null) {
		                    mDailyAnimUIManager.checkAndMatchPosition();
		                }
		            }
		        }
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		boolean isTranslucentEnabled = (getResources().getIdentifier(
		       "windowTranslucentStatus", "attr", "android") != 0);
		if (!isTranslucentEnabled) {
			    // non-ASUS devices
			getActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK)); // App's action bar background color.
			((ColorfulLinearLayout) findViewById(R.id.colorful_layout)).setActionBarBackgroundVisibility(View.GONE);
			((ColorfulLinearLayout) findViewById(R.id.colorful_layout)).setStatusBarBackgroundVisibility(View.GONE);
			mDrawerLayout.attachActivity(this);
		} else {
			    // Asus devices or Android version >= 4.4
		    mDrawerLayout.attachActivity(this, (ColorfulLinearLayout) findViewById(R.id.colorful_layout), Color.BLACK);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    mDrawerToggle.syncState();
	}

	private void updateDrawer(){

		mDrawerList.setAdapter(new DrawerAdapter(this,mMainHandler));
	}

	public void editProfile(View view){
		Intent intent=new Intent(this, SetupProfileActivity.class);
		startActivity(intent);
	}

    class WeekPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                //Log.d(TAG, "SCROLL_STATE_DRAGGING");
            } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                //Log.d(TAG, "SCROLL_STATE_SETTLING");
            } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                //Log.d(TAG, "SCROLL_STATE_IDLE");
            }
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
            //Log.d(TAG, "onPageScrolled(): offset = " + offset + " , offsetPixels = " + offsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            //Log.d(TAG, "onPageSelected(): position = " + position);
        }
    }

/*
    class WeekPageChangeListener implements ViewPager.OnPageChangeListener {
        int title_page_offset = mWeekTitleView.getCurrentWeekIndex() - (mSlidePagerAdapter.getCount() - 1);
        int nowPos = -1;
        float prevOffset = 0f;
        boolean isGoNext = false;
        boolean isScrollToBegin = true;

        @Override
        public void onPageScrollStateChanged(int state) {
            //Log.d(TAG, "onPageScrollStateChanged");
            if(state == ViewPager.SCROLL_STATE_DRAGGING) {
                //Log.d(TAG, "SCROLL_STATE_DRAGGING, nowPos = " + nowPos);
                mWeekTitleView.notifyVpStateScroll();
            } else if(state == ViewPager.SCROLL_STATE_SETTLING) {
                //Log.d(TAG, "SCROLL_STATE_SETTLING, nowPos = " + nowPos);
            } else if(state == ViewPager.SCROLL_STATE_IDLE) {
                //Log.d(TAG, "SCROLL_STATE_IDLE, nowPos = " + nowPos);
                reset();
            }
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
            //Log.d(TAG, "onPageScrolled(): offset = " + offset + " , offsetPixels = " + offsetPixels
            //        + " , isJumpTo = " + mWeekTitleView.isJumpTo());
            nowPos = position;
            if(offset == 0) {
                isScrollToBegin = true;
                return;
            }
            if(isScrollToBegin) {
                if (mWeekTitleView.isJumpTo()) {
                    prevOffset = 1f;
                    isGoNext = false;
                } else {
                    if (offset < 0.5) {
                        prevOffset = 0f;
                        isGoNext = true;
                    } else {
                        prevOffset = 1f;
                        isGoNext = false;
                    }
                }
                isScrollToBegin = false;
            } else {
                float offsetDelta = offset - prevOffset;
                if (offsetDelta < 0) {
                    if (isGoNext == true && offsetDelta < -0.3) {
                        if (offset < 0.5) {
                            prevOffset = 0f;
                            isGoNext = true;
                        } else {
                            prevOffset = 1f;
                            isGoNext = false;
                        }
                    } else {
                        isGoNext = false;
                        mWeekTitleView.vpScrollX(offsetDelta);
                        prevOffset = offset;
                    }
                } else if (offsetDelta > 0) {
                    if(mWeekTitleView.isJumpTo()) {
                        prevOffset = 1f;
                        isGoNext = false;
                        return;
                    }
                    if (isGoNext == false && offsetDelta > 0.3) {
                        if(offset < 0.5) {
                            prevOffset = 0f;
                            isGoNext = true;
                        } else {
                            prevOffset = 1f;
                            isGoNext = false;
                        }
                    } else {
                        isGoNext = true;
                        mWeekTitleView.vpScrollX(offsetDelta);
                        prevOffset = offset;
                    }
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
        }

        private void reset() {
            prevOffset = 0f;
            isScrollToBegin = true;
            mWeekTitleView.notifyVpStateIdle();
            int step = nowPos + title_page_offset - mWeekTitleView.getCurrentWeekIndex();
            if(step > 0) {
                mWeekTitleView.goNextStep(step);
            } else if(step < 0) {
                mWeekTitleView.goPreviousStep(-step);
            } else {
                mWeekTitleView.vpScrollDone();
            }
        }
    }
*/

    private void setSlidePager(int type) {
        //mWeekTitleView = (WeekTitleView)findViewById(R.id.week_title_view);
        mViewPager = (ViewPager)findViewById(R.id.wellness_info_pager);
        mSlidePagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(), type, this);
        try{
          //  if (mViewPager.getAdapter() == null){
                mViewPager.setAdapter(mSlidePagerAdapter);
        //    }
            mViewPager.setCurrentItem(mSlidePagerAdapter.getCount());
            //mWeekTitleView.reset();
            mViewPager.setOnPageChangeListener(new WeekPageChangeListener());
        }catch (Exception e){
            Log.e(TAG, "setSlidePager: " + e);
        }

    }

	public void dailyView(View view){
        mViewType = TYPE_DAILY_VIEW;
		setButton(TYPE_DAILY_VIEW);
		findViewById(R.id.daily_view_layout_container).setVisibility(View.VISIBLE);
	    findViewById(R.id.week_view_layout_container).setVisibility(View.GONE);
		findViewById(R.id.wellness_info_pager).setVisibility(View.GONE);

        Utility.trackerScreennView(getApplicationContext(), "App Dialy");
       /* // Get tracker
        Tracker t = GAApplication.getInstance().getTracker(getApplicationContext());
        // Set screen name.
        t.setScreenName("App Daily");
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());*/

        data.clear();
        data.put("projectid", "dailypage");
        data.put("operation", "1");
        CMAgent.onEvent(CmHelper.PAGEACTION_MSG_ID, data);
	}

	public void weeklyView(View view){
        mViewType = TYPE_WEEKLY_VIEW;
		findViewById(R.id.daily_view_layout_container).setVisibility(View.GONE);
	    findViewById(R.id.week_view_layout_container).setVisibility(View.VISIBLE);
		findViewById(R.id.wellness_info_pager).setVisibility(View.VISIBLE);
		setButton(TYPE_WEEKLY_VIEW);
		setSlidePager(TYPE_WEEKLY_VIEW);
        data.clear();
        data.put("projectid", "weeklypage");
        data.put("operation", "1");
        CMAgent.onEvent(CmHelper.PAGEACTION_MSG_ID, data);

        Utility.trackerScreennView(getApplicationContext(), "App Weekly");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.main_wellness_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void startDetailActivity(View view){
        ParseDataManager parseDataManager=ParseDataManager.getInstance();
        Day day = parseDataManager.getDay(nowTime);
        if(parseDataManager.getDayStepInfo(this,day)==null && parseDataManager.getDayWorkoutInfo(this, day.startTimeMilli, day.endTimeMilli)==null
                && parseDataManager.getDayRunInfo(this, day)==null && parseDataManager.getDayHeartRateInfo(this, day)==null){
            return;
        }
		Intent intent=new Intent(this, DailyDetailActivityActivity.class);
        intent.putExtra(DailyDetailActivityActivity.KEY_DAY_INFO, parseDataManager.getDay(nowTime));
		startActivity(intent);
	}

	public void startDetailEnergyLevel(View view){
        if (WApplication.getInstance().getConnectedDevice().getIsRobin()){
            ParseDataManager parseDataManager=ParseDataManager.getInstance();
            Day day = parseDataManager.getDay(nowTime);
            if (parseDataManager.getDayStressInfo(this, day)==null && parseDataManager.getDayRelaxInfo(this, day)==null){
                return;
            }
            Intent intent=new Intent(this, DailyDetailEnergyActivity.class);
            intent.putExtra(DailyDetailActivityActivity.KEY_DAY_INFO,day);
            startActivity(intent);
        }
        else{
            ParseDataManager parseDataManager=ParseDataManager.getInstance();
            Day day = parseDataManager.getDay(nowTime);
            if( SleepHelper.getSleepQualityByDate(day.startTimeMilli) == null){
                return;
            }
            Intent intent=new Intent(this, DailyDetailSleepActivity.class);
            intent.putExtra(DailyDetailActivityActivity.KEY_DAY_INFO,day);
            startActivity(intent);
        }
	}

	public long getFocusTime() {
	    return nowTime;
	}

    public void goPreviousDate() {
        //Log.d(TAG, "goPreviousDate");
        goToDate(nowTime - Utility.ONE_DAY_MS);
    }

    public void goNextDate() {
        //Log.d(TAG, "goNextDate");
        goToDate(nowTime + Utility.ONE_DAY_MS);
    }

    public void goToDate(long timeMS) {
        int dayOffset = 0;
        if (timeMS != nowTime) {
            if (timeMS <= startTime) {
                nowTime = startTime;
            } else if (timeMS >= todayTime) {
                nowTime = todayTime;
            } else {
                dayOffset = Utility.getDateOffset(timeMS, nowTime);
                nowTime += (dayOffset * Utility.ONE_DAY_MS);
            }
        }
        Log.d(TAG, "goToDate : " + Utility.getDateTime(nowTime, "MM/dd"));
        changeDailyFragment(nowTime, false);
    }

	public void changeDailyFragment(long time, boolean isNeedUpdate){
		Log.d("circle", "change daily:" + Utility.getDateTime(time, "yyyy-MM-dd hh:mm a") + " time: " + time);
       if(mMainHandler!=null) {
           mMainHandler.removeCallbacks(mChangeDailyRunable);
           mChangeDailyRunable.setTime(time);
           mChangeDailyRunable.setNeedUpdate(isNeedUpdate);
           mMainHandler.postDelayed(mChangeDailyRunable, 100);
       }
	}
    class ChangeDailyRunnable implements Runnable {
        public ChangeDailyRunnable()
        {

        }
        long  time;
        boolean isNeedUpdate;
        public void setTime(long time)
        {
            this.time = time;
        }
        public void setNeedUpdate(boolean isNeedUpdate)
        {
            this.isNeedUpdate = isNeedUpdate;
        }

        @Override
        public void run() {
            changeDailyFramgmentImpl(time, isNeedUpdate);
        }
    }
    private void changeDailyFramgmentImpl(long time ,boolean isNeedUpdate)
    {
        Log.d("smile", "changeDailyFramgmentImpl: "+time+" isNeedUpdate: "+isNeedUpdate);
        if(dailyPageTimelineFragment!=null){
//            if(time == dailyPageTimelineFragment.getNowTime() && !isNeedUpdate)
//            {
//                Log.d("smile","changeDailyFragment time same return ");
//                return;
//            }
            dailyPageTimelineFragment.cancelLoadTimelineTask();
        }
        dailyPageTimelineFragment = new DailyPageFragment();
        dailyPageTimelineFragment.setNowTime(time);
        Bundle args = new Bundle();
        args.putLong(DailyPageFragment.ONEDAY_TIME_MILLI, time);
        dailyPageTimelineFragment.setArguments(args);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.daily_fragment_container, dailyPageTimelineFragment);
        if(!isDestroy){
            transaction.commitAllowingStateLoss();
        }
    }

    public void getBodyInfo(long earlyTime, long lateTime, long nextTime) {
        if(mGetInfoExecutor == null) {
            mGetInfoExecutor = new ScheduledThreadPoolExecutor(1);
        }
        if(mBodyInfoMap == null) {
            synchronized (this) {
                if(mBodyInfoMap == null) {
                    mBodyInfoMap = Collections.synchronizedMap(new HashMap<Long,Integer>());
                }
            }
        }
        //if (mGetInfoRunnable == null) {
        mGetInfoRunnable = new GetInfoRunnable();
        //}
        mFocusLateTime = lateTime;
        mGetInfoRunnable.toGetBodyInfo(earlyTime, lateTime, nextTime);
        mGetInfoExecutor.submit(mGetInfoRunnable);
    }

    public void getMindInfo(long earlyTime, long lateTime, long nextTime) {
        if(mGetInfoExecutor == null) {
            mGetInfoExecutor = new ScheduledThreadPoolExecutor(1);
        }
        if(mMindInfoMap == null) {
            synchronized (this) {
                if (mMindInfoMap == null) {
                    mMindInfoMap = Collections.synchronizedMap(new HashMap<Long,Integer>());
                }
            }
        }
        //if (mGetInfoRunnable == null) {
        mGetInfoRunnable = new GetInfoRunnable();
        //}
        mFocusLateTime = lateTime;
        mGetInfoRunnable.toGetMindInfo(earlyTime, lateTime, nextTime);
        mGetInfoExecutor.submit(mGetInfoRunnable);
    }

    public void getSleepInfo(long earlyTime, long lateTime, long nextTime) {
        if(mGetInfoExecutor == null) {
            mGetInfoExecutor = new ScheduledThreadPoolExecutor(1);
        }
        if(mSleepInfoMap == null) {
            synchronized (this) {
                if (mSleepInfoMap == null) {
                    mSleepInfoMap = Collections.synchronizedMap(new HashMap<Long,Integer>());
                }
            }
        }
        //if (mGetInfoRunnable == null) {
        mGetInfoRunnable = new GetInfoRunnable();
        //}
        mFocusLateTime = lateTime;
        mGetInfoRunnable.toGetSleepInfo(earlyTime, lateTime, nextTime);
        mGetInfoExecutor.submit(mGetInfoRunnable);
    }

    private class GetInfoRunnable implements Runnable {
        private long mEarlyTime = 0, mLateTime = 0, mNextTime = 0;
        private int infoType = NONE;

        public void toGetBodyInfo(long earlyTime, long lateTime, long nextTime) {
            mEarlyTime = earlyTime;
            mLateTime = lateTime;
            mNextTime = nextTime;
            infoType = INFO_TYPE_BODY;
        }

        public void toGetMindInfo(long earlyTime, long lateTime, long nextTime) {
            mEarlyTime = earlyTime;
            mLateTime = lateTime;
            mNextTime = nextTime;
            infoType = INFO_TYPE_MIND;
        }

        public void toGetSleepInfo(long earlyTime, long lateTime, long nextTime) {
            mEarlyTime = earlyTime;
            mLateTime = lateTime;
            mNextTime = nextTime;
            infoType = INFO_TYPE_SLEEP;
        }

        @Override
        public void run() {
            int earlyData = -1, lateData = -1, nextData = -1;
            Message sendMsg = null;
            if(mLateTime != mFocusLateTime) {
                return;
            }
            switch(infoType) {
                case INFO_TYPE_BODY:
                    earlyData = getCompletion(mEarlyTime);
                    lateData = getCompletion(mLateTime);
                    nextData = getCompletion(mNextTime);
                    sendMsg = Utility.generateMessage(null, earlyData, lateData, MSG_UPDATE_BODYINFO);
                    mBodyInfoMap.clear();
                    mBodyInfoMap.put(mEarlyTime, earlyData);
                    mBodyInfoMap.put(mLateTime, lateData);
                    mBodyInfoMap.put(mNextTime, nextData);
                    break;
                case INFO_TYPE_MIND:
                    earlyData = getAverageEnergy(mEarlyTime);
                    lateData = getAverageEnergy(mLateTime);
                    nextData = getAverageEnergy(mNextTime);
                    sendMsg = Utility.generateMessage(null, earlyData, lateData, MSG_UPDATE_MINDINFO);
                    mMindInfoMap.clear();
                    mMindInfoMap.put(mEarlyTime, earlyData);
                    mMindInfoMap.put(mLateTime, lateData);
                    mMindInfoMap.put(mNextTime, nextData);
                    break;
                case INFO_TYPE_SLEEP:
                    earlyData = getSleepScore(mEarlyTime);
                    lateData = getSleepScore(mLateTime);
                    nextData = getSleepScore(mNextTime);
                    sendMsg = Utility.generateMessage(null, earlyData, lateData, MSG_UPDATE_SLEEPINFO);
                    mSleepInfoMap.clear();
                    mSleepInfoMap.put(mEarlyTime, earlyData);
                    mSleepInfoMap.put(mLateTime, lateData);
                    mSleepInfoMap.put(mNextTime, nextData);
                    break;
            }
            if(mMainHandler != null && sendMsg != null) {
                mMainHandler.sendMessage(sendMsg);
            }
        }

       /* private int getCompletion(long targetTime) {
            ParseDataManager pfd = ParseDataManager.getInstance();
            Day day = pfd.getDay(targetTime);
            ArrayList<StepInfo> arrayStepInfo = pfd.getDayStepInfo(mContext, day);
            int count = 0;
            if (arrayStepInfo != null) {
                for (StepInfo info : arrayStepInfo) {
                    count += info.stepCount;
                }
            } else {
               // Log.d(TAG, "no completion data");
                return -1;
            }
            int stepGoal = pfd.getDayStepGoal(mContext, day);
            int completion = (int)((float)count / stepGoal * 100);
            *//*if(completion>=100){
                completion=100;
            }*//*
            //Log.d(TAG, "completion: " + completion);
            return completion;
        }*/



        /*private int getSleepScore(long targetTime){
            ParseDataManager pfd =ParseDataManager.getInstance();;
            Day day = pfd.getDay(targetTime);
            EcgInfo ecgInfo = pfd.getDaySleepDataFromEcg(day.startTimeMilli);
            if(ecgInfo.hasData && ecgInfo.totalMins>0){
                int score =ecgInfo.score;
                return score;
            }
            else {
                return -1;
            }
        }*/
    }

    private int getCompletion(long targetTime) {
        ParseDataManager pfd = ParseDataManager.getInstance();
        Day day = pfd.getDay(targetTime);
        ArrayList<StepInfo> arrayStepInfo = pfd.getDayStepInfo(mContext, day);
        int count = 0;
        if (arrayStepInfo != null) {
            for (StepInfo info : arrayStepInfo) {
                count += info.stepCount;
            }
        } else {
            // Log.d(TAG, "no completion data");
            return -1;
        }
        int stepGoal = pfd.getDayStepGoal(mContext, day);
        int completion = (int)((float)count / stepGoal * 100);
            /*if(completion>=100){
                completion=100;
            }*/
        //Log.d(TAG, "completion: " + completion);
        return completion;
    }

    private int getAverageEnergy(long targetTime) {
        ParseDataManager pfd =ParseDataManager.getInstance();;
        Day day = pfd.getDay(targetTime);
        ArrayList<StressInfo> arrayStressInfo = pfd.getDayStressInfo(mContext, day);
        ArrayList<RelaxInfo> arrayRelaxInfo = pfd.getDayRelaxInfo(mContext, day);

        int count = 0;
        int allEnergy = 0;
        if (arrayStressInfo != null) {
            for (StressInfo info : arrayStressInfo) {
                count++;
                allEnergy += (100 - info.measureValue);
            }
        }
        if (arrayRelaxInfo != null) {
            for (RelaxInfo info : arrayRelaxInfo) {
                count++;
                allEnergy += info.measureValue;
            }
        }
        if (count == 0) {
            // Log.d(TAG, "no energy data");
            return -1;
        } else {
            //Log.d(TAG, "energy: " + (allEnergy / count));
            return allEnergy / count;
        }
    }

    private int getSleepScore(long targetTime){
        ParseDataManager pfd =ParseDataManager.getInstance();;
        Day day = pfd.getDay(targetTime);
        EcgInfo ecgInfo = pfd.getDaySleepDataFromEcg(day.startTimeMilli);
        if(ecgInfo.hasData && ecgInfo.totalMins>0){
            int score =ecgInfo.score;
            return score;
        }
        else {
            return -1;
        }
    }

    private class MainHandler extends Handler {
        MainWellness mMainWellness = null;

        public MainHandler(MainWellness instance) {
            mMainWellness = instance;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_BODYINFO:
                    mMainWellness.setDailyAnimUiInfo(MSG_UPDATE_BODYINFO, msg.arg1, msg.arg2);
                    break;
                case MSG_UPDATE_MINDINFO:
                    mMainWellness.setDailyAnimUiInfo(MSG_UPDATE_MINDINFO, msg.arg1, msg.arg2);
                    break;
                case MSG_UPDATE_SLEEPINFO:
                    mMainWellness.setDailyAnimUiInfo(MSG_UPDATE_SLEEPINFO, msg.arg1, msg.arg2);
                    break;
                case SetupProfileActivity.REQUEST_CODE_PICK_IMAGE:
                    mNeverShowTime = System.currentTimeMillis();
                    PermissionHelper.checkStoragePermission(MainWellness.this, GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_PICK);
                    // Utility.pickImage(MainWellness.this);
                    break;
                case MSG_UPDATE_DEVICETYPE:
                    //Log.i("emily","handle updateDailyUI message");
                    updateDailyUI(WApplication.getInstance().getConnectedDevice().getIsRobin());
                    break;
                //smile_gao add for take photo
                case SetupProfileActivity.REQUEST_CODE_TAKE_PHOTO:
                    mNeverShowTime = System.currentTimeMillis();
                    PermissionHelper.checkStoragePermission(MainWellness.this, GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_TAKEPHOTO);
                    //Utility.takePhoto(MainWellness.this);
                    break;
                case MSG_UPDATE_DATE_AND_DAILY_ANIM_UI:
                    updateDataAndDailyAnimUI();
                    break;
                case MSG_UPDATE_DAILY_UI:
                    updateDailyAnimUI();
                    break;

                default:
                    break;
            }
        }
    }

    private void updateDataAndDailyAnimUI() {
        updateSpinnerSelection();
        updateDate();
        mDailyAnimUIManager.setActivityButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetailActivity(null);
            }
        });

        mDailyAnimUIManager.setEnergyButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetailEnergyLevel(null);
            }
        });
        updateDailyAnimUI();
        showTips();
    }
    String mPicUrl=null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK  ){
            switch(requestCode){
                case SetupProfileActivity.REQUEST_CODE_PICK_IMAGE:
                    if(data != null && data.getData()!=null){
                        Uri uri = data.getData();
                        mPicUrl = uri.toString();
                        Intent intent=new Intent("com.android.camera.action.CROP");
                        intent.setDataAndType(uri, "image/*");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utility.getOutImageUri());
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        try {
                            startActivityForResult(intent, SetupProfileActivity.REQUEST_CODE_CROP_IMAGE);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    break;
                case SetupProfileActivity.REQUEST_CODE_CROP_IMAGE:
                    String mSelectedPhotoUri = null;
                    if(data != null){
                        if( data.getData()!=null)
                        {
                            mSelectedPhotoUri=data.getData().toString();
                        }else if(data.getAction()!=null && data.getAction().startsWith("file")&& data.getAction().endsWith("jpg"))
                        {
                            mSelectedPhotoUri = data.getAction();
                        }
                    }
//                    else if(data.getExtras()!=null)
//                    {
//                        mSelectedPhotoUri = getBitmapDataFromBundle(data.getExtras(),MainWellness.this);
//                    }
                    if(mSelectedPhotoUri == null && mPicUrl!=null)
                    {
                        mSelectedPhotoUri = mPicUrl;
                        mPicUrl=null;
                    }
                    if(mSelectedPhotoUri!=null) {
                        ProfileHelper.updateProfilePhoto(mSelectedPhotoUri);
                        DrawerAdapter adapter = (DrawerAdapter) mDrawerList.getAdapter();
                        adapter.notifyDataSetChanged();
                        //smile add for sync Profile photo
                        Bitmap bitmap = Utility.getPhotoBitmap(mContext, mSelectedPhotoUri);
                        SyncHelper.sendPhptoToWear(this, bitmap, mSelectedPhotoUri);
                    }
                    break;
                case SetupProfileActivity.REQUEST_CODE_TAKE_PHOTO:
                    {
                        Uri originalUri =  SetupProfileActivity.takePhotoUri;
                        if(originalUri == null)
                        {
                             return;
                        }
                        mPicUrl =  originalUri.toString();
                        Intent intent=new Intent("com.android.camera.action.CROP");
                        intent.setDataAndType(originalUri, "image/*");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utility.getOutImageUri());
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        try {
                            startActivityForResult(intent, SetupProfileActivity.REQUEST_CODE_CROP_IMAGE);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        //                      final  Bundle bd  = data.getExtras();
                        Log.i("smile","REQUEST_CODE_TAKE_PHOTO "+originalUri.toString());

                    }
                case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_BODYSENSOR:
                    Log.i("smile","GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE ok");
                   break;
//                case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_PICK:
//                    Utility.pickImage(MainWellness.this);
//                    break;
//                case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_TAKEPHOTO:
//                    Utility.takePhoto(MainWellness.this);
//                    break;
                default:
                    break;
            }
        }
        else {
            if(requestCode == GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_BODYSENSOR)
            {
                Log.i("smile","GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE Fail!!!");
            }
        }
    }
    public  String getBitmapDataFromBundle(Bundle bd, Activity context) {
        if (bd != null) {
            File picfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            String filename = System.currentTimeMillis() + ".jpg";
            File photofile = new File(picfile.getAbsolutePath(), filename);
            Bitmap photo = bd.getParcelable("data");
            try {
                FileOutputStream outputStream = new FileOutputStream(photofile);
                photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, filename, null);
                if (!TextUtils.isEmpty(url)) {
//                    Log.i("smile", "url:  " + url);
//                    //String  mSelectedPhotoUri = url;
//                    Intent intent = new Intent("com.android.camera.action.CROP");
//                    intent.setDataAndType(Uri.parse(url), "image/*");
//                    context.startActivityForResult(intent, SetupProfileActivity.REQUEST_CODE_CROP_IMAGE);
                    return url;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
    //backdoor
//    public String insertFakeData(String arg) {
//
//    	if(arg.compareTo("wellnesscalabash") == 0)
//    	{
//			ringProgressDialog = ProgressDialog.show(MainWellness.this, "Please wait ...", "Insert data ...", true);
//			ringProgressDialog.setCancelable(true);
//			new InsertTask().execute();
//			return "True";
//    	}
//		return "False";
//    }

   public String getsmallestScreenWidthDp(){
	   String res = "";
	   int a = getResources().getConfiguration().smallestScreenWidthDp;
	   res = Integer.toString(a);
	   return res;
   }

//    class InsertTask extends AsyncTask<Void, Void, Void> {
//
//		@Override
//		protected Void doInBackground(Void... arg0) {
//			// TODO Auto-generated method stub
//			//
//			WellnessSQLiteOpenHelper.insertData(getContentResolver());
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			// TODO Auto-generated method stub
//			super.onPostExecute(result);
//			ringProgressDialog.dismiss();
//		}
//	}

    /*add by emily*/
	private void updateSpinnerSelection() {
        if(mSpinner != null && mSpinner.getAdapter().getCount()>1){
            String spinnerName = ((Device)mSpinner.getSelectedItem()).getName();
            String connectName = WApplication.getInstance().getConnectedDevice().getName();
            if(!spinnerName.equals(connectName)){
                Log.i(TAG,"updateSpinnerSelection()"+", spinnerName= " + spinnerName+", connectName = " + connectName);
                DeviceSpinnerAdapter adapter = (DeviceSpinnerAdapter)mSpinner.getAdapter();
                adapter.notifyDataSetChanged();
                updateSpinner();
                mSpinner.setSelection(0);
                changeDailyFragment(nowTime,true);
            }
        }
    }


    private void updateDailyUI(boolean isRobin) {
        //Log.i("emily", "updateDailyUI() " + isRobin);
        updateSpinner();
        mSpinner.setSelection(0);
        if (mDailyAnimUIManager != null) {
            mDailyAnimUIManager = null;
            ParseDataManager pdm = ParseDataManager.getInstance();
            ProfileData pfd = pdm.getProfileData(mContext);
            startTime = pfd.start_time;
            nowTime = System.currentTimeMillis();

            updateDate();
            mDailyAnimUIManager.setActivityButton(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDetailActivity(null);
                }
            });

            mDailyAnimUIManager.setEnergyButton(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDetailEnergyLevel(null);
                }
            });
            updateDailyAnimUI();
            updateMindSleepLeaf(isRobin);
        }
    }
    void updateMindSleepLeaf(boolean isRobin)
    {
        int visibility = isRobin ? View.VISIBLE : View.GONE;
        findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.R_mind).setVisibility(visibility);
        findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.L_body).setVisibility(visibility);
        findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.M_calendar).setVisibility(visibility);

        findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.R_sleep).setVisibility(isRobin ? View.GONE : View.VISIBLE);
        findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.L_body_sparrow).setVisibility(isRobin ? View.GONE : View.VISIBLE);
        findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.M_calendar_sparrow).setVisibility(isRobin ? View.GONE : View.VISIBLE);
    }

    public void startIntentService() {
        Log.i("larry", "start TaskWatcherService");
        Intent intent = new Intent(this, TaskWatcherService.class);
        startService(intent);
    }

    /* Tips start, add by emily */
    private boolean mIsShowingTips = false;
    private float mScaleRatio = 1.0f;
    private void showTips(){
        if (!isDailyTipsShowed() ){
            if(!mIsShowingTips) {
                gotoTipsPage1();
            }
        }
    }
    private void gotoTipsPage1(){
        Utility.trackerScreennView(getApplicationContext(), "Goto Tutorial P1");
        mScaleRatio = Utility.getScaleRatio(mContext);
        mIsShowingTips = true;
        mSpinner.setOnTouchListener(this);
        findViewById(R.id.tips_page1).setOnTouchListener(this);

        setBackgroundTransparent(findViewById(R.id.tips_page1));
        setCircleScale();

        int scaleOffset = (int)(getResources().getDimension(R.dimen.dailyanim_L1_master_circle_width_large)*(mScaleRatio -1))/2;
        View standard_L_Circle;
        View standard_R_Circle;
        int[] location_L = new int[2];
        int[] location_R = new int[2];
        if(WApplication.getInstance().getConnectedDevice().getIsRobin()) {
            standard_L_Circle = findViewById(R.id.daily_view_layout_container).findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.L_body).findViewById(R.id.tips_L_circle_standard);
            standard_R_Circle = findViewById(R.id.daily_view_layout_container).findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.R_mind).findViewById(R.id.tips_R_circle_standard);
        }
        else{
            standard_L_Circle = findViewById(R.id.daily_view_layout_container).findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.L_body_sparrow).findViewById(R.id.tips_L_circle_standard);
            standard_R_Circle = findViewById(R.id.daily_view_layout_container).findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.R_sleep).findViewById(R.id.tips_R_circle_standard);
        }
        standard_L_Circle.getLocationOnScreen(location_L);
        standard_R_Circle.getLocationOnScreen(location_R);
        int circleOffset = (int)(getResources().getDimension(R.dimen.dailyanim_L1_master_circle_width_large) - getResources().getDimension(R.dimen.dailyanim_L1_master_circle_width))/2;
        int left_L = location_L[0]+scaleOffset-circleOffset;
        int top_L = location_L[1] - getStatusBarHeight()+scaleOffset-circleOffset;
        int top_R = top_L;
        int right_R = location_L[0]+scaleOffset-circleOffset;

        if(Utility.isPadDevice(mContext)){
            top_L = location_L[1] - getStatusBarHeight() - getActionBar().getHeight()+scaleOffset-circleOffset;
            top_R = top_L;
            int left_R = location_R[0]-(int)(circleOffset*mScaleRatio);
            setCircleLayout(left_L, top_L, top_R, left_R);
            int[] result = drawLines_Pad(left_L, left_R, top_L);
            setTextViewsLayout_Pad(result);
            findViewById(R.id.tips_page1).findViewById(R.id.tips_p1_next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoTipsPage2_forPad();
                }
            });
        }
        else{
            setCircleLayout(left_L, top_L, top_R, right_R);
            int topmMargin = drawLines(left_L, right_R, top_L);
            setTextViewsLayout(topmMargin);
            findViewById(R.id.tips_page1).findViewById(R.id.tips_p1_next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoTipsPage2();
                }
            });
        }
    }

    private void gotoTipsPage2_forPad(){
        Utility.trackerScreennView(getApplicationContext(), "Goto Tutorial P2");
        findViewById(R.id.tips_page1).setVisibility(View.GONE);
        findViewById(R.id.tips_page2).setOnTouchListener(this);
        findViewById(R.id.tips_page2).setBackground(getResources().getDrawable(R.drawable.tips_bg_pad));
        findViewById(R.id.tips_page2).setVisibility(View.VISIBLE);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.tips_bg));

        findViewById(R.id.tips_page2).findViewById(R.id.tips_p2_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afterDailyTipsShowed();
            }
        });
    }

    private void gotoTipsPage2(){
        Utility.trackerScreennView(getApplicationContext(), "Goto Tutorial P2");
        findViewById(R.id.tips_page1).setVisibility(View.GONE);
        findViewById(R.id.tips_page2).setOnTouchListener(this);
        setBackgroundTransparent(findViewById(R.id.tips_page2));
        setImageViewsLayoutForPage2();

        findViewById(R.id.tips_page2).findViewById(R.id.tips_p2_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoTipsPage3();
            }
        });
    }

    private void gotoTipsPage3(){
        Utility.trackerScreennView(getApplicationContext(), "Goto Tutorial P3");
        findViewById(R.id.tips_page2).setVisibility(View.GONE);
        findViewById(R.id.tips_page3).setOnTouchListener(this);
        setBackgroundTransparent(findViewById(R.id.tips_page3));

        findViewById(R.id.tips_page3).findViewById(R.id.tips_p3_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afterDailyTipsShowed();
            }
        });
    }

    private void setCircleScale(){
        findViewById(R.id.tips_page1).findViewById(R.id.tips_L_circle).setScaleX(mScaleRatio);
        findViewById(R.id.tips_page1).findViewById(R.id.tips_L_circle).setScaleY(mScaleRatio);
        findViewById(R.id.tips_page1).findViewById(R.id.tips_R_circle).setScaleX(mScaleRatio);
        findViewById(R.id.tips_page1).findViewById(R.id.tips_R_circle).setScaleY(mScaleRatio);
    }

    private void setBackgroundTransparent(View view){
        if(view.getId() == R.id.tips_page3){
            view.setBackground(null);
            View dailyView = findViewById(R.id.daily_view_layout_container);
            View animationView = findViewById(R.id.tips_page3).findViewById(R.id.tips_p3_animation_view);
            int[] loc = new int[2];
            dailyView.getLocationOnScreen(loc);
            int offset = (int)getResources().getDimension(R.dimen.daily_info_change_date_view_height) + loc[1];
            ViewGroup.MarginLayoutParams margin=new ViewGroup.MarginLayoutParams(animationView.getLayoutParams());
            margin.bottomMargin =Utility.getScreenHeight(this)-offset;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
            layoutParams.gravity= Gravity.BOTTOM;
            animationView.setLayoutParams(layoutParams);
        }
        else{
            view.setBackground(getResources().getDrawable(R.drawable.tips_bg));
        }
        view.setVisibility(View.VISIBLE);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.tips_bg));
    }

    private void setCircleLayout(int left_L, int top_L ,int top_R, int right_R){
        View leftCircle = findViewById(R.id.tips_page1).findViewById(R.id.tips_L_circle);
        View rightCircle = findViewById(R.id.tips_page1).findViewById(R.id.tips_R_circle);
        setLeftCircleLayout(leftCircle, left_L, top_L);
        setRightCircleLayout(rightCircle, top_R, right_R);
        setCircleData();
    }

    private void setCircleData(){
        if(mIsShowingTips && findViewById(R.id.tips_page1).getVisibility() == View.VISIBLE) {
            View leftCircle = findViewById(R.id.tips_page1).findViewById(R.id.tips_L_circle);
            View rightCircle = findViewById(R.id.tips_page1).findViewById(R.id.tips_R_circle);
            if (WApplication.getInstance().getConnectedDevice().getIsRobin()) {
                ((ImageView)rightCircle.findViewById(R.id.tips_R_circle_sleep_energy)).setImageResource(R.drawable.r_circle1);
                ((TextView)rightCircle.findViewById(R.id.tips_average)).setText(getString(R.string.text_average));
                ((TextView)rightCircle.findViewById(R.id.tips_text_mind)).setText(getString(R.string.text_energy));
                int average = getAverageEnergy(Calendar.getInstance().getTimeInMillis());
                if (average < 0) {
                    average = 0;
                }
                ((TextView) rightCircle.findViewById(R.id.tips_r1_txt)).setText(String.valueOf(average));
            } else {
                ((ImageView)rightCircle.findViewById(R.id.tips_R_circle_sleep_energy)).setImageResource(R.drawable.r_sleep_circle1);
                ((TextView)rightCircle.findViewById(R.id.tips_average)).setText(getString(R.string.sleep_quality_score));
                ((TextView)rightCircle.findViewById(R.id.tips_text_mind)).setText(getString(R.string.text_sleep));
                int score = getSleepScore(Calendar.getInstance().getTimeInMillis());
                if (score < 0) {
                    score = 0;
                }
                ((TextView)rightCircle.findViewById(R.id.tips_r1_txt)).setText(String.valueOf(score));
            }
            int completion = getCompletion(Calendar.getInstance().getTimeInMillis());
            if (completion < 0) {
                completion = 0;
            }
            int width =(int)mContext.getResources().getDimension(R.dimen.dailyanim_L1_slave_circle_width);
            // (int) (mContext.getResources().getDimension(R.dimen.dailyanim_L1_slave_circle_width) * Utility.getScaleRatio(mContext));
            TextView tips_l1_txt = (TextView) leftCircle.findViewById(R.id.tips_l1_txt);
           // tips_l1_txt.getLayoutParams().width = width;
            tips_l1_txt.setText(String.valueOf(completion) + "%");
        }
    }

    public static void setLeftCircleLayout(View view,int left,int top)
    {
        ViewGroup.MarginLayoutParams margin=new ViewGroup.MarginLayoutParams(view.getLayoutParams());
        margin.leftMargin=left;
        margin.topMargin=top;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }

    /*  margin1 : top margin
       margin2 : right margin for phone, left margin for pad */
    public void setRightCircleLayout(View view,int margin1, int margin2)
    {
        FrameLayout.LayoutParams layoutParams;
        ViewGroup.MarginLayoutParams margin=new ViewGroup.MarginLayoutParams(view.getLayoutParams());
        margin.topMargin=margin1;
        if (Utility.isPadDevice(mContext)){
            margin.leftMargin = margin2;
            layoutParams = new FrameLayout.LayoutParams(margin);
        }
        else{
            margin.rightMargin=margin2;
            layoutParams = new FrameLayout.LayoutParams(margin);
            layoutParams.gravity= Gravity.RIGHT;
        }
        view.setLayoutParams(layoutParams);
    }

    private int drawLines(int leftMargin, int rightMargin, int top){
        ImageView line_1=(ImageView) findViewById(R.id.tips_page1).findViewById(R.id.tips_line_1);
        ViewGroup.MarginLayoutParams margin_1=new ViewGroup.MarginLayoutParams(line_1.getLayoutParams());
        margin_1.topMargin=top + (int)getResources().getDimension(R.dimen.dailyanim_L1_master_circle_width)/2;
        margin_1.leftMargin = leftMargin;
        margin_1.rightMargin = rightMargin;
        FrameLayout.LayoutParams layoutParams_1 = new FrameLayout.LayoutParams(margin_1);
        layoutParams_1.gravity= Gravity.CENTER_HORIZONTAL;
        line_1.setLayoutParams(layoutParams_1);

        ImageView line_2=(ImageView) findViewById(R.id.tips_page1).findViewById(R.id.tips_line_2);
        ViewGroup.MarginLayoutParams margin_2=new ViewGroup.MarginLayoutParams(line_2.getLayoutParams());
        margin_2.topMargin=margin_1.topMargin - (int)getResources().getDimension(R.dimen.tips_line_2_height);
        FrameLayout.LayoutParams layoutParams_2 = new FrameLayout.LayoutParams(margin_2);
        layoutParams_2.gravity= Gravity.CENTER_HORIZONTAL;
        line_2.setLayoutParams(layoutParams_2);

        ImageView line_3=(ImageView) findViewById(R.id.tips_page1).findViewById(R.id.tips_line_3);
        ViewGroup.MarginLayoutParams margin_3=new ViewGroup.MarginLayoutParams(line_3.getLayoutParams());
        margin_3.topMargin=margin_2.topMargin;
        margin_3.leftMargin=(int)getResources().getDimension(R.dimen.tips_line_3_width)/2;
        FrameLayout.LayoutParams layoutParams_3 = new FrameLayout.LayoutParams(margin_3);
        layoutParams_3.gravity= Gravity.CENTER_HORIZONTAL;
        line_3.setLayoutParams(layoutParams_3);
        return margin_3.topMargin;
    }

    private void setTextViewsLayout(int topMargin){
        View textViews= findViewById(R.id.tips_p1_l_tvs);
        ViewGroup.MarginLayoutParams margin=new ViewGroup.MarginLayoutParams(textViews.getLayoutParams());
        margin.topMargin=topMargin;
        margin.leftMargin=(int)getResources().getDimension(R.dimen.tips_line_3_width) + (int)getResources().getDimension(R.dimen.tips_daily_p1_text_width)/2;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        layoutParams.gravity= Gravity.CENTER_HORIZONTAL;
        textViews.setLayoutParams(layoutParams);
    }

    private void setTextViewsLayout_Pad(int[] result){
        int topMargin = result[0];
        int leftMargin = result[1];
        View textViews= findViewById(R.id.tips_p1_l_tvs);
        ViewGroup.MarginLayoutParams margin=new ViewGroup.MarginLayoutParams(textViews.getLayoutParams());
        margin.topMargin=topMargin;
        margin.leftMargin=leftMargin+ (int)getResources().getDimension(R.dimen.tips_line_3_width);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        textViews.setLayoutParams(layoutParams);
    }

    private void setImageViewsLayoutForPage2(){
        View calendarView;
        if(WApplication.getInstance().getConnectedDevice().getIsRobin()) {
            calendarView = findViewById(R.id.daily_view_layout_container).findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.M_calendar);
        }
        else{
            calendarView = findViewById(R.id.daily_view_layout_container).findViewById(R.id.wellness_daily_anim_view).findViewById(R.id.M_calendar_sparrow);
        }

        View dailyView = findViewById(R.id.daily_view_layout_container);
        int[] containerLoc = new int[2];
        dailyView.getLocationOnScreen(containerLoc);

        ImageButton todayBtn = (ImageButton)calendarView.findViewById(R.id.today_btn);
        ImageView todayBtnTipsImg = (ImageView)findViewById(R.id.tips_page2).findViewById(R.id.tips_page2_today);
        int[] location = new int[2];
        todayBtn.getLocationOnScreen(location);
        int offset = (getViewMeasure(todayBtnTipsImg)[0] - getViewMeasure(todayBtn)[0])/2;
        ViewGroup.MarginLayoutParams todayTipsMargin=new ViewGroup.MarginLayoutParams(todayBtnTipsImg.getLayoutParams());
        int left = location[0];
        todayTipsMargin.leftMargin = location[0];//(int)(getResources().getDimension(R.dimen.dailyanim_today_btn_marginLeft));
        todayTipsMargin.topMargin = location[1]-getStatusBarHeight() - getActionBar().getHeight()- getViewMeasure(todayBtnTipsImg)[1]+ getViewMeasure(todayBtn)[1] + offset;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(todayTipsMargin);
        layoutParams.gravity = Gravity.TOP;
        todayBtnTipsImg.setLayoutParams(layoutParams);

        ImageView calendarImg = (ImageView)findViewById(R.id.tips_page2).findViewById(R.id.tips_page2_calendar);
        ViewGroup.MarginLayoutParams calendarImgMargin=new ViewGroup.MarginLayoutParams(calendarImg.getLayoutParams());
        View todayView = calendarView.findViewById(R.id.c5_txt);
        todayView.getLocationOnScreen(location);
        calendarImgMargin.leftMargin = location[0] + (int)getResources().getDimension(R.dimen.dailyanim_c_txt_width)/2 - measureTextWidth()/2;
        int offsetCalendar = (int)getResources().getDimension(R.dimen.daily_info_change_date_view_height) + containerLoc[1];
        calendarImgMargin.topMargin=containerLoc[1] - getStatusBarHeight() - getActionBar().getHeight();
        calendarImgMargin.bottomMargin =Utility.getScreenHeight(this)-offsetCalendar -  getViewMeasure(todayBtn)[1]*2;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(calendarImgMargin);
        calendarImg.setLayoutParams(params);

        View textViews= findViewById(R.id.tips_p2_l_tvs);
        ViewGroup.MarginLayoutParams tvsMargin=new ViewGroup.MarginLayoutParams(textViews.getLayoutParams());
        TextView tvUp = (TextView)findViewById(R.id.tips_p2_l_tvs_up);
        TextView tvDown = (TextView)findViewById(R.id.tips_p2_l_tvs_down);
        LinearLayout.LayoutParams tvsParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvsParams.width = left+getViewMeasure(todayBtnTipsImg)[0]/2 - tvsMargin.leftMargin;
        tvUp.setLayoutParams(tvsParams);
        tvDown.setLayoutParams(tvsParams);
    }

    private int measureTextWidth(){
        TextView txtView = new TextView(mContext);
        txtView.setTextSize(getResources().getDimension(R.dimen.dailyanim_c_txt_large_textsize));
        txtView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return txtView.getMeasuredWidth();
    }

    private int[] drawLines_Pad(int leftMargin_1, int leftMargin_2, int top){
        int largeCircle = (int)(getResources().getDimension(R.dimen.dailyanim_L1_master_circle_width_large)* mScaleRatio);
        ImageView line_1=(ImageView) findViewById(R.id.tips_page1).findViewById(R.id.tips_line_1);
        line_1.getLayoutParams().width =  leftMargin_2 - leftMargin_1- largeCircle;
        ViewGroup.MarginLayoutParams margin_1=new ViewGroup.MarginLayoutParams(line_1.getLayoutParams());
        margin_1.topMargin=top + (int)getResources().getDimension(R.dimen.dailyanim_L1_master_circle_width)/2;
        margin_1.leftMargin = leftMargin_1 + largeCircle;
        FrameLayout.LayoutParams layoutParams_1 = new FrameLayout.LayoutParams(margin_1);

        ImageView line_2=(ImageView) findViewById(R.id.tips_page1).findViewById(R.id.tips_line_2);
        ViewGroup.MarginLayoutParams margin_2=new ViewGroup.MarginLayoutParams(line_2.getLayoutParams());
        margin_2.topMargin=margin_1.topMargin - (int)getResources().getDimension(R.dimen.tips_line_2_height);
        margin_2.leftMargin = leftMargin_1 + largeCircle + (leftMargin_2-leftMargin_1-largeCircle)/2;
        FrameLayout.LayoutParams layoutParams_2 = new FrameLayout.LayoutParams(margin_2);

        ImageView line_3=(ImageView) findViewById(R.id.tips_page1).findViewById(R.id.tips_line_3);
        ViewGroup.MarginLayoutParams margin_3=new ViewGroup.MarginLayoutParams(line_3.getLayoutParams());
        margin_3.topMargin=margin_2.topMargin;
        margin_3.leftMargin= margin_2.leftMargin;
        FrameLayout.LayoutParams layoutParams_3 = new FrameLayout.LayoutParams(margin_3);

        line_1.setLayoutParams(layoutParams_1);
        line_2.setLayoutParams(layoutParams_2);
        line_3.setLayoutParams(layoutParams_3);
        int[] result = {margin_3.topMargin,margin_3.leftMargin };
        return result;
    }

    private int[] getViewMeasure(View view){
        int[] measure = new int[2];
        view.measure(View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        measure[0] = view.getMeasuredWidth();
        measure[1] = view.getMeasuredHeight();
        return measure;
    }

    public int getStatusBarHeight(){
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        return statusBarHeight;
    }

    private void afterDailyTipsShowed() {
        SharedPreferences sp = getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
        sp.edit().putBoolean(getString(R.string.pref_key_tips_daily_showed), true).commit();
        findViewById(R.id.tips_page2).setVisibility(View.GONE);
        findViewById(R.id.tips_page3).setVisibility(View.GONE);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        mIsShowingTips = false;
        mSpinner.setOnTouchListener(null);
        checkPermission();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
    /* Tips end, add by emily */

}

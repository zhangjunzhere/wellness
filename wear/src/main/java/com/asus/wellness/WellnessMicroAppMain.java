package com.asus.wellness;

import com.asus.sharedata.SyncProfile;
import com.asus.wellness.Profile.view.RealSetupProfileFragment;
import com.asus.wellness.adapter.SwipeAdapter;
import com.asus.wellness.datalayer.DataLayerManager;
import com.asus.wellness.fragment.IdleAlarmFragment;
import com.asus.wellness.fragment.StartMeasureFragment;
import com.asus.wellness.sleep.SleepActionFragment;
import com.asus.wellness.fragment.StartWorkoutFragment;
import com.asus.wellness.fragment.TargetStatusFragment;
import com.asus.wellness.fragment.UpdateStepListener;
import com.asus.wellness.microprovider.ActivityDb;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.ActivityHelper;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.dbhelper.Profile;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.viewpagerindicator.IconPageIndicator;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class WellnessMicroAppMain extends FragmentActivity{
	
	public static final String ACTION_HAVE_ACTIVITY="com.asus.wellness.haveactivity";
	public static final String ACTION_NO_ACTIVITY="com.asus.wellness.noactivity";
	public static final String KEY_STEP_COUNTS="key_step_counts";
	public static final String KEY_ACTIVTY_TYPE="key_activity_type";
    public static final String KEY_TOTAL_STEP_COUNT="key_total_step_counts";
    public static final String KEY_CALORIES_BURNED="key_calories_burned";
	public static final String KEY_DISTANCE="key_distance";
	IconPageIndicator mIndicator;
	DataLayerManager dataLayerManager = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("smile","WellnessMicroAppMain oncreate");

		//onresume original
		if(ActivityDb.checkIsHaveActivity(this)){
			setPager(ActivityHelper.getActivityIntent(this));
		}
		else{
			setPager(null);	
		}
		//requestProfileupdate();
		//NotificaionHelper.getInstance(this).showReachGoalNotification(1000);
        EventBus.getDefault().register(this);
    }
	
	private void requestProfileupdate()
	{
		List<Profile> profileList  =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
	    if(profileList !=null && profileList.size() > 0 && profileList.get(0).getStep_goal() != null &&  profileList.get(0).getStep_goal()!=Utility.TARGET_GOAL)
		{
			return;
		}
		dataLayerManager =  DataLayerManager.getInstance(this);
		dataLayerManager.connectGoogleApiClient(new CollectStepCountService.GoogleApiConnectCallback() {
			@Override
			public void onConnect() {
				//smile_gao send profile request
//				List<Profile> profileList  =  WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao().loadAll();
//				if(profileList ==null || profileList.size() == 0)
//				{
//					Log.i("smile", "request profile update");
//					dataLayerManager.sendRequestProfile();
//				}
				//end smile
			dataLayerManager.getData(new ResultCallback<DataItemBuffer>() {
				@Override
				public void onResult(DataItemBuffer dataItems) {
					long time=System.currentTimeMillis();
					Log.i("smile","getData onResult  "+dataItems.getCount());
					if (dataItems.getCount() != 0 && dataItems.getCount()<1000) {
						for (int i=0 ;i<dataItems.getCount();i++)
						{
							DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));
						//	Log.i("smile", "Url: " + dataMapItem.getUri().toString());
							if(dataMapItem.getUri().getPath().matches(SyncProfile.PROFILE_PATH))
							{
								Log.i("smile", "profile find: " + dataMapItem.getUri().toString());
								final DataMap data=dataMapItem.getDataMap();
								AsyncTask task = new AsyncTask() {
									@Override
									protected Object doInBackground(Object[] params) {
										ProfileHelper.updateProfile(WellnessMicroAppMain.this, data, dataLayerManager);
										return  null;
									}
								};
								task.execute();

							}
						}
					}
					Log.i("smile","getData onResult  over "+(System.currentTimeMillis()-time));

					dataItems.release();
				}
			});
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		checkPermission();
		Log.i("smile","WellnessMicroAppMain onResume");
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ACTION_HAVE_ACTIVITY);
        intentFilter.addAction(ACTION_NO_ACTIVITY);
		intentFilter.addAction(ACTION_NO_ACTIVITY);
        registerReceiver(activityBroadcastReceiver, intentFilter);
	}


	
//	private void addIndicator(int index, int total){
//		LinearLayout container=(LinearLayout)findViewById(R.id.indicator_container);
//		container.removeAllViews();
//
//		for(int i=0;i<total;i++){
//			if(i==index){
//				ImageView onImage=new ImageView(this);
//				onImage.setImageResource(R.drawable.asus_indicator_on);
//				container.addView(onImage);
//			}
//			else{
//				ImageView offImage=new ImageView(this);
//				offImage.setImageResource(R.drawable.asus_indicator_off);
//				container.addView(offImage);
//			}
//		}
//	}
	

	private SwipeAdapter getAdaper(Intent intent)
	{
		List<Fragment> mFragmentList = new ArrayList<>();
        mFragmentList.add(TargetStatusFragment.newInstance(intent));

		if(Utility.isSupportAsusEcg(this)){
            mFragmentList.add(new StartMeasureFragment());
		}
		mFragmentList.add(new StartWorkoutFragment());
		if(Utility.isSupportSleepSensor(this))
		{
			mFragmentList.add(new SleepActionFragment());
		}
		mFragmentList.add(IdleAlarmFragment.newInstance());
		mFragmentList.add(new RealSetupProfileFragment());



		SwipeAdapter adapter=new SwipeAdapter(getSupportFragmentManager(), mFragmentList);
		return  adapter;
	}
	int mSelectedPage=0;
	
	private void setPager(Intent intent){//intent null:no activity
		ViewPager pager=(ViewPager)findViewById(R.id.swipe_pager);
		final SwipeAdapter adapter=getAdaper(intent);
		if(pager.getAdapter()!=null){
			if(pager.getAdapter().getCount()>adapter.getCount()){//3 pages->2 pages
				if(mSelectedPage!=0){
					mSelectedPage-=1;	
				}
			}
			else if(pager.getAdapter().getCount()<adapter.getCount()){//2 pages->3pages
				mSelectedPage+=1;
			}	
		}
		pager.setAdapter(adapter);
		mIndicator = (IconPageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(pager);
		int visible = adapter.getCount()>1? View.VISIBLE: View.GONE;
		mIndicator.setVisibility(visible);

		pager.setCurrentItem(mSelectedPage);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//if(dataLayerManager!=null)
		//	dataLayerManager.disConnectGoogleApiClient();

        EventBus.getDefault().unregister(this);

	}

	private void updateFragmentSteps(Intent intent)
	{
		Log.i("smile","updateFragmentSteps");
		ViewPager pager=(ViewPager)findViewById(R.id.swipe_pager);
		if(pager.getAdapter() == null || pager.getAdapter().getCount() ==0)
		{
			return;
		}
		SwipeAdapter swipeAdapter = (SwipeAdapter)pager.getAdapter();
		for(int i=0;i<swipeAdapter.getCount();i++)
		{
			if(swipeAdapter.getItem(i) instanceof UpdateStepListener)
			{
				UpdateStepListener listener = ((UpdateStepListener) (swipeAdapter.getItem(i)));
				listener.updateSteps(intent);
			}
		}
	}
	

	private BroadcastReceiver activityBroadcastReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub\
			if(intent.getAction().matches(ACTION_HAVE_ACTIVITY)){
				updateFragmentSteps(intent);
			}
//			else if(intent.getAction().matches(ACTION_NO_ACTIVITY)){
//				updateFragmentSteps(null);
//			}
		}
		
	};

    public void onEventMainThread(EBCommand ebCommand){
        // when workout start, finish this
        if(EBCommand.COMMAND_SHOW_COACH_NOTIFICATION.equals(ebCommand.command)){
            finish();
        }
    }
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ViewPager pager=(ViewPager)findViewById(R.id.swipe_pager);
				pager.setCurrentItem(0);
			}
			
		}, 1000);

        unregisterReceiver(activityBroadcastReceiver);

	}

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

	void checkPermission()
	{
		String[] permissons = new String[]{
				Manifest.permission.BODY_SENSORS,
				Manifest.permission.WAKE_LOCK
		};
		List<String> needGrantPermissionList = new ArrayList<>();
		for(String p : permissons)
		{
			if(!checkPermission(p))
			{
				needGrantPermissionList.add(p);
			}
		}
		if(needGrantPermissionList.size()>0)
		{
			grantPermission(needGrantPermissionList);
		}
		else
		{
			Log.i("smile", " needGrantPermissionList size =0 ");
		}
//
//        checkPermission( Manifest.permission.READ_EXTERNAL_STORAGE);
//        checkPermission( Manifest.permission.ACCESS_COARSE_LOCATION);
//        checkPermission(Manifest.permission.BODY_SENSORS);
//        checkPermission("com.google.android.apps.photos.permission.GOOGLE_PHOTOS");
	}
	boolean checkPermission(String permisson)
	{

		int val =  ContextCompat.checkSelfPermission(this, permisson);
		if(val == PackageManager.PERMISSION_GRANTED)
		{
			Log.i("smile", permisson+ " granted");
			return  true;
		}
		Log.i("smile", permisson + " not granted");
		return false;
	}
	void grantPermission(List<String> permissons)
	{
		List<String> pRets = new ArrayList<>();
		for(String permisson : permissons)
		{
			if(ActivityCompat.shouldShowRequestPermissionRationale(this, permisson))
			{
				Log.i("smile", permisson+ " shouldShowRequestPermissionRationale true");
			}
			pRets.add(permisson);
		}
		String[] ptrs = new String[]{};
		ptrs=pRets.toArray(ptrs);
		if(ptrs.length==0)
		{
			Log.i("smile","need request Permisson size 0");
			return;
		}
		ActivityCompat.requestPermissions(this, ptrs, 111);

	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.i("smile", "onRequestPermissionsResult " + requestCode);
//		Intent intent = new Intent(this,CollectStepCountService.class);
//		intent.setAction(CollectStepCountService.ACTION_GET_HISTORY_DATA);
//		this.startService(intent);
	}
}

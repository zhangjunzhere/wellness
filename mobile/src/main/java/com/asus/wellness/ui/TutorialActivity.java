package com.asus.wellness.ui;

import java.io.UnsupportedEncodingException;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.asus.wellness.DataLayerManager;
import com.asus.wellness.DatabaseBackupHelper;
import com.asus.wellness.EcgAndStepCountListenerService;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.ui.profile.SetupProfileActivity;
import com.asus.wellness.ui.setting.SettingActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.MessageEvent;

public class TutorialActivity extends BaseActivity implements MessageListener{

    public static final String TAG = "TutorialActivity";
	public static final String KEY_TUTORIAL_PAGE="key_tutorial_page";
    public static final String KEY_IDLE_ALARM_SWITCH="pref_key_idle_alarm_switch";
   private static boolean mCheckWatchType = true;
	DataLayerManager dlm;

	@Override
	public  String getPageName(){
		return  TutorialActivity.class.getSimpleName();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d("WellnessVersionCode", "WellnessVersionCode: " + getCurrentVersionCode());
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dlm=new DataLayerManager(this);

		dlm.connectGoogleApiClientForCheckAsusWatch(true,mCheckWatchType, this);
		if(mCheckWatchType)
			mCheckWatchType = false;
		
		if(getIntent().hasExtra(KEY_TUTORIAL_PAGE)){
			switch(getIntent().getIntExtra(KEY_TUTORIAL_PAGE, -1)){
				/*case 2:
					toMeasureTutorial(null);
				break;*/
				case 1:
					toTutorialPaired();
					break;
				case 3:
					setContentView(R.layout.terms_and_service_layout);
					Utility.trackerScreennView(getApplicationContext(), "WOOBE ToS");
					CheckBox check=(CheckBox)findViewById(R.id.checkbox_agree);
					findViewById(R.id.finish_tutorial).setEnabled(false);
					check.setOnCheckedChangeListener(new OnCheckedChangeListener(){

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							ImageButton ib=(ImageButton) findViewById(R.id.finish_tutorial);
							if(isChecked){
								ib.setEnabled(true);
								ib.setImageResource(R.drawable.asus_wellness_tutorial_button_right);
							}
							else{
								ib.setEnabled(false);
								ib.setImageResource(R.drawable.asus_wellness_btn_right_dis);
							}
						}
					
					});
				break;
			}
		}
		else{
			if(checkFirstTimeLaunchAndRestored())
			{
			  AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.alertdialog_find_user_data_message_title)
				.setMessage(R.string.alertdialog_find_user_data_message_content)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//restore data
						if (checkGoogleServiceVersion()) {
							DatabaseBackupHelper.restoreDatabase(TutorialActivity.this);
							//smile_gao fix bug 637700, notify device update
							dlm.sendProfileToRobin();
							dlm.checkWatchType();
							//end smile

							SharedPreferences sp = getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
							sp.edit().putBoolean(getString(R.string.pref_key_private_first_launch), false).commit();

							//restore notification
							SharedPreferences sp_default = PreferenceManager.getDefaultSharedPreferences(TutorialActivity.this);
							Boolean enableAlarm = sp_default.getBoolean(KEY_IDLE_ALARM_SWITCH, false);
							Log.d(TAG, "onRestore enableAlarm= " + enableAlarm);

							if (enableAlarm) {
							//smile_gao add for key_ga not backup 
								SharedPreferences mSharedPref = getSharedPreferences(SettingActivity.KEY_GA, MODE_PRIVATE);
								SharedPreferences.Editor editor = mSharedPref.edit();
								editor.putBoolean(SettingActivity.IS_REMIND_OPT_OUT, !enableAlarm);
								editor.commit();
								dlm.sendIdleAlarmSetting();
							}

							if (checkProfileExist()) {
								Intent intent = new Intent(TutorialActivity.this, MainWellness.class);
								startActivity(intent);
								finish();
							} else {
								setContentView(R.layout.tutorial_paired_layout);
								Utility.trackerScreennView(getApplicationContext(), "WOOBE Intro");
							}
						}
					}

				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences sp = getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
						sp.edit().putBoolean(getString(R.string.pref_key_private_first_launch), false).commit();
						setContentView(R.layout.tutorial_paired_layout);
						Utility.trackerScreennView(getApplicationContext(), "WOOBE Intro");
					}
				}).create();
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
			}
			else{
			    SharedPreferences sp = getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
                sp.edit().putBoolean(getString(R.string.pref_key_private_first_launch), false).commit();
			    if (checkProfileExist()){
		            Intent intent=new Intent(TutorialActivity.this, MainWellness.class);
                    startActivity(intent);
                    finish();       
                }else{
                    setContentView(R.layout.tutorial_paired_layout);
					Utility.trackerScreennView(getApplicationContext(), "WOOBE Intro");
                }
			}
		}

		//Utility.trackerScreennView(getApplicationContext(), "Goto Tutorial");
	}
	
	public int getCurrentVersionCode() {
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();
       
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            return  -1;
        }
    }
	
	public void toMeasureTutorial(View view){
		/*if(this.checkGoogleServiceVersion()){
			setContentView(R.layout.tutorial_measure_layout);	
		}*/
	}

	//To second Setup Profile page.
	public void toSetupProfile(View view){
		measureTutorialNext(null);
	}

	//To fist totorial page.
	public void toTutorialPaired(){
		if(checkGoogleServiceVersion()){
			setContentView(R.layout.tutorial_paired_layout);
			Utility.trackerScreennView(getApplicationContext(), "WOOBE Intro");
		}
	}

	public void measureTutorialNext(View view){
		Intent intent=new Intent(this, SetupProfileActivity.class);
		intent.putExtra(SetupProfileActivity.EXTRA_FIRST_SETUP, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		overridePendingTransition(0, 0);
		finish();
	}
	
	public void measureTutorialPre(View view){
		setContentView(R.layout.tutorial_paired_layout);
	}
	
	private boolean checkProfileExist(){
		boolean isExist=false;
		Cursor cursor=getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
		if(cursor!=null && cursor.moveToFirst()){
			isExist=true;
		}
		else{
			isExist=false;
		}
		if(cursor!=null)
			cursor.close();
		return isExist;
	}
	
	private boolean checkFirstTimeLaunchAndRestored(){
	    SharedPreferences sp = getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
	    if (sp.contains(getString(R.string.pref_key_private_first_launch))){
	        return false;
	    }
	    
	    boolean isBackupDatabaseExists = getDatabasePath(DatabaseBackupHelper.BACKUP_DATABASE_NAME).exists();
	    if (isBackupDatabaseExists){
            try {
                DataHelper dbHelper = new DataHelper(this, DatabaseBackupHelper.BACKUP_DATABASE_NAME);
                List<Profile> profiles = dbHelper.getDaoSession().getProfileDao().loadAll();
                return profiles.size() > 0;
            }catch (Exception e){
                if(e != null){
                    Log.e(TAG, e.toString());
                }
            }
	    }
	    return false;
	}
	
	public void toDailyPage(View view){
		Intent wearService = new Intent(this, EcgAndStepCountListenerService.class);
		startService(wearService);

		Intent intent=new Intent(this, MainWellness.class);
		startActivity(intent);
		finish();
	}
	
	public void toSettingPage(View view){
		measureTutorialNext(null);
	}
	
	public boolean checkGoogleServiceVersion(){
	    // Check that Google Play services is available
	    int resultCode =
	            GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

	    // If Google Play services is available
	    if (ConnectionResult.SUCCESS == resultCode) {
	        return true;
	    } else {
	        GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
	        return false;
	    }	
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		// TODO Auto-generated method stub
	     if(messageEvent.getPath().equals(DataLayerManager.RETURN_CHECK_WATCH_PATH)){
	    	try {
				String srt2=new String(messageEvent.getData(),"UTF-8");
	        	Log.d("circle","tutorial manufactor:"+srt2);
	        	if(!srt2.toLowerCase().matches("asus")){
	        		TutorialActivity.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
			        		new AlertDialog.Builder(TutorialActivity.this)
			        		.setTitle(getString(R.string.not_pair_asus_watch_title))
			        		.setMessage(getString(R.string.not_pair_asus_watch_message))
			        		.setCancelable(false)
			        		.setPositiveButton(getString(R.string.not_pair_asus_watch_button), new OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									finish();
								}
			        			
			        		})
			        		.show();
						}
	        			
	        		});
	        	}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dlm.removeListener(this);
	}
	
	//backdoor
	public String insertFakeData(String arg) {
    	
    	if(arg.compareTo("wellnesscalabash") == 0)
    	{
			return "NotMainWellness";
    	}	
		return "False";
    }
   
   public String getsmallestScreenWidthDp(){
	   String res = "";
	   int a = getResources().getConfiguration().smallestScreenWidthDp;
	   res = Integer.toString(a);
	   return res;
   }
}

package com.asus.wellness.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Spinner;

import com.asus.wellness.DataLayerManager;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.ParseDataManager.ProfileData;
import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.adapter.ProfileArrayAdapter;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.dbhelper.ProfileDao;
import com.asus.wellness.ga.GACategory;
import com.asus.wellness.ga.GAHelper;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.ui.BaseActivity;
import com.asus.wellness.ui.TutorialActivity;
import com.asus.wellness.ui.permission.GrantPermissionActivity;
import com.asus.wellness.ui.permission.PermissionDialog;
import com.asus.wellness.ui.permission.PermissionHelper;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;
import com.asus.commonui.datetimepicker.date.DatePickerDialog;
import com.cmcm.common.statistics.CMAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class SetupProfileActivity extends BaseActivity implements OnClickListener, OnItemSelectedListener, TextWatcher, OnValueChangeListener, OnGlobalLayoutListener{
	private EditText edt_name;
	private Button but_age;
	private Button but_height;
	private Button but_weight;
	private Spinner spinner_gender, spinner_height_unit, spinner_weight_unit;
	private UserHeadImageView img_profile;
	public static final int REQUEST_CODE_PICK_IMAGE=1234;
	public static final int REQUEST_CODE_CROP_IMAGE=REQUEST_CODE_PICK_IMAGE+1;
	//smile add for take photo
	public static final int REQUEST_CODE_TAKE_PHOTO=REQUEST_CODE_CROP_IMAGE+1;
	//end smile
	public static final String EXTRA_FIRST_SETUP="extra_first_setup";
	public static final String EXTRA_START_MEASURE_TIME="extra_start_measure_time";
	DataLayerManager dataLayerManager;
	int numHundred,numTen, numOne, numFeet = -1;
	float numInch = -1;

    final int AGE_DEFAULT = 30;
    int mYear, mMonth, mDay =-1;
	
	private ViewGroup mActivityRootView;
	private ViewGroup mBottomContainer;
	private View mTutorialButtonContainer;
	private ViewGroup mScrollerContent;
	
	final int INCH_DIALOG_ID=13579;
	public static Uri takePhotoUri;
	private long mNeverShowTime = 0;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
			case R.id.save:
				setProfileNext(null);
				break;
			case R.id.cancel:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private int mSelectedGender=ProfileTable.MALE;
	private int mSelectedHeightUnit=ProfileTable.HEIGHT_UNIT_CM;
	private int mSelectedWeightUnit=ProfileTable.WEIGHT_UNIT_KG;
	private String mSelectedPhotoUri;
	private ProfileData mProfileData=null;

	@Override
	public String getPageName(){
		return SetupProfileActivity.class.getSimpleName();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		dataLayerManager=new DataLayerManager(this);
		dataLayerManager.connectGoogleApiClient();
		mProfileData=getProfileData();
		initLayout();
	}
	
	public ProfileData getProfileData(){
		return ParseDataManager.getInstance().getProfileData(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		if(!getIntent().hasExtra(EXTRA_FIRST_SETUP)){
			getMenuInflater().inflate(R.menu.setup_profile_menu, menu);
			if(mProfileData==null){
				menu.findItem(R.id.cancel).setVisible(false);
			}
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	private void initLayout(){
		int spinner_resource;
		if(getIntent().hasExtra(EXTRA_FIRST_SETUP)){
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.setup_profile_layout);
			ImageButton ib=(ImageButton)findViewById(R.id.setup_profile_next);
			ib.setEnabled(false);
			spinner_resource = R.layout.setup_profile_spinner;
			Utility.trackerScreennView(getApplicationContext(), "Goto WOOBE SetUpProfile");
		}else{
			setContentView(R.layout.setup_profile_layout_editor);
			spinner_resource = R.layout.setup_profile_spinner_light;
			Utility.trackerScreennView(getApplicationContext(), "Goto SetupProfile");
		}
		
		but_age=(Button)findViewById(R.id.button_age);
		but_height=(Button)findViewById(R.id.button_height);
		but_weight=(Button)findViewById(R.id.button_weight);
		edt_name=(EditText)findViewById(R.id.edit_name);
		edt_name.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				ImageButton ib=(ImageButton)findViewById(R.id.setup_profile_next);
				if(ib!=null){
					if(s.length()==0){
						ib.setEnabled(false);
						ib.setImageResource(R.drawable.asus_wellness_btn_next_dis);
					}
					else{
						ib.setEnabled(true);
						ib.setImageResource(R.drawable.asus_wellness_tutorial_button_next);
					}	
				}
			}
			
		});

		spinner_gender=(Spinner)findViewById(R.id.spinner_gender);
		ArrayAdapter<String> adapter_gender  = new ArrayAdapter<String>(this, spinner_resource, android.R.id.text1, getResources().getStringArray(R.array.profile_gender_option));
		spinner_gender.setAdapter(adapter_gender);
		adapter_gender.setDropDownViewResource(R.layout.setup_profile_spinner_item);

		spinner_height_unit=(Spinner)findViewById(R.id.height_unit);
		final String[] heightoptions = getResources().getStringArray(R.array.profile_height_option);
//		ArrayAdapter<String> adapter_height  = new ArrayAdapter<String>(this, spinner_resource, android.R.id.text1, heightoptions){
//			@Override
//			public View getDropDownView(int position, View convertView, ViewGroup parent) {
//				//return super.getDropDownView(position, convertView, parent);
//				View view = View.inflate(getContext(), R.layout.setup_profile_spinner_item,null);
//				TextView tv =(TextView) view.findViewById(android.R.id.text1);
//				tv.setText(heightoptions[position]);
//				return  view;
//			}
//		};
		ProfileArrayAdapter adapter_height = new ProfileArrayAdapter(this,spinner_resource,heightoptions);
		spinner_height_unit.setAdapter(adapter_height);
		//adapter_height.setDropDownViewResource(R.layout.setup_profile_spinner_item);

 		spinner_weight_unit=(Spinner)findViewById(R.id.weight_unit);
		final String[] weightoptions = getResources().getStringArray(R.array.profile_weight_option);
	//	ArrayAdapter<String> adapter_unit  = new ArrayAdapter<String>(this, spinner_resource, android.R.id.text1, getResources().getStringArray(R.array.profile_weight_option));
		ProfileArrayAdapter adapter_weight = new ProfileArrayAdapter(this,spinner_resource,weightoptions);
		spinner_weight_unit.setAdapter(adapter_weight);
	//	adapter_unit.setDropDownViewResource(R.layout.setup_profile_spinner_item);

        //fix bug 546477 , spinner popup dialog cover arrow icon
		int offSet = 35 * (int) getResources().getDisplayMetrics().density;
		final int spinnerVerticalOffset = (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT + 1) ? offSet : 0;
        spinner_gender.setDropDownVerticalOffset(spinnerVerticalOffset);
        spinner_height_unit.setDropDownVerticalOffset(spinnerVerticalOffset);
        spinner_weight_unit.setDropDownVerticalOffset(spinnerVerticalOffset);

		img_profile=(UserHeadImageView)findViewById(R.id.profile_image);
		
		img_profile.setOnClickListener(this);
		//smile_gao add for take photo
		img_profile.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					mNeverShowTime = System.currentTimeMillis();
					PermissionHelper.checkStoragePermission(SetupProfileActivity.this, GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_TAKEPHOTO);
					return  true;
				}
				Utility.takePhoto(SetupProfileActivity.this);
				return  true;
			}
		});
		spinner_gender.setOnItemSelectedListener(this);
		spinner_height_unit.setOnItemSelectedListener(this);
		spinner_weight_unit.setOnItemSelectedListener(this);


		mTutorialButtonContainer =  findViewById(R.id.tutorial_button_container);
		
		if(getIntent().hasExtra(EXTRA_FIRST_SETUP)){
		    edt_name.setBackgroundResource(R.drawable.asus_edittext_wellness);
		}
		
		if(mProfileData==null){
			if(!getIntent().hasExtra(EXTRA_FIRST_SETUP)){
			    mTutorialButtonContainer.setVisibility(View.GONE);
				invalidateOptionsMenu();
			}
			findViewById(R.id.btn_set_new_profile).setVisibility(View.GONE);

			//emily ++++
            /* set deafult age = 30. birthday: year = current year - 30, month = 1, day  = 1 */
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.YEAR, -AGE_DEFAULT);
            cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
            setBirthDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH));
            but_age.setText(formatter.format(cal.getTime()));
            //emily ----

		}
		else{
			if(getIntent().hasExtra(EXTRA_FIRST_SETUP)){
				findViewById(R.id.btn_set_new_profile).setVisibility(View.GONE);
			}
			else{
				findViewById(R.id.btn_set_new_profile).setVisibility(View.VISIBLE);
				mTutorialButtonContainer.setVisibility(View.GONE);
			}
			setExistData();
		}

		mScrollerContent = (ViewGroup) findViewById(R.id.scroller_content);
		mActivityRootView =(ViewGroup) findViewById(R.id.activity_root);
		mActivityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
		
		mBottomContainer = (ViewGroup) findViewById(R.id.bottom_container);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(saveProfile()){
			super.onBackPressed();
		}
	}

	private void setExistData(){
	    //emily ++++
	    //but_age.setText(String.valueOf(mProfileData.age));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();
        cal = getBirthDayCal(mProfileData.birthday);
        setBirthDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH));
        but_age.setText(formatter.format(cal.getTime()));
        //emily ----

        int height = mProfileData.height;
        if(mProfileData.heightUnit==ProfileTable.HEIGHT_UNIT_FT){
            numFeet=(int) Math.floor(Utility.InchToFt(mProfileData.height));
            numInch=mProfileData.height-Utility.ftToInch(numFeet);
            if((int)Math.round(numInch) == 12){  // 1 feet = 12 inch
                numInch = 0;
                ++numFeet;
            }
            but_height.setText(numFeet+"' "+(int)Math.round(numInch)+"\"");
        }
		else{
			but_height.setText(String.valueOf(mProfileData.height));	
		}

        but_weight.setText(String.valueOf(mProfileData.weight));

		edt_name.setText(mProfileData.name);
		spinner_gender.setSelection(mProfileData.gender);
		spinner_height_unit.setSelection(mProfileData.heightUnit);
		mSelectedHeightUnit=mProfileData.heightUnit;
		mSelectedWeightUnit=mProfileData.weightUnit;
		spinner_weight_unit.setSelection(mProfileData.weightUnit);

		if(mProfileData.photo_path!=null && !mProfileData.photo_path.contains("com.google.android.apps.photos.content")){
			Bitmap bitmap = Utility.getPhotoBitmap(this, mProfileData.photo_path);
			if(bitmap==null)
			{
				img_profile.setImageResource(R.drawable.asus_wellness_photo_people);
			}
			else 
			{
				img_profile.setImageBitmap(bitmap);
			}
		}
		else{
			img_profile.setImageResource(R.drawable.asus_wellness_photo_people);
		}
		
		mSelectedPhotoUri=mProfileData.photo_path;
	}

	public boolean saveProfile() {
		if (checkNameEmpty(edt_name.getText().toString().trim())) {
//			Toast.makeText(this, "Please enter a name.", Toast.LENGTH_LONG).show();
			edt_name.setHintTextColor(Color.RED);
			edt_name.setHint(getString(R.string.profile_edittext_name_hint_alarm));
			edt_name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.asus_wellness_ic_redicon, 0);
			edt_name.addTextChangedListener(this);
			return false;
		} else {
			ProfileDao profileDao = WApplication.getInstance().getDataHelper().getDaoSession().getProfileDao();
			List<Profile> profileList = profileDao.loadAll();

			Profile profile;
			HashMap<String, String> data = new HashMap<>();
			if (profileList.size() > 0) {
				profile = profileList.get(0);
				data.put("uptype", "3");
			} else {
				long theOldestMeasureTime = ProfileHelper.getStartMeasureTime();//System.currentTimeMillis();
				profile = new Profile();
//				Cursor ecgCursor = getContentResolver().query(EcgTable.TABLE_URI, null, null, null, EcgTable.COLUMN_MEASURE_TIME);
//				if (ecgCursor.moveToFirst()) {
//					long ecgTime = ecgCursor.getLong(ecgCursor.getColumnIndex(EcgTable.COLUMN_MEASURE_TIME));
//					if (ecgTime < theOldestMeasureTime) {
//						theOldestMeasureTime = ecgTime;
//					}
//					ecgCursor.close();
//				}
//				Cursor activityCursor = getContentResolver().query(ActivityStateTable.TABLE_URI, null, null, null, ActivityStateTable.COLUMN_START);
//				if (activityCursor.moveToFirst()) {
//					long activityTime = activityCursor.getLong(activityCursor.getColumnIndex(ActivityStateTable.COLUMN_START));
//					if (activityTime < theOldestMeasureTime) {
//						theOldestMeasureTime = activityTime;
//					}
//					activityCursor.close();
//				}
				profile.setStart_time(theOldestMeasureTime);
				data.put("uptype", "1");
			}

			//emily ++++
			//profile.setAge(Integer.parseInt(but_age.getText().toString()));
            long timeMillis = getBirthdayInTimeMillis();
            Calendar calendar = Calendar.getInstance();
            int age = calendar.get(Calendar.YEAR);
            calendar.setTimeInMillis(timeMillis);
            age -= calendar.get(Calendar.YEAR);
            profile.setAge(age);
            profile.setBirthday(timeMillis);
            //emily ----

			profile.setGender(mSelectedGender);
			profile.setHeight_unit(mSelectedHeightUnit);
			int height;
            if (mSelectedHeightUnit == ProfileTable.HEIGHT_UNIT_FT) {
                //numInch = (int) numInch;
                int inch = (int) (Math.round(Utility.ftToInch(numFeet) + numInch));
                height = inch;
            }
            else {
				height = Integer.parseInt(but_height.getText().toString());
			}
            profile.setHeight(height);
			profile.setName(edt_name.getText().toString());
			profile.setPhoto_path(mSelectedPhotoUri);
            profile.setWeight(Integer.parseInt(but_weight.getText().toString()));
			profile.setWeight_unit(mSelectedWeightUnit);
			//profile.setStart_time(System.currentTimeMillis());
            Utility.updateLastUpdateTime(getApplicationContext(),System.currentTimeMillis());

			profileDao.insertOrReplace(profile);
			dataLayerManager.sendProfileToRobin();

			//for CM data collect
			//data.put("profile_name", edt_name.getText().toString());
			if(mSelectedPhotoUri!=null)
				data.put("profile_photo", "1");
			else
				data.put("profile_photo", "0");

			data.put("age", but_age.getText().toString());
			data.put("gender",String.valueOf(mSelectedGender+1));
			data.put("start_time", String.valueOf(profile.getStart_time()));

			String[] info= CmHelper.heightWeightInfo(profile);
			data.put("height", info[0]);
			data.put("weight", info[1]);
			CMAgent.onEvent(CmHelper.PROFILE_MSG_ID, data);
			//GAHelper.getInstance().send(getApplicationContext(),"gender",GACategory.ActionSex,"");
			GACategory.setSelectGender(mSelectedGender);
			//GAHelper.getInstance(getApplicationContext()).getTracker().set(GACategory.mTrackKey1,GACategory.getActionSex());
			GAHelper.getInstance(getApplicationContext()).sendEvent(GACategory.CategoryProfile, GACategory.getActionSex(),GACategory.LabelGender,-1);
			markGADataGenderUploaded(mSelectedGender);
			return true;
		}
	}

	private void markGADataGenderUploaded(int gender) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor= sp.edit();
		Log.i("ga", "mark gender in SetupProfileActivity: " + gender);
		editor.putInt("MarkGender", gender);
		editor.commit();
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.profile_image:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					mNeverShowTime = System.currentTimeMillis();
					PermissionHelper.checkStoragePermission(SetupProfileActivity.this, GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_PICK);
					break;
				}
				Utility.pickImage(SetupProfileActivity.this);
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK ){
			switch(requestCode){
				case REQUEST_CODE_PICK_IMAGE:
					if(data != null && data.getData()!=null){
						mSelectedPhotoUri=data.getData().toString();
						Uri uri = data.getData();
//						if(Utility.isNewGooglePhotosUri(uri))
//						{
//							String pathUri = data.getData().getPath();
//							String newUri = pathUri.substring(pathUri.indexOf("content"), pathUri.lastIndexOf("/ACTUAL"));
//							String str=Utility.getDataColumn(this, Uri.parse(newUri), null, null);
//
//							//  uri=  Uri.parse(str);
//							Log.i("smile","data main: "+str+" uri: "+uri.toString());
//						}

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
				case REQUEST_CODE_CROP_IMAGE:
					if(data != null){
						if( data.getData()!=null)
						{
							mSelectedPhotoUri=data.getData().toString();
						}
						else if(data.getAction().startsWith("file")&& data.getAction().endsWith("jpg"))
						{
							mSelectedPhotoUri = data.getAction();
						}

					}
					if(mSelectedPhotoUri!=null){
						//mSelectedPhotoUri=data.getData().toString();
						Bitmap bitmap = Utility.getPhotoBitmap(SetupProfileActivity.this,mSelectedPhotoUri);
						if(bitmap != null)
						{
							img_profile.setImageBitmap(bitmap);
						}

					}
					break;
				case SetupProfileActivity.REQUEST_CODE_TAKE_PHOTO:
				{
					Uri originalUri =  SetupProfileActivity.takePhotoUri;
					if(originalUri == null)
					{
						return;
					}
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
				break;
				case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_PICK:
					Utility.pickImage(SetupProfileActivity.this);
					break;
				case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_TAKEPHOTO:
					Utility.takePhoto(SetupProfileActivity.this);
					break;
                default:
                    break;
			}	
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.weight_unit:
			if(arg2==0){
				if(mSelectedWeightUnit!=ProfileTable.WEIGHT_UNIT_KG){
					mSelectedWeightUnit=ProfileTable.WEIGHT_UNIT_KG;
                    float weightLBS = Float.valueOf(but_weight.getText().toString());
                    int weightKG=(int)Math.round(Utility.LbsToKg(weightLBS));
                    but_weight.setText(String.valueOf(weightKG));
                }
			}
			else{
				if(mSelectedWeightUnit!=ProfileTable.WEIGHT_UNIT_LBS){
					mSelectedWeightUnit=ProfileTable.WEIGHT_UNIT_LBS;
                    float weightKG = Float.valueOf(but_weight.getText().toString());
                    int weightLBS=(int)Math.round(Utility.kgToLbs(weightKG));
                    but_weight.setText(String.valueOf(weightLBS));
                }
			}
			break;
		case R.id.height_unit:
			if(arg2==0){
				mSelectedHeightUnit=ProfileTable.HEIGHT_UNIT_CM;
				if(!(numFeet<0&&numInch<0)){
				    float feet=numFeet+Utility.InchToFt(numInch);
					int cm=(int) Math.round(Utility.ftToCm(feet));
					but_height.setText(String.valueOf(cm));
				}
			}
			else{
				mSelectedHeightUnit=ProfileTable.HEIGHT_UNIT_FT;
				try{
					float floatFeet= Utility.cmToFt(Float.valueOf(but_height.getText().toString()));
					numFeet=(int) Math.floor(floatFeet);
					numInch= Utility.ftToInch(floatFeet)-Utility.ftToInch(numFeet);
                    if((int)Math.round(numInch) == 12){
                        numInch = 0;
                        ++numFeet;
                    }
					but_height.setText(String.valueOf(numFeet+"' "+(int)Math.round(numInch)+"\""));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}
			break;
		case R.id.spinner_gender:
			if(arg2==0){
				mSelectedGender=ProfileTable.MALE;
			}
			else{
				mSelectedGender=ProfileTable.FEMALE;
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean checkNameEmpty(String text){
		if(text.length()==0){
			return true;
		}
		else{
			return false;
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
					Utility.pickImage(SetupProfileActivity.this);
				}
				break;
			case GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_TAKEPHOTO:
				if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					Utility.takePhoto(SetupProfileActivity.this);
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
	public void setNewProfile(View view){
		new AlertDialog.Builder(this)
		.setTitle(R.string.set_new_profile_alarm_title)
		.setMessage(R.string.set_new_profile_alarm_message)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Cursor startMeasureCursor=getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
				Intent intent=new Intent(SetupProfileActivity.this, SetupProfileActivity.class);
				if(startMeasureCursor != null && startMeasureCursor.moveToFirst()){
					intent.putExtra(EXTRA_START_MEASURE_TIME, startMeasureCursor.getLong(startMeasureCursor.getColumnIndex(ProfileTable.COLUMN_START_TIME)));
                    startMeasureCursor.close();
                }
				getContentResolver().delete(ProfileTable.TABLE_URI, null, null);
				startActivity(intent);
				finish();
			}
		})
		.setNegativeButton(android.R.string.no, null)
		.show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dataLayerManager.disConnectGoogleApiClient();
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		if(s.length()==0){
			edt_name.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.asus_wellness_ic_redicon,0);
		}
		else{
			edt_name.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
		}
	}
	
	public void setProfileNext(View view){
		if(saveProfile()){
			if(getIntent().hasExtra(EXTRA_FIRST_SETUP)){
				Intent intent=new Intent(this, TutorialActivity.class);
				intent.putExtra(TutorialActivity.KEY_TUTORIAL_PAGE, 3);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
			finish();	
		}	
	}
	
	public void setProfilePre(View view){
		Intent intent=new Intent(this, TutorialActivity.class);
		intent.putExtra(TutorialActivity.KEY_TUTORIAL_PAGE, 1);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);

		overridePendingTransition(0, 0);
		finish();
	}
	
	public void startAgePicker(final View view){
		//createNumberPickerDialog(view, getString(R.string.dialog_number_picker_age_title), 0, 99, 2);

        DatePickerDialog datePickerDialog = new DatePickerDialog();
        datePickerDialog.initialize(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int date) {
                int minYear = datePickerDialog.getMinYear();
                setBut_age(year, month, date,view, minYear);

            }
        },mYear, mMonth, mDay);
        datePickerDialog.show(getFragmentManager(), getString(R.string.profile_birthday_title));
	}
	
	public void startHeightPicker(View view){
		if(this.mSelectedHeightUnit==ProfileTable.HEIGHT_UNIT_FT){
			createInchDialog(getString(R.string.dialog_number_picker_height_title), numFeet, (int)Math.round(numInch));
		}
		else{
			createNumberPickerDialog(view, getString(R.string.dialog_number_picker_height_title), 20, 300, 3);
		}
	}
	
	public void startWeightPicker(View view){
		createNumberPickerDialog(view, getString(R.string.dialog_number_picker_weight_title), 2, 880, 3);
	}
	
	private void createInchDialog(String title, int feetNum, float inchNum){
		numFeet=feetNum;
		numInch=inchNum;
		final View inchView=new View(this);
		inchView.setId(INCH_DIALOG_ID);
		inchView.setTag(Utility.ftToInch(numFeet)+numInch);
		
		View view=getLayoutInflater().inflate(R.layout.inch_picker_layout, null);
		final NumberPicker feet=(NumberPicker)view.findViewById(R.id.num_picker_feet);
		final NumberPicker inch=(NumberPicker)view.findViewById(R.id.num_picker_inch);
		
		feet.setMaxValue(9);
		feet.setMinValue(0);
		feet.setOnValueChangedListener(this);
		feet.setValue(feetNum);
        feet.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);//emily

		inch.setMaxValue(11);
		inch.setMinValue(0);
		inch.setOnValueChangedListener(this);
		inch.setValue(Math.round(inchNum));
        inch.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);//emily
		
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setView(view)
		.setPositiveButton(R.string.dialog_number_picker_done, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				numFeet = feet.getValue();
				numInch = inch.getValue();
				float feettoinch=Utility.ftToInch(numFeet);
				testRange(inchView, (int) (feettoinch+numInch));
			}
		})
		.show();
	}
	
	private void createNumberPickerDialog(final View buttonView, String title, int min, int max, int numPickers){
		Button button=(Button)buttonView;
		int value=Integer.valueOf(button.getText().toString());
		numHundred=value/100;
		numTen=(value-numHundred*100)/10;
		numOne=value%10;
		
		int maxHundred, minHundred;
		minHundred=min/100;
		maxHundred=max/100;
		
		View view=getLayoutInflater().inflate(R.layout.number_picker_layout, null);
		final NumberPicker hundred=(NumberPicker)view.findViewById(R.id.num_picker_hundred);
		hundred.setMaxValue(maxHundred);
		hundred.setMinValue(minHundred);
		hundred.setOnValueChangedListener(this);
		hundred.setValue(numHundred);
        hundred.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);//emily
		final NumberPicker ten=(NumberPicker)view.findViewById(R.id.num_picker_ten);
		ten.setMaxValue(9);
		ten.setMinValue(0);
		ten.setOnValueChangedListener(this);
		ten.setValue(numTen);
        ten.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //emily;
		final NumberPicker one=(NumberPicker)view.findViewById(R.id.num_picker_one);
		one.setMaxValue(9);
		one.setMinValue(0);
		one.setValue(numOne);
		one.setOnValueChangedListener(this);
        one.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //emily;

        switch(numPickers){
			case 1:
				hundred.setVisibility(View.GONE);
				ten.setVisibility(View.GONE);
				break;
			case 2:
				hundred.setVisibility(View.GONE);
				break;
		}
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setView(view)
		.setPositiveButton(R.string.dialog_number_picker_done, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				numTen = ten.getValue();
				numOne = one.getValue();
				numHundred = hundred.getValue();
				switch(buttonView.getId()){
				case R.id.button_age:
					int age=numTen*10+numOne;
					testRange(buttonView, age);
					break;
				case R.id.button_height:
					int height=numHundred*100+numTen*10+numOne;
					testRange(buttonView, height);
					break;
				case R.id.button_weight:
					int weight=numHundred*100+numTen*10+numOne;
					testRange(buttonView, weight);
					break;
				}
			}
		})
		.show();
	}

	//emily ++++
    public void showAgeRange(View view, int minYear)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();


        cal.add(Calendar.YEAR, -1);
        cal.set(cal.get(Calendar.YEAR), 11, 31, 0, 0, 0);
        String maxCal = formatter.format(cal.getTime());

        //cal.add(Calendar.YEAR, -100);
        cal.set(minYear, 0, 1, 0, 0, 0);
        String minCal = formatter.format(cal.getTime());

        String errorMessage = getResources().getString(R.string.range_msg)+(minCal + "~" + maxCal);
        //String errorMessage=getResources().getStringArray(R.array.profile_error_message)[4];
        String title=getString(R.string.profile_birthday_title);

        showAlertDialog(errorMessage, view, title,1,99, 2);
    }
    //emily ----
	
	public void testRange(final View view, int value){
		String errorMessage="";
		String title="";
		int min=0;
		int max=0;
		int numpicker=0;
		switch(view.getId()){
			case R.id.button_age:
				title=getString(R.string.dialog_number_picker_age_title);
				min=0;
				max=99;
				numpicker=2;
				if(!(value<=99&&value>=1)){
					errorMessage=getResources().getStringArray(R.array.profile_error_message)[4];
				}
				else{
					but_age.setText(String.valueOf(value));
				}
				break;
			case R.id.button_height:
				title=getString(R.string.dialog_number_picker_height_title);
				min=20;
				max=300;
				numpicker=3;
				if(mSelectedHeightUnit==ProfileTable.HEIGHT_UNIT_CM){
					if(!(value<=300&&value>=20)){
						errorMessage=getResources().getStringArray(R.array.profile_error_message)[0];
					}
					else{
						but_height.setText(String.valueOf(value));
					}
				}
				break;
			case R.id.button_weight:
				title=getString(R.string.dialog_number_picker_weight_title);
				min=2;
				max=880;
				numpicker=3;
				if(mSelectedWeightUnit==ProfileTable.WEIGHT_UNIT_KG){
					if(!(value<=400&&value>=2)){
						errorMessage=getResources().getStringArray(R.array.profile_error_message)[2];
					}
					else{
						but_weight.setText(String.valueOf(value));
					}
				}
				else{
					if(!(value<=880&&value>=4)){
						errorMessage=getResources().getStringArray(R.array.profile_error_message)[3];
					}
					else{
						but_weight.setText(String.valueOf(value));
					}
				}
				break;
			case INCH_DIALOG_ID:
				if(!(value<=118&&value>=8)){
					errorMessage=getResources().getStringArray(R.array.profile_error_message)[1];
				}
				else{
					but_height.setText(numFeet+"' "+(int)Math.round(numInch)+"\"");
				}
				break;
		}
		final String finaltitle=title;
		final int finalmin=min;
		final int finalmax=max;
		final int finalnumpicker=numpicker;
		if(!errorMessage.matches("")){
            showAlertDialog(errorMessage, view, finaltitle,finalmin,finalmax, finalnumpicker);
		}
	}

    private void showAlertDialog(String errorMessage, final View view, final String finaltitle,
                                 final int finalmin, final int finalmax, final int  finalnumpicker){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.accepted_range_title))
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if(view.getId() == R.id.button_age){
                            startAgePicker(view);
                        }
                        else if(view.getId()==INCH_DIALOG_ID){
                            float inch=(Float) view.getTag();
                            numFeet=(int) Utility.InchToFt(inch);
                            numInch=inch-Utility.ftToInch(numFeet);
                            createInchDialog(getString(R.string.dialog_number_picker_height_title), numFeet, numInch);
                        }
                        else{
                            createNumberPickerDialog(view, finaltitle, finalmin, finalmax, finalnumpicker);
                        }
                    }
                })
                .show();
    }

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		// TODO Auto-generated method stub
		switch(picker.getId()){
			case R.id.num_picker_hundred:
				numHundred=newVal;
				break;
			case R.id.num_picker_ten:
				numTen=newVal;
				break;
			case R.id.num_picker_one:
				numOne=newVal;
				break;
			case R.id.num_picker_feet:
				numFeet=newVal;
				break;
			case R.id.num_picker_inch:
				numInch=newVal;
				break;
		}
	}

	//Used to detect keyboard show/hide
    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        mActivityRootView.getWindowVisibleDisplayFrame(r);
    
        int heightDiff = mActivityRootView.getRootView().getHeight() - (r.bottom - r.top);
        if (heightDiff > TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())) { // if more than 100 dp, its probably a keyboard...
            //soft keyboard showing
            if (mTutorialButtonContainer.getVisibility() == View.VISIBLE){
                if (mBottomContainer.getChildCount() > 0){
                    int index = -1;
                    for (int i = 0 ; i < mBottomContainer.getChildCount(); i++){
                        if (mTutorialButtonContainer == mBottomContainer.getChildAt(i)){
                            index = i;
                            break;
                        }
                    }
                    if (index >= 0){
                    	mBottomContainer.removeView(mTutorialButtonContainer);
                        mScrollerContent.addView(mTutorialButtonContainer);
                    }
                }
            }
            
        }else{
            //soft keyboard hiding
            if (mTutorialButtonContainer.getVisibility() == View.VISIBLE && mScrollerContent.findViewById(R.id.tutorial_button_container) != null){
                mScrollerContent.removeView(mTutorialButtonContainer);
                mBottomContainer.addView(mTutorialButtonContainer);
            }
        }
    }

    //emily ++++
    private void setBirthDay(int year, int month, int day){
        mYear = year;
        mMonth = month;
        mDay = day;
    }

    private void setBut_age(int year, int month, int date, View view, int minYear){
        Calendar cal = Calendar.getInstance();

        if(year < cal.get(Calendar.YEAR) ){//&& year >= (cal.get(Calendar.YEAR)-100)){
            setBirthDay(year,month,date);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            cal.set(year, month, date);
            but_age.setText(formatter.format(cal.getTime()));
        }
        else{
            showAgeRange(view, minYear);
        }
    }
	
	private Calendar getBirthDayCal(Long birthday){
        Calendar cal=Calendar.getInstance();
        if(birthday == null || birthday == 0) {
            int age = AGE_DEFAULT;
            if(mProfileData != null){
                age = mProfileData.age;
            }
            cal.add(Calendar.YEAR, -age);
            cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
        }
        else {
            cal.setTimeInMillis(birthday);
        }
        return cal;
    }

    private long getBirthdayInTimeMillis(){
        long timeMillis = 0;
        if(mYear!=-1 && mMonth!=-1 && mDay!=-1){
            Calendar calendar = Calendar.getInstance();
            calendar.set(mYear, mMonth, mDay, 0, 0, 0);
            timeMillis = calendar.getTimeInMillis();
        }
        return timeMillis;
    }
    //emily ----
    

}

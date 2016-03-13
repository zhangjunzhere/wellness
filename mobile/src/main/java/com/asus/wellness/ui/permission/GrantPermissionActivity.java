package com.asus.wellness.ui.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class GrantPermissionActivity extends Activity {
    TextView mTitle;
    TextView mSubTitle;
    TextView mNote;
    Button mTurnon;
    public static String TYPEKEY="permisson_key";
    public  static final int TYPE_BODY_SENSOR =0;
    public  static final int TYPE_LOCATION =1 ;
    public  static final int TYPE_STORAGE =2;
    public int mType = TYPE_BODY_SENSOR;
    public static final int START_ACTIVITY_REQUEST_CODE_BODYSENSOR = 11;
    public static final int START_ACTIVITY_REQUEST_CODE_STORAGE_PICK = 12;
    public static final int START_ACTIVITY_REQUEST_CODE_STORAGE_TAKEPHOTO = 13;
    public static final int START_ACTIVITY_REQUEST_CODE_LOCATION = 14;
    public static final int START_ACTIVITY_REQUEST_CODE_STORAGE_WRITE = 15;

    public static final int NEVER_SHOW_TIME = 200;
    private long mNeverShowTime=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_permission);
        mTitle= (TextView) findViewById(R.id.perm_title);
        mSubTitle = (TextView) findViewById(R.id.perm_subtitle);
        mNote = (TextView) findViewById(R.id.perm_note);
        mTurnon = (Button) findViewById(R.id.perm_turnon_now);
        mType = TYPE_BODY_SENSOR;
        Intent intent = getIntent();
        if(intent!=null)
        {
            mType =  intent.getIntExtra(TYPEKEY, TYPE_BODY_SENSOR);
        }
        showUI(mType);
        mTurnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermission();
            }
        });

        Utility.trackerScreennView(getApplicationContext(), "Goto GrantPermission");
    }
    private void grantPermission()
    {
        String permisson=getPermission();
        if(PermissionHelper.checkPermission(permisson,this))
        {
            Log.i("smile", "permission" + permisson + " has granted");
            setResult(RESULT_OK);
            this.finish();

        }
        mNeverShowTime = System.currentTimeMillis();
        PermissionHelper.grantPermission(permisson, this, mType);


    }
    String getPermission()
    {
        String permisson =  Manifest.permission.BODY_SENSORS;
        switch (mType)
        {
            case TYPE_LOCATION:
                permisson =  Manifest.permission.ACCESS_COARSE_LOCATION;
                break;
            case TYPE_STORAGE:
                permisson =  Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
            case TYPE_BODY_SENSOR:
                permisson =  Manifest.permission.BODY_SENSORS;
                break;
        }
        return permisson;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String permisson=getPermission();
        if(PermissionHelper.checkPermission(permisson,this))
        {
            Log.i("smile", "permission" + permisson + " has granted");
            setResult(RESULT_OK);
            this.finish();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0] ==   PackageManager.PERMISSION_GRANTED)
        {
            Log.i("smile", "onRequestPermissionsResult PERMISSION_GRANTED ");
            setResult(RESULT_OK);
            this.finish();
        }
        else
        {
            long span = System.currentTimeMillis() - mNeverShowTime;
            Log.i("smile", "onRequestPermissionsResult span "+span);
            if(span< NEVER_SHOW_TIME)
            {
                Utility.openSettingAppInfo(getPackageName(),this);
            }
        }

        Log.i("smile", "onRequestPermissionsResult " + requestCode);
    }
    private void showUI(int type)
    {
        String title = getString(R.string.permisson_body_sensor_title);
        String subtitle= getString(R.string.permisson_body_sensor_subtitle);
        String note = getString(R.string.permisson_notice_bodysensor_location);
        switch (type)
        {
            case TYPE_LOCATION:
                title = getString(R.string.permisson_location_title);
                subtitle= getString(R.string.permisson_location_subtitle);
                break;
            case TYPE_STORAGE:
                title = getString(R.string.permisson_storage_title);
                subtitle= getString(R.string.permisson_storage_subtitle);
                note = getString(R.string.permisson_notice_storage);
                break;
            case TYPE_BODY_SENSOR:
            default:
                break;
        }
        mTitle.setText(title);
        mSubTitle.setText(subtitle);
        mNote.setText(note);
    }
}

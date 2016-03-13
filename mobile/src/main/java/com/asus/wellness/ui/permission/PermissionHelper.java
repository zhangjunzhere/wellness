package com.asus.wellness.ui.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.asus.wellness.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/11/18.
 */
public class PermissionHelper {

    public static boolean checkLocationPermission(Activity context,int requestCode)
    {
        if(!PermissionHelper.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, context))
        {
            grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION, context,requestCode);
            return  false;
        }
        return  true;
    }
  public static void checkStoragePermission(Activity context,int requestCode)
    {
        if(!PermissionHelper.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, context))
        {
//            Intent intent = new Intent(context, GrantPermissionActivity.class);
//            intent.putExtra(GrantPermissionActivity.TYPEKEY,GrantPermissionActivity.TYPE_STORAGE);
//            context.startActivityForResult(intent,requestCode);
             grantPermission(Manifest.permission.READ_EXTERNAL_STORAGE, context,requestCode);
        }
        else {
            if(requestCode == GrantPermissionActivity.START_ACTIVITY_REQUEST_CODE_STORAGE_PICK)
            {
                Utility.pickImage(context);
            }
            else
            {
                Utility.takePhoto(context);
            }
        }
    }
   public static    boolean checkPermission(String permisson,Context context)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        int val =  ContextCompat.checkSelfPermission(context, permisson);
        Log.i("smile", permisson + " val: "+val);
        if(val == PackageManager.PERMISSION_GRANTED)
        {
            Log.i("smile", permisson + " granted");
            return  true;
        }
        Log.i("smile", permisson + " not granted");
        return false;
    }
    public static void grantPermission(String permisson,Activity context,int requestCode)
    {
       if(!ActivityCompat.shouldShowRequestPermissionRationale(context, permisson))
       {
           Log.i("smile", permisson+ " shouldShowRequestPermissionRationale false");
       }
        else {
           Log.i("smile", permisson+ " shouldShowRequestPermissionRationale true");
       }

        String[] ptrs = new String[]{permisson};
        ActivityCompat.requestPermissions(context, ptrs, requestCode);

    }
   public static void grantPermission(List<String> permissons,Activity context,int requestCode)
    {


        List<String> pRets = new ArrayList<>();
        for(String permisson : permissons)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permisson))
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
        ActivityCompat.requestPermissions(context, ptrs, requestCode);

    }
//type  GrantPermissionActivity.type_body_sensor
    public static  void checkSpecialPermission(Context context,int type)
    {
        String permisson =  Manifest.permission.BODY_SENSORS;
        switch (type)
        {
            case GrantPermissionActivity.TYPE_LOCATION:
                permisson =  Manifest.permission.ACCESS_COARSE_LOCATION;
                break;
            case GrantPermissionActivity.TYPE_STORAGE:
                permisson =  Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
            case GrantPermissionActivity.TYPE_BODY_SENSOR:
                permisson =  Manifest.permission.BODY_SENSORS;
                break;
        }
        if(checkPermission(permisson,context))
        {
            return;
        }
        Intent intent = new Intent(context,GrantPermissionActivity.class);
        intent.putExtra(GrantPermissionActivity.TYPEKEY,type);
        context.startActivity(intent);
    }
}

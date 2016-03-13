package com.asus.wellness.sync;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.asus.sharedata.ShareUtils;
import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2015/9/8.
 */
public class SyncHelper {

    public static  void sendPhptoToWear(Context context,Bitmap bitmap,String photoUrl)
    {
        if(bitmap == null)
        {
            return;
        }
        byte[] bmpdata = ShareUtils.toByteArray(bitmap);
       sendPhptoToWear(context,bmpdata,photoUrl);
    }
    public static  void sendPhptoToWear(Context context,byte[] bmpdata,String photoUrl)
    {
        Intent i = new Intent(context, SyncService.class);
        i.putExtra(SyncService.Key_Command,SyncService.Command_Photo);
        i.putExtra(SyncService.Key_Command_Photo_Data,bmpdata);
        i.putExtra(SyncService.Key_Command_Photo_Url,photoUrl);
        context.startService(i);
    }
}

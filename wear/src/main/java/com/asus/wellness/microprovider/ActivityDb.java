package com.asus.wellness.microprovider;

import android.content.Context;

import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Activity_status;

import java.util.List;

/**
 * Created by smile_gao on 2015/6/17.
 */
public class ActivityDb {
    public static boolean checkIsHaveActivity(Context context){
//        Cursor cursor=context.getContentResolver().query(ActivityStatusTable.TABLE_URI, null, null, null, null);
//        boolean isHaveActivity=false;
//        if(cursor.moveToFirst()){
//            isHaveActivity=true;
//        }
//        else{
//            isHaveActivity=false;
//        }
//        cursor.close();
        List<Activity_status> list = WApplication.getInstance().getDataHelper().getDaoSession().getActivity_statusDao().loadAll();
        if(list!=null && list.size()>0)
        {
            return  true;
        }
        return false;
    }
}

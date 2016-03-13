package com.asus.wellness.dbhelper;

import android.content.Context;

import android.util.Log;

/**
 * Created by smile_gao on 2015/5/14.
 */
public class DataHelper {
    private Context context;
    private static final String TAG = "DatabaseHelper";

    private String DATABASE_NAME = "asus_wellness.db";

    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private static DataHelper sDataHelper = null;

    public static DataHelper getInstance(Context context){
        if(sDataHelper == null){
            synchronized (DataHelper.class){
                if(sDataHelper == null) {
                    sDataHelper = new DataHelper(context);
                }
            }

        }
        return  sDataHelper;
    }


    public DataHelper(Context context, String database){
        DATABASE_NAME = database;
        this.context = context;
        daoMaster = getDaoMaster();
        daoSession = getDaoSession();
    }

    private DataHelper(Context context){
        this(context, "asus_wellness.db");
    }

    private  DaoMaster getDaoMaster()
    {
        if (daoMaster == null)
        {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(DataHelper.this.context.getApplicationContext(), DATABASE_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    public  DaoSession getDaoSession()
    {
        if (daoSession == null)
        {
            if (daoMaster == null)
            {
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}

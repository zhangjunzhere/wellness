package com.asus.wellness.dbhelper;

import android.content.Context;

/**
 * Created by smile_gao on 2015/5/14.
 */
public class DataHelper {
    private Context context;
    private static final String TAG = "DatabaseHelper";

    private String DATABASE_NAME = "asus_wellness.db";

    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public DataHelper(Context context, String database){
        this(context);
        DATABASE_NAME = database;
    }
    public DataHelper(Context context){
        this.context = context;
        daoMaster = getDaoMaster();
        daoSession = getDaoSession();
    }
    private  DaoMaster getDaoMaster()
    {
        if (daoMaster == null)
        {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(DataHelper.this.context, DATABASE_NAME, null);
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

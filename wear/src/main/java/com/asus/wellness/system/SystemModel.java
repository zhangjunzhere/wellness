package com.asus.wellness.system;

import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.SystemInfo;
import com.asus.wellness.dbhelper.SystemInfoDao;

import java.util.List;

/**
 * Created by smile_gao on 2015/5/22.
 */
public class SystemModel {

    static  SystemModel sm = null;
    static  SystemInfo mSystemInfo = null;
    private SystemModel()
    {
        init();
    }


    /**
     * get systeminfo dao
     */
    private SystemInfoDao getSystemInfoDao()
    {
        return WApplication.getInstance().getDataHelper().getDaoSession().getSystemInfoDao();
    }

    /**
     *  init, when not exist ,insert one
     */
    private void init()
    {
        List<SystemInfo> list = getSystemInfoDao().loadAll();
        if(list ==null || list.size() ==0)
        {
            SystemInfo s = new SystemInfo(null,true);
            Long  id= getSystemInfoDao().insert(s);
            if(id!=null)
            {
                mSystemInfo =  getSystemInfoDao().load(id);
                return;
            }
        }
        if(list.size()>0)
        {
            mSystemInfo = list.get(0);
        }
    }

    /**
     * get systemmodel, when not exist ,create one
     * @return
     */
    public static SystemModel getIntance()
    {
        if(sm == null)
        {
            sm = new SystemModel();
        }
        return sm;
    }

    /**
     * get first use value
     * @return
     */
    public boolean isFirstUse()
    {
        return  mSystemInfo.getFirtuse();
    }
    /**
     * set first use
     * @param firstUse   firstuse of not
     */
    public  void setFirstUse(boolean firstUse)
    {
        if(mSystemInfo!= null)
        {
            mSystemInfo.setFirtuse(firstUse);;
            getSystemInfoDao().update(mSystemInfo);
        }
    }
}

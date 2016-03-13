package com.asus.wellness.utils;

import android.content.Context;
import android.database.Cursor;

import com.asus.wellness.WApplication;
import com.asus.wellness.WellnessLocationManager;
import com.asus.wellness.dbhelper.Location_change;
import com.asus.wellness.dbhelper.Location_changeDao;
import com.asus.wellness.provider.LocationChangeTable;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by smile_gao on 2015/7/2.
 */
public class LocationHelper {
    private volatile static  LocationHelper mInstance = null;
    private static ArrayList<SoftReference<LocationVal>> locatoinArray = null;
    private static long mFirstLocationTime = -1;
    private LocationHelper(Context context)
    {
        initData(context);
    }
    public static LocationHelper getInstance(Context context)
    {
        if(mInstance == null)
        {
            init(context);
        }
        return mInstance;
    }
    public static void init(Context context)
    {
        if(mInstance == null)
        {
            synchronized (LocationHelper.class) {
                if(mInstance == null)
                    mInstance = new LocationHelper(context);
            }
        }
    }
    public  synchronized    void initData(Context mContext)
    {
        locatoinArray = new ArrayList<SoftReference<LocationVal>>();
        Location_changeDao ldao = WApplication.getInstance().getDataHelper().getDaoSession().getLocation_changeDao();
       List<Location_change> list = ldao.queryBuilder().where(Location_changeDao.Properties.District.notEq(WellnessLocationManager.DEFAULT_DISTRICT)).orderDesc(Location_changeDao.Properties.Get_location_time).limit(100).list();

        for(Location_change lc : list)
        {
            LocationVal lv = new LocationVal(lc.getId(),lc.getGet_location_time(),lc.getDistrict());
            SoftReference<LocationVal> slv= new SoftReference<LocationVal>(lv);
            locatoinArray.add(slv);
        }
       List<Location_change> listfirt = ldao.queryBuilder().where(Location_changeDao.Properties.District.notEq(WellnessLocationManager.DEFAULT_DISTRICT)).limit(1).list();
        if(listfirt!=null && listfirt.size()>0)
        {
            mFirstLocationTime = listfirt.get(0).getGet_location_time();
        }
    }
    public synchronized void updateLocationDate(int id, String district)
    {
        if(locatoinArray == null)
        {
           return;
        }
        for (int i =0 ;i < locatoinArray.size() ; i++)
        {
            if(locatoinArray.get(i)==null|| locatoinArray.get(i).get() == null)
            {
                continue;
            }
            if(locatoinArray.get(i).get().id == id)
            {
                locatoinArray.get(i).get().district =  district;
            }
        }
    }
    public synchronized void addLocationData(LocationVal lval)
    {
        if(locatoinArray == null)
        {
            locatoinArray = new ArrayList<SoftReference<LocationVal>>();
        }
        locatoinArray.add(0,new SoftReference<LocationVal>(lval));
    }
    public synchronized String  getDistrict(long starttime,long endtime)
    {
        if(locatoinArray == null || locatoinArray.size() ==0 || mFirstLocationTime ==-1 )
        {
            return  WellnessLocationManager.DEFAULT_DISTRICT;
        }
        String ditrict = getDistrictReal(endtime);
        if(endtime>0 && ditrict.equals(WellnessLocationManager.DEFAULT_DISTRICT))
        {
            return  getDistrictReal(endtime);
        }
        if( starttime> mFirstLocationTime )
        {
            return getLocationByTime(starttime);
        }
        else if(endtime > mFirstLocationTime)
        {
            return getLocationByTime(endtime);
        }
        return  ditrict;
    }
    public String getDistrictReal(long time)
    {
        if(locatoinArray == null || locatoinArray.size() ==0)
        {
            return  WellnessLocationManager.DEFAULT_DISTRICT;
        }
        if(locatoinArray.get(0)==null||locatoinArray.get(0).get() == null)
        {
            return  getLocationByTime(time);
        }
//        if(time>= locatoinArray.get(0).get().time)
//        {
//            return  locatoinArray.get(0).get().district;
//        }
        for(int i=0;i<locatoinArray.size()-1;i++)
        {
            if(locatoinArray.get(i)==null || locatoinArray.get(i+1)==null || locatoinArray.get(i).get()==null || locatoinArray.get(i+1).get()==null)
            {
                return  getLocationByTime(time);
            }
            if(time<=locatoinArray.get(i).get().time && time>= locatoinArray.get(i+1).get().time)
            {
                return  locatoinArray.get(i+1).get().district;
            }
        }
        return  WellnessLocationManager.DEFAULT_DISTRICT;
    }
    public synchronized  String getLocationByTime(long time)
    {
       QueryBuilder qb=WApplication.getInstance().getDataHelper().getDaoSession().getLocation_changeDao().queryBuilder();
       List<Location_change> locationValList= qb.where(Location_changeDao.Properties.District.notEq(WellnessLocationManager.DEFAULT_DISTRICT))
        .where(Location_changeDao.Properties.Get_location_time.le(time))
                .orderDesc(Location_changeDao.Properties.Get_location_time).list();
        if(locationValList!=null && locationValList.size()>0)
        {
            return locationValList.get(0).getDistrict();
        }
        return  WellnessLocationManager.DEFAULT_DISTRICT;
    }
    public synchronized  Location_change getLocationChangeByTime(long time)
    {
        QueryBuilder qb=WApplication.getInstance().getDataHelper().getDaoSession().getLocation_changeDao().queryBuilder();
        List<Location_change> locationValList= qb.where(Location_changeDao.Properties.District.notEq(WellnessLocationManager.DEFAULT_DISTRICT))
                .where(Location_changeDao.Properties.Get_location_time.le(time))
                .orderDesc(Location_changeDao.Properties.Get_location_time).list();
        if(locationValList!=null && locationValList.size()>0)
        {
            return locationValList.get(0);
        }
        return  null;
    }
   public class  LocationVal {
        public  long id;
        public  long time;
        public String district;
        public LocationVal(long id,long t, String dis)
        {
            this.id = id;
            time = t ;
            district = dis;
        }
    }
}

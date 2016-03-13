package com.asus.wellness.utils;

import android.content.Context;
import android.net.wifi.WpsInfo;

import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.sync.DataEventResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/8/13.
 */
public class EcgHelper {
    public static DataEventResult writeEcgInfo(EcgDao ecgDao,Long deviceId,Long measure_time, Long measure_value, Long measure_type, String measure_comment, Context context)
    {
        DataEventResult dataEventResult = new DataEventResult();
        List<Ecg> ecgList =   ecgDao.queryBuilder().where(EcgDao.Properties.DeviceId.eq(deviceId),
                EcgDao.Properties.Measure_time.eq(measure_time),
                EcgDao.Properties.Measure_value.eq(measure_value),
                EcgDao.Properties.Measure_type.eq(measure_type)).list();
        if(ecgList == null || ecgList.size() == 0) {
            Ecg ecg = new Ecg(null, measure_time, measure_value, measure_type, measure_comment, deviceId);
            ecgDao.insert(ecg);
            Utility.updateLastUpdateTime(context, System.currentTimeMillis());
            dataEventResult.ecgUpdate = true;
        }
        return dataEventResult;
    }
    public static DataEventResult writeEcgInfo(List<Ecg> ecgList,Long deviceId)
    {
        EcgDao ecgDao = WApplication.getInstance().getDataHelper().getDaoSession().getEcgDao();
        DataEventResult dataEventResult = new DataEventResult();
        List<Ecg> list = new ArrayList<>();
        for(Ecg ecg : ecgList)
        {
            ecg.setDeviceId(deviceId);
            ecg.setId(null);
            if(!isExist(ecg,ecgDao))
            {
                list.add(ecg);
            }
        }
        if(list.size()>0) {
            dataEventResult.ecgUpdate = true;
            ecgDao.insertInTx(list);
        }
        return dataEventResult;
    }
    public static boolean isExist(Ecg ecg,EcgDao ecgDao)
    {
        List<Ecg> ecgList =   ecgDao.queryBuilder().where(EcgDao.Properties.DeviceId.eq(ecg.getDeviceId()),
                EcgDao.Properties.Measure_time.eq(ecg.getMeasure_time()),
                EcgDao.Properties.Measure_value.eq(ecg.getMeasure_value()),
                EcgDao.Properties.Measure_type.eq(ecg.getMeasure_type())).list();
        if(ecgList ==null || ecgList.size() ==0 )
        {
            return  false;
        }
        return  true;
    }
}

package com.asus.wellness.utils;

import android.util.Log;

import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Activity_state;
import com.asus.wellness.dbhelper.Activity_stateDao;
import com.asus.wellness.dbhelper.DataHelper;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.dbhelper.DeviceDao;
import com.asus.wellness.dbhelper.Ecg;
import com.asus.wellness.dbhelper.EcgDao;
import com.asus.wellness.provider.WellnessProvider;

import java.util.Iterator;
import java.util.List;

/**
 * Created by smile_gao on 2015/7/8.
 */
public class DeviceHelper {
    public synchronized static Device addOrUpdateDeivce(Device connectedWatch)
    {
        connectedWatch.setLastconnecttime(System.currentTimeMillis());
        DeviceDao deviceDao = WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao();
        if(connectedWatch.getIsRobin())
        {
            long start = System.currentTimeMillis();
            List<Device> robinDevice = deviceDao.queryBuilder().where(DeviceDao.Properties.IsRobin.eq(true)).list();
            if(robinDevice == null || robinDevice.size() ==0)
            {
              long id= deviceDao.insert(connectedWatch);
                if(id>0)
                {
                  connectedWatch.setId(id);
                }
                Log.i("smile", "update time " + (System.currentTimeMillis() - start));
                return connectedWatch;

            }
            else if(robinDevice !=null && robinDevice.size()>0)
            {
                Device device = robinDevice.get(0);
                if(device.getBlueaddr()==null)
                {
                    connectedWatch.setId(device.getId());
                    deviceDao.update(connectedWatch);
                    Log.i("smile", "update  old robin watch with addr null");
                    return  connectedWatch;
                }
            }

        }
        List<Device> devices = deviceDao.queryBuilder().where(DeviceDao.Properties.Name.eq(connectedWatch.getName())).orderDesc(DeviceDao.Properties.Lastconnecttime).list();
        if(devices.size() > 0)
        {
            devices.get(0).setLastconnecttime(System.currentTimeMillis());
            devices.get(0).setIsRobin(connectedWatch.getIsRobin());
            connectedWatch = devices.get(0);
            deviceDao.update(connectedWatch);
        }
        else
        {
            deviceDao.insert(connectedWatch);
        }
        return  connectedWatch;

    }

    public static void updateDeviceConnectTime(Device device){
        DataHelper dbHelper = WApplication.getInstance().getDataHelper();
        DeviceDao deviceDao =  dbHelper.getDaoSession().getDeviceDao();
        device.setLastconnecttime(System.currentTimeMillis());
        deviceDao.update(device);
    }

    public static List<Device> getConnectedDevices(){
        DataHelper dbHelper = WApplication.getInstance().getDataHelper();
        DeviceDao deviceDao =  dbHelper.getDaoSession().getDeviceDao();
        return deviceDao.queryBuilder().orderDesc(DeviceDao.Properties.Lastconnecttime).list();
    }

    public static Device getDefaultDevice(){
        List<Device> devices = getConnectedDevices();
        Device device;
        if(devices.size() >0){
            device =  devices.get(0);
        }else{
             device =  new Device(null);
            device.setIsRobin(true);
        }
        return device;
    }

    public static boolean isDeviceValid(Device device){
        return  device.getId() != null;
    }
    public static Device isDeviceExist(Device device)
    {
        List<Device> devices = getConnectedDevices();
        if(devices == null || devices.size()==0)
        {
            return  null;
        }
        for (Device d : devices)
        {
            if(d.getName().equals(device.getName()))
            {
                return  d;
            }
        }
        return  null;
    }
/// get device id by name
    public static  Long getDeviceIdByName(Device device)
    {

      //  return getDefaultDevice().getId();
        return  getDeviceByName(device).getId();
    }
    public synchronized static Device getDeviceByName(Device device)
    {
        if(device == null)
        {
            Device device1 = getDefaultRobinOrCreateNew(WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao());
            if(WApplication.getInstance().getConnectedDevice().getId() == null)
            {
                WApplication.getInstance().setConnectedDevice(device1);
            }
            return device1;
        }
        Device deviceDb = isDeviceExist(device);
        if(deviceDb!=null)
        {
            if(WApplication.getInstance().getConnectedDevice().getId() == null)
            {
                WApplication.getInstance().setConnectedDevice(deviceDb);
            }
            return  deviceDb;
        }

//        List<Device> devices = getConnectedDevices();
//        if(devices == null || devices.size()==0)
//        {
//            //fix clean db , no device bug
//            Device device1  =  addOrUpdateDeivce(device);
//            if(WApplication.getInstance().getConnectedDevice().getId() == null)
//            {
//                WApplication.getInstance().setConnectedDevice(device);
//            }
//            return device1;
//        }
//        for (Device d : devices)
//        {
//            if(d.getName().equals(device.getName()))
//            {
//                device.setId(d.getId());
//                return  d;
//            }
//        }
        // new device income
        return addDevice(device);
    }
    private synchronized static  Device getDefaultRobinOrCreateNew(DeviceDao deviceDao)
    {
        Device device = null;
   //     DeviceDao deviceDao =   WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao();
        List<Device> robinDevice = deviceDao.queryBuilder().where(DeviceDao.Properties.IsRobin.eq(true)).list();
        if(robinDevice !=null && robinDevice.size()>0)
        {
            device = robinDevice.get(0);
            return device;
        }
        else
        {
           return addNewRobinDevice(deviceDao);
        }
    }
    private static Device addDevice(Device device)
    {

        if(device.getIsRobin())
        {
            //update blue adr == null device
             Device d =  updateExistRobinDevice(device);
            if(d != null)
            {
                return  d;
            }
        }
        device.setLastconnecttime(0L);
        DeviceDao deviceDao = WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao();
        long id = deviceDao.insert(device);
        device.setId(id);
        if(WApplication.getInstance().getConnectedDevice().getId() == null)
        {
            WApplication.getInstance().setConnectedDevice(device);
        }
        return device;
    }
    private static Device updateExistRobinDevice(Device device)
    {
        DeviceDao deviceDao = WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao();
        List<Device> robinDevices = deviceDao.queryBuilder().where(DeviceDao.Properties.IsRobin.eq(true)).list();
        if(robinDevices!=null && robinDevices.size()>0) {
            Device robindevice = robinDevices.get(0);
            if (robindevice.getBlueaddr() == null) {
                device.setId(robindevice.getId());
                device.setLastconnecttime(System.currentTimeMillis());
                deviceDao.update(device);
                Log.i("smile", "update  old robin watch with addr null");
                return device;
            }
        }
        return  null;
    }
    private static Device addNewRobinDevice(DeviceDao deviceDao)
    {
        Device device = new Device(null); //,"Asus ZenWatch",null,true,System.currentTimeMillis(),null,null
        device.setName("Asus ZenWatch");
        device.setIsRobin(true);
        device.setLastconnecttime(System.currentTimeMillis());
        long id = deviceDao.insert(device);
        Log.i("smile","robin not exist id: "+id +" deviceid: "+device.getId());
        return  device;
    }
   //first start for init robin device
    public static void initRobinDevice(DataHelper dh)
    {
        Activity_stateDao activity_stateDao =   dh.getDaoSession().getActivity_stateDao();
        EcgDao ecgDao =  dh.getDaoSession().getEcgDao();
        DeviceDao deviceDao =   dh.getDaoSession().getDeviceDao();
        try {
            initRobinDeviceImpl(activity_stateDao, ecgDao, deviceDao);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    private static Device initRobinDeviceImpl(Activity_stateDao activity_stateDao, EcgDao ecgDao, DeviceDao deviceDao)
    {
        Device rdevice = null;

        List<Activity_state> activity_stateIterator = activity_stateDao.queryBuilder().where(Activity_stateDao.Properties.DeviceId.isNull()).list();
        List<Ecg> ecglist = ecgDao.queryBuilder().where(EcgDao.Properties.DeviceId.isNull()).list();
        if((activity_stateIterator!=null && activity_stateIterator.size()>0)
                ||(ecglist!=null && ecglist.size()>0) )
        {
                rdevice = getDefaultRobinOrCreateNew(deviceDao);
                Long id = rdevice.getId();
                updateDeviceIdNullActivity(id,activity_stateDao,activity_stateIterator,ecgDao,ecglist);

        }
        return rdevice;

    }


    private static void updateDeviceIdNullActivity(Long id,Activity_stateDao activity_stateDao,List<Activity_state> activity_stateIterator,EcgDao ecgDao, List<Ecg> ecglist)
    {
        if(activity_stateIterator.size()>0) {
            for (int i = 0; i < activity_stateIterator.size(); i++) {
                activity_stateIterator.get(i).setDeviceId(id);
            }
            activity_stateDao.updateInTx(activity_stateIterator);
        }

        if(ecglist.size()>0 ) {
            for (int i = 0; i < ecglist.size(); i++) {
                ecglist.get(i).setDeviceId(id);
            }
            ecgDao.updateInTx(ecglist);
        }
    }
}

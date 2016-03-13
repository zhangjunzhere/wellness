package com.asus.wellness.sleep;

import com.asus.sharedata.SyncSleep;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Sleep;
import com.asus.wellness.dbhelper.SleepDao;
import com.asus.wellness.utils.EBCommand;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/6/17.
 */
public class SleepDataModel {


    public  enum eSleep{
        AWAKE,START,FINISH;
    }

    private Sleep sleepRecord;
    private long start;
    private List<Integer> sleepItems = new ArrayList<Integer>();

    private eSleep esleep = eSleep.AWAKE;


    public static SleepDataModel sModel;
    public static SleepDataModel getInstance(){
        if(sModel == null){
            sModel = new SleepDataModel();
        }
        return sModel;
    }

    private SleepDataModel(){

    }

    public void setSleepStatus(eSleep esleep){
        this.esleep = esleep;
    }
    public eSleep getSleepStatus(){
        return this.esleep;
    }

    public boolean getSleepEnabled(){
        return this.esleep == eSleep.START;
    }

    public   void onSleepTriggered( boolean enabled)
    {
        SleepDao sleepDao = WApplication.getInstance().getDataHelper().getDaoSession().getSleepDao();
        long now  = System.currentTimeMillis();
        String date =  new SimpleDateFormat(SyncSleep.DATE_FORMAT).format(now);

        if(enabled){
            start = now;
            sleepItems.clear();
            sleepRecord = new Sleep(null, System.currentTimeMillis(), -1L, date, "");
        }else{
            //save sleep data, if span less than 2 minute, invalid sleep
            if(sleepItems.size() == 0 || (now - start < 1*60*1000)) {
                return ;
            }
            StringBuilder sb = new StringBuilder();

            for (long value : sleepItems) {
                sb.append(SyncSleep.SEPERATOR +value);
            }
            sb.deleteCharAt(0);

            //sleepRecord.setStart(start);
            sleepRecord.setEnd(now);
            sleepRecord.setDate(date);
            sleepRecord.setData(sb.toString());

            sleepDao.insert(sleepRecord);

        }

    }

    public void onValueChanged(long value){
//        SleepItem sleepItem = new SleepItem(-1L,System.currentTimeMillis(),value);
        sleepItems.add((int)value);
//        SleepItemDao sleepItemDao = WApplication.getInstance().getDataHelper().getDaoSession().getSleepItemDao();
//        sleepItemDao.insert(sleepItem);
    }

    /**
     * @param timeMillis
     * @return
     */

    public Sleep getSleepRecordByDate(Long timeMillis){
        SleepDao sleepDao = WApplication.getInstance().getDataHelper().getDaoSession().getSleepDao();
        String date =  new SimpleDateFormat(SyncSleep.DATE_FORMAT).format(timeMillis);
        List<Sleep> sleepList = sleepDao.queryBuilder().where(SleepDao.Properties.Date.eq(date)).orderDesc(SleepDao.Properties.End).list();
        if(sleepList.size() > 0){
            return  sleepList.get(0);
        }else{
            return null;
        }
    }

    public void changeSleepState(){
        EBCommand ebCommand = new EBCommand(this.getClass().getName(), SleepActivity.class.getName(),EBCommand.COMMAND_SLEEP_STATE,null);
        EventBus.getDefault().post(ebCommand);
    }
}

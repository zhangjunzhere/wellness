package com.asus.wellness.sync;

/**
 * Created by smile_gao on 2015/8/6.
 */
public class DataEventResult {
    public boolean stepUpdate =false;
    public boolean ecgUpdate = false;
    public boolean sleepUpdate = false;
    public boolean coachUpdate = false;

    public void setResult(DataEventResult der)
    {
        if(der.stepUpdate)
        {
            stepUpdate = der.stepUpdate;
        }
        if(der.ecgUpdate)
        {
            ecgUpdate = der.ecgUpdate;
        }
    }

}

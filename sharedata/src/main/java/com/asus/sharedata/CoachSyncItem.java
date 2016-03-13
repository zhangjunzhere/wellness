package com.asus.sharedata;

import com.google.gson.annotations.Expose;

/**
 * Created by smile_gao on 2015/9/16.
 */
public class CoachSyncItem {

    @Expose
    public Long start;
    @Expose
    public Long end;
    @Expose
    public Long value;
    @Expose
    public Long duration;
    @Expose
    public Long type;
    public CoachSyncItem(Long start, Long end, Long val, Long dura,Long type)
    {
        this.start = start;
        this.end = end;
        this.value  = val;
        this.duration = dura;
        this.type = type;
    }
    public Long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
    public Long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getType() {
        if(type == null)
        {
           return  0;
        }
        return type.intValue();
    }

    public void setType(Long type) {
        this.type = type;
    }
}

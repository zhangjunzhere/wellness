package com.asus.wellness.Profile;

/**
 * Created by smile_gao on 2015/5/27.
 */
public class ProfileEvent  {
    private String mCmd;
    private String mPrams;
    public ProfileEvent(String cmd)
    {
        this(cmd,null);
    }
    public ProfileEvent(String cmd, String p)
    {
        mCmd = cmd;
        mPrams = p;
    }
    public String getEventCmd()
    {
        return  mCmd;
    }
    public String getmPrams()
    {
        return mPrams;
    }
}

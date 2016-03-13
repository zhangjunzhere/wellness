package com.asus.wellness.microprovider;

/**
 * Created by smile_gao on 2014/12/24.
 */
public class StepCountData {
    public long DAILY_TOTAL_STEPS;
    public long DAILY_TARGET_STEPS;

    public StepCountData(long dailyTotalSteps, long dailyTargetSteps){
        DAILY_TOTAL_STEPS=dailyTotalSteps;
        DAILY_TARGET_STEPS=dailyTargetSteps;
    }
}

package com.asus.ecg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AsusEcgProcessor {
    private final static String TAG = "AsusEcgProcessor";

    private final int MAX_RRHRLIST_SIZE = 20;
    List<Integer> mRRHRList = new LinkedList<Integer>();
    List<Integer> mSortedRRHRList = new LinkedList<Integer>();

    private final long ONE_MINUTE_MS = 60000;
    private final int MAXRRHR = 210;
    private final int MINRRHR = 30;

    private int mHeartRateMean = 0;
    private int mHeartRateMedian1 = 0;
    private int mHeartRateMedian2 = 0;
    private int mHeartRateMode = 0;

    public AsusEcgProcessor() {
        reset();
    }

    public void reset() {
        mHeartRateMean = 0;
        mHeartRateMedian1 = 0;
        mHeartRateMedian2 = 0;
        mHeartRateMode = 0;

        if(mRRHRList != null) {
            mRRHRList.clear();
        }

        if(mSortedRRHRList != null) {
            mSortedRRHRList.clear();
        }
    }

    public void inputRR(int rr) {
        int rrhr = 0;

        if(rr == 0) return;

        rrhr = (int)(ONE_MINUTE_MS / rr);
        if(rrhr < MINRRHR || rrhr > MAXRRHR) return;
        if (mRRHRList.size() >= MAX_RRHRLIST_SIZE) {
            mRRHRList.remove(0);
        }
        mRRHRList.add(rrhr);

        mSortedRRHRList.clear();
        for(int i = 0; i < mRRHRList.size(); i++) {
            mSortedRRHRList.add(mRRHRList.get(i));
        }
        Collections.sort(mSortedRRHRList);

        processHR();
    }

    public int getHeartRate() {
        return getHeartRateMedian2();
    }

    public int getHeartRateMean() {
        return mHeartRateMean;
    }

    public int getHeartRateMedian1() {
        return mHeartRateMedian1;
    }

    public int getHeartRateMedian2() {
        return mHeartRateMedian2;
    }

    public int getHeartRateMode() {
        return mHeartRateMode;
    }

    private void processHR() {
        processHR_Median2();
    }

    private void processHR_Mean() {
        int i;
        long sum = 0;
        int rrhrCount = mRRHRList.size();
        if (rrhrCount < 2) return;
        for (i = 0; i < rrhrCount; i++) {
            sum += mRRHRList.get(i);
        }
        mHeartRateMean = (int)(sum / rrhrCount);
    }

    private void processHR_Median1() {
        int rrhrCount = mSortedRRHRList.size();
        if (rrhrCount < 3) return;
        int middle = rrhrCount / 2;
        if(rrhrCount % 2 == 0) {
            mHeartRateMedian1 = (mSortedRRHRList.get(middle-1) + mSortedRRHRList.get(middle)) / 2;
        } else {
            mHeartRateMedian1 = mSortedRRHRList.get(middle);
        }
    }

    private void processHR_Median2() {
        int rrhrCount = mSortedRRHRList.size();
        if (rrhrCount < 3) return;
        int middle = rrhrCount / 2;
        if (rrhrCount == 3 || rrhrCount == 5) {
            mHeartRateMedian2 = mSortedRRHRList.get(middle);
        } else if (rrhrCount == 4 || rrhrCount == 6) {
            mHeartRateMedian2 = (mSortedRRHRList.get(middle-1) + mSortedRRHRList.get(middle)) / 2;
        } else {
            if (rrhrCount % 2 == 0) {
                mHeartRateMedian2 = (mSortedRRHRList.get(middle-2) + mSortedRRHRList.get(middle-1)
                        + mSortedRRHRList.get(middle) + mSortedRRHRList.get(middle+1)) / 4;
            } else {
                mHeartRateMedian2 = (mSortedRRHRList.get(middle-1) + mSortedRRHRList.get(middle) + mSortedRRHRList.get(middle+1)) / 3;
            }
        }
    }

    private void processHR_Mode() {
        int i;
        int rrhr = 0, count = 0;
        int maxCount = 0, modeHR = 0;
        int rrhrCount = mSortedRRHRList.size();
        if (rrhrCount < 3) return;
        int middle = rrhrCount / 2;
        for(i = 0; i < rrhrCount; i++) {
            if(mSortedRRHRList.get(i) != rrhr) {
                rrhr = mSortedRRHRList.get(i);
                count = 1;;
            } else {
                count++;
            }
            if(count > maxCount) {
                modeHR = rrhr;
                maxCount = count;
            }
        }
        if(maxCount <= 1) {
            if(rrhrCount % 2 == 0) {
                modeHR = (mSortedRRHRList.get(middle-1) + mSortedRRHRList.get(middle)) / 2;
            } else {
                modeHR = mSortedRRHRList.get(middle);
            }
        }
        mHeartRateMode = modeHR;
    }
}

package com.asus.wellness.utils;

import android.content.res.Resources;

import com.asus.wellness.R;

/**
 * Created by Kim_Bai on 5/14/2015.
 */
public class Constant {
    public static final int NUM_ITEMS = 4;
    public static final String[] CHEESES = {"C1", "CB", "CC", "CD"};

    public static  final String KEY_POSITION = "position";
    public static  final String KEY_TRIGGERED_BY_SELF = "trigger_by_self";
    public static final String KEY_MIME_TYPE = "key_mime_type";

    public static final String INTENT_MIME_RUNNING = "vnd.google.fitness.activity/running";
    public static final String INTEN_ACTION_COACH = "vnd.google.fitness.activity";

    //smile
    public  static final  int NUM_PROFILE  = 2;

    private Constant() {}

    public static int  COLOR_GREEN1, COLOR_GREEN2, COLOR_GREEN3, COLOR_GREEN4;

    public static void putColorValues(Resources resources) {
        COLOR_GREEN1 = resources.getColor(R.color.GREEN1);
        COLOR_GREEN2 = resources.getColor(R.color.GREEN2);
        COLOR_GREEN3 = resources.getColor(R.color.GREEN3);
        COLOR_GREEN4 = resources.getColor(R.color.GREEN4);
    }

}

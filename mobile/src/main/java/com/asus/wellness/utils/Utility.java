package com.asus.wellness.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.asus.wellness.EcgInfo;
import com.asus.wellness.R;
import com.asus.wellness.dbhelper.Coach;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.provider.StepGoalTable;
import com.asus.wellness.sync.SyncHelper;
import com.asus.wellness.ui.MainWellness;
import com.asus.wellness.ui.profile.SetupProfileActivity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class Utility {
    public static final boolean DEBUG = false;

    public static final long ONE_DAY_MS = 86400000;

    // kim_bai
    private static String mAmString, mPmString;
    private final static String HOURS_24 = "kk";
    private final static String HOURS = "h";
    private final static String MINUTES = ":mm";
    private static boolean mIsCheckded = false;

    public static final String HOUR_OF_DAY_FROM = "hour_of_day_from";
    public static final String MINUTE_FROM = "minute_from";
    public static final String HOUR_OF_DAY_TO = "hour_of_day_to";
    public static final String MINUTE_TO = "minute_to";

    public static String TimeFormat(int hour, int minute, boolean is24mode) {
        String[] ampm = new DateFormatSymbols().getAmPmStrings();
        mAmString = ampm[0];
        mPmString = ampm[1];

        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        String mHoursFormat = false ? HOURS_24 : HOURS;
        CharSequence h = DateFormat.format(mHoursFormat, c);
        CharSequence m = DateFormat.format(MINUTES, c);
        String mFlag = c.get(Calendar.AM_PM) == 0 ? mAmString : mPmString;
        if (is24mode) {
            return String.valueOf(h) + String.valueOf(m);
        } else {
            return String.valueOf(h) + String.valueOf(m) + " " + mFlag;
        }
    }

    public static Cursor getStepGoalCursor(Context context) {
        return context.getContentResolver().query(StepGoalTable.TABLE_URI, null, null, null, StepGoalTable.COLUMN_DATE_TIME_MILLI + " DESC");
    }

    public static Cursor getNextStepGoalCursor(Context context) {
        return context.getContentResolver().query(StepGoalTable.TABLE_URI, null, StepGoalTable.COLUMN_DATE_TIME_MILLI + "=?", new String[]{Utility.getDateTime(System.currentTimeMillis() + Utility.ONE_DAY_MS, "yyyy-MM-dd")}, StepGoalTable.COLUMN_DATE_TIME_MILLI + " DESC");
    }

    public static Cursor getPreStepGoalCursor(Context context) {
        return context.getContentResolver().query(StepGoalTable.TABLE_URI, null, StepGoalTable.COLUMN_DATE_TIME_MILLI + "=?", new String[]{Utility.getDateTime(System.currentTimeMillis() - Utility.ONE_DAY_MS, "yyyy-MM-dd")}, StepGoalTable.COLUMN_DATE_TIME_MILLI + " DESC");
    }
    // end

    public static String getDateTime(long milli, String template) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                template, Locale.getDefault());
        Date date = new Date();
        date.setTime(milli);
        return dateFormat.format(date);
    }

    public static String getDateTime(long milli, java.text.DateFormat dateFormat) {
        Date date = new Date();
        date.setTime(milli);
        return dateFormat.format(date);
    }

    public static String getDateTime(Context context, long milli) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(milli);
        String template;
        if (DateFormat.is24HourFormat(context)) {
            template = HOURS_24 + MINUTES;
        } else
            template = "h:mm a";
        return DateFormat.format(template, c).toString();
    }

    public static String getIntensityLevel(Context context, int hrBpm) {
        Cursor cursor = context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
        String intensity = "error";
        if (cursor.moveToFirst()) {
            int age = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_AGE));
            float density = (float) hrBpm / (220 - age);

            if (density >= 0 && density < 0.6) {
                intensity = context.getResources().getStringArray(R.array.exercise_intensity)[0];
            } else if (density >= 0.6 && density < 0.7) {
                intensity = context.getResources().getStringArray(R.array.exercise_intensity)[1];
            } else if (density >= 0.7 && density < 0.8) {
                intensity = context.getResources().getStringArray(R.array.exercise_intensity)[2];
            } else if (density >= 0.8 && density < 0.9) {
                intensity = context.getResources().getStringArray(R.array.exercise_intensity)[3];
            } else if (density >= 0.9) {
                intensity = context.getResources().getStringArray(R.array.exercise_intensity)[4];
            }
        }
        cursor.close();
        return intensity;
    }

    public static String getIntensityLevelText(Context context, int hrBpm) {
        Cursor cursor = context.getContentResolver().query(ProfileTable.TABLE_URI, null, null, null, null);
        String intensity = "error";
        if (cursor.moveToFirst()) {
            int age = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_AGE));
            float density = (float) hrBpm / (220 - age);

            if (density >= 0 && density < 0.6) {
                intensity = context.getResources().getString(R.string.exercise_intensity_info_l1_desc);
            } else if (density >= 0.6 && density < 0.7) {
                intensity = context.getResources().getString(R.string.exercise_intensity_info_l2_desc);
            } else if (density >= 0.7 && density < 0.8) {
                intensity = context.getResources().getString(R.string.exercise_intensity_info_l3_desc);
            } else if (density >= 0.8 && density < 0.9) {
                intensity = context.getResources().getString(R.string.exercise_intensity_info_l4_desc);
            } else if (density >= 0.9) {
                intensity = context.getResources().getString(R.string.exercise_intensity_info_l5_desc);
            }
        }
        cursor.close();
        return intensity;
    }

    public static float getIntensityLevelValue(int age, int hrBpm) {
        float intensity = (float) hrBpm / (220 - age);
        if (intensity < 0) {
            intensity = 0f;
        } else if (intensity > 1) {
            intensity = 1.0f;
        }
        return intensity;
    }

    public static float getWalkCalories(int heightInCM, int stepCounts, int weightInKG) {
        float distance = (float) getWalkDistanceInCM(heightInCM, stepCounts) / 100 / 1000;
        return 0.76f * (float) weightInKG * (float) distance;
    }

    public static int getWalkDistanceInCM(int heightInCM, int stepCounts) {
        return getWalkStepLength(heightInCM) * stepCounts;
    }

    public static int getBikeCalories(float bikeHour, int weightInKG) {
        return (int) (5.5f * (float) weightInKG * (float) bikeHour);
    }

    public static int getWalkStepLength(int heightInCM) {
        return Math.max(heightInCM - 100, 50);
    }

    public static float kgToLbs(float kg) {
        return (kg * 2.20462262f);
    }

    public static float LbsToKg(float lbs) {
        return (lbs * 0.45359237f);
    }

    public static float cmToFt(float cm) {
        return (0.0328083f * cm);
    }

    public static float ftToInch(float ft) {
        return 12 * ft;
    }

    public static float InchToFt(float inch) {
        return inch / 12;
    }

    public static float ftToCm(float ft) {
        return (30.48f * ft);
    }

    public static String getIntensityString(Context context, int stepGoal) {
        String string = context.getResources().getStringArray(R.array.step_intensity_term)[2];
        if (stepGoal >= 0 && stepGoal < 5000) {
            string = context.getResources().getStringArray(R.array.step_intensity_term)[0];
        } else if (stepGoal >= 5000 && stepGoal < 7500) {
            string = context.getResources().getStringArray(R.array.step_intensity_term)[1];
        } else if (stepGoal >= 7500 && stepGoal < 10000) {
            string = context.getResources().getStringArray(R.array.step_intensity_term)[2];
        } else if (stepGoal >= 10000 && stepGoal < 12500) {
            string = context.getResources().getStringArray(R.array.step_intensity_term)[3];
        } else if (stepGoal >= 12500) {
            string = context.getResources().getStringArray(R.array.step_intensity_term)[4];
        }
        return string;
    }

    public static String getIntensityExplainString(Context context, int stepGoal) {

        String string = context.getResources().getStringArray(R.array.step_intensity_term_range)[2];
        if (stepGoal >= 0 && stepGoal < 5000) {
            string = context.getResources().getStringArray(R.array.step_intensity_term_range)[0];
        } else if (stepGoal >= 5000 && stepGoal < 7500) {
            string = context.getResources().getStringArray(R.array.step_intensity_term_range)[1];
        } else if (stepGoal >= 7500 && stepGoal < 10000) {
            string = context.getResources().getStringArray(R.array.step_intensity_term_range)[2];
        } else if (stepGoal >= 10000 && stepGoal < 12500) {
            string = context.getResources().getStringArray(R.array.step_intensity_term_range)[3];
        } else if (stepGoal >= 12500) {
            string = context.getResources().getStringArray(R.array.step_intensity_term_range)[4];
        }

        String explain = String.format(context.getString(R.string.step_counts_intensity_explain), string);

        return explain;
    }

    public static String intToStr(int value) {
        return String.valueOf(value);
    }

    public static String getRelaxLevelString(Context context, int value, int index) {
        int level = getFiveLevel(value);
        Random random = new Random();
        String[] text = null;
        switch (level) {
            case 0:
            case 1:
                text = context.getResources().getStringArray(R.array.relax_level_1_2);
                break;
            case 2:
                text = context.getResources().getStringArray(R.array.relax_level_3);
                break;
            case 3:
            case 4:
                text = context.getResources().getStringArray(R.array.relax_level_4_5);
                break;
        }
        if (index >= text.length) {
            index = 0;
        }
        return text[index];
    }

    public static String getStressLevelString(Context context, int value) {
        int level = getFiveLevel(value);
        Random random = new Random();
        String[] text = null;
        switch (level) {
            case 0:
            case 1:
                text = context.getResources().getStringArray(R.array.stress_level_1_2);
                break;
            case 2:
                text = context.getResources().getStringArray(R.array.stress_level_3);
                break;
            case 3:
            case 4:
                text = context.getResources().getStringArray(R.array.stress_level_4_5);
                break;
        }
        return text[random.nextInt(text.length)];
    }

    public static int getFiveLevel(int value) {
        if (value >= 0 && value < 20) {
            return 0;
        } else if (value >= 20 && value < 40) {
            return 1;
        } else if (value >= 40 && value < 60) {
            return 2;
        } else if (value >= 60 && value < 80) {
            return 3;
        } else if (value >= 80 && value <= 100) {
            return 4;
        }
        return 2;
    }

    public static String getOneDigitFloatString(float f) {
        return String.format("%.1f", f);
    }

    public static int getStressDrawableId(int stress) {
        int[] drawableIds = {R.drawable.asus_wellness_ic_strees_level_1
                , R.drawable.asus_wellness_ic_strees_level_2
                , R.drawable.asus_wellness_ic_strees_level_3
                , R.drawable.asus_wellness_ic_strees_level_4
                , R.drawable.asus_wellness_ic_strees_level_5};
        return drawableIds[getFiveLevel(stress)];
    }

    public static int getRelaxDrawableId(int relax) {
        int[] drawableIds = {R.drawable.asus_wellness_ic_energy_level_5
                , R.drawable.asus_wellness_ic_energy_level_4
                , R.drawable.asus_wellness_ic_energy_level_3
                , R.drawable.asus_wellness_ic_energy_level_2
                , R.drawable.asus_wellness_ic_energy_level_1};
        return drawableIds[getFiveLevel(relax)];
    }

    /*
     *  Return day offset from targetTimeMS to baseTimeMS.
     *  0 : the same date, n : n days after, -n : n days before
     */
    public static int getDateOffset(long targetTimeMS, long baseTimeMS) {
        int offset = 0;

        if (targetTimeMS == baseTimeMS)
            return offset;

        long tmpTimeMS;
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTimeInMillis(targetTimeMS);
        Date targetD = targetCal.getTime();
        Calendar cal = Calendar.getInstance();

        if (targetTimeMS > baseTimeMS) {
            offset = (int) ((targetTimeMS - baseTimeMS) / ONE_DAY_MS);
            while (true) {
                tmpTimeMS = baseTimeMS + (offset * ONE_DAY_MS);
                cal.setTimeInMillis(tmpTimeMS);
                Date dd = cal.getTime();
                if (dd.getYear() == targetD.getYear() && dd.getMonth() == targetD.getMonth()
                        && dd.getDate() == targetD.getDate()) {
                    break;
                }
                offset++;
            }
        } else {
            offset = (int) ((baseTimeMS - targetTimeMS) / ONE_DAY_MS);
            while (true) {
                tmpTimeMS = baseTimeMS - (offset * ONE_DAY_MS);
                cal.setTimeInMillis(tmpTimeMS);
                Date dd = cal.getTime();
                if (dd.getYear() == targetD.getYear() && dd.getMonth() == targetD.getMonth()
                        && dd.getDate() == targetD.getDate()) {
                    break;
                }
                offset++;
            }
            offset *= -1;
        }

        return offset;
    }

    public static boolean detectOverlap(long aStartTime, long aEndTime, long bStartTime, long bEndTime) {
        if (aStartTime >= bEndTime || aEndTime <= bStartTime) {
            return false;
        } else {
            return true;
        }
    }

    public static Message generateMessage(Object obj, int arg1, int arg2, int what) {
        Message msg = Message.obtain();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        msg.what = what;
        return msg;
    }

    public static Message generateEmptyMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        return msg;
    }

    public static void updateLastUpdateTime(Context context, long time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putLong(context.getString(R.string.pref_key_last_updated), time);
        editor.commit();

        //let wellness know to update drawer data.
        Intent intent = new Intent(MainWellness.ACTION_UPDATE_TIME);
        context.sendBroadcast(intent);
    }

    public static String getLastUpdateTime(Context context, long defaultTime, java.text.DateFormat dateFormat) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (DateFormat.is24HourFormat(context)) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(sp.getLong(context.getString(R.string.pref_key_last_updated), defaultTime));
            return getDateTime(sp.getLong(context.getString(R.string.pref_key_last_updated), defaultTime), dateFormat) +
                    DateFormat.format(" kk:mm", c);
        } else
            return getDateTime(sp.getLong(context.getString(R.string.pref_key_last_updated), defaultTime), dateFormat) +
                    getDateTime(sp.getLong(context.getString(R.string.pref_key_last_updated), defaultTime), " a h:mm");
    }

    public static boolean isPadDevice(Context context) {
        int sdimens = context.getResources().getConfiguration().smallestScreenWidthDp;
        if (sdimens >= 800) {
            return true;
        }
        return false;
    }

    // maybe change
    public static boolean isPadLayout(Context context) {
        return (isPadDevice(context));// && getSizeDimens(context) != SIZEDIMENS.SW720DP);
    }

    public enum SIZEDIMENS {
        SW320DP, SW360DP, SW600DP, SW720DP, SW800DP, SW1080DP
    }

    public static SIZEDIMENS getSizeDimens(Context context) {
        int curDimens = context.getResources().getConfiguration().smallestScreenWidthDp;
        if (curDimens >= 320 && curDimens < 360) {
            return SIZEDIMENS.SW320DP;
        } else if (curDimens >= 360 && curDimens < 600) {
            return SIZEDIMENS.SW360DP;
        } else if (curDimens >= 600 && curDimens < 720) {
            return SIZEDIMENS.SW600DP;
        } else if (curDimens >= 720 && curDimens < 800) {
            return SIZEDIMENS.SW720DP;
        } else if (curDimens >= 800 && curDimens < 1080) {
            return SIZEDIMENS.SW800DP;
        } else if (curDimens >= 1080) {
            return SIZEDIMENS.SW1080DP;
        }
        return SIZEDIMENS.SW320DP;
    }

    public static String commaNumber(long value) {
        DecimalFormat formatter = new DecimalFormat("#,###,###.#");
        String formattedString = formatter.format(value);
        return formattedString;
    }

    public static String formatNumber(long value) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String formattedString = formatter.format(value);
        return formattedString;
    }


    public static String formatDistance(double value, int unit){
        double result=0;
        if(unit == 0){ //km
            result = Arith.div(value, 1000.0f, 3);
        }
        else{   //mile
            result = Arith.div(value, 1609.344f, 3);
        }
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        String formattedString = formatter.format(result);
        return formattedString;
    }

    public static int removeCommaNumber(String text) {
        int num = -1;
        try {
            Number removeCommaNumber = NumberFormat.getNumberInstance(java.util.Locale.US).parse(text);
            num = removeCommaNumber.intValue();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return num;
    }

    public static Bitmap calculateBitmap(String path) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        o.inSampleSize = calculateInSampleSize(o, 150, 150);
        o.inJustDecodeBounds = false;
        Bitmap resizeBitmap = BitmapFactory.decodeFile(path, o);
        return resizeBitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static boolean getRemindMeMoveChecbox() {
        Log.i("Utilty", "mIsCheckded get: " + mIsCheckded);
        return mIsCheckded;
    }

    public static void setRemindMeMoveChecbox(boolean isChecked) {
        mIsCheckded = isChecked;
        Log.i("Utilty", "mIsCheckded set: " + mIsCheckded);
    }

    public static String inputStream2String(InputStream is) {
        Scanner scaner = new Scanner(is);
        try
        {
            String text = scaner.next();
            return text;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

//       Log.i("smile","scan: "+text);
//        BufferedReader in = new BufferedReader(new InputStreamReader(is));
//        StringBuffer buffer = new StringBuffer();
//        String line = "";
//        try {
//            while ((line = in.readLine()) != null){
//                buffer.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            try {
//                in.close();
//             //   is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//       Log.i("smile","buffer.toString(): "+buffer.toString());
        return  null;
    }


    public static final int AGE_DEFAULT = 30;
    public static int calcAge(Long birthday) {
        if(birthday == null || birthday == 0) {
            return AGE_DEFAULT;
        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(birthday);
            Calendar calnow = Calendar.getInstance();
            calnow.setTimeInMillis(System.currentTimeMillis());
            int age = calnow.getTime().getYear() - cal.getTime().getYear();
            Log.i("smile", "calcAge: " + age);
            return age;
        }
    }


    public static Bitmap getPhotoBitmap(Context context, String path) {
        Bitmap bmp = null;
        if (path.startsWith("file")) {
            Uri url = Uri.parse(path);

            bmp = calculateBitmap(url.getPath());
        } else {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(Uri.parse(path), null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                    bmp = calculateBitmap(cursor.getString(columnIndex));
                }

            } catch (Exception e) {
                e.printStackTrace();
                bmp = null;
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        return bmp;
    }

    public static String getCurrTime() {
        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String nowTime = "";
        nowTime = df.format(dt);
        return nowTime;
    }

    public static Uri getOutImageUri() {
        // Store image in dcim
        File picfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String filename = System.currentTimeMillis() + ".jpg";
        File file = new File(picfile.getAbsolutePath(), filename);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            parentFile.mkdirs();
        }
        Uri uriMyImage = Uri.fromFile(file);
        SetupProfileActivity.takePhotoUri = uriMyImage;
        return uriMyImage;
    }

    public static void printDp(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;
        int height = metric.heightPixels;
        float density = metric.density;
        int densityDpi = metric.densityDpi;
        int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;
        Log.i("emily", "w = " + width + ", h = " + height + ", density = " + density + ", densityDpi = " + densityDpi + ", smallestScreenWidthDp  = " + smallestScreenWidthDp);

    }

    public static int getScreenWidth(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;
        return width;
    }

    public static int getScreenHeight(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.heightPixels;
        return width;
    }

    public static final int oneHour = 60;

    // public static final int oneMin = 60;
    /* format: 6hr32min */
    public static String formatSleepTimes(long durationTime, Context context) {
        String hourstr = context.getString(R.string.detail_sleep_shorthour);
        String minstr = context.getString(R.string.detail_sleep_shortmin);
        String time = "";
        if (durationTime > 0) {
            int hour = (int) durationTime / oneHour;
            int min = (int) (durationTime - hour * oneHour);
            time = hour <= 0 ? "" : (String.valueOf(hour) + hourstr);
            time += (min <= 0 ? "" : (String.valueOf(min)) + minstr);
        } else {
            time = "0" + minstr;
        }
        return time;
    }

    ///for goolge new phot app

    public static boolean isNewGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static long getMidnightMilles(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.add(Calendar.HOUR_OF_DAY, -cal.get(Calendar.HOUR_OF_DAY));
        return getHourMilles(cal.getTimeInMillis());
    }

    private static long getHourMilles(long now) {
        long ONE_HOUR_MILLIES = 1 * 60 * 60 * 1000;
        long hour = now / ONE_HOUR_MILLIES * ONE_HOUR_MILLIES;
        return hour;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void fitFontSizeForView(TextView txtView, int fontsize, int maxwidth) {
        if (txtView == null) {
            return;
        }
        float size = txtView.getTextSize();
        if (size < fontsize) {
            size = fontsize;
        }
        int measureWidth = txtView.getWidth();
        if (measureWidth <= 0) {
            txtView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            measureWidth = txtView.getMeasuredWidth();
        }

        Log.i("smile", "maxwidth: " + maxwidth + " measureWidth: " + measureWidth + " fontsize " + fontsize);
        if (measureWidth >= maxwidth) {
            size = size * maxwidth / measureWidth;
            size += 1;
            txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            txtView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            measureWidth = txtView.getMeasuredWidth();
        }
        Log.i("smile", "maxwidth: " + maxwidth + " measureWidth: " + measureWidth + " fontsize " + fontsize);
        while (measureWidth >= maxwidth) {
            size = size - 1;
            txtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            txtView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            measureWidth = txtView.getMeasuredWidth();
            Log.i("smile", "size: " + size);
        }
    }

    public static void adjustTextSizePx(final TextView tv, final float textSizePx, final float margin) {
        Activity context = (Activity) tv.getContext();
        int tv_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int tv_height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv.measure(tv_width, tv_height);

        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);

        int width = Math.min(tv.getMeasuredWidth(), (metric.widthPixels - dip2px(context, margin)));

        Paint paint = new Paint();
        paint.setTextSize(textSizePx);
        String text = tv.getText().toString();
        final float stepsWidth = paint.measureText(text);
        float ratio = 1.0f;
        if (width < stepsWidth) {
            ratio = width / stepsWidth;
        }
        //	Log.i("smile","adjustTextSizePx "+textSizePx+" ratio:"+ratio+" tvwidth: "+tv.getMeasuredWidth()+" width "+width);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx * ratio);
    }

    public static void adjustTextSize(final TextView tv, final float origSizeDp, final float margin) {
        Activity context = (Activity) tv.getContext();
        int textSizePx = dip2px(context, origSizeDp);
        adjustTextSizePx(tv, textSizePx, margin);
    }


    public static void adjustTextSize2(final TextView tv, final float origSize, final float maxWidth) {
        Activity context = (Activity) tv.getContext();
        int textSizePx = dip2px(context, origSize);

        int tv_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int tv_height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv.measure(tv_width, tv_height);

        int width = Math.min(tv.getMeasuredWidth(), dip2px(context, maxWidth));
        Paint paint = new Paint();
        paint.setTextSize(textSizePx);
        String text = tv.getText().toString();
        final float stepsWidth = paint.measureText(text);
        float ratio = 1.0f;
        if (width < stepsWidth) {
            ratio = width / stepsWidth;
        }

        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx * ratio);
    }

   /* public static void setSleepScore(TextView tv, int score, Context context) {
        if (context.getString(R.string.text_score).equals("分")) {
            tv.setText(context.getString(R.string.text_sleep_quality_score) + score + context.getString(R.string.text_score));
        } else {
            tv.setText(context.getString(R.string.text_sleep_quality_score) + " " + context.getString(R.string.text_score) + " " + score);
        }
    }*/

  /*  public static void setSleepScoreSleepActivity(TextView tv, int score, Context context) {
        String scoreStr = String.valueOf(score);
        String scoreUnit = context.getString(R.string.text_score);
        String title = context.getString(R.string.text_sleep_quality_score);
        //#ff188560
        int color = context.getResources().getColor(R.color.sleep_detail_quality_socre_color);
        if (scoreUnit.equals("分")) {

            SpannableString ss = new SpannableString(title + scoreStr + scoreUnit);
            ForegroundColorSpan fcs = new ForegroundColorSpan(color);
            ss.setSpan(fcs, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(fcs, title.length(), scoreStr.length() + title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);

        } else {
            title += " ";
            scoreUnit += " ";
            SpannableString ss = new SpannableString(title + scoreUnit + scoreStr);
            ForegroundColorSpan fcs = new ForegroundColorSpan(color);
            ss.setSpan(fcs, title.length() + scoreUnit.length(), title.length() + scoreUnit.length() + scoreStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //    ss.setSpan(fcs, title.length(), scoreStr.length() + title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            //tv.setText(context.getString(R.string.text_sleep_quality_score)  + " " +context.getString(R.string.text_score) + " " + score);
        }
    }*/

    public static int pxTodip(Context context, float width) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (width / scale + 0.5);
    }

    public static int calcTextViewWidth(Context context, String str1, String str2, float origSize) {
        int textSizePx = dip2px(context, origSize);
        Paint paint = new Paint();
        paint.setTextSize(textSizePx);
        int width = pxTodip(context, str1.length() > str2.length() ? paint.measureText(str1) : paint.measureText(str2));
        return width;
    }

    public static String getBitmapDataFromBundle(Bundle bd, Activity context) {
        if (bd != null) {
            File picfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            String filename = System.currentTimeMillis() + ".jpg";
            File photofile = new File(picfile.getAbsolutePath(), filename);
            Bitmap photo = bd.getParcelable("data");
            try {
                FileOutputStream outputStream = new FileOutputStream(photofile);
                photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, filename, null);
                if (!TextUtils.isEmpty(url)) {
//                    Log.i("smile", "url:  " + url);
//                    //String  mSelectedPhotoUri = url;
//                    Intent intent = new Intent("com.android.camera.action.CROP");
//                    intent.setDataAndType(Uri.parse(url), "image/*");
//                    context.startActivityForResult(intent, SetupProfileActivity.REQUEST_CODE_CROP_IMAGE);
                    return url;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public static TimeSpanItem getTimeSpan(long timespan)
    {
        int l =  (int)timespan;
        int day=l/(24*60);
        int hour=(l/60-day*24);
        int min=(l-day*24*60-hour*60);
        //  long s=(l/1000-day*24*60*60-hour*60*60-min*60);
        TimeSpanItem ts = new TimeSpanItem(hour,min);
        return  ts;
    }

    public static SpannableString getHourMinStr(Context context, TimeSpanItem item, boolean shortUnit)
    {
        String hourstr = " "+context.getString(R.string.daily_info_time_hours)+" ";
        String minstr = " "+context.getString(R.string.time_unit);
        if(shortUnit){
            hourstr =  " "+context.getString(R.string.detail_sleep_shorthour)+" ";
            minstr = " "+context.getString(R.string.detail_sleep_shortmin);
        }
        String ret="";
        if(item.getHour()>0)
        {
            ret+=item.getHour()+hourstr;
        }

        ret+=item.getMinute()+minstr;
        SpannableString ss =new SpannableString(ret);
        int start = 0;
        int end =0;
        if(item.getHour()>0)
        {
            end = String.valueOf(item.getHour()).length();
            ss.setSpan(new TextAppearanceSpan(context,R.style.timestring_big),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start =  end + hourstr.length();
        }
        end = start + String.valueOf(item.getMinute()).length();
        ss.setSpan(new TextAppearanceSpan(context,R.style.timestring_big),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    public static String getHourMinStr_normal(Context context, TimeSpanItem item)
    {
        String hourstr = " "+context.getString(R.string.daily_info_time_hours)+" ";
        String minstr = " "+context.getString(R.string.time_unit);
        String ret="";
        if(item.getHour()>0)
        {
            ret+=item.getHour()+hourstr;
        }
        ret+=item.getMinute()+minstr;

        return ret;
    }

    public static String getHourMinStr_short_unit(Context context, TimeSpanItem item)
    {
        String hourstr = " "+context.getString(R.string.detail_sleep_shorthour)+" ";
        String minstr = " "+context.getString(R.string.detail_sleep_shortmin);
        String ret="";
        if(item.getHour()>0)
        {
            ret+=item.getHour()+hourstr;
        }
        ret+=item.getMinute()+minstr;

        return ret;
    }


    public static SpannableString getFormatStr(String str, String unit, Context context){
        int start = 0;
        int end = str.length();
        SpannableString ss =new SpannableString(str+" "+unit);
        ss.setSpan(new TextAppearanceSpan(context,R.style.timestring_big),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /*public static String getHourMinStr(Context context, TimeSpanItem sts)
    {
        String hourstr = context.getString(R.string.detail_sleep_shorthour);
        String minstr = context.getString(R.string.detail_sleep_shortmin);
        String ret="";
        if(sts.getHour()>0)
        {
            if(sts.getMinute()>0){
                ret+=sts.getHour()+hourstr;
                ret+=" "+sts.getMinute()+minstr;
            }
            else{
                ret+=sts.getHour()+" "+hourstr;
            }
        }
        else {
            ret += sts.getMinute() + " "+ minstr;
        }
        return ret;
    }*/
   public static void pickImage(Activity context)
    {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        try {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivityForResult(intent, SetupProfileActivity.REQUEST_CODE_PICK_IMAGE);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void takePhoto(Activity context)
    {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,Utility.getOutImageUri());
        try {
            context.startActivityForResult(intent, SetupProfileActivity.REQUEST_CODE_TAKE_PHOTO);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static float getScaleRatio(Context context){
        float ratio = 1.0f;
        SIZEDIMENS dimens = Utility.getSizeDimens(context);
        switch(dimens) {
            case SW320DP:
                ratio = 1; // 0.9f;
                break;
            case SW360DP:
                ratio = 1;
                break;
            case SW600DP:
                ratio = 1.34f; // 1.234f;
                break;
            case SW720DP:
                ratio = 1.34f;
                break;
            default:
                ratio = 1;
        }
        return  ratio;
    }
    public static void trackerScreennView(Context context, String screenName){
        // Get tracker
        Tracker t = GAApplication.getInstance().getTracker(context);
        // Set screen name.
        t.setScreenName(screenName);
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void openSettingAppInfo(String packagename,Context context)
    {
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + packagename)); //getPackageName()
        context.startActivity(i);
    }


}

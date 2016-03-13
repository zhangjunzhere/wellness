package com.asus.ecg;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EcgLogHelper {
    private final static String TAG = "EcgLogHelper";

    private static String mFileName = "EcgLog.txt";
    private static File mOutFile = null;
    private static PrintWriter mPrintWriter = null;

    public static void prepareLogFile() {
        Log.d(TAG, "prepareLogFile()");
        mOutFile = null;
        mPrintWriter = null;
        if (isExternalStorageWritable()) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dir.mkdirs() || dir.isDirectory()) {
                try {
                    mOutFile = new File(dir, mFileName);
                    Log.d(TAG, "Start log : " + mOutFile.getCanonicalPath() + " (" + (mOutFile.length() / 1024f) + " KB)");
                    mPrintWriter = new PrintWriter(new FileWriter(mOutFile, true));
                } catch (Exception e) {
                    mPrintWriter = null;
                    mOutFile = null;
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    public static void releaseLogFile() {
        Log.d(TAG, "releaseLogFile()");
        if(mPrintWriter != null) {
            mPrintWriter.flush();
            mPrintWriter.close();
            mPrintWriter = null;
        }
        if(mOutFile != null) {
            Log.d(TAG, "End log : " + (mOutFile.length() / 1024f) + " KB");
            mOutFile = null;
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getDateTime(long milli, String template) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(template, Locale.getDefault());
        Date date = new Date();
        date.setTime(milli);
        return dateFormat.format(date);
    }

    public static boolean recordStringData(String data) {
        //Log.d(TAG, "recordStringData() : " + data);
        //if(mPrintWriter == null) {
        //    prepareLogFile();
        //}
        if(mPrintWriter == null) return false;
        //String timestamp = getDateTime(System.currentTimeMillis(), "MM/dd/HH:mm:ss");
        //String dataToSave = timestamp + " " + data;
        //String dataToSave = "time=" + System.currentTimeMillis() + " " + data;
        String dataToSave = data + "";
        Log.d(TAG, dataToSave);
        try {
            mPrintWriter.println(dataToSave);
            mPrintWriter.flush();
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }
}

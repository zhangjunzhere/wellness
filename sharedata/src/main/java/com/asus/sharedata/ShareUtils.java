package com.asus.sharedata;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
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
import java.io.OutputStream;
import java.util.Calendar;

/**
 * Created by smile_gao on 2015/9/8.
 */
public class ShareUtils {
    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        closeQuietly(stream);
        return byteArray;
    }
    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            Log.e("Utilty", "IOException while closing closeable.", e);
        }
    }

    public static long getMidnightMilles(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.add(Calendar.HOUR_OF_DAY, -cal.get(Calendar.HOUR_OF_DAY));
        return getHourMilles(cal.getTimeInMillis());
    }

    public static long getHourMilles(long now) {
        long ONE_HOUR_MILLIES = 1 * 60 * 60 * 1000;
        long hour = now / ONE_HOUR_MILLIES * ONE_HOUR_MILLIES;
        return hour;
    }

    public static File getBakcupDbFile(String filename,boolean clean, String version)
    {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//+"/wellness/";
        if(!file.exists())
        {
            boolean ismk = file.mkdir();
            Log.i("smile","download ismk: "+ismk);
        }
        else if(file.isFile())
        {
            file.delete();
            boolean ismk = file.mkdir();
            Log.i("smile","download 1 ismk: "+ismk);
        }
        if(!file.canWrite())
        {
            return  null;
        }
        String path = file.getAbsolutePath()+"//wellness//";
        File f = new File(path);
        if(f.exists()){
            if(f.isFile())
            {
                f.delete();
                boolean ismk = f.mkdir();
                Log.i("smile","ismk: "+ismk);
            }
            if(clean) {
                File[] files = f.listFiles();
                for (File temp : files) {
                    temp.delete();
                }
            }

        }
        else
        {
            boolean ismk = f.mkdir();
            Log.i("smile","ismk: "+ismk);
        }

        String name = "";
        if (!version.equals("")){
            name = "wellness_" + version+".db";
        }else{
            name = "wellness.db";
        }

        filename = filename == null? name: filename;
        path = path+filename;
        f = new File(path);
        if(f.exists())
        {
            f.delete();

        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        return getStringFromFile(fl);
    }
    public static String getStringFromFile(File fl) throws  Exception
    {
        FileInputStream fin = new FileInputStream(fl);

        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
    public static byte[] getBytesByFile(File file)
    {

        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }
    public static void  writeStreamToFile(InputStream input, File file)
    {
        try {
        OutputStream output = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            } finally {
                output.close();
                input.close();
            }

         } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String getVersionCode(Context context)
    {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return String.valueOf(pi.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}

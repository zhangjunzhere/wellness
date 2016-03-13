package com.asus.wellness.sleep;

import android.content.Intent;
import android.hardware.SensorEvent;
import android.hardware.TriggerEvent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.asus.wellness.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorEventLogger extends DataWriter {

    protected static final String DATA_LOG_PREFIX = "wellness_sleep_accelerometer";
    /**
     * Extension for data log files
     */
    protected static final String DATA_LOG_EXT = ".csv";
    /**
     * Delimiter used to separate values within the data log
     */
    protected static final String DATA_LOG_DELIMITER = ",";

    private static StringBuilder sStringBuilder = new StringBuilder();


    private String mDelimiter;

    private static SensorEventLogger sensorEventLogger;
    public static SensorEventLogger getInstance() {
        if(sensorEventLogger == null) {
            sensorEventLogger = new SensorEventLogger();
        }
        return sensorEventLogger;
    }

    private SensorEventLogger() {
        super(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        String delimiter = DATA_LOG_DELIMITER;
        mDelimiter = delimiter;
    }

    public void setHeader(String header) throws IOException{
        write(header);
    }

    public void log(SensorEvent event)  {
        try {
            log(event.timestamp, event.sensor.getType(), event.accuracy, event.values);
        }catch (Exception e){
            Log.e(Utility.TAG, e.toString());
        }
    }

    public void log(TriggerEvent event) throws IOException {
        log(event.timestamp, event.sensor.getType(), 0, event.values);
    }
    
    public void log(long ts, int sensorType, int accuracy, float[] values) throws IOException {
        sStringBuilder.setLength(0);
        // System timestamp
        SimpleDateFormat dateF = new SimpleDateFormat("MMdd-HH:mm:ss");

        sStringBuilder.append(dateF.format(System.currentTimeMillis()));

        // timestamp
        sStringBuilder.append(mDelimiter);
        sStringBuilder.append(String.valueOf(ts));

        // sensor type
        sStringBuilder.append(mDelimiter);
        sStringBuilder.append(sensorType);

        // accuracy
        sStringBuilder.append(mDelimiter);
        sStringBuilder.append(accuracy);

        // sensor values
        for (int i = 0; i < values.length; i++) {
            sStringBuilder.append(mDelimiter);
            sStringBuilder.append(values[i]);
        }
        writeln(sStringBuilder.toString());
    }

    
    public File getFile() {
        return mFile;
    }

    public void enableLogging() {
        try {
            SimpleDateFormat dateF = new SimpleDateFormat("MMdd-HHmmss");
            Date date = new Date();
            final String filename = DATA_LOG_PREFIX + String.valueOf(dateF.format(date)) + DATA_LOG_EXT;
            Log.d(TAG, filename);
            // open logger
            open(filename, false);
            String header = "System.currentTimeMillis, event.timestamp, type, accuracy, values \n";

            setHeader(header);

        } catch (IOException e) {
            Log.e(TAG, "Error enabling logging", e);
        }
    }


    /**
     * Disables the data logger, closes the file and notifies the system that a
     * new file is available
     */
    public void disableLogging() {
        try {
            // close logger
            close();
        } catch (IOException e) {
            Log.e(TAG, "Error disabling logging", e);
        }
    }

}

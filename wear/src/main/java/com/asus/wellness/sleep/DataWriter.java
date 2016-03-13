package com.asus.wellness.sleep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataWriter {
    
    protected static final String TAG = "DataWriter";

    protected BufferedWriter mBufferedWriter;
    private File mDir;
    private String mFilename;
    protected File mFile;
    private boolean mAppend = false;
    private boolean mIsOpen = false;
    

    public DataWriter(File dir) {
        mDir = dir;
    }

    public void open(String filename, boolean append) throws IOException {
        mAppend = append;
        
        if (mIsOpen) {
            close();
        }
        
        File file = new File(mDir, filename);

        // create file, directories if needed
        try {
            setupFile(file);
        } catch (IOException e) {
            throw new IOException("Error opening writer", e);
        }

        // setup writer
        if (mBufferedWriter == null) {
            try {
                setupWriter(file);
            } catch (IOException e) {
                throw new IOException("Error opening writer", e);
            }
        }
        mFile = file;
        mFilename = filename;
        mIsOpen = true;
    }

    public void write(String data) throws IOException {
        if (mBufferedWriter == null) {
            throw new IOException("Datawriter not open for " + mFilename);
        }

        // write data
        try {
            mBufferedWriter.write(data);
            mBufferedWriter.flush();
        } catch (IOException e) {
            throw new IOException("Error writing to " + mFilename, e);
        }
    }

    public void writeln(String data) throws IOException {
        try {
            write(data);
            mBufferedWriter.newLine();
        } catch (IOException e) {
            throw new IOException("Error writing to " + mFilename + ": " + data, e);
        }
    }
    
    public void writeln() throws IOException {
        try {
            mBufferedWriter.newLine();
        } catch (IOException e) {
            throw new IOException("Error writing to " + mFilename, e);
        }
    }

    public void close() throws IOException {
        //Log.d("DataWriter", "close() mFilename: " + mFilename + ", mIsOpen: " + String.valueOf(mIsOpen));
        if ((mBufferedWriter != null) && (mIsOpen)) {
            try {
                mBufferedWriter.flush();
                mBufferedWriter.close();
                mBufferedWriter = null;
            } catch (IOException e) {
                throw new IOException("Error closing " + mFilename, e);
            }
        }
        mIsOpen = false;

    }

    protected boolean setupFile(File file) throws IOException {
        // create dir
        if (!mDir.isDirectory()) {
            if (!mDir.mkdirs()) {
                throw new IOException("Error creating directory: " + mDir);
            }
        }

        // create file if it doesn't exist
        boolean newFileCreated = false;
        if (!file.exists()) {
            try {
                newFileCreated = file.createNewFile();
            } catch (IOException e) {
                throw new IOException("Error creating file: " + file, e);
            }
        }
        return newFileCreated;
    }

    protected void setupWriter(File file) throws IOException {
        try {
            FileWriter fw = new FileWriter(file, mAppend);
            mBufferedWriter = new BufferedWriter(fw);
        } catch (IOException e) {
            throw new IOException("Error creating writer: " + file, e);
        }
    }
    
}

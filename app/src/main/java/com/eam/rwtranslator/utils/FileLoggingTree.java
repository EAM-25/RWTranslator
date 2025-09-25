package com.eam.rwtranslator.utils;
import android.os.Environment;
import android.util.Log;

import com.eam.rwtranslator.AppConfig;

import com.eam.rwtranslator.BuildConfig;
import com.eam.rwtranslator.utils.FilesHandler;
import timber.log.Timber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileLoggingTree extends Timber.Tree {
    private static final String TAG = "FileLoggingTree";
    private File log_file ;
    public  FileLoggingTree(File log_file) {
    	this.log_file=log_file;
    }
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        try {
            String effectiveTag = (tag != null) ? tag : getCallerClassName();
            String logMessage = createLogMessage(priority, effectiveTag, message);
            writeLogToFile(logMessage);
        } catch (IOException e) {
            Log.e(TAG, "Error writing log to file", e);
        }
    }

    private String createLogMessage(int priority, String tag, String message) {
        String logLevel;
        switch (priority) {
            case Log.VERBOSE:
                logLevel = "VERBOSE";
                break;
            case Log.DEBUG:
                logLevel = "DEBUG";
                break;
            case Log.INFO:
                logLevel = "INFO";
                break;
            case Log.WARN:
                logLevel = "WARN";
                break;
            case Log.ERROR:
                logLevel = "ERROR";
                break;
            default:
                logLevel = "UNKNOWN";
                break;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());
        return String.format(Locale.getDefault(), "[%s] %s/%s: %s\n", timestamp, logLevel, tag, message);
    }

    private void writeLogToFile(String logMessage) throws IOException {
        File logFile = log_file;
        FileOutputStream outputStream = new FileOutputStream(logFile, true);
        outputStream.write(logMessage.getBytes());
        outputStream.close();
    }
    private String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final int index = 8; // Adjust this index as needed
        if (stackTrace.length > index) {
            return stackTrace[index].getClassName();
        }
        return "Unknown";
    }
}

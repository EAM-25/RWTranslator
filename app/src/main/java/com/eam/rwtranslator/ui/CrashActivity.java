package com.eam.rwtranslator.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.eam.rwtranslator.R;
import java.io.File;

public class CrashActivity extends AppCompatActivity {
    
    public static final String EXTRA_CRASH_INFO = "crash_info";
    public static final String EXTRA_LOG_FILE_PATH = "log_file_path";
    
    private TextView crashInfoTextView;
    private Button shareLogButton;
    private Button restartAppButton;
    
    private String crashInfo;
    private String logFilePath;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        
        initViews();
        initData();
        setupClickListeners();

    }
    
    private void initViews() {
        crashInfoTextView = findViewById(R.id.tv_crash_info);
        shareLogButton = findViewById(R.id.btn_share_log);
        restartAppButton = findViewById(R.id.btn_restart_app);
    }
    
    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            crashInfo = intent.getStringExtra(EXTRA_CRASH_INFO);
            logFilePath = intent.getStringExtra(EXTRA_LOG_FILE_PATH);
            
            if (crashInfo != null) {
                crashInfoTextView.setText(crashInfo);
            }
        }
    }
    
    private void setupClickListeners() {
        shareLogButton.setOnClickListener(v -> shareLogFile());
        
        restartAppButton.setOnClickListener(v -> restartApp());
    }
    
    private void shareLogFile() {
        if (logFilePath != null && new File(logFilePath).exists()) {
            try {
                File logFile = new File(logFilePath);
                Uri fileUri = FileProvider.getUriForFile(this, 
                    getPackageName() + ".fileprovider", logFile);
                
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "App Crash Log");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "App crash log file");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                startActivity(Intent.createChooser(shareIntent, "Share crash log"));
            } catch (Exception e) {
                // Fallback to sharing crash info as text
                shareCrashInfoAsText();
            }
        } else {
            shareCrashInfoAsText();
        }
    }
    
    private void shareCrashInfoAsText() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "App Crash Information");
        shareIntent.putExtra(Intent.EXTRA_TEXT, crashInfo != null ? crashInfo : "No crash info available");
        
        startActivity(Intent.createChooser(shareIntent, "Share crash info"));
    }
    
    private void restartApp() {
        // Get the main activity class name from package manager
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
        }
        
        // Kill current process
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}

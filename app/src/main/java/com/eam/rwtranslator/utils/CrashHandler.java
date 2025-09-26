package com.eam.rwtranslator.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.Looper;
import android.view.Gravity;
import com.eam.rwtranslator.AppConfig;
import com.eam.rwtranslator.utils.MyActivityLifecycleCallbacks;
import java.lang.reflect.Field;
import android.os.Build;
import java.util.Date;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import android.widget.Toast;
import android.app.Application;
import java.util.Map;
import java.text.DateFormat;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import android.util.Log;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import timber.log.Timber;
import com.eam.rwtranslator.ui.CrashActivity;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
  // 自定义Toast
  private static Toast mCustomToast;
  // 提示文字
  private static String mCrashTip = "Sorry, the program encountered an exception and is about to exit";
  // 系统默认的UncaughtException处理类
  private Thread.UncaughtExceptionHandler mDefaultHandler;
  // CrashHandler实例
  private static CrashHandler mCrashHandler;
  // 程序的App对象
  public Application mApplication;
  // 生命周期监听
  MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
  // 用来存储设备信息和异常信息
  private final Map<String, String> infos = new HashMap<>();
  // 用于格式化日期,作为日志文件名的一部分
  @SuppressLint("SimpleDateFormat")
  private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  // 是否是保存到文件
  private boolean isSaveFile;
  // 是否重启APP
  private boolean mIsRestartApp;
  // 重启APP时间
  private long mRestartTime;
  // 重启后的第一个Activity class文件
  private Class mClassOfFirstActivity;
  // 是否已经toast
  private boolean hasToast;
  // 崩溃日志文件路径
  private String crashLogFilePath;

  /** 私有构造函数 */
  private CrashHandler() {}

  /**
   * 获取CrashHandler实例 ,单例模式
   *
   * @return
   * @since V1.0
   */
  public static CrashHandler getInstance() {
    if (mCrashHandler == null) mCrashHandler = new CrashHandler();
    return mCrashHandler;
  }

  public static void setCloseAnimation(int closeAnimation) {
    MyActivityLifecycleCallbacks.sAnimationId = closeAnimation;
  }

  public static void setCustomToast(Toast customToast) {
    mCustomToast = customToast;
  }

  public static void setCrashTip(String crashTip) {
    mCrashTip = crashTip;
  }

  public void init(
      Application application,
      boolean isDebug,
      boolean isRestartApp,
      long restartTime,
      Class classOfFirstActivity) {
    mIsRestartApp = isRestartApp;
    mRestartTime = restartTime;
    mClassOfFirstActivity = classOfFirstActivity;
    initCrashHandler(application, isDebug);
  }

  public void init(Application application, boolean isDebug) {
    initCrashHandler(application, isDebug);
  }

  /**
   * 初始化
   *
   * @since V1.0
   */
  private void initCrashHandler(Application application, boolean isDebug) {
    isSaveFile = isDebug;
    mApplication = application;
    mApplication.registerActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks);
    // 设置该CrashHandler为程序的默认处理器
    Thread.setDefaultUncaughtExceptionHandler(this);
    // 获取系统默认的UncaughtException处理器
    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
  }

  /** 当UncaughtException发生时会转入该函数来处理 */
  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    boolean isHandle = handleException(ex);
    if (!isHandle && mDefaultHandler != null) {
      // 如果我们没有处理则让系统默认的异常处理器来处理
      mDefaultHandler.uncaughtException(thread, ex);
    } else {
      try {
        // 启动CrashActivity显示崩溃信息
        startCrashActivity(ex);
      } catch (Exception e) {
        Timber.e(e,"Failed to start CrashActivity: %s " , e.getMessage());
        // 如果启动CrashActivity失败，则使用原来的退出逻辑
      }
    }
  }

  /**
   * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
   *
   * @param ex
   * @return true:如果处理了该异常信息;否则返回false.
   */
  private boolean handleException(Throwable ex) {
    if (ex == null) {
      return false;
    }

    if (isSaveFile) {
      // 收集设备参数信息
      collectDeviceInfo();
      // 保存日志文件
      crashLogFilePath = saveCatchInfo2File(ex);
    }

    return true;
  }

  /**
   * 收集设备参数信息
   *
   * @since V1.0
   */
  public void collectDeviceInfo() {
    try {
      PackageManager pm = mApplication.getPackageManager();
      PackageInfo pi =
          pm.getPackageInfo(mApplication.getPackageName(), PackageManager.GET_ACTIVITIES);
      if (pi != null) {
        String versionName = pi.versionName == null ? "null" : pi.versionName;
        String versionCode = pi.versionCode + "";
        infos.put("versionName", versionName);
        infos.put("versionCode", versionCode);
      }
    } catch (PackageManager.NameNotFoundException e) {
      Timber.e(
          "%s",
          "collectDeviceInfo() an error occured when collect package info NameNotFoundException:");
    }
    Field[] fields = Build.class.getDeclaredFields();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        infos.put(field.getName(), field.get(null).toString());
        Log.i("%s", field.getName() + " : " + field.get(null));
      } catch (Exception e) {
        Timber.e("%s", "collectDeviceInfo() an error occured when collect crash info Exception:");
      }
    }
  }

  /**
   * 保存错误信息到文件中
   *
   * @param ex
   * @return 文件名称
   */
  private String saveCatchInfo2File(Throwable ex) {
    StringBuffer sb = new StringBuffer();
    sb.append("------------------------start------------------------------\n");
    for (Map.Entry<String, String> entry : infos.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      sb.append(key + "=" + value + "\n");
    }
    sb.append(getCrashInfo(ex));
    sb.append("\n------------------------end------------------------------");
    try {
      long timestamp = System.currentTimeMillis();
      String time = formatter.format(new Date());
      String fileName = "crash-" + time + "-" + timestamp + ".txt";
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

        File dir = AppConfig.externalLogDir;
        String path = dir.getAbsolutePath() + File.separatorChar + fileName;
        if (!dir.exists()) dir.mkdirs();
        // 创建新的文件
        if (!dir.exists()) dir.createNewFile();

        FileOutputStream fos = new FileOutputStream(path);
        fos.write(sb.toString().getBytes());
        // 答出log日志到控制台
        LogcatCrashInfo(path);
        fos.close();
        return path; // 返回完整路径而不是文件名
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      Timber.e("%s", "saveCatchInfo2File() an error occured while writing file... Exception:");
    }
    return null;
  }

  /**
   * 将捕获的导致崩溃的错误信息保存在sdcard 和输出到LogCat中
   *
   * @param fileName
   * @since V1.0
   */
  private void LogcatCrashInfo(String fileName) {
    if (!new File(fileName).exists()) {
        Timber.tag(getClass().getSimpleName()).e("LogcatCrashInfo() 日志文件不存在");
      return;
    }
      String s = null;
      try (FileInputStream fis = new FileInputStream(fileName); BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "GBK"))) {
          try {
              while (true) {
                  s = reader.readLine();
                  if (s == null) break;
                  Timber.tag(getClass().getSimpleName()).i(s);
              }
          } catch (IOException e) {
              Timber.e(e);
              e.printStackTrace();
          }
      } catch (IOException e) {
          Timber.e(e);
      }
  }

  /** 得到程序崩溃的详细信息 */
  public String getCrashInfo(Throwable ex) {
    Writer result = new StringWriter();
    PrintWriter printWriter = new PrintWriter(result);
    ex.setStackTrace(ex.getStackTrace());
    ex.printStackTrace(printWriter);
    printWriter.close();
    return result.toString();
  }

  /**
   * 启动CrashActivity显示崩溃信息
   * @param ex 异常信息
   */
  private void startCrashActivity(Throwable ex) {
    try {
      String crashInfo = getCrashInfo(ex);
      
      Intent intent = new Intent(mApplication, CrashActivity.class);
      intent.putExtra(CrashActivity.EXTRA_CRASH_INFO, crashInfo);
      if (crashLogFilePath != null) {
        intent.putExtra(CrashActivity.EXTRA_LOG_FILE_PATH, crashLogFilePath);
      }
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      
      // 关闭所有现有Activity
      mMyActivityLifecycleCallbacks.removeAllActivities();
      
      mApplication.startActivity(intent);
    } catch (Exception e) {
      Timber.e(e,"startCrashActivity() failed: %s", e.getMessage());
      throw e; // 重新抛出异常，让调用者处理
    }
  }
}

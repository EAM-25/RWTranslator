package com.eam.rwtranslator.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;

import com.eam.rwtranslator.AppConfig;
import com.eam.rwtranslator.data.model.DataSet;
import com.eam.rwtranslator.R;
import com.eam.rwtranslator.utils.FilesHandler;
import com.eam.rwtranslator.utils.UrlUtils;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.SwitchPreferenceCompat;
import java.io.File;
import java.util.Locale;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat {
  public static int TranslateMode = 1;
  public static final int ADD = 1;
  public static final int REPLACE = 2;
  private static final int REQUEST_CODE_CUSTOM_EXPORT_PATH = 1002;

  @Override
  public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);
    PreferenceCategory projectCategory = findPreference("project");
      PreferenceCategory developCategory=findPreference("development");
    SwitchPreferenceCompat pre = projectCategory.findPreference("is_override");
    pre.setOnPreferenceChangeListener(
        (preferce, val) -> {
          Timber.v(val.toString());
          TranslateMode = (boolean) val ? REPLACE : ADD;
          return true;
        });
    projectCategory
        .findPreference("clean")
        .setOnPreferenceClickListener(
            p1 -> {
              for (File file : AppConfig.externalCacheSerialDir.listFiles()) file.delete();
              showMsg(getString(R.string.setting_act_clear_message));
              return true;
            });
    
    // 设置自定义导出路径点击处理
            projectCategory.findPreference("custom_export_path")
      .setOnPreferenceClickListener(
          p1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              openDirectoryPicker();
            } else {
              showMsg(getString(R.string.setting_act_android_version_requirement));
            }
            return true;
          });

      projectCategory.findPreference("defaulter_export_path")
              .setOnPreferenceClickListener(p2->{
                  var defaultPath=getString(R.string.setting_act_export_path_default_value);
                  PreferenceManager.getDefaultSharedPreferences(getContext())
                          .edit()
                          .putString("custom_export_path", defaultPath)
                          .apply();

                  p2.getParent().findPreference("custom_export_path").setSummary(defaultPath);
                  return true;
              });
    developCategory
        .findPreference("joinGroup")
        .setOnPreferenceClickListener(
            p1 -> {
              Intent intent = new Intent(Intent.ACTION_VIEW);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              intent.setData(Uri.parse("https://discord.gg/ubxRGXBcSd"));
              try {
                startActivity(intent);
              } catch (Exception e) {
                showMsg(getString(R.string.setting_act_open_url_failed_message));
                e.printStackTrace();
              }
              return true;
            });
      developCategory
              .findPreference("about_development")
              .setOnPreferenceClickListener(
                      p1 -> {
                          UrlUtils.openGitHubRepo(getContext());
                          return true;
                      });
  }

  private void showMsg(String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
  }
  
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void openDirectoryPicker() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    startActivityForResult(intent, REQUEST_CODE_CUSTOM_EXPORT_PATH);
  }
  
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_CUSTOM_EXPORT_PATH && resultCode == getActivity().RESULT_OK && data != null) {
      Uri treeUri = data.getData();
      if (treeUri != null) {
        // 获得持久化权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          getActivity().getContentResolver().takePersistableUriPermission(treeUri, 
            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        
        // 将URI转换为路径并保存到SharedPreferences
        String selectedPath = treeUri.getPath();
        if (selectedPath != null) {
          // 处理URI路径格式
          if (selectedPath.startsWith("/tree/primary:")) {
            selectedPath = selectedPath.replace("/tree/primary:", "/storage/emulated/0/");
          } else if (selectedPath.startsWith("/tree/")) {
            selectedPath = selectedPath.substring(6); // 移除"/tree/"前缀
          }
          
          // 保存到SharedPreferences
          PreferenceManager.getDefaultSharedPreferences(getContext())
              .edit()
              .putString("custom_export_path", selectedPath)
              .apply();
          
          // 更新preference的summary显示
          Preference customExportPathPref = findPreference("custom_export_path");
          if (customExportPathPref != null) {
            customExportPathPref.setSummary(selectedPath);
          }
          
          showMsg(getString(R.string.setting_act_export_path_set_success, selectedPath));
        }
      }
    }
  }
}

package com.eam.rwtranslator.data.repository;

import android.content.Context;
import android.net.Uri;
import com.eam.rwtranslator.AppConfig;
import com.eam.rwtranslator.data.model.DataSet;
import com.eam.rwtranslator.data.model.SectionModel;
import com.eam.rwtranslator.data.model.IniFileModel;
import com.eam.rwtranslator.utils.FilesHandler;
import com.eam.rwtranslator.ui.project.TranslationConfigManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import org.ini4j.Wini;
import timber.log.Timber;

public class ProjectRepository {
  private final ExecutorService ioExecutor = Executors.newFixedThreadPool(4);

  // 处理压缩文件解压
  public CompletableFuture<Boolean> handleFileImport(Uri uri, Context context) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            File selectedFile = FilesHandler.uriToFile(uri, context);
            String projectDir = FilesHandler.getBaseName(selectedFile.getName());
            File targetDir = new File(AppConfig.externalProjectDir, projectDir);
            targetDir.mkdirs();
            FilesHandler.unzip(selectedFile, targetDir);
            return true;
          } catch (IOException e) {
            Timber.e(e);
            throw new RuntimeException(e);
          }
        },
        ioExecutor);
  }
  
  // 处理文件夹导入
  public CompletableFuture<Boolean> handleFolderImport(Uri uri, Context context) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            // 获取文件夹名称作为项目名称
            String folderName = FilesHandler.getFolderNameFromUri(uri, context);
            if (folderName.isEmpty()) {
              folderName = "ImportedProject_" + System.currentTimeMillis();
            }
            
            // 处理重名项目，确保项目名称唯一
            String uniqueFolderName = getUniqueProjectName(folderName);
            
            File targetDir = new File(AppConfig.externalProjectDir, uniqueFolderName);
            targetDir.mkdirs();
            
            // 复制文件夹内容到项目目录
            FilesHandler.copyFolderFromUri(uri, targetDir, context);
            return true;
          } catch (Exception e) {
            Timber.e(e, "Failed to import folder");
            throw new RuntimeException("Folder import failed: " + e.getMessage(), e);
          }
        },
        ioExecutor);
  }

  // 删除项目，返回true表示删除成功
  public CompletableFuture<Boolean> deleteProject(String projectName) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            File projectDir = new File(AppConfig.externalProjectDir, projectName);
            File cacheFile = new File(AppConfig.externalCacheSerialDir, projectName + ".json");
            FilesHandler.delDir(projectDir);
            if (cacheFile.exists()) cacheFile.delete();
            // 删除后应返回!projectDir.exists()
            return !projectDir.exists();
          } catch (Exception e) {
            Timber.e(e);
            throw new RuntimeException("Delete project failed: " + e.getMessage(), e);
          }
        },
        ioExecutor);
  }

  public CompletableFuture<Boolean> deleteProjectCache(String projectName) {
    return CompletableFuture.supplyAsync(
        () -> {
          File cacheFile = new File(AppConfig.externalCacheSerialDir, projectName + ".json");
          if (cacheFile.exists()) {
            return cacheFile.delete();
          }
          return false;
        },
        ioExecutor);
  }

  public CompletableFuture<TranslationConfigManager> loadProjectData(String projectName) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            File projectDir = new File(AppConfig.externalProjectDir, projectName);
            TranslationConfigManager project = TranslationConfigManager.getInstance(projectDir);

            // 处理INI数据装配
            HashMap<String, IniFileModel> dataMap = new HashMap<>();
            for (Wini ini : project.getTranslationIniFiles()) {
              ArrayList<SectionModel> groups = new ArrayList<>();
              Map<String, List<SectionModel.Pair>> sectionMap =
                  project.getTranMap(ini);
              sectionMap.forEach(
                  (sectionName, pairs) -> groups.add(new SectionModel(sectionName, pairs)));
              dataMap.put(ini.getFile().getPath(), new IniFileModel(ini, groups));
            }
            DataSet.setIniFileModel(dataMap);
            return project;
          } catch (Exception e) {
            Timber.e(e);
          throw new RuntimeException("Project loading failed:" + e.getMessage(), e);
          }
        },
        ioExecutor);
  }

  // 释放线程池资源（如有需要，可在Application退出时调用）
  public void close() {
    ioExecutor.shutdown();
  }
  
  /**
   * 获取唯一的项目名称，如果项目名称已存在，则添加数字后缀
   */
  private String getUniqueProjectName(String baseName) {
    File baseDir = new File(AppConfig.externalProjectDir, baseName);
    if (!baseDir.exists()) {
      return baseName;
    }
    
    int counter = 1;
    String uniqueName;
    File uniqueDir;
    
    do {
      uniqueName = baseName + "_" + counter;
      uniqueDir = new File(AppConfig.externalProjectDir, uniqueName);
      counter++;
    } while (uniqueDir.exists());
    
    return uniqueName;
  }
}

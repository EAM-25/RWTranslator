package com.eam.rwtranslator.utils;

import androidx.annotation.Keep;

import com.eam.rwtranslator.AppConfig;
import timber.log.Timber;
import java.io.File;

@Keep
public class CacheManager {
    
    /**
     * 清理所有序列化缓存文件
     * @return 清理的文件数量
     */
    public static int clearSerializationCache() {
        int deletedCount = 0;
        
        try {
            File serialDir = AppConfig.externalCacheSerialDir;
            if (serialDir != null && serialDir.exists() && serialDir.isDirectory()) {
                File[] files = serialDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".json")) {
                            if (file.delete()) {
                                Timber.d("删除缓存文件: %s", file.getName());
                                deletedCount++;
                            } else {
                                Timber.w("删除缓存文件失败: %s", file.getName());
                            }
                        }
                    }
                }
            }
            
            Timber.i("清理序列化缓存完成，删除了 %d 个文件", deletedCount);
        } catch (Exception e) {
            Timber.e(e, "清理序列化缓存时发生异常");
        }
        
        return deletedCount;
    }
    
    /**
     * 清理指定项目的序列化缓存文件
     * @param projectName 项目名称
     * @return 是否成功删除
     */
    public static boolean clearProjectCache(String projectName) {
        try {
            File projectFile = new File(AppConfig.externalCacheSerialDir, projectName + ".json");
            if (projectFile.exists()) {
                boolean deleted = projectFile.delete();
                if (deleted) {
                    Timber.d("删除项目缓存文件: %s", projectName);
                } else {
                    Timber.w("删除项目缓存文件失败: %s", projectName);
                }
                return deleted;
            }
        } catch (Exception e) {
            Timber.e(e, "删除项目缓存文件时发生异常: %s", projectName);
        }
        
        return false;
    }
    
    /**
     * 列出所有序列化缓存文件
     */
    public static void listCacheFiles() {
        try {
            File serialDir = AppConfig.externalCacheSerialDir;
            if (serialDir != null && serialDir.exists() && serialDir.isDirectory()) {
                File[] files = serialDir.listFiles();
                if (files != null) {
                    Timber.d("序列化缓存目录: %s", serialDir.getAbsolutePath());
                    for (File file : files) {
                        if (file.isFile()) {
                            Timber.d("缓存文件: %s (大小: %d bytes)", file.getName(), file.length());
                        }
                    }
                } else {
                    Timber.d("序列化缓存目录为空");
                }
            } else {
                Timber.d("序列化缓存目录不存在");
            }
        } catch (Exception e) {
            Timber.e(e, "列出缓存文件时发生异常");
        }
    }
}
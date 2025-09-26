package com.eam.rwtranslator.data.model;

import com.eam.rwtranslator.data.model.IniFileModel;
import com.eam.rwtranslator.ui.project.TranslationConfigManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局数据缓存类，存储当前项目及其INI文件数据。
 * 注意：所有字段和方法均为静态，若多线程访问需注意线程安全。
 */
public class DataSet {
  // 当前项目配置
  public static TranslationConfigManager currentProject;
  // INI文件数据映射
  private static HashMap<String, IniFileModel> iniFileModelsMap;

  public static TranslationConfigManager getCurrentProject() {
    return currentProject;
  }

  public static void setCurrentProject(TranslationConfigManager mcurrentProject) {
    currentProject = mcurrentProject;
  }
  /**
   * 获取所有INI文件数据，判空保护，防止空指针异常。
   */
  public static ArrayList<IniFileModel> getIniListDatas() {
    if (iniFileModelsMap == null) return new ArrayList<>();
    return iniFileModelsMap.values().stream().collect(Collectors.toCollection(ArrayList::new));
  }

  public static void setIniFileModel(HashMap<String, IniFileModel> miniFileModelMap) {
    iniFileModelsMap = miniFileModelMap;
  }

  public static IniFileModel getIniFileModel(String key) {
    if (iniFileModelsMap == null) return null;
    return iniFileModelsMap.get(key);
  }
}

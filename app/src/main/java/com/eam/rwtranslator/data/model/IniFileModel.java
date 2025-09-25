package com.eam.rwtranslator.data.model;

import java.util.ArrayList;

import org.ini4j.Wini;

/**
 * INI文件数据模型，封装文件名、Wini对象、分组数据和颜色属性。
 */
public class IniFileModel{
    // INI文件名
    private String ininame;
    // Wini对象（ini4j库）
    private Wini rwini;
    // 分组数据
    private ArrayList<SectionModel> data;
    // 颜色属性（可用于UI高亮等）
    private int color;
    
    // 性能优化：缓存计算结果
    private int cachedSectionCount = -1;
    private int cachedItemCount = -1;
    private String cachedSectionText = null;
    private String cachedItemText = null;
    
    // 标记状态（用于保存功能）
    private boolean isViewed = false;
    // 修改状态（用于标记指示器）
    private boolean isModified = false;
    public IniFileModel(Wini rwini, ArrayList<SectionModel> data) {
        this.rwini = rwini;
        this.ininame = rwini.getFile().getName();
        this.data = data;
    }
    
    // 文件名setter
    public void setIniname(String ininame) {
        this.ininame = ininame;
    }
    // 文件名getter
    public String getIniname() {
        return ininame;
    }
    // 颜色getter
    public int getColor() {
        return this.color;
    }
    // 颜色setter
    public void setColor(int color) {
        this.color = color;
    }
    // 分组数据getter
    public ArrayList<SectionModel> getData() {
        return this.data;
    }
    // 分组数据setter
    public void setData(ArrayList<SectionModel> data) {
        this.data = data;
    }
    // Wini对象getter
    public Wini getRwini() {
        return this.rwini;
    }
    
    // 缓存相关方法
    public int getCachedSectionCount() {
        return cachedSectionCount;
    }
    
    public void setCachedSectionCount(int cachedSectionCount) {
        this.cachedSectionCount = cachedSectionCount;
    }
    
    public int getCachedItemCount() {
        return cachedItemCount;
    }
    
    public void setCachedItemCount(int cachedItemCount) {
        this.cachedItemCount = cachedItemCount;
    }
    
    public String getCachedSectionText() {
        return cachedSectionText;
    }
    
    public void setCachedSectionText(String cachedSectionText) {
        this.cachedSectionText = cachedSectionText;
    }
    
    public String getCachedItemText() {
        return cachedItemText;
    }
    
    public void setCachedItemText(String cachedItemText) {
        this.cachedItemText = cachedItemText;
    }
    
    // 清除缓存（当数据发生变化时调用）
    public void clearCache() {
        this.cachedSectionCount = -1;
        this.cachedItemCount = -1;
        this.cachedSectionText = null;
        this.cachedItemText = null;
    }
    
    // 标记状态getter
    public boolean isViewed() {
        return isViewed;
    }
    
    // 标记状态setter
    public void setViewed(boolean viewed) {
        this.isViewed = viewed;
    }
    
    // 修改状态getter
    public boolean isModified() {
        return isModified;
    }
    
    // 修改状态setter
    public void setModified(boolean modified) {
        this.isModified = modified;
    }
}

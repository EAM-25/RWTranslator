package com.eam.rwtranslator.ui.common;

import android.content.Context;
import android.content.res.Configuration;
import timber.log.Timber;

/**
 * 多选模式性能优化配置类
 * 提供可调节的性能参数，以适应不同的使用场景
 */
public class MultiSelectPerformanceConfig {
    
    // 批量更新阈值：当选中项目数量超过此比例时，使用全量刷新而非逐项更新
    public static final double BATCH_UPDATE_THRESHOLD = 0.3;
    
    // 视图缓存大小：用于缓存ViewHolder状态
    public static final int VIEW_CACHE_SIZE = 50;
    
    // 延迟更新时间：批量操作时的延迟时间（毫秒）
    public static final int BATCH_UPDATE_DELAY_MS = 16; // 约1帧的时间
    
    // 大数据集阈值：超过此数量的项目被认为是大数据集
    public static final int LARGE_DATASET_THRESHOLD = 500;
    
    // 可见项目缓存大小：用于优化ExpandableListView的可见项目更新
    public static final int VISIBLE_ITEMS_CACHE_SIZE = 20;
    
    // 正常状态颜色（透明）
    public static final int NORMAL_COLOR = 0x00000000;   // 透明
    
    // 性能监控开关
    public static final boolean ENABLE_PERFORMANCE_MONITORING = false;
    
    // 调试模式开关
    public static final boolean DEBUG_MODE = false;
    
    /**
     * 根据当前主题获取选中状态的背景颜色
     * @param context 上下文，用于获取主题信息
     * @return 选中状态的背景颜色
     */
    public static int getSelectedColor(Context context) {
        if (context == null) {
            return 0xFFE0E0E0; // 兼容旧版本，使用默认浅灰色
        }
        
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES ->
                // Night mode: Use bright blue with good contrast against dark background
                    0xFF1976D2; // Material Blue 700 - high contrast for dark theme
            default ->
                // Day mode: Use light blue with good contrast against light background
                    0xFFBBDEFB; // Material Blue 100 - high contrast for light theme
        };
    }
    
    /**
     * 根据数据集大小判断是否应该使用优化策略
     * @param itemCount 项目总数
     * @return 是否使用优化策略
     */
    public static boolean shouldUseOptimization(int itemCount) {
        return itemCount > LARGE_DATASET_THRESHOLD;
    }
    
    /**
     * 根据选中项目数量判断是否应该使用批量更新
     * @param selectedCount 选中项目数量
     * @param totalCount 总项目数量
     * @return 是否使用批量更新
     */
    public static boolean shouldUseBatchUpdate(int selectedCount, int totalCount) {
        if (totalCount == 0) return false;
        return (double) selectedCount / totalCount > BATCH_UPDATE_THRESHOLD;
    }
    
    /**
     * 获取适合当前数据集大小的缓存大小
     * @param itemCount 项目总数
     * @return 缓存大小
     */
    public static int getOptimalCacheSize(int itemCount) {
        if (itemCount < 100) {
            return Math.min(VIEW_CACHE_SIZE / 2, itemCount);
        } else if (itemCount < LARGE_DATASET_THRESHOLD) {
            return VIEW_CACHE_SIZE;
        } else {
            return VIEW_CACHE_SIZE * 2;
        }
    }
    
    /**
     * 性能监控日志
     * @param operation 操作名称
     * @param duration 耗时（毫秒）
     */
    public static void logPerformance(String operation, long duration) {
        if (ENABLE_PERFORMANCE_MONITORING && DEBUG_MODE) {
            Timber.d("[MultiSelect Performance] " + operation + ": " + duration + "ms");
        }
    }
}
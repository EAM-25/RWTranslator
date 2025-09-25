package com.eam.rwtranslator.ui.common;

import java.util.Set;

/**
 * 多选适配器通用接口
 * 支持RecyclerView.Adapter和BaseExpandableListAdapter等不同类型的适配器
 */
public interface MultiSelectAdapter {
    
    /**
     * 获取适配器中的总项目数
     * @return 总项目数
     */
    int getTotalItemCount();
    
    /**
     * 通知单个项目状态变化（高性能局部更新）
     * @param position 位置
     */
    void notifyItemSelectionChanged(int position);

    /**
     * 通知所有项目状态变化
     */
    void notifyAllSelectionChanged();

    /**
     * 通知批量项目状态变化（高性能批量更新）
     * @param positions 位置集合
     */
    void notifyItemsSelectionChanged(Set<Integer> positions);

    /**
     * 通知进入多选模式
     */
    void notifyEnterMultiSelectMode();


    /**
     * 通知退出多选模式
     */
    void notifyExitMultiSelectMode();
    
    /**
     * 获取适配器类型
     * @return 适配器类型
     */
    AdapterType getAdapterType();
    
    /**
     * 适配器类型枚举
     */
    enum AdapterType {
        RECYCLER_VIEW,
        EXPANDABLE_LIST_VIEW
    }
}
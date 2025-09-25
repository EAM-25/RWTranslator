package com.eam.rwtranslator.ui.common;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.RecyclerView;
import java.util.Set;

/**
 * RecyclerView适配器的多选包装器
 * 提供高性能的局部更新机制
 */
public class RecyclerViewMultiSelectAdapter implements MultiSelectAdapter {
    
    private final RecyclerView.Adapter<?> adapter;
    private static final String SELECTION_PAYLOAD = "selection_changed";
    
    public RecyclerViewMultiSelectAdapter(RecyclerView.Adapter<?> adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public int getTotalItemCount() {
        return adapter.getItemCount();
    }
    
    @Override
    public void notifyItemSelectionChanged(int position) {
        // 使用payload进行高性能局部更新
        adapter.notifyItemChanged(position, SELECTION_PAYLOAD);
    }
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void notifyAllSelectionChanged() {
        adapter.notifyDataSetChanged();
    }
    @Override
    public void notifyItemsSelectionChanged(Set<Integer> positions) {
        // 批量更新，使用payload
        for (Integer position : positions) {
            adapter.notifyItemChanged(position, SELECTION_PAYLOAD);
        }
    }
    
    @Override
    public void notifyEnterMultiSelectMode() {
        // RecyclerView通常不需要全局刷新来进入多选模式
        // 如果需要，可以在这里添加特定逻辑
    }
    
    @Override
    public void notifyExitMultiSelectMode() {
        // 退出多选模式时，刷新所有可见项目以清除选中状态
        adapter.notifyItemRangeChanged(0, adapter.getItemCount(), SELECTION_PAYLOAD);
    }
    
    @Override
    public AdapterType getAdapterType() {
        return AdapterType.RECYCLER_VIEW;
    }
    
    /**
     * 获取选中状态变化的payload标识
     * @return payload字符串
     */
    public static String getSelectionPayload() {
        return SELECTION_PAYLOAD;
    }
}
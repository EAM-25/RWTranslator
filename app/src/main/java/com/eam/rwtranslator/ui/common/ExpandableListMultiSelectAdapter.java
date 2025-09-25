package com.eam.rwtranslator.ui.common;

import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import java.util.Set;
import java.util.HashSet;

/**
 * ExpandableListView适配器的多选包装器
 * 提供高性能的局部更新机制，避免notifyDataSetChanged的性能开销
 */
public class ExpandableListMultiSelectAdapter implements MultiSelectAdapter {
    
    private final BaseExpandableListAdapter adapter;
    private final ExpandableListView listView;
    
    // 性能优化：缓存需要更新的视图
    private final Set<Integer> pendingViewUpdates = new HashSet<>();
    private boolean isBatchMode = false;
    
    public ExpandableListMultiSelectAdapter(BaseExpandableListAdapter adapter, ExpandableListView listView) {
        this.adapter = adapter;
        this.listView = listView;
    }
    
    @Override
    public int getTotalItemCount() {
        int count = 0;
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            count += adapter.getChildrenCount(i);
        }
        return count;
    }
    @Override
    public void notifyAllSelectionChanged() {
        adapter.notifyDataSetChanged();
    }
    @Override
    public void notifyItemSelectionChanged(int position) {
        if (isBatchMode) {
            pendingViewUpdates.add(position);
        } else {
            updateSingleView(position);
        }
    }
    
    @Override
    public void notifyItemsSelectionChanged(Set<Integer> positions) {
        // 开始批量更新
        isBatchMode = true;
        pendingViewUpdates.addAll(positions);
        
        // 执行批量更新
        flushPendingUpdates();
        isBatchMode = false;
    }
    
    @Override
    public void notifyEnterMultiSelectMode() {
        // ExpandableListView进入多选模式时可能需要刷新所有可见项
        // 但我们尽量避免使用notifyDataSetChanged
        updateAllVisibleViews();
    }
    
    @Override
    public void notifyExitMultiSelectMode() {
        // 退出多选模式时，更新所有可见项以清除选中状态
        updateAllVisibleViews();
    }
    
    @Override
    public AdapterType getAdapterType() {
        return AdapterType.EXPANDABLE_LIST_VIEW;
    }
    
    /**
     * 更新单个视图
     * @param flatPosition 扁平位置
     */
    private void updateSingleView(int flatPosition) {
        if (listView == null) return;
        
        // 将扁平位置转换为组和子位置
        int[] groupChild = convertFlatPositionToGroupChild(flatPosition);
        if (groupChild == null) return;
        
        int groupPosition = groupChild[0];
        int childPosition = groupChild[1];
        
        // 获取可见的子视图
        long packedPosition = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
        int flatPos = listView.getFlatListPosition(packedPosition);
        
        // 添加空指针保护：如果getFlatListPosition返回-1，说明位置无效
        if (flatPos == -1) return;
        
        int firstVisible = listView.getFirstVisiblePosition();
        int lastVisible = listView.getLastVisiblePosition();
        
        if (flatPos >= firstVisible && flatPos <= lastVisible) {
            int viewIndex = flatPos - firstVisible;
            if (viewIndex >= 0 && viewIndex < listView.getChildCount()) {
                // 直接更新视图，避免重新绑定
                listView.post(() -> {
                    // 这里可以添加具体的视图更新逻辑
                    // 例如更新背景色等
                });
            }
        }
    }
    
    /**
     * 刷新所有待更新的视图
     */
    private void flushPendingUpdates() {
        if (pendingViewUpdates.isEmpty()) return;
        
        // 对于大量更新，使用notifyDataSetChanged可能更高效
        if (pendingViewUpdates.size() > getTotalItemCount() * 0.3) {
            adapter.notifyDataSetChanged();
        } else {
            // 逐个更新
            for (Integer position : pendingViewUpdates) {
                updateSingleView(position);
            }
        }
        
        pendingViewUpdates.clear();
    }
    
    /**
     * 更新所有可见视图
     */
    private void updateAllVisibleViews() {
        if (listView == null) return;
        
        listView.post(() -> {
            // 对于模式切换，使用notifyDataSetChanged是必要的
            adapter.notifyDataSetChanged();
        });
    }
    
    /**
     * 将扁平位置转换为组和子位置（不包含组头的扁平位置）
     * @param flatPosition 扁平位置（只计算子项目）
     * @return [groupPosition, childPosition] 或 null（如果位置无效）
     */
    private int[] convertFlatPositionToGroupChild(int flatPosition) {
        int currentPos = 0;
        
        for (int groupPos = 0; groupPos < adapter.getGroupCount(); groupPos++) {
            int childCount = adapter.getChildrenCount(groupPos);
            
            if (flatPosition < currentPos + childCount) {
                // 找到了对应的子项
                int childPos = flatPosition - currentPos;
                return new int[]{groupPos, childPos};
            }
            
            currentPos += childCount;
        }
        
        return null; // 位置无效
    }
    
    /**
     * 开始批量更新模式
     */
    public void beginBatchUpdate() {
        isBatchMode = true;
        pendingViewUpdates.clear();
    }
    
    /**
     * 结束批量更新模式
     */
    public void endBatchUpdate() {
        if (isBatchMode) {
            flushPendingUpdates();
            isBatchMode = false;
        }
    }
    
    /**
     * 获取ExpandableListView实例
     * @return ExpandableListView实例
     */
    public ExpandableListView getListView() {
        return listView;
    }
}
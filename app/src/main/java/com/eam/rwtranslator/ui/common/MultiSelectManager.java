package com.eam.rwtranslator.ui.common;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

import com.eam.rwtranslator.R;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.Set;

/**
 * 多选管理器接口，用于统一管理多选功能
 * 提供进入/退出多选模式、选择操作等功能
 */
public class MultiSelectManager {
    private boolean isMultiSelectMode = false;
    private final MultiSelectDecorator decorator;
    private final RecyclerView.Adapter<?> adapter;
    private final MaterialToolbar toolbar;
    private final View multiSelectButton;
    private final OnMultiSelectListener listener;
    
    public interface OnMultiSelectListener {
        /**
         * 多选模式状态改变时调用
         * @param isMultiSelectMode 是否处于多选模式
         */
        void onMultiSelectModeChanged(boolean isMultiSelectMode);
        
        /**
         * 选中项目改变时调用
         * @param selectedCount 选中数量
         * @param totalCount 总数量
         */
        void onSelectionChanged(int selectedCount, int totalCount);
        
        /**
         * 获取总项目数
         * @return 总数量
         */
        int getTotalItemCount();
    }
    
    public MultiSelectManager(MultiSelectDecorator decorator, 
                            RecyclerView.Adapter<?> adapter,
                            MaterialToolbar toolbar,
                            View multiSelectButton,
                            OnMultiSelectListener listener) {
        this.decorator = decorator;
        this.adapter = adapter;
        this.toolbar = toolbar;
        this.multiSelectButton = multiSelectButton;
        this.listener = listener;
        
        // 设置多选按钮点击事件
        if (multiSelectButton != null) {
            multiSelectButton.setOnClickListener(v -> toggleMultiSelectMode());
        }
    }
    
    /**
     * 简化构造函数，用于基本的多选功能
     * @param decorator 装饰器
     * @param adapter 适配器
     * @param toolbar 工具栏
     */
    public MultiSelectManager(MultiSelectDecorator decorator, 
                            RecyclerView.Adapter<?> adapter,
                            MaterialToolbar toolbar) {
        this(decorator, adapter, toolbar, null, new DefaultMultiSelectListener(adapter));
    }
    
    /**
     * 默认的多选监听器实现
     */
    private static class DefaultMultiSelectListener implements OnMultiSelectListener {
        private final RecyclerView.Adapter<?> adapter;
        
        public DefaultMultiSelectListener(RecyclerView.Adapter<?> adapter) {
            this.adapter = adapter;
        }
        
        @Override
        public void onMultiSelectModeChanged(boolean isMultiSelectMode) {
            // 默认实现为空
        }
        
        @Override
        public void onSelectionChanged(int selectedCount, int totalCount) {
            // 默认实现为空
        }
        
        @Override
        public int getTotalItemCount() {
            return adapter != null ? adapter.getItemCount() : 0;
        }
    }
    
    /**
     * 切换多选模式
     */
    public void toggleMultiSelectMode() {
        if (isMultiSelectMode) {
            exitMultiSelectMode();
        } else {
            enterMultiSelectMode();
        }
    }
    
    /**
     * 进入多选模式
     */
    public void enterMultiSelectMode() {
        isMultiSelectMode = true;
        rotateMultiSelectButton(true);
        updateSubtitle();
        adapter.notifyDataSetChanged();
        if (listener != null) {
            listener.onMultiSelectModeChanged(true);
        }
    }
    
    /**
     * 退出多选模式
     */
    public void exitMultiSelectMode() {
        isMultiSelectMode = false;
        rotateMultiSelectButton(false);
        decorator.clearSelections();
        updateSubtitle();
        adapter.notifyDataSetChanged();
        if (listener != null) {
            listener.onMultiSelectModeChanged(false);
        }
    }
    
    /**
     * 处理项目点击
     * @param position 点击的位置
     * @return 是否处理了点击事件
     */
    public boolean handleItemClick(int position) {
        if (isMultiSelectMode) {
            toggleItemSelection(position);
            return true;
        }
        return false;
    }
    
    /**
     * 处理项目长按
     * @param position 长按的位置
     * @return 是否处理了长按事件
     */
    public boolean handleItemLongClick(int position) {
        if (!isMultiSelectMode) {
            enterMultiSelectMode();
            toggleItemSelection(position);
            return true;
        }
        return false;
    }
    
    /**
     * 切换指定位置的选中状态
     * @param position 位置
     */
    public void toggleItemSelection(int position) {
        decorator.toggleSelection(position);
        updateSubtitle();
        // 使用payload进行局部更新，避免完整重绑定
        adapter.notifyItemChanged(position, "selection_changed");
        
        // 如果没有选中项，退出多选模式
        if (decorator.getSelectedCount() == 0) {
            exitMultiSelectMode();
        }
    }
    
    /**
     * 全选
     */
    public void selectAll() {
        if (!isMultiSelectMode) {
            enterMultiSelectMode();
        }
        int itemCount = adapter.getItemCount();
        decorator.selectAll(itemCount);
        updateSubtitle();
        // 使用批量更新，性能更好
        adapter.notifyItemRangeChanged(0, itemCount, "selection_changed");
    }
    
    /**
     * 反选
     */
    public void inverseSelection() {
        if (!isMultiSelectMode) {
            enterMultiSelectMode();
        }
        int itemCount = adapter.getItemCount();
        decorator.inverseSelection(itemCount);
        updateSubtitle();
        // 使用批量更新，性能更好
        adapter.notifyItemRangeChanged(0, itemCount, "selection_changed");
        
        // 如果反选后没有选中项，退出多选模式
        if (decorator.getSelectedCount() == 0) {
            exitMultiSelectMode();
        }
    }
    
    /**
     * 反选（别名方法）
     */
    public void invertSelection() {
        inverseSelection();
    }
    
    /**
     * 获取选中的位置
     * @return 选中位置集合
     */
    public Set<Integer> getSelectedPositions() {
        return decorator.getSelectedPositions();
    }
    
    /**
     * 获取选中数量
     * @return 选中数量
     */
    public int getSelectedCount() {
        return decorator.getSelectedCount();
    }
    
    /**
     * 是否处于多选模式
     * @return 是否多选模式
     */
    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }
    
    /**
     * 是否处于多选模式（别名方法）
     * @return 是否多选模式
     */
    public boolean isInMultiSelectMode() {
        return isMultiSelectMode;
    }
    
    /**
     * 获取装饰器
     * @return 装饰器
     */
    public MultiSelectDecorator getDecorator() {
        return decorator;
    }
    
    /**
     * 旋转多选按钮
     * @param clockwise 是否顺时针旋转
     */
    private void rotateMultiSelectButton(boolean clockwise) {
        if (multiSelectButton != null) {
            View iconView = multiSelectButton.findViewById(R.id.imageview_multi_select);
            if (iconView != null) {
                iconView.setRotation(clockwise ? 45f : 0f);
            }
        }
    }
    
    /**
     * 更新工具栏副标题
     */
    private void updateSubtitle() {
        if (toolbar != null && listener != null) {
            int selectedCount = decorator.getSelectedCount();
            int totalCount = listener.getTotalItemCount();
            toolbar.setSubtitle(selectedCount + "/" + totalCount);

            listener.onSelectionChanged(selectedCount, totalCount);
        }
    }
}
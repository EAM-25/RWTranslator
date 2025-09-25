package com.eam.rwtranslator.ui.common;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 通用的多选装饰器，用于RecyclerView的多选功能
 * 提供选中状态的视觉反馈和状态管理
 */
public class MultiSelectDecorator extends RecyclerView.ItemDecoration {
    private final Set<Integer> selectedPositions = new HashSet<>();
    private final Paint paint;
    private final Context context;
    
    public MultiSelectDecorator() {
        this.context = null;
        this.paint = new Paint();
        this.paint.setAlpha(100); // 半透明效果
    }
    
    public MultiSelectDecorator(int selectedColor) {
        this.context = null;
        this.paint = new Paint();
        this.paint.setColor(selectedColor);
        this.paint.setAlpha(100); // 半透明效果
    }
    
    /**
     * 使用Context的构造函数，支持动态主题颜色
     * @param context 上下文
     */
    public MultiSelectDecorator(Context context) {
        this.context = context;
        this.paint = new Paint();
        this.paint.setAlpha(100); // 半透明效果
    }
    
    /**
     * 切换指定位置的选中状态
     * @param position 要切换的位置
     */
    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
    }
    
    /**
     * 设置指定位置的选中状态
     * @param position 位置
     * @param selected 是否选中
     */
    public void setSelection(int position, boolean selected) {
        if (selected) {
            selectedPositions.add(position);
        } else {
            selectedPositions.remove(position);
        }
    }
    
    /**
     * 检查指定位置是否被选中
     * @param position 位置
     * @return 是否选中
     */
    public boolean isSelected(int position) {
        return selectedPositions.contains(position);
    }
    
    /**
     * 清除所有选中状态
     */
    public void clearSelections() {
        selectedPositions.clear();
    }
    
    /**
     * 全选指定范围内的项目
     * @param itemCount 总项目数
     */
    public void selectAll(int itemCount) {
        selectedPositions.clear();
        // 使用批量操作避免循环
        Set<Integer> allPositions = new HashSet<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            allPositions.add(i);
        }
        selectedPositions.addAll(allPositions);
    }
    
    /**
     * 反选指定范围内的项目
     * @param itemCount 总项目数
     */
    public void inverseSelection(int itemCount) {
        // 创建全集
        Set<Integer> allPositions = new HashSet<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            allPositions.add(i);
        }
        
        // 计算差集（全集 - 当前选中）
        allPositions.removeAll(selectedPositions);
        
        // 更新选中状态
        selectedPositions.clear();
        selectedPositions.addAll(allPositions);
    }
    
    /**
     * 获取选中的位置列表
     * @return 选中位置的集合
     */
    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedPositions);
    }
    
    /**
     * 获取选中项目的数量
     * @return 选中数量
     */
    public int getSelectedCount() {
        return selectedPositions.size();
    }
    
    /**
     * Get selection background color based on current theme (day/night mode)
     * @return Color value for selected item background with high contrast
     */
    private int getSelectionBackgroundColor() {
        if (context == null) {
            return Color.LTGRAY; // 兼容旧版本，使用默认颜色
        }
        
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode: Use bright blue with good contrast against dark background
                return 0xFF1976D2; // Material Blue 700 - high contrast for dark theme
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                // Day mode: Use light blue with good contrast against light background
                return 0xFFBBDEFB; // Material Blue 100 - high contrast for light theme
        }
    }
    
    /**
     * 应用装饰效果到指定视图
     * @param view 要装饰的视图
     * @param position 位置
     * @deprecated 使用ViewHolder的updateSelectionState方法代替，性能更好
     */
    @Deprecated
    public void applyDecoration(View view, int position) {
        if (selectedPositions.contains(position)) {
            view.setBackgroundColor(getSelectionBackgroundColor());
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }
    
    // 移除onDraw方法，因为选中状态现在直接在ViewHolder中处理
    // 这避免了每次绘制时遍历所有子视图的性能开销
    
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 可以在这里调整选中项的间距
    }
}
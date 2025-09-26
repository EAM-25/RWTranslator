package com.eam.rwtranslator.ui.project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.eam.rwtranslator.AppConfig;
import com.eam.rwtranslator.data.model.SectionModel;
import com.eam.rwtranslator.data.model.IniFileModel;
import com.eam.rwtranslator.R;
import com.eam.rwtranslator.ui.setting.SettingsFragment;
import com.eam.rwtranslator.ui.common.UniversalMultiSelectManager;
import com.eam.rwtranslator.ui.common.RecyclerViewMultiSelectAdapter;
import com.eam.rwtranslator.ui.common.MultiSelectAdapter;
import com.eam.rwtranslator.utils.DialogUtils;
import com.eam.rwtranslator.utils.Translator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import timber.log.Timber;
public class IniFileManagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements Translator.TranslateTaskCallBack, MultiSelectAdapter {
  /**
   * @param filePath      文件路径
   * @param sectionName   分组名称
   * @param keyName       键名
   * @param originalValue 原始值
   * @param error         异常对象
   */ // 用于记录翻译过程中的异常信息
    public record TranslationError(String filePath, String sectionName, String keyName,
                                   String originalValue, Throwable error) {

    @NonNull
    @Override
      public String toString() {
        return "File: " + filePath +
                "\nSection: " + sectionName +
                "\nKey: " + keyName +
                "\nOriginal Value: " + originalValue +
                "\nError: " + (error != null ? error.getMessage() : "Unknown error");
      }
    }
  
  // 翻译错误列表
  public static final List<TranslationError> translationErrors = new LinkedList<>();
  
  private List<IniFileModel> mData;
  private onItemClickListener clickListener;
  private onItemLongClickListener onclickListener;
  private final List<IniFileModel> sourceList;
  private String lastFilteredText;
  private Context context;
  private UniversalMultiSelectManager multiSelectManager;
  private OnTranslationCompleteListener translationCompleteListener;
  private OnFileMarkListener fileMarkListener;
  
  // 选择状态缓存，提升多选性能
  private final Map<Integer, Boolean> selectionCache = new HashMap<>();
  private boolean cacheValid = false;
  
  // 性能优化：启用稳定ID
  {
    setHasStableIds(true);
  }

  public IniFileManagerAdapter(Context context, List<IniFileModel> data) {
    this.context = context;
    mData = data;
        
    sourceList = new LinkedList<>();
    sourceList.addAll(data);
  }

  public interface onItemClickListener {
    void onItemClick(int position);
  }

  public interface onItemLongClickListener {
    void onItemLongClick(int position, View view);
  }

  public interface OnTranslationCompleteListener {
    void onTranslationComplete(List<IniFileModel> translatedFiles);
  }

  public interface OnFileMarkListener {
    void onFileMark(IniFileModel file, boolean isMarked);
  }

  public void setOnItemClickListener(onItemClickListener listener) {
    this.clickListener = listener;
  }

  public void setOnTranslationCompleteListener(OnTranslationCompleteListener listener) {
    this.translationCompleteListener = listener;
  }

  public void setOnFileMarkListener(OnFileMarkListener listener) {
    this.fileMarkListener = listener;
  }
  
  /**
   * 切换文件标记状态
   * @param position 文件位置
   */
  public void toggleFileMark(int position) {
    if (position >= 0 && position < mData.size()) {
      IniFileModel file = mData.get(position);
      boolean newMarkState = !file.isViewed();
      file.setViewed(newMarkState);
      
      // 通知监听器
      if (fileMarkListener != null) {
        fileMarkListener.onFileMark(file, newMarkState);
      }
      
      // 更新UI
      notifyItemChanged(position);
    }
  }
  
  /**
   * 设置多选管理器
   * @param multiSelectManager 通用多选管理器
   */
  public void setMultiSelectManager(UniversalMultiSelectManager multiSelectManager) {
    this.multiSelectManager = multiSelectManager;
    // 直接将ProjectFileManagerAdapter注册到多选管理器
    multiSelectManager.setAdapter(this);
  }
  
  /**
   * 获取选中的项目
   * @return 选中的IniFileModel列表
   */
  public List<IniFileModel> getSelectedItems() {
    List<IniFileModel> selectedItems = new LinkedList<>();
    if (multiSelectManager != null) {
      Set<Integer> selectedPositions = multiSelectManager.getSelectedPositions();
      for (Integer position : selectedPositions) {
        if (position >= 0 && position < mData.size()) {
          selectedItems.add(mData.get(position));
        }
      }
    }
    return selectedItems;
  }

  @SuppressLint("NotifyDataSetChanged")
  public void filterIniFileModel(String text) {
    if (text.isEmpty()) {
      addAll(sourceList);
      lastFilteredText = null;
      return;
    }

    if (lastFilteredText != null && text.contains(lastFilteredText)) {
      mData.removeIf(IniFileModel -> !IniFileModel.getIniname().contains(text));
    } else {
      mData =
          sourceList.parallelStream()
              .filter(IniFileModel -> IniFileModel.getIniname().contains(text))
              .collect(Collectors.toList());
    }

    mData.sort(Comparator.comparingInt(o -> o.getIniname().indexOf(text)));

    lastFilteredText = text;
    invalidateSelectionCache();
    notifyDataSetChanged();
  }

  public void setColorByPosition(int position) {
    IniFileModel data = getItem(position);
    ColorUtils.blendARGB(data.getColor(), Color.BLACK, 0.3f);
    notifyItemChanged(position);
  }

  @SuppressLint("NotifyDataSetChanged")
  public void clear() {
    mData.clear();
    invalidateSelectionCache();
    notifyDataSetChanged();
  }

  @SuppressLint("NotifyDataSetChanged")
  public void addAll(List<IniFileModel> data) {
    clear();
    mData.addAll(data);
    invalidateSelectionCache();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.project_act_item, parent, false);
    ViewHolder vh = new ViewHolder(view);
    view.setOnClickListener(v1 -> {
      int position = vh.getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        // 如果多选管理器处理了点击事件，就不执行原来的点击逻辑
        if (multiSelectManager == null || !multiSelectManager.handleItemClick(position)) {
          if (clickListener != null) {
            clickListener.onItemClick(position);
          }
        }
      }
    });
    view.setOnLongClickListener(
        v2 -> {
          int position = vh.getAdapterPosition();
          if (position != RecyclerView.NO_POSITION) {
            // 如果多选管理器处理了长按事件，返回true
            if (multiSelectManager != null && multiSelectManager.handleItemLongClick(position)) {
              return true;
            }
            // 如果不在多选模式，长按切换文件标记状态
            if (multiSelectManager == null || !multiSelectManager.isMultiSelectMode()) {
              toggleFileMark(position);
              return true;
            }
            // 否则执行原来的长按逻辑
            if (onclickListener != null) {
              onclickListener.onItemLongClick(position, view);
            }
          }
          return true;
        });
    return vh;
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    IniFileModel data = mData.get(position);
    ViewHolder mholder = (ViewHolder) holder;
    
    // 设置位置缓存
    mholder.setCachedPosition(position);
    
    // 基本数据绑定
    mholder.iniNameView.setText(data.getIniname());
    
    // 性能优化：缓存计算结果，避免重复计算
    if (data.getCachedSectionCount() == -1) {
      List<SectionModel> groups = data.getData();
      data.setCachedSectionCount(groups.size());
      int cnt = 0;
      for (SectionModel group : groups) {
        cnt += group.getItems().size();
      }
      data.setCachedItemCount(cnt);
    }
    
    // 性能优化：避免重复的字符串拼接，使用StringBuilder提高性能
    if (data.getCachedSectionText() == null) {
      StringBuilder sb = new StringBuilder();
      sb.append(data.getCachedSectionCount()).append(" section");
      data.setCachedSectionText(sb.toString());
    }
    if (data.getCachedItemText() == null) {
      StringBuilder sb = new StringBuilder();
      sb.append(data.getCachedItemCount()).append(" Pair");
      data.setCachedItemText(sb.toString());
    }
    mholder.tv_sectionCount.setText(data.getCachedSectionText());
    mholder.tv_itemCount.setText(data.getCachedItemText());
    
    // 性能优化：使用缓存机制检查选中状态
    boolean isSelected = false;
    if (multiSelectManager != null && multiSelectManager.isMultiSelectMode()) {
      // 先检查缓存
      if (cacheValid && selectionCache.containsKey(position)) {
        isSelected = selectionCache.get(position);
      } else {
        // 缓存未命中，查询实际状态并缓存
        isSelected = multiSelectManager.isSelected(position);
        selectionCache.put(position, isSelected);
        // 首次使用时标记缓存为有效
        if (!cacheValid) {
          cacheValid = true;
        }
      }
    }
    mholder.updateSelectionState(isSelected);
    
    // 更新查看状态
    mholder.updateViewedState(data.isViewed());
    
    // 更新修改状态
    mholder.updateModifyState(data.isModified());
  }
  
  /**
   * 重载方法，支持payload参数进行局部更新
   * @param holder ViewHolder
   * @param position 位置
   * @param payloads payload列表
   */
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      // 如果没有payload，执行完整绑定
      onBindViewHolder(holder, position);
    } else {
      // 处理payload，只更新选中状态
      ViewHolder mholder = (ViewHolder) holder;
      for (Object payload : payloads) {
        if ("selection_changed".equals(payload) || RecyclerViewMultiSelectAdapter.getSelectionPayload().equals(payload)) {
          if (multiSelectManager != null && multiSelectManager.isMultiSelectMode()) {
            // 使用缓存机制检查选中状态
            boolean isSelected = false;
            if (cacheValid && selectionCache.containsKey(position)) {
              isSelected = selectionCache.get(position);
            } else {
              isSelected = multiSelectManager.isSelected(position);
               selectionCache.put(position, isSelected);
               if (!cacheValid) {
                 cacheValid = true;
               }
            }
            mholder.updateSelectionState(isSelected);
          } else {
            mholder.updateSelectionState(false);
          }
          break;
        }
      }
    }
  }

  @Override
  public int getItemCount() {
    return mData.size();
  }
  
  /**
   * 提供稳定的ID，提升RecyclerView性能
   * @param position 位置
   * @return 稳定的ID
   */
  @Override
  public long getItemId(int position) {
    if (position >= 0 && position < mData.size()) {
      // 使用文件名的hashCode作为稳定ID
      return mData.get(position).getRwini().getFile().getPath().hashCode();
    }
    return RecyclerView.NO_ID;
  }

  /**
   * Get selection background color based on current theme (day/night mode)
   * @return Color value for selected item background with high contrast
   */
  private int getSelectionBackgroundColor() {
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

  @Override
  public void onTranslate(boolean enable_llm) {
    // 初始化对话框
    DialogUtils dialogUtils = new DialogUtils(context);
    AlertDialog dialog = dialogUtils.createLoadingDialog("Initializing...");
    dialog.show();

    List<IniFileModel> selecttionList;


    if(multiSelectManager.isMultiSelectMode()){
       selecttionList=new LinkedList<>();
      for (var i:multiSelectManager.getSelectedPositions()) {
        selecttionList.add( mData.get(i));
      }
    }else
      selecttionList = mData;

    //打上修改标记
    selecttionList.forEach(i->{
      i.setModified(true);
    });
    
    // 清空翻译错误列表
    synchronized (translationErrors) {
      translationErrors.clear();
    }
    
    // 预计算总任务数
    final int[] totalTasks = {0};
    for (IniFileModel iniFile : selecttionList) {
      for (SectionModel section : iniFile.getData()) {
        for (SectionModel.Pair pair : section.getItems()) {
          totalTasks[0]++;
        }
      }
    }

    // 获取进度文本视图
    TextView progressTextView = dialog.findViewById(R.id.mainactivityloadingTextView);
      assert progressTextView != null;
      progressTextView.setText("Translating (0/" + totalTasks[0] + ")...");

    // 无任务直接返回
    if (totalTasks[0] == 0) {
      dialog.dismiss();
      return;
    }
    // 原子计数器跟踪完成数
    AtomicInteger completedTasks = new AtomicInteger(0);
    // 遍历选中的IniFileModel进行翻译
    for (IniFileModel iniFile : selecttionList) {
      for (SectionModel section : iniFile.getData()) {
        for (SectionModel.Pair pair : section.getItems()) {
          var translateCallBack = new Translator.TranslateCallBack() {
            @Override
            public void onSuccess(String translation, String srcLang, String tgtLang) {
              // 更新翻译结果
              if (SettingsFragment.TranslateMode == SettingsFragment.ADD) {
                pair.getLang_pairs().put(tgtLang, translation);
              } else {
                pair.setOri_val(translation);
              }
              updateProgress(completedTasks, totalTasks[0], progressTextView, dialog, selecttionList);
            }

            @Override
            public void onError(Throwable t) {
              updateProgress(completedTasks, totalTasks[0], progressTextView, dialog, selecttionList);
              
              // 记录详细的日志信息
              String filePath = iniFile.getRwini().getFile().getAbsolutePath();
              String keyName = pair.getKey().getKeyName();
              String originalValue = pair.getOri_val();
              
              Timber.e(t, "Translation error for file: %s, section: %s, key: %s, value: %s", 
                      filePath, section.getName(), keyName, originalValue);
              
              // 添加到翻译错误列表
              synchronized (translationErrors) {
                translationErrors.add(new TranslationError(
                        filePath,
                        section.getName(),
                        keyName,
                        originalValue,
                        t
                ));
              }
            }
          };
          if(enable_llm){
              Translator.LLM_translate(
                      pair.getOri_val(),
                      AppConfig.CurrentFromLanguage,
                      AppConfig.CurrentTargetLanguage,
                      translateCallBack
              );
          }else{
            Translator.translate(
              pair.getOri_val(),
                    translateCallBack);
          }          
        }
      }
    }
  }

  // 进度更新方法
  @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
  private void updateProgress(AtomicInteger counter, int total, TextView tv, AlertDialog dialog, List<IniFileModel> translatedFiles) {
    int current = counter.incrementAndGet();
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              tv.setText("Translating (" + current + "/" + total + ")...");
              if (current == total) {
                dialog.dismiss();
                invalidateSelectionCache();
                notifyDataSetChanged(); // 刷新列表显示新数据
                if ( multiSelectManager.isMultiSelectMode()) multiSelectManager.exitMultiSelectMode();
                // 通知翻译完成
                  translationCompleteListener.onTranslationComplete(translatedFiles);
              }
            });
  }

  public IniFileModel getItem(int position) {
    int count = getItemCount();
    if (position >= 0 && position < count) {
      return mData.get(position);
    }
    throw new IllegalArgumentException("Invalid position");
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView iniNameView, tv_sectionCount, tv_itemCount;
    View viewMarkIndicator;
    View modifyMarkIndicator;
    private boolean isSelected = false;
    private int cachedPosition = -1; // 缓存位置，避免重复查询
    private boolean positionCacheValid = false;
    private int originalBackgroundColor = Color.TRANSPARENT;
    private Context context;
    private boolean isViewed = false;
    private boolean isModified = false;
    
    // 性能优化：缓存颜色值，避免重复计算
    private int cachedSelectionBgColor = -1;
    private int cachedViewedIndicatorColor = -1;
    private int cachedModifyIndicatorColor = -1;
    private boolean colorCacheValid = false;

    
    public ViewHolder(View itemView) {
      super(itemView);
      this.context = itemView.getContext();
      iniNameView = itemView.findViewById(R.id.IniListItemTextView1);
      tv_sectionCount = itemView.findViewById(R.id.IniListItemTextView2);
      tv_itemCount = itemView.findViewById(R.id.IniListItemTextView3);
      viewMarkIndicator = itemView.findViewById(R.id.mark_indicator_view);
      modifyMarkIndicator = itemView.findViewById(R.id.mark_indicator_modify);
      
      // 初始化颜色缓存
      initializeColorCache();
    }
    
    /**
     * 初始化颜色缓存，避免重复计算主题相关颜色
     */
    private void initializeColorCache() {
      int nightModeFlags = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
      boolean isNightMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
      
      if (isNightMode) {
        cachedSelectionBgColor = 0xFF1976D2; // Material Blue 700
        cachedViewedIndicatorColor = 0xFFFF9800; // Material Orange 500
        cachedModifyIndicatorColor = 0xFF4CAF50; // Material Green 500
      } else {
        cachedSelectionBgColor = 0xFFE3F2FD; // Material Blue 50
        cachedViewedIndicatorColor = 0xFFE65100; // Material Deep Orange 900
        cachedModifyIndicatorColor = 0xFF2E7D32; // Material Green 800
      }
      colorCacheValid = true;
    }
    
    /**
     * 更新选中状态，避免重复设置背景色
     * @param selected 是否选中
     */
    public void updateSelectionState(boolean selected) {
      if (this.isSelected != selected) {
        this.isSelected = selected;
        updateBackgroundColor();
      }
    }
    
    /**
     * 更新查看状态
     * @param viewed 是否查看
     */
    public void updateViewedState(boolean viewed) {
      if (this.isViewed != viewed) {
        this.isViewed = viewed;
        // 显示/隐藏右边的竖线指示器
        if (viewMarkIndicator != null) {
          if (viewed) {
            viewMarkIndicator.setVisibility(View.VISIBLE);
            viewMarkIndicator.setBackgroundColor(getCachedViewedIndicatorColor());
          } else {
            viewMarkIndicator.setVisibility(View.GONE);
          }
        }
      }
    }
    
    /**
     * 更新修改状态
     * @param modified 是否修改
     */
    public void updateModifyState(boolean modified) {
      if (this.isModified != modified) {
        this.isModified = modified;
        // 显示/隐藏左边的修改指示器
        if (modifyMarkIndicator != null) {
          if (modified) {
            modifyMarkIndicator.setVisibility(View.VISIBLE);
            modifyMarkIndicator.setBackgroundColor(getCachedModifyIndicatorColor());
          } else {
            modifyMarkIndicator.setVisibility(View.GONE);
          }
        }
      }
    }
    
    /**
     * 更新背景色，只考虑选中状态
     */
    private void updateBackgroundColor() {
      if (isSelected) {
        itemView.setBackgroundColor(getCachedSelectionBackgroundColor());
      } else {
        itemView.setBackgroundColor(originalBackgroundColor);
      }
    }
    
    /**
     * 获取缓存的选中状态背景色
     * @return 背景色
     */
    private int getCachedSelectionBackgroundColor() {
      if (!colorCacheValid) {
        initializeColorCache();
      }
      return cachedSelectionBgColor;
    }
    
    /**
     * 获取缓存的查看指示器颜色
     * @return 指示器颜色
     */
    private int getCachedViewedIndicatorColor() {
      if (!colorCacheValid) {
        initializeColorCache();
      }
      return cachedViewedIndicatorColor;
    }
    
    /**
     * 获取缓存的修改指示器颜色（绿色）
     * @return 指示器颜色
     */
    private int getCachedModifyIndicatorColor() {
      if (!colorCacheValid) {
        initializeColorCache();
      }
      return cachedModifyIndicatorColor;
    }
    
    /**
     * 设置缓存位置
     * @param position 位置
     */
    public void setCachedPosition(int position) {
      this.cachedPosition = position;
      this.positionCacheValid = true;
    }
    
    /**
     * 获取缓存位置
     * @return 缓存的位置，如果无效返回-1
     */
    public int getCachedPosition() {
      return positionCacheValid ? cachedPosition : -1;
    }
    
    /**
     * 清除位置缓存
     */
    public void clearPositionCache() {
      this.positionCacheValid = false;
      this.cachedPosition = -1;
    }
    
    /**
     * 重置颜色缓存，用于主题变化时
     */
    public void resetColorCache() {
      this.colorCacheValid = false;
      initializeColorCache();
    }
  }



  public List<IniFileModel> getMData() {
    return this.mData;
  }

  public void setMData(LinkedList<IniFileModel> mData) {
    this.mData = mData;
  }

  // 实现 MultiSelectAdapter 接口
  @Override
  public int getTotalItemCount() {
    return getItemCount();
  }

  @Override
  public void notifyItemSelectionChanged(int position) {
    // 更新特定位置的缓存而不是清除整个缓存
    if (multiSelectManager != null && cacheValid) {
      boolean newState = multiSelectManager.isSelected(position);
      selectionCache.put(position, newState);
    }
    notifyItemChanged(position, "selection_changed");
  }

  @SuppressLint("NotifyDataSetChanged")
  @Override
  public void notifyAllSelectionChanged() {
    notifyDataSetChanged();
  }


  @Override
  public void notifyItemsSelectionChanged(Set<Integer> positions) {
    // 批量更新特定位置的缓存而不是清除整个缓存
    if (multiSelectManager != null && cacheValid) {
      for (Integer position : positions) {
        boolean newState = multiSelectManager.isSelected(position);
        selectionCache.put(position, newState);
      }
    }
    for (Integer position : positions) {
      notifyItemChanged(position, "selection_changed");
    }
  }


  public void notifyEnterMultiSelectMode() {
    // 进入多选模式时清除缓存并使用局部更新
    invalidateSelectionCache();
    // 使用局部更新替代 notifyDataSetChanged 以提升性能
    notifyItemRangeChanged(0, getItemCount(), "selection_changed");
  }

  @Override
  public void notifyExitMultiSelectMode() {
    // 退出多选模式时清除缓存并使用局部更新
    invalidateSelectionCache();
    // 使用局部更新替代 notifyDataSetChanged 以提升性能
    notifyItemRangeChanged(0, getItemCount(), "selection_changed");
  }

  @Override
   public AdapterType getAdapterType() {
     return AdapterType.RECYCLER_VIEW;
   }

   /**
   * 清除选择状态缓存
   */
  private void invalidateSelectionCache() {
    selectionCache.clear();
    cacheValid = false;
  }

}

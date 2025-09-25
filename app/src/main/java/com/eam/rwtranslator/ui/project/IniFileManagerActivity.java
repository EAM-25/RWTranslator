package com.eam.rwtranslator.ui.project;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eam.rwtranslator.AppConfig;
import com.eam.rwtranslator.BuildConfig;
import com.eam.rwtranslator.R;
import com.eam.rwtranslator.data.model.DataSet;
import com.eam.rwtranslator.data.model.IniFileModel;
import com.eam.rwtranslator.ui.fragment.ConfigTranslatorFragment;
import com.eam.rwtranslator.ui.fragment.ConfigLLMTranslatorFragment;
import com.eam.rwtranslator.ui.editor.SectionEditorActivity;
import com.eam.rwtranslator.ui.setting.AppSettings;
import com.eam.rwtranslator.ui.setting.SettingActivity;
import com.eam.rwtranslator.ui.common.UniversalMultiSelectManager;
import com.eam.rwtranslator.ui.common.MultiSelectDecorator;
import com.eam.rwtranslator.ui.common.RecyclerViewMultiSelectAdapter;
import com.eam.rwtranslator.utils.DialogUtils;
import com.eam.rwtranslator.utils.FilesHandler;
import com.eam.rwtranslator.utils.PopupUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import timber.log.Timber;

public class IniFileManagerActivity extends AppCompatActivity {
  public static String Download_PATH;
  private static final int REQUEST_CODE_DOUBLELIST_ACT = 1;
  private MaterialCardView ImgInverseSelection,
          ImgSelectAll,
      ImgMultiSelect,
      ImgTranslate,
      ImgLLMTranslate;
  private HashSet<String> modifiedList;
  Context context = this;
  int ClickItem;
  SharedPreferences sharedpreferences;
  LinkedList<String> allTran;
  List<IniFileModel> inilistdatalist;
  SwipeRecyclerView list;
  IniFileManagerAdapter adapter;
  String clickdir;
  DialogUtils du;
  TranslationConfigManager project;
  Handler handler = new Handler(Looper.getMainLooper());
  MaterialToolbar projectManagerToolbar;
  RelativeLayout filterLayout;
  private UniversalMultiSelectManager multiSelectManager;
  private MultiSelectDecorator multiSelectDecorator;

  @SuppressLint("NotifyDataSetChanged")
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_CODE_DOUBLELIST_ACT: {
        if (data != null) {
          String returnDir = data.getStringExtra("returnDir");
          boolean isModified = data.getBooleanExtra("isModified", false);
          var iniFileModel = DataSet.getIniFileModel(returnDir);
          iniFileModel.setViewed(true);
          if (resultCode == RESULT_OK && isModified) {
            modifiedList.add(returnDir);
            iniFileModel.setModified(true);
          }
          adapter.notifyDataSetChanged();
        }
        break;
      }
      case REQUEST_CODE_DOCUMENT_TREE: {
        if (resultCode == RESULT_OK && data != null) {
          Uri uri = data.getData();
          if (uri != null) {
            // 获取持久化权限
            getContentResolver().takePersistableUriPermission(uri, 
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            showMsg(getString(R.string.project_act_document_permission_granted));
            // 权限获取成功后执行导出
            performExportWithDocumentApi();
          }
        } else {
          showMsg(getString(R.string.project_act_document_permission_denied));
        }
        break;
      }
    }
  }
   
   private String getPathFromUri(Uri uri) {

     try {
       String docId = DocumentsContract.getTreeDocumentId(uri);
       String[] split = docId.split(":");
       if (split.length >= 2) {
         String type = split[0];
         if ("primary".equalsIgnoreCase(type)) {
           return "/storage/emulated/0/" + split[1];
         }
       }
     } catch (Exception e) {
       Timber.e(e, "Error parsing URI path");
     }
     return null;
   }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_act_layout);
    initView();
    initData();
    setListeners();
  }

    @Override
    protected void onResume() {
        super.onResume();

        if(!TranslationConfigManager.errorFiles.isEmpty()){
            showErrorFilesDialog();
        }
    }
    private void showErrorFilesDialog() {
        new Handler(context.getMainLooper()).post(() -> {
            // 检查是否有错误需要显示
            boolean hasConfigErrors = !TranslationConfigManager.errorFiles.isEmpty();
            boolean hasTranslationErrors = !IniFileManagerAdapter.translationErrors.isEmpty();
            
            if (!hasConfigErrors && !hasTranslationErrors) {
                return;
            }
            
            // 构建异常文件列表信息
            StringBuilder errorMessage = new StringBuilder();
            int[] index = {1};
            
            // 处理配置加载错误
            if (hasConfigErrors) {
                errorMessage.append("=== Configuration Loading Errors ===\n\n");
                String formatStr = " \n    File: %s\n    Exception: %s";
                TranslationConfigManager.errorFiles.forEach((key, value) -> {
                    errorMessage.append("Error #").append(index[0]++).append(String.format(formatStr, key.getAbsolutePath(), value.getMessage()));
                    errorMessage.append("\n");
                });
                errorMessage.append("\n");
            }
            
            // 处理翻译错误
            if (hasTranslationErrors) {
                errorMessage.append("=== Translation Errors ===\n\n");
                synchronized (IniFileManagerAdapter.translationErrors) {
                    for (IniFileManagerAdapter.TranslationError error : IniFileManagerAdapter.translationErrors) {
                        errorMessage.append("Error #").append(index[0]++).append(" \n");
                        errorMessage.append("    File: ").append(error.filePath()).append("\n");
                        errorMessage.append("    Section: ").append(error.sectionName()).append("\n");
                        errorMessage.append("    Key: ").append(error.keyName()).append("\n");
                        errorMessage.append("    Original Value: ").append(error.originalValue()).append("\n");
                        errorMessage.append("    Exception: ").append(error.error() != null ? error.error().getMessage() : "Unknown error").append("\n");
                        errorMessage.append("\n");
                    }
                }
            }

            // 创建并显示对话框
            DialogUtils dialogUtils = new DialogUtils(context);
            dialogUtils.createSimpleDialog(
                    getString(R.string.exception_occurred),
                    errorMessage.toString(),
                    (dialog, which) -> {
                        dialog.dismiss();
                        TranslationConfigManager.errorFiles.clear();
                        IniFileManagerAdapter.translationErrors.clear();
                    },
                    (dialog,which)->{
                        dialog.dismiss();
                        TranslationConfigManager.errorFiles.clear();
                        IniFileManagerAdapter.translationErrors.clear();
                    }
            ).show();
        });
    }
    private void initView() {
    projectManagerToolbar = findViewById(R.id.inilist_toolbar);
    list = findViewById(R.id.inilistview);
    ImgInverseSelection = findViewById(R.id.inilist_cardview_inverse_selection);
    ImgSelectAll = findViewById(R.id.inilist_cardview_select_all);
    ImgMultiSelect = findViewById(R.id.inilist_cardview_multi_select);
    ImgTranslate = findViewById(R.id.inilist_cardview_translate);
    ImgLLMTranslate = findViewById(R.id.project_act_bottom_appbar_card_ai_translate);
  }

  private void initData() {
    Download_PATH =context.getResources().getString(R.string.setting_act_export_path_default_value) ;
    du = new DialogUtils(context);
    clickdir = getIntent().getStringExtra("clickdir");
    allTran = new LinkedList<>();
    modifiedList = new HashSet<>();
    inilistdatalist = DataSet.getIniListDatas();
    filterLayout = findViewById(R.id.filter_relativelayout);
    project = DataSet.getCurrentProject();
    sharedpreferences = getSharedPreferences(clickdir, Context.MODE_PRIVATE);
    adapter = new IniFileManagerAdapter(context, inilistdatalist);
    list.setLayoutManager(new LinearLayoutManager(this));
    
    // RecyclerView 性能优化配置
    list.setHasFixedSize(true); // 如果RecyclerView大小固定，提升性能
    list.setItemViewCacheSize(10); // 增加视图缓存大小
    list.setDrawingCacheEnabled(true); // 启用绘制缓存
    list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    
    // 初始化多选功能
    multiSelectDecorator = new MultiSelectDecorator(this);
    
    // 创建适配器包装器
    RecyclerViewMultiSelectAdapter adapterWrapper = new RecyclerViewMultiSelectAdapter(adapter);
    
    // 初始化多选管理器
    multiSelectManager = new UniversalMultiSelectManager(adapterWrapper, multiSelectDecorator, projectManagerToolbar, ImgMultiSelect,
        new UniversalMultiSelectManager.OnMultiSelectListener() {
          @Override
          public void onEnterMultiSelectMode() {
            // 进入多选模式时的回调
          }
          
          @Override
          public void onExitMultiSelectMode() {
            // 退出多选模式时的回调
          }
        });
    
    // 设置选中状态变化监听器
    multiSelectManager.setOnSelectionChangedListener(new UniversalMultiSelectManager.OnSelectionChangedListener() {
      @Override
      public void onSelectionChanged(int selectedCount, boolean isItemSelected) {
        // 选中项数量变化时的回调
      }
    });
    
    adapter.setMultiSelectManager(multiSelectManager);
    
    // 设置翻译完成监听器
    adapter.setOnTranslationCompleteListener(new IniFileManagerAdapter.OnTranslationCompleteListener() {
      @SuppressLint("NotifyDataSetChanged")
      @Override
      public void onTranslationComplete(List<IniFileModel> translatedFiles) {
        // 将翻译完成的文件自动标记
        for (IniFileModel file : translatedFiles) {
          file.setViewed(true);
          modifiedList.add(file.getRwini().getFile().getPath());
        }
        // 刷新适配器以显示标记状态
        adapter.notifyDataSetChanged();
        
        if (!IniFileManagerAdapter.translationErrors.isEmpty()) {
          // 显示错误对话框
          showErrorFilesDialog();
        } else {
          // 显示翻译完成消息
          showMsg(translatedFiles.size() + " files translated and marked");
        }
      }
    });
    
    // 设置文件标记监听器
    adapter.setOnFileMarkListener(new IniFileManagerAdapter.OnFileMarkListener() {
      @Override
      public void onFileMark(IniFileModel file, boolean isMarked) {
        if (isMarked) {
          modifiedList.add(file.getRwini().getFile().getPath());
        } else {
          modifiedList.remove(file.getRwini().getFile().getPath());
        }
      }
    });
    
    list.addItemDecoration(multiSelectDecorator);
    
    setSwipeMenu();
    setSwipeMenuListener();
    list.setAdapter(adapter);
    projectManagerToolbar.setTitle(clickdir);
    projectManagerToolbar.setSubtitle("0/" + inilistdatalist.size());
  }

  private void setListeners() {
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                // 如果在多选模式下，先退出多选模式
                if (multiSelectManager != null && multiSelectManager.isMultiSelectMode()) {
      multiSelectManager.exitMultiSelectMode();
    } else {
                  ensureExit();
                }
              }
            });
    BottomAppBar bottomAppBar = findViewById(R.id.inilist_bottom_app_bar);
    ViewTreeObserver vto = bottomAppBar.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            bottomAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            int bottomAppBarHeight = bottomAppBar.getHeight();
            NestedScrollView nsv = findViewById(R.id.inilist_NestedScrollView);
            nsv.setPadding(0, 0, 0, bottomAppBarHeight);
          }
        });
    adapter.setOnItemClickListener(
        p1 -> {
          inilistdatalist.get(p1).setViewed(true);
          ClickItem = p1;
          Intent intent =
              new Intent(IniFileManagerActivity.this, SectionEditorActivity.class);
          intent.putExtra("ClickDatadir", adapter.getItem(p1).getRwini().getFile().getPath());
          startActivityForResult(intent, REQUEST_CODE_DOUBLELIST_ACT);
        });
    projectManagerToolbar.setNavigationOnClickListener(v -> ensureExit());
    projectManagerToolbar.setOnMenuItemClickListener(
        menuitem -> {
          int id = menuitem.getItemId();
          if (id == R.id.searchIni_item) {
            showMsg(getString(R.string.project_act_under_development));
          } else if (id == R.id.filter) {
            showFilterView();
          } else if (id == R.id.export) {
            exportRwmodFile();
          } else if (id == R.id.saveAll_item) {
            saveToIniFiles();
          } else if (id == R.id.setting) {
            startActivity(new Intent(context, SettingActivity.class));
          }
          return true;
        });
    ImgInverseSelection.setOnClickListener(v1 -> {
        multiSelectManager.inverseSelection();

    });

    ImgSelectAll.setOnClickListener(v2 -> {
        multiSelectManager.selectAll();
    });

    
    ImgTranslate.setOnClickListener(
        v5 -> {
          
          ConfigTranslatorFragment dialogFragment = new ConfigTranslatorFragment(context, adapter);
              dialogFragment.show(getSupportFragmentManager(), "translate_dialog");
        });
    ImgLLMTranslate.setOnClickListener(
        v5 -> {
          ConfigLLMTranslatorFragment dialogFragment = new ConfigLLMTranslatorFragment(context, adapter);
          dialogFragment.show(getSupportFragmentManager(), "llm_translate_dialog");
        });
  }

  private void setSwipeMenu() {
    int[] RmenuImgId = {R.drawable.restore, R.drawable.delete, R.drawable.information};
    SwipeMenuCreator mSwipeMenuCreator =
        new SwipeMenuCreator() {
          @Override
          public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
            for (int id : RmenuImgId) {
              SwipeMenuItem menuItem = new SwipeMenuItem(context);
              menuItem.setWidth(AppConfig.dp2px(40));
              menuItem.setImage(id);
              rightMenu.addMenuItem(menuItem);
            }
          }
        };
    list.setSwipeMenuCreator(mSwipeMenuCreator);
  }

  private void setSwipeMenuListener() {
    OnItemMenuClickListener mItemMenuClickListener =
        new OnItemMenuClickListener() {
          // @param position:SwipeMenuBridge在RecyclerView的索引
          @Override
          public void onItemClick(SwipeMenuBridge menuBridge, int position) {
            // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
            menuBridge.closeMenu();
            int dir = menuBridge.getDirection(); // -1代表右菜单 1代表左菜单
            int menuPosition = menuBridge.getPosition();
            IniFileModel data = adapter.getItem(position);
            File file = data.getRwini().getFile();
            if (dir == -1) {
              // 菜单在Item中的Position：
              switch (menuPosition) {
                case 0 -> {
                  InputMethodManager imm =
                      (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                   var layout= LayoutInflater.from(context).inflate(R.layout.edittext,null);        
                  TextInputLayout textinputlayout = layout.findViewById(R.id.edittext_textinputlayout);
                  TextInputEditText ed =layout.findViewById(R.id.edittext_textinput);
                  ed.addTextChangedListener(
                      new TextWatcher() {
                        @Override
                        public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(
                            CharSequence s, int start, int before, int count) {}

                        @Override
                        public void afterTextChanged(Editable s) {
                          if (s.toString().isEmpty()) {
                            textinputlayout.setError(getString(R.string.project_act_input_empty_error));
                          } else {
                            textinputlayout.setError(null);
                          }
                        }
                      });
                  ed.post(
                      () -> {
                        ed.requestFocus();
                        ed.setSelection(0, file.getName().lastIndexOf('.'));
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                      });
                 ed.setText(file.getName());           
                 var builder=new MaterialAlertDialogBuilder(context);
                  builder.setTitle(R.string.project_act_rename_title);
                  builder.setView(layout);          
                  builder.setPositiveButton(R.string.positive_button,(dialog, which) -> {
                        dialog.dismiss();
                        imm.hideSoftInputFromWindow(ed.getWindowToken(), 0);
                        String newName = ed.getText().toString();
                        File newfile = new File(file.getParentFile(), newName);
                        file.renameTo(newfile);
                        // 更新缓存文件
                        try {
                          data.getRwini().setFile(newfile);
                          DataSet.getCurrentProject().serialize();
                        } catch (Exception err) {
                          Timber.e(err);
                          err.printStackTrace();
                        }
                        // 更新UI
                        data.setIniname(newName);
                        adapter.notifyItemChanged(position);
                      });          
                  builder.create().show();                               
                }
                case 1 -> {
                  du.createSimpleDialog(
                      getString(R.string.project_act_delete_title),
                      getString(R.string.project_act_delete_message),
                      (dialog, which) -> {
                        dialog.dismiss();
                        file.delete();
                        showMsg(getString(R.string.project_act_deleted));
                        // 更新缓存文件
                        try {
                          DataSet.getCurrentProject().getTranslationIniFiles().remove(data.getRwini());
                          DataSet.getCurrentProject().serialize();
                        } catch (Exception err) {
                          Timber.e(err);
                          err.printStackTrace();
                        }
                        // 更新UI
                        adapter.getMData().remove(data);
                        adapter.notifyItemRemoved(position);
                      }).show();
                }
                case 2 -> {
                  MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                  View v = LayoutInflater.from(context).inflate(R.layout.inifile_info, null);
                  TextView tv1 = v.findViewById(R.id.tv_dir),
                      tv2 = v.findViewById(R.id.tv_size),
                      tv3 = v.findViewById(R.id.tv_time);
                  tv1.setText(file.getPath());
                  tv1.setOnClickListener(
                      v5 -> {
                        ClipboardManager clipboard =
                            (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", tv1.getText());
                        clipboard.setPrimaryClip(clip);
                        showMsg(getString(R.string.project_act_copied_clipboard));
                      });
                  tv2.setText(FilesHandler.getFileSizeString(file));
                  long lastModified = file.lastModified();
                  Date date = new Date(lastModified);
                  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                  String formattedDate = sdf.format(date);
                  tv3.setText(formattedDate);
                  builder.setTitle(R.string.project_act_file_info);
                  builder.setView(v);
                  builder.setPositiveButton(R.string.positive_button, null);
                  builder.create();
                  builder.show();
                }
              }
            }
          }
        };
    list.setOnItemMenuClickListener(mItemMenuClickListener);
  }

  private void showFilterView() {

    ImageButton crosscutting = filterLayout.findViewById(R.id.filter_crossButton);
    EditText filter_edit = findViewById(R.id.filter_editText);
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    var menu = projectManagerToolbar.getMenu();
    var item1 = menu.findItem(R.id.filter);
    var item2 = menu.findItem(R.id.saveAll_item);
    crosscutting.setOnClickListener(
        v -> {
          item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
          item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
          filterLayout.setVisibility(View.GONE);
          filter_edit.setText("");
          adapter.filterIniFileModel("");
          imm.hideSoftInputFromWindow(filter_edit.getWindowToken(), 0);
        });
    filterLayout.setVisibility(View.VISIBLE);
    filter_edit.post(
        () -> {
          item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
          item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
          filter_edit.requestFocus();
          imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        });
    filter_edit.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filterIniFileModel(s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }

  private void showEmbeddingTranDialog() {
    View v = LayoutInflater.from(context).inflate(R.layout.embedded_tran_dialogview, null);
    ListView list = v.findViewById(R.id.embedded_tran_listview);
    list.setAdapter(
        new ArrayAdapter<>(
            context,
            android.R.layout.simple_list_item_1,
            Objects.requireNonNull(
                AppConfig.externalProjectDir.list((dir, name) -> !name.equals(clickdir)))));
    list.setOnItemClickListener(
        (parent, v3, postion, v5) -> {
          new Thread(
                  () -> {
                    try {
                      FilesHandler.EmbeddingTran(
                          parent.getItemAtPosition(postion).toString(),
                          AppSettings.getReplace_Lang());
                      IniFileManagerActivity.this.runOnUiThread(() -> showMsg("Operation successful!"));
                    } catch (Exception err) {
                      err.printStackTrace();
                      IniFileManagerActivity.this.runOnUiThread(
                          () -> showMsg(getString(R.string.project_act_operation_failed, err.toString())));
                    } finally {
                      IniFileManagerActivity.this.runOnUiThread(du::dismissLoadingDialog);
                    }
                  })
              .start();
          du.createLoadingDialog().show();
        });
    new MaterialAlertDialogBuilder(context)
        .setTitle(R.string.project_act_old_module_dialog_title)
        .setView(v)
        .setNegativeButton(R.string.negative_button, null)
        .create()
        .show();
  }

  public void saveToIniFiles() {
    new Thread(
            () -> {
              boolean[] allSuccess = {true};
              modifiedList
                  .forEach(
                      i -> {
                        Map<String, Map<String, String>> map = new HashMap<>();
                        IniFileModel key = DataSet.getIniFileModel(i);
                        key.getData()
                            .forEach(
                                doubleListGroup -> {
                                  String sectionName = doubleListGroup.getName();
                                  doubleListGroup
                                      .getItems()
                                      .forEach(
                                          item -> {
                                            var submap =
                                                map.computeIfAbsent(
                                                    sectionName, k -> new HashMap<>());
                                            submap.putIfAbsent(
                                                item.getKey().getKeyName(), item.getOri_val());
                                            for (var entry : item.getLang_pairs().entrySet()) {
                                              submap.putIfAbsent(
                                                  item.getKey().getKeyName() + "_" + entry.getKey(),
                                                  entry.getValue());
                                            }
                                          });
                                });
                        if (!project.setPairs(i, map)) {
                            allSuccess[0] = false;
                        }
                          key.setModified(false);
                      });
              handler.post(
                  () -> {
                    if (allSuccess[0]) {
                      showMsg(getString(R.string.project_act_saved_files, modifiedList.size()));
                    } else {
                      showMsg(getString(R.string.project_act_operation_failed));
                    }
                    modifiedList.clear();
                    adapter.notifyDataSetChanged();
                    du.dismissLoadingDialog();
                  });

              try {
                project.serialize();
              } catch (Exception err) {
                err.printStackTrace();
                 handler.post(()-> {
                     var message = err.getMessage();
                     if(message!=null) {
                         showMsg(message);
                     }
                 });
                Timber.e(err);
              }
            })
        .start();
  }

  private static final int REQUEST_CODE_DOCUMENT_TREE = 1001;
  
  private void exportRwmodFile() {
    String customExportPath = getCustomExportPath();
    if (customExportPath.equals(Download_PATH)) {
      // 使用默认Downloads目录，直接用File类操作
      performExportToDownloads();
    } else {
      // 使用自定义路径，需要Document权限
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11+需要检查Document权限
        if (hasDocumentTreePermission(customExportPath)) {
          performExportWithDocumentApi();
        } else {
          requestDocumentTreePermission();
        }
      } else {
        // Android 10及以下直接导出
        performExport();
      }
    }
  }
  
  private String getCustomExportPath() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    return prefs.getString("custom_export_path", Download_PATH);
  }
  
  private boolean hasDocumentTreePermission(String path) {
    try {
      List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
      for (UriPermission permission : permissions) {
        if (permission.isWritePermission()) {
          DocumentFile documentFile = DocumentFile.fromTreeUri(this, permission.getUri());
          if (documentFile != null && documentFile.exists()) {
            String documentPath = getPathFromUri(permission.getUri());
            if (documentPath != null && path.startsWith(documentPath)) {
              return true;
            }
          }
        }
      }
    } catch (Exception e) {
      Timber.e(e, "Error checking document permissions");
    }
    return false;
  }
  
  private void requestDocumentTreePermission() {
    String customExportPath = getCustomExportPath();
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    try {
      Uri initialUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", 
          "primary:" + customExportPath.replace("/storage/emulated/0/", ""));
      intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
    } catch (Exception e) {
      Timber.e(e, "Error setting initial URI");
    }
    startActivityForResult(intent, REQUEST_CODE_DOCUMENT_TREE);
  }
  
  private void performExportWithDocumentApi() {
    String customExportPath = getCustomExportPath();
    try {
      List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
      DocumentFile targetDir = null;
      
      for (UriPermission permission : permissions) {
        if (permission.isWritePermission()) {
          DocumentFile documentFile = DocumentFile.fromTreeUri(this, permission.getUri());
          if (documentFile != null && documentFile.exists()) {
            String documentPath = getPathFromUri(permission.getUri());
            if (documentPath != null && customExportPath.startsWith(documentPath)) {
              targetDir = documentFile;
              break;
            }
          }
        }
      }
      
      if (targetDir == null) {
        showMsg(getString(R.string.project_act_document_permission_denied));
        return;
      }
      
      String fileName = clickdir + ".rwmod";
      DocumentFile existingFile = targetDir.findFile(fileName);
      if (existingFile != null) {
        existingFile.delete();
      }
      
      DocumentFile newFile = targetDir.createFile("application/octet-stream", fileName);
       if (newFile == null) {
          showMsg(getString(R.string.project_act_export_failed, getString(R.string.project_act_export_create_file_failed)));
          return;
        }
      
      du.createLoadingDialog(getString(R.string.project_act_exporting)).show();
      Handler handler = new Handler(Looper.getMainLooper());
      new Thread(() -> {
        try {
          OutputStream outputStream = getContentResolver().openOutputStream(newFile.getUri());
          if (outputStream != null) {
            FilesHandler.compressFolder(
                context,
                AppConfig.externalProjectDir + "/" + clickdir,
                outputStream);
            outputStream.close();
            handler.post(() -> {
              showMsg(getString(R.string.project_act_export_success, customExportPath));
              du.dismissLoadingDialog();
            });
          } else {
             handler.post(() -> {
                showMsg(getString(R.string.project_act_export_failed, getString(R.string.project_act_export_open_stream_failed)));
                du.dismissLoadingDialog();
              });
           }
        } catch (IOException e) {
          e.printStackTrace();
          Timber.e(e);
          handler.post(() -> {
            showMsg(getString(R.string.project_act_export_failed, e.getMessage()));
            du.dismissLoadingDialog();
          });
        }
      }).start();
      
    } catch (Exception e) {
      e.printStackTrace();
      Timber.e(e);
      showMsg(getString(R.string.project_act_export_failed, e.getMessage()));
    }
  }
  
  private void performExportToDownloads() {
    File downloads_dir = new File(Download_PATH);
    if (!downloads_dir.exists()) downloads_dir.mkdirs();
    File doucument = new File(downloads_dir, clickdir + ".rwmod");
    du.createLoadingDialog(getString(R.string.project_act_exporting)).show();
    Handler handler = new Handler(Looper.getMainLooper());
    new Thread(
            () -> {
              try {
                FilesHandler.compressFolder(
                    context,
                    AppConfig.externalProjectDir + "/" + clickdir,
                    new FileOutputStream(doucument));
                handler.post(
                    () -> {
                      showMsg(getString(R.string.project_act_export_success, Download_PATH));
                      du.dismissLoadingDialog();
                    });
              } catch (IOException e) {
                e.printStackTrace();
                Timber.e(e);
                handler.post(
                    () -> {
                      showMsg(getString(R.string.project_act_export_failed, e.getMessage()));
                      du.dismissLoadingDialog();
                    });
              }
            })
        .start();
  }
  

  
  private void performExport() {
    String customExportPath = getCustomExportPath();
    File export_dir = new File(customExportPath);
    if (!export_dir.exists()) export_dir.mkdirs();
    File doucument = new File(export_dir, clickdir + ".rwmod");
    du.createLoadingDialog(getString(R.string.project_act_exporting)).show();
    Handler handler = new Handler(Looper.getMainLooper());
    new Thread(
            () -> {
              try {
                FilesHandler.compressFolder(
                    context,
                    AppConfig.externalProjectDir + "/" + clickdir,
                    new FileOutputStream(doucument));
                handler.post(
                    () -> {
                      showMsg(getString(R.string.project_act_export_success, customExportPath));
                      du.dismissLoadingDialog();
                    });
              } catch (IOException e) {
                e.printStackTrace();
                Timber.e(e);
                handler.post(
                    () -> {
                      showMsg(getString(R.string.project_act_export_failed, e.getMessage()));
                      du.dismissLoadingDialog();
                    });
              }
            })
        .start();
  }

  @Override
  protected void onDestroy() {
    ensureExit();
    super.onDestroy();
  }

  public void showMsg(String s) {

    Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show();
  }

  private void saveData() {
    SharedPreferences.Editor editor = sharedpreferences.edit();
    editor.putInt("curTranInterface_flag", PopupUtils.curTranInterface_flag);
    editor.commit();

  }

  private void openFile(File file) {
    Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(uri, "text/plain"); // 这里的MIME类型取决于文件类型
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(intent);
    }
  }

  private String getRightPath(String path) {
    File f = new File(AppConfig.externalProjectDir, path);
    File[] list = f.listFiles();
    if (list.length == 1 && list[0].isDirectory()) return list[0].getAbsolutePath();
    else return path;
  }

  /**
   * 获取选中的文件列表
   * @return 选中的IniFileModel列表
   */
  public List<IniFileModel> getSelectedFiles() {
    if (adapter != null) {
      return adapter.getSelectedItems();
    }
    return new ArrayList<>();
  }
  
  /**
   * 获取选中文件的数量
   * @return 选中文件数量
   */
  public int getSelectedCount() {
    if (multiSelectManager != null) {
      return multiSelectManager.getSelectedCount();
    }
    return 0;
  }

  private void ensureExit() {
    if (!modifiedList.isEmpty())
      new MaterialAlertDialogBuilder(context)
          .setTitle(clickdir)
          .setMessage(R.string.project_act_unsaved_message)
          .setPositiveButton(
              R.string.project_act_save_exit,
              (v1, v2) -> {
                saveData();
                saveToIniFiles();
                finish();
              })
          .setNegativeButton(R.string.project_act_discard, (v1, v2) -> finish())
          .create()
          .show();
    else finish();
  }
}

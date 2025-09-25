package com.eam.rwtranslator.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eam.rwtranslator.data.model.DataSet;
import com.eam.rwtranslator.data.model.SectionModel;
import com.eam.rwtranslator.data.model.IniFileModel;
import com.eam.rwtranslator.R;
import com.eam.rwtranslator.ui.fragment.ConfigLLMTranslatorFragment;
import com.eam.rwtranslator.ui.fragment.ConfigTranslatorFragment;
import com.eam.rwtranslator.utils.FilesHandler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.LinkedList;

import timber.log.Timber;

public class SectionEditorActivity extends AppCompatActivity
    implements ConfigTextFragmentAdapter.doubleListActCallBack {
    public static Context mcontext;
    private SectionEditorAdapter madapter;
    private ActivityResultLauncher<Intent> extendlauncher;
    private MaterialCardView imgSelectAll, imgInverseSelection, imgTranslate, imgLLMTranslate;
    private ExpandableListView list;
    private IniFileModel inilistdata;
    private boolean isModified = false;
    private MaterialToolbar toolbar;
    private ConfigTextFragmentAdapter tAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_editor_content);
        initView();
        initListeners();
    }

    private void initView() {
        mcontext = this;
        inilistdata = DataSet.getIniFileModel(getIntent().getStringExtra("ClickDatadir"));
        imgSelectAll = findViewById(R.id.cardview_seclect_all);
        imgInverseSelection = findViewById(R.id.cardview_inverse_selection);
        imgTranslate = findViewById(R.id.cardview_translate);
        imgLLMTranslate = findViewById(R.id.section_act_bottom_appbar_card_ai_translate);
        list = findViewById(R.id.list);
        toolbar = findViewById(R.id.doublelist_toolbar);
        toolbar.setTitle(FilesHandler.getBaseName(inilistdata.getRwini().getFile().getName()));
        ImageView multiselectButton = findViewById(R.id.imageview_multi_select);
        madapter =
                new SectionEditorAdapter(
                        mcontext,
                        inilistdata.getData(),
                        findViewById(R.id.cardview_multi_select),
                        toolbar,
                        list
                );

        toolbar.setSubtitle("0/" + madapter.getFlatCount());
        list.setAdapter(madapter);
        ExpandAll();
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        ViewTreeObserver vto = bottomAppBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        bottomAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int bottomAppBarHeight = bottomAppBar.getHeight();
                        list.setPadding(0, 0, 0, bottomAppBarHeight);
                    }
                });
    }

    private void initListeners() {
        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                returnData();
                                finish();
                            }
                        });
        extendlauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> tAdapter.updateEditText(result));
        madapter.setOnItemClickListener(
                (parentPosition, childPosition) -> {
                    View dialogview = LayoutInflater.from(mcontext).inflate(R.layout.translater_dialog, null);
                    var item =
                            (SectionModel.Pair) madapter.getChild(parentPosition, childPosition);
                    RecyclerView lv = dialogview.findViewById(R.id.translater_rv);
                    tAdapter = new ConfigTextFragmentAdapter(item, mcontext, SectionEditorActivity.this);
                    ConfigTextFragment dialog =
                            new ConfigTextFragment(mcontext, dialogview, item, tAdapter, madapter);
                    lv.setLayoutManager(new LinearLayoutManager(this));
                    lv.setAdapter(tAdapter);
                    dialog.show(getSupportFragmentManager(), "tr");
                });

        imgSelectAll.setOnClickListener(
                v3 -> {
                    madapter.selectAll();
                });
        imgInverseSelection.setOnClickListener(
                v4 -> {
                    madapter.inverseSelection();
                });
        imgLLMTranslate.setOnClickListener(
                v5 -> {
                    ConfigLLMTranslatorFragment dialogFragment = new ConfigLLMTranslatorFragment(mcontext, madapter);
                    dialogFragment.show(getSupportFragmentManager(), "llm_translate_dialog");
                });

        imgTranslate.setOnClickListener(
                v -> {
                    ConfigTranslatorFragment dialogFragment = new ConfigTranslatorFragment(mcontext, madapter);
                    dialogFragment.show(getSupportFragmentManager(), "translate_dialog");
                });

        toolbar.setOnMenuItemClickListener(
                menuitem -> {
                    final var id = menuitem.getItemId();
                    if (id == R.id.doublelist_menuitem_expand_all) {
                        ExpandAll();
                    } else if (id == R.id.doublelist_menuitem_collapse_all) {
                        CollapseAll();
                    }
                    return true;
                });
        toolbar.setNavigationOnClickListener(
                v -> {
                    returnData();
                    finish();
                });
    }

    @Override
    public void setPairEntry(
            TextInputLayout layout, TextInputEditText valueEditText, Integer index, String key) {
        layout.setEndIconOnClickListener(
                v -> {
                    Intent i = new Intent(mcontext, ExtendEditorActivity.class);
                    i.putExtra("key", key);
                    i.putExtra("index", index);
                    i.putExtra("tran", valueEditText.getText().toString());
                    extendlauncher.launch(i);
                });
    }

    private void ExpandAll() {
        int count = madapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            list.expandGroup(i);
        }
    }

    private void CollapseAll() {
        int count = madapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            list.collapseGroup(i);
        }
    }

    public void returnData() {
        Intent resultIntent = new Intent();
        Timber.d("isModified:%b", isModified);
        resultIntent.putExtra("returnDir", inilistdata.getRwini().getFile().getPath());
        resultIntent.putExtra("isModified", isModified);
        if (isModified) {
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED, resultIntent);
        }
    }

    public void showMsg(String msg) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Set the modification status of the current editing session
     *
     * @param modified true if content has been modified, false otherwise
     */
    public void setModified(boolean modified) {
        this.isModified = modified;
        Timber.d("Modification status set to: %b", modified);
    }
}

package com.eam.rwtranslator.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.eam.rwtranslator.R;
import com.eam.rwtranslator.ui.setting.AppSettings;
import com.eam.rwtranslator.utils.LanguageVariant;
import com.eam.rwtranslator.utils.LanguageCodeHelper;
import com.eam.rwtranslator.utils.Translator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class ConfigTranslatorFragment extends DialogFragment {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ViewGroup mContainer;
    private Spinner sp1;
    private AutoCompleteTextView targetLanguageAutoComplete;
    /*
    *  index  ->   data
    *  0-> 翻译引擎的spinner的索引值
    *  1-> 目标语言的spinner的索引值
    * */
    private final int[] selectedIndex = new int[2];
    private Translator.TranslateTaskCallBack callback;
    public ConfigTranslatorFragment(Context context, Translator.TranslateTaskCallBack callback) {
        this.context = context;
        this.callback = callback;
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = context.getSharedPreferences("translate_dialog", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    @MainThread
    @Nullable
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;
        return null;
    }

    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view =
                getLayoutInflater().inflate(R.layout.config_translator_content, mContainer, false);
        sp1 = view.findViewById(R.id.selscttranebgine_spinner1);
        targetLanguageAutoComplete = view.findViewById(R.id.target_language_autocomplete);
        setupEngines();
        setupLanguageAutoComplete();
        // 初始化Spinner选择状态
        selectedIndex[0] = sharedPreferences.getInt("translator_provider_marker", 0);
        selectedIndex[1] = sharedPreferences.getInt("to_langage_marker", 1);
        // 设置Spinner默认选项
        sp1.setSelection(selectedIndex[0]);
        
        // 设置AutoCompleteTextView默认值
        String savedLanguageCode = sharedPreferences.getString("target_language_code", "en");
        LanguageCodeHelper.LanguageInfo savedLanguage = LanguageCodeHelper.getLanguageByCode(savedLanguageCode);
        if (savedLanguage != null) {
            targetLanguageAutoComplete.setText(savedLanguage.toString());
        }

        // 设置Spinner监听器
        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // 设置AutoCompleteTextView监听器
        targetLanguageAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            String languageCode = LanguageCodeHelper.getLanguageCodeFromDisplayText(selectedText);
            if (languageCode != null) {
                List<String> suffixes = new ArrayList<>();
                for (LanguageVariant lang : LanguageVariant.values()) {
                    suffixes.add(lang.getSuffix());
                }
                int index = suffixes.indexOf(languageCode);
                selectedIndex[1] = index >= 0 ? index : 1; // 如果找不到，默认为1
            }
        });
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.project_act_config_translator_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(
                R.string.positive_button,
                (dialog, which) -> {
                    dialog.dismiss();
                    // 从AutoCompleteTextView获取语言代码
                    String selectedText = targetLanguageAutoComplete.getText().toString();
                    String languageCode = LanguageCodeHelper.getLanguageCodeFromDisplayText(selectedText);
                    
                    if (languageCode == null || languageCode.isEmpty()) {
                        // 如果没有选择有效的语言代码，尝试直接使用输入的文本作为语言代码
                        languageCode = selectedText.trim().toLowerCase();
                        if (!LanguageCodeHelper.isValidLanguageCode(languageCode)) {
                            languageCode = "en"; // 默认使用英语
                        }
                    }
                    
                    // 设置语言配置，from language使用auto
                    AppSettings.CurrentFromLanguage = "auto";
                    AppSettings.CurrentTargetLanguage = languageCode;
                    setPreferences();
                    AppSettings.translationProvider = Translator.getTranslatorCodeByIndex(selectedIndex[0]);
                    callback.onTranslate(false);
                });
        builder.setNegativeButton(R.string.negative_button, (d,w)->setPreferences());

        return builder.create();
    }

    private void setPreferences() {
        editor.putInt("translator_provider_marker", selectedIndex[0]);
        editor.putInt("to_langage_marker", selectedIndex[1]);
        
        // 保存语言代码
        String selectedText = targetLanguageAutoComplete.getText().toString();
        String languageCode = LanguageCodeHelper.getLanguageCodeFromDisplayText(selectedText);
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = selectedText.trim().toLowerCase();
            if (!LanguageCodeHelper.isValidLanguageCode(languageCode)) {
                languageCode = "en";
            }
        }
        editor.putString("target_language_code", languageCode);
        editor.apply();
    }

    private void setupEngines() {
        ArrayAdapter<String> adapter= new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.select_engine));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(adapter);
        sp1.setSelection(sharedPreferences.getInt("translator_provider_marker", 0));
    }

    private void setupLanguageAutoComplete() {
        // 设置AutoCompleteTextView的适配器
        List<String> languageDisplayList = new ArrayList<>();
        for (LanguageCodeHelper.LanguageInfo info : LanguageCodeHelper.getAllLanguages()) {
            languageDisplayList.add(info.toString());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context, 
            android.R.layout.simple_dropdown_item_1line, 
            languageDisplayList
        );
        
        targetLanguageAutoComplete.setAdapter(adapter);
        targetLanguageAutoComplete.setThreshold(1);
    }

}

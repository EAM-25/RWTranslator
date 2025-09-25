package com.eam.rwtranslator.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.eam.rwtranslator.R;
import com.eam.rwtranslator.ui.setting.AppSettings;
import com.eam.rwtranslator.utils.LanguageVariant;
import com.eam.rwtranslator.utils.LanguageCodeHelper;
import com.eam.rwtranslator.utils.Translator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigLLMTranslatorFragment extends DialogFragment {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ViewGroup mContainer;
    private Spinner modelSpinner;
    private AutoCompleteTextView targetLanguageAutoComplete;
    private TextInputEditText systemPromptEditText;
    /*
    *  index  ->   data
    *  0-> LLM模型的spinner的索引值
    *  1-> 目标语言的spinner的索引值
    * */
    private int[] selectedIndex = new int[2];
    private Translator.TranslateTaskCallBack callback;
    
    public ConfigLLMTranslatorFragment(Context context, Translator.TranslateTaskCallBack callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = context.getSharedPreferences("llm_translate_dialog", Context.MODE_PRIVATE);
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

    @SuppressLint("MissingInflatedId")
    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view =
                getLayoutInflater().inflate(R.layout.config_llm_translator_content, mContainer, false);
        modelSpinner = view.findViewById(R.id.llm_model_spinner);
        targetLanguageAutoComplete = view.findViewById(R.id.target_language_autocomplete);
        systemPromptEditText = view.findViewById(R.id.system_prompt_edit_text);
        
        setupModelSpinner();
        setupLanguageAutoComplete();
        
        // 初始化选择状态
        selectedIndex[0] = sharedPreferences.getInt("llm_model_marker", 0);
        selectedIndex[1] = sharedPreferences.getInt("llm_to_language_marker", 0);
        
        modelSpinner.setSelection(selectedIndex[0]);
        
        // 从SharedPreferences加载语言代码并设置到AutoCompleteTextView
        String savedLanguageCode = sharedPreferences.getString("llm_target_language_code", "en");
        LanguageCodeHelper.LanguageInfo languageInfo = LanguageCodeHelper.getLanguageByCode(savedLanguageCode);
        if (languageInfo != null) {
            targetLanguageAutoComplete.setText(languageInfo.toString(), false);
        } else {
            targetLanguageAutoComplete.setText(savedLanguageCode, false);
        }
        
        // 加载系统提示词
        String defaultPrompt = AppSettings.DefaultLLMSystemPrompt;
        String savedPrompt = sharedPreferences.getString("llm_system_prompt", defaultPrompt);
        systemPromptEditText.setText(savedPrompt);
        
        // 设置监听器
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        targetLanguageAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            String languageCode = LanguageCodeHelper.getLanguageCodeFromDisplayText(selectedText);
            if (languageCode != null) {
                // 更新selectedIndex[1]
                for (int i = 0; i < LanguageVariant.values().length; i++) {
                    if (LanguageVariant.values()[i].getSuffix().equals(languageCode)) {
                        selectedIndex[1] = i;
                        break;
                    }
                }
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.project_act_config_translator_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(
                R.string.positive_button,
                (dialog, which) -> {
                    //检测系统提示词格式
                    var text=systemPromptEditText.getText();
                    if (TextUtils.indexOf(text,"{target_language}")==-1) {
                        Toast.makeText(context, R.string.project_act_target_language_identifier_is_not_find, Toast.LENGTH_SHORT).show();
                        return;
                    }
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
                    AppSettings.translationProvider = "llm";
                    callback.onTranslate(false);
                });
        builder.setNegativeButton(R.string.negative_button, (d,w)->setPreferences());

        Dialog dialog = builder.create();
        
        // 设置软键盘模式，防止EditText被输入法遮挡
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        
        return dialog;
    }

    private void setPreferences() {
        editor.putInt("llm_model_marker", selectedIndex[0]);
        editor.putInt("llm_to_language_marker", selectedIndex[1]);
        
        // 保存语言代码
        String selectedText = targetLanguageAutoComplete.getText().toString();
        String languageCode = LanguageCodeHelper.getLanguageCodeFromDisplayText(selectedText);
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = selectedText.trim().toLowerCase();
            if (!LanguageCodeHelper.isValidLanguageCode(languageCode)) {
                languageCode = "en";
            }
        }
        editor.putString("llm_target_language_code", languageCode);
        
        // 保存系统提示词
        String systemPrompt = systemPromptEditText.getText().toString();
        editor.putString("llm_system_prompt", systemPrompt);
        
        editor.apply();
    }

    private void setupModelSpinner() {
        // 设置LLM模型列表
        List<String> modelNames = Arrays.asList(
            "Gemini 2.0 Flash",
            "Gemini 2.0 Flash-Lite"
        );
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(
            context, android.R.layout.simple_spinner_item, modelNames);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(modelAdapter);
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
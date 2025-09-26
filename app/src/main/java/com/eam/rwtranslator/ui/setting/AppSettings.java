package com.eam.rwtranslator.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;

import com.eam.rwtranslator.utils.Translator;

import app.nekogram.translator.DeepLTranslator;

public class AppSettings {
    private static final String PREF_NAME = "AppSettings";
    // 翻译服务提供者
    public static String translationProvider = Translator.PROVIDER_GOOGLE;
    // DeepL翻译正式/非正式风格
    public static int deepLFormality = DeepLTranslator.FORMALITY_DEFAULT;
    // 当前源语言
    public static String CurrentFromLanguage = "en";
    // 当前目标语言
    public static String CurrentTargetLanguage = "zh";
    // LLM翻译默认系统提示词
    public static String DefaultLLMSystemPrompt = "You are a professional translation assistant. Translate the following text to {target_language}. Only output the translation without any additional text.";
    private static boolean isReplace_CB = false;
    private static int replace_Lang = 0;
    private static SharedPreferences preferences;
    public static Context context;
    public static void init(Context context) {
        AppSettings.context=context;
    	preferences=context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    public static boolean getIsReplace_CB() {
        return isReplace_CB;
    }

    public static void setIsReplace_CB(boolean isReplace_CB) {
        AppSettings.isReplace_CB = isReplace_CB;
    }

    public static int getReplace_Lang() {
        return replace_Lang;
    }

    public static void setReplace_Lang(int replace_Lang) {
        AppSettings.replace_Lang = replace_Lang;
    }
    public static void apply() {
        SharedPreferences.Editor editor=preferences.edit();
    	editor.putBoolean("isReplace_CB",isReplace_CB);
        editor.putInt("replace_Lang",replace_Lang);
        editor.apply();
    }
}

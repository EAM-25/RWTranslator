package com.eam.rwtranslator.utils;
import android.content.Context;

import androidx.annotation.Keep;

import com.eam.rwtranslator.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
public enum LanguageVariant {
    ZH("zh", R.string.project_act_zh_language_name, "Simplified Chinese"),
    RU("ru", R.string.project_act_ru_language_name, "Russian"),
    EN("en", R.string.project_act_en_language_name, "English"),
    JA("ja", R.string.project_act_ja_language_name, "Japanese"),
    DE("de", R.string.project_act_de_language_name, "German"),
    ES("es", R.string.project_act_es_language_name, "Spanish"),
    FR("fr", R.string.project_act_fr_language_name, "French"),
    PT("pt", R.string.project_act_pt_language_name, "Portuguese"),
    IT("it", R.string.project_act_it_language_name, "Italian"),
    NL("nl", R.string.project_act_nl_language_name, "Dutch"),
    TR("tr", R.string.project_act_tr_language_name, "Turkish"),
    PL("pl", R.string.project_act_pl_language_name, "Polish"),
    UK("uk", R.string.project_act_uk_language_name, "Ukrainian");

    private final String suffix;
    private final int languageNameResId;
    private final String englishName;

    LanguageVariant(String suffix, int languageNameResId, String englishName) {
        this.suffix = suffix;
        this.languageNameResId = languageNameResId;
        this.englishName = englishName;
    }

    // 新增：获取英文名称
    public String getEnglishName() {
        return englishName;
    }

    // 新增：获取语言代码到英文名称的映射表
    private static final Map<String, String> LANGUAGE_CODE_TO_ENGLISH_NAME;

    static {
        Map<String, String> map = new HashMap<>();
        for (LanguageVariant variant : values()) {
            map.put(variant.suffix.toLowerCase(), variant.englishName);
        }
        LANGUAGE_CODE_TO_ENGLISH_NAME = Collections.unmodifiableMap(map);
    }

    // 新增：根据语言代码获取英文名称
    public static String getEnglishNameByCode(String languageCode) {
        String englishName = LANGUAGE_CODE_TO_ENGLISH_NAME.get(languageCode.toLowerCase());
        return englishName != null ? englishName : languageCode;
    }
    public String getSuffix() {
        return this.suffix;
    }

    public String getLanguageName(Context context) {
        return context.getString(languageNameResId);
    }

    public static String getLanguageNameBySuffix(Context context, String suffix) {
        for (LanguageVariant lang : values()) {
            if (lang.suffix.equalsIgnoreCase(suffix)) {
                return context.getString(lang.languageNameResId);
            }
        }
        return "";
    }
}
package com.eam.rwtranslator.utils;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageCodeHelper {
    
    public static class LanguageInfo {
        public final String code;
        public final String englishName;
        
        public LanguageInfo(String code, String englishName) {
            this.code = code;
            this.englishName = englishName;
        }
        
        @Override
        public String toString() {
            return code + " - " + englishName;
        }
    }
    
    private static final Map<String, LanguageInfo> LANGUAGE_MAP = new HashMap<>();
    
    static {
        // 现有支持的语言
        LANGUAGE_MAP.put("zh", new LanguageInfo("zh", "Chinese (Simplified)"));
        LANGUAGE_MAP.put("ru", new LanguageInfo("ru", "Russian"));
        LANGUAGE_MAP.put("en", new LanguageInfo("en", "English"));
        LANGUAGE_MAP.put("ja", new LanguageInfo("ja", "Japanese"));
        LANGUAGE_MAP.put("de", new LanguageInfo("de", "German"));
        LANGUAGE_MAP.put("es", new LanguageInfo("es", "Spanish"));
        LANGUAGE_MAP.put("fr", new LanguageInfo("fr", "French"));
        LANGUAGE_MAP.put("pt", new LanguageInfo("pt", "Portuguese"));
        LANGUAGE_MAP.put("it", new LanguageInfo("it", "Italian"));
        LANGUAGE_MAP.put("nl", new LanguageInfo("nl", "Dutch"));
        LANGUAGE_MAP.put("tr", new LanguageInfo("tr", "Turkish"));
        LANGUAGE_MAP.put("pl", new LanguageInfo("pl", "Polish"));
        LANGUAGE_MAP.put("uk", new LanguageInfo("uk", "Ukrainian"));
        
        LANGUAGE_MAP.put("ar", new LanguageInfo("ar", "Arabic"));
        LANGUAGE_MAP.put("bg", new LanguageInfo("bg", "Bulgarian"));
        LANGUAGE_MAP.put("ca", new LanguageInfo("ca", "Catalan"));
        LANGUAGE_MAP.put("cs", new LanguageInfo("cs", "Czech"));
        LANGUAGE_MAP.put("da", new LanguageInfo("da", "Danish"));
        LANGUAGE_MAP.put("el", new LanguageInfo("el", "Greek"));
        LANGUAGE_MAP.put("et", new LanguageInfo("et", "Estonian"));
        LANGUAGE_MAP.put("fi", new LanguageInfo("fi", "Finnish"));
        LANGUAGE_MAP.put("he", new LanguageInfo("he", "Hebrew"));
        LANGUAGE_MAP.put("hi", new LanguageInfo("hi", "Hindi"));
        LANGUAGE_MAP.put("hr", new LanguageInfo("hr", "Croatian"));
        LANGUAGE_MAP.put("hu", new LanguageInfo("hu", "Hungarian"));
        LANGUAGE_MAP.put("id", new LanguageInfo("id", "Indonesian"));
        LANGUAGE_MAP.put("is", new LanguageInfo("is", "Icelandic"));
        LANGUAGE_MAP.put("ko", new LanguageInfo("ko", "Korean"));
        LANGUAGE_MAP.put("lt", new LanguageInfo("lt", "Lithuanian"));
        LANGUAGE_MAP.put("lv", new LanguageInfo("lv", "Latvian"));
        LANGUAGE_MAP.put("ms", new LanguageInfo("ms", "Malay"));
        LANGUAGE_MAP.put("mt", new LanguageInfo("mt", "Maltese"));
        LANGUAGE_MAP.put("no", new LanguageInfo("no", "Norwegian"));
        LANGUAGE_MAP.put("ro", new LanguageInfo("ro", "Romanian"));
        LANGUAGE_MAP.put("sk", new LanguageInfo("sk", "Slovak"));
        LANGUAGE_MAP.put("sl", new LanguageInfo("sl", "Slovenian"));
        LANGUAGE_MAP.put("sv", new LanguageInfo("sv", "Swedish"));
        LANGUAGE_MAP.put("th", new LanguageInfo("th", "Thai"));
        LANGUAGE_MAP.put("vi", new LanguageInfo("vi", "Vietnamese"));
        LANGUAGE_MAP.put("zh-tw", new LanguageInfo("zh-tw", "Chinese (Traditional)"));
        LANGUAGE_MAP.put("af", new LanguageInfo("af", "Afrikaans"));
        LANGUAGE_MAP.put("sq", new LanguageInfo("sq", "Albanian"));
        LANGUAGE_MAP.put("am", new LanguageInfo("am", "Amharic"));
        LANGUAGE_MAP.put("az", new LanguageInfo("az", "Azerbaijani"));
        LANGUAGE_MAP.put("be", new LanguageInfo("be", "Belarusian"));
        LANGUAGE_MAP.put("bn", new LanguageInfo("bn", "Bengali"));
        LANGUAGE_MAP.put("bs", new LanguageInfo("bs", "Bosnian"));
        LANGUAGE_MAP.put("eu", new LanguageInfo("eu", "Basque"));
        LANGUAGE_MAP.put("fa", new LanguageInfo("fa", "Persian"));
        LANGUAGE_MAP.put("ga", new LanguageInfo("ga", "Irish"));
        LANGUAGE_MAP.put("gl", new LanguageInfo("gl", "Galician"));
        LANGUAGE_MAP.put("gu", new LanguageInfo("gu", "Gujarati"));
        LANGUAGE_MAP.put("ka", new LanguageInfo("ka", "Georgian"));
        LANGUAGE_MAP.put("kk", new LanguageInfo("kk", "Kazakh"));
        LANGUAGE_MAP.put("km", new LanguageInfo("km", "Khmer"));
        LANGUAGE_MAP.put("kn", new LanguageInfo("kn", "Kannada"));
        LANGUAGE_MAP.put("ky", new LanguageInfo("ky", "Kyrgyz"));
        LANGUAGE_MAP.put("lo", new LanguageInfo("lo", "Lao"));
        LANGUAGE_MAP.put("mk", new LanguageInfo("mk", "Macedonian"));
        LANGUAGE_MAP.put("ml", new LanguageInfo("ml", "Malayalam"));
        LANGUAGE_MAP.put("mn", new LanguageInfo("mn", "Mongolian"));
        LANGUAGE_MAP.put("mr", new LanguageInfo("mr", "Marathi"));
        LANGUAGE_MAP.put("my", new LanguageInfo("my", "Myanmar"));
        LANGUAGE_MAP.put("ne", new LanguageInfo("ne", "Nepali"));
        LANGUAGE_MAP.put("pa", new LanguageInfo("pa", "Punjabi"));
        LANGUAGE_MAP.put("si", new LanguageInfo("si", "Sinhala"));
        LANGUAGE_MAP.put("sr", new LanguageInfo("sr", "Serbian"));
        LANGUAGE_MAP.put("sw", new LanguageInfo("sw", "Swahili"));
        LANGUAGE_MAP.put("ta", new LanguageInfo("ta", "Tamil"));
        LANGUAGE_MAP.put("te", new LanguageInfo("te", "Telugu"));
        LANGUAGE_MAP.put("tg", new LanguageInfo("tg", "Tajik"));
        LANGUAGE_MAP.put("ur", new LanguageInfo("ur", "Urdu"));
        LANGUAGE_MAP.put("uz", new LanguageInfo("uz", "Uzbek"));
        LANGUAGE_MAP.put("cy", new LanguageInfo("cy", "Welsh"));
        LANGUAGE_MAP.put("yi", new LanguageInfo("yi", "Yiddish"));
    }
    
    public static List<LanguageInfo> getAllLanguages() {
        return new ArrayList<>(LANGUAGE_MAP.values());
    }
    
    public static List<LanguageInfo> getFilteredLanguages(String query) {
        List<LanguageInfo> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (LanguageInfo info : LANGUAGE_MAP.values()) {
            if (info.code.toLowerCase().contains(lowerQuery) ||
                info.englishName.toLowerCase().contains(lowerQuery)) {
                filtered.add(info);
            }
        }
        
        return filtered;
    }
    
    public static LanguageInfo getLanguageByCode(String code) {
        return LANGUAGE_MAP.get(code.toLowerCase());
    }
    
    
    public static boolean isValidLanguageCode(String code) {
        return LANGUAGE_MAP.containsKey(code.toLowerCase());
    }
    
    public static String getLanguageCodeFromDisplayText(String displayText) {
        for (LanguageInfo info : LANGUAGE_MAP.values()) {
            if (info.toString().equals(displayText)) {
                return info.code;
            }
        }
        return null;
    }
}
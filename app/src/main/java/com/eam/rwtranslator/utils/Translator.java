package com.eam.rwtranslator.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import app.nekogram.translator.BaiduTranslator;
import app.nekogram.translator.BaseTranslator;
import app.nekogram.translator.DeepLTranslator;
import app.nekogram.translator.MicrosoftTranslator;
import app.nekogram.translator.Result;
import app.nekogram.translator.SogouTranslator;
import app.nekogram.translator.TranSmartTranslator;
import app.nekogram.translator.GoogleAppTranslator;
import app.nekogram.translator.YandexTranslator;

import com.eam.rwtranslator.AppConfig;
import com.eam.rwtranslator.ui.setting.AppSettings;
import com.eam.rwtranslator.utils.translator.BaseLLMTranslator;
import com.eam.rwtranslator.utils.translator.GeminiTranslator;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Translator {
    public interface TranslateTaskCallBack {
        void onTranslate(boolean enable_llm);
    }

    public interface TranslateCallBack {
        void onSuccess(String translation, String sourceLanguage, String targetLanguage);

        void onError(Throwable t);
    }

    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_MICROSOFT = "microsoft";
    public static final String PROVIDER_YANDEX = "yandex";
    public static final String PROVIDER_DEEPL = "deepl";
    public static final String PROVIDER_BAIDU = "baidu";
    public static final String PROVIDER_SOGOU = "sogou";
    public static final String PROVIDER_TENCENT = "tencent";
    //LLM 翻译器标签
    public static final String PROVIDER_GEMINI = "gemini-2.0-flash";
    public static final String PROVIDER_GEMINI_LITE = "gemini-2.0-flash-lite";

    private static final ListeningExecutorService executorService =
            MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    private static final LruCache<Pair<String, String>, Pair<String, String>> cache =
            new LruCache<>(200);

    public static ListeningExecutorService getExecutorService() {
        return executorService;
    }

    public static BaseTranslator getCurrentTranslator() {
        return getTranslator(AppSettings.translationProvider);
    }

    public static BaseTranslator getTranslator(String type) {
        return switch (type) {
            case PROVIDER_DEEPL -> {
                DeepLTranslator.setFormality(AppSettings.deepLFormality);
                yield DeepLTranslator.getInstance();
            }
            case PROVIDER_MICROSOFT -> MicrosoftTranslator.getInstance();
            case PROVIDER_BAIDU -> BaiduTranslator.getInstance();
            case PROVIDER_SOGOU -> SogouTranslator.getInstance();
            case PROVIDER_TENCENT -> TranSmartTranslator.getInstance();
            case PROVIDER_YANDEX -> YandexTranslator.getInstance();
            case PROVIDER_GEMINI -> new GeminiTranslator(PROVIDER_GEMINI);
            case PROVIDER_GEMINI_LITE -> new GeminiTranslator(PROVIDER_GEMINI_LITE);
            default -> GoogleAppTranslator.getInstance();
        };
    }

    public static String getLLMTranslatorCodeByIndex(int index) {
        return switch (index) {
            case 0 -> PROVIDER_GEMINI;
            case 1 -> PROVIDER_GEMINI_LITE;
            default -> PROVIDER_GEMINI_LITE;
        };
    }

    public static String getTranslatorCode(BaseTranslator translator) {
        if (translator instanceof DeepLTranslator) {
            return PROVIDER_DEEPL;
        } else if (translator instanceof YandexTranslator) {
            return PROVIDER_YANDEX;
        } else if (translator instanceof MicrosoftTranslator) {
            return PROVIDER_MICROSOFT;
        } else if (translator instanceof BaiduTranslator) {
            return PROVIDER_BAIDU;
        } else if (translator instanceof SogouTranslator) {
            return PROVIDER_SOGOU;
        } else if (translator instanceof TranSmartTranslator) {
            return PROVIDER_TENCENT;
        }else {
            return PROVIDER_GOOGLE;
        }
    }

    public static String getTranslatorCodeByIndex(int index) {
        return switch (index) {
            case 0 -> PROVIDER_TENCENT;
            case 1 -> PROVIDER_BAIDU;
            case 2 -> PROVIDER_SOGOU;
            case 3 -> PROVIDER_GOOGLE;
            case 4 -> PROVIDER_MICROSOFT;
            case 5 -> PROVIDER_DEEPL;
            case 6 -> PROVIDER_YANDEX;
            default -> PROVIDER_GOOGLE; // 默认返回有道翻译
        };
    }

    private static String getCurrentTargetLanguage() {
        return AppSettings.CurrentTargetLanguage;
    }

    private static String getCurrentFromLanguage() {
        return AppSettings.CurrentFromLanguage;
    }

    public static void translate(String query, TranslateCallBack translateCallBack) {
        translate(query, getCurrentFromLanguage(), getCurrentTargetLanguage(), translateCallBack);
    }

    public static void translate(
            String query, String fl, String tl, TranslateCallBack translateCallBack) {
        BaseTranslator translator = getCurrentTranslator();
        String language = tl == null ? getCurrentTargetLanguage() : tl;

        if (!translator.supportLanguage(language)) {
            translateCallBack.onError(new UnsupportedTargetLanguageException());
        } else {
            startTask(translator, query, fl, language, translateCallBack);
        }
    }

    public static void LLM_translate(String query, String fl, String tl, TranslateCallBack translateCallBack) {
        BaseLLMTranslator llmTranslator = (BaseLLMTranslator) getCurrentTranslator();
        String language = tl == null ? getCurrentTargetLanguage() : tl;
        llmTranslator.translate(query, fl, tl, translateCallBack);
    }

    private static class UnsupportedTargetLanguageException extends IllegalArgumentException {
    }

    private static void startTask(
            BaseTranslator translator,
            String query,
            String fromLang,
            String toLang,
            TranslateCallBack translateCallBack) {
        var result = cache.get(Pair.create(query, toLang + "|" + AppSettings.translationProvider));
        if (result != null) {
            translateCallBack.onSuccess(
                    result.first,
                    result.second == null ? fromLang : translator.convertLanguageCode(result.second, ""),
                    toLang);
        } else {
            TranslateTask translateTask =
                    new TranslateTask(translator, query, fromLang, toLang, translateCallBack);
            ListenableFuture<Pair<String, String>> future = getExecutorService().submit(translateTask);
            Futures.addCallback(
                    future, translateTask, ContextCompat.getMainExecutor(AppConfig.applicationContext));
        }
    }

    private record TranslateTask(BaseTranslator translator, String query, String fl, String tl,
                                 TranslateCallBack translateCallBack)
            implements Callable<Pair<String, String>>, FutureCallback<Pair<String, String>> {

        @Override
        public Pair<String, String> call() {
            String from =translator.convertLanguageCode(fl, "");
            // 为不同的翻译器设置正确的自动检测语言代码
            if (translator instanceof MicrosoftTranslator || translator instanceof YandexTranslator) {
               from=null;
            }
            var to = translator.convertLanguageCode(tl, "");
            Result result = translator.translate(query, from, to);
            return Pair.create(result.translation, result.sourceLanguage);
        }

        @Override
        public void onSuccess(Pair<String, String> result) {
            translateCallBack.onSuccess(
                    result.first,
                    result.second == null ? fl : translator.convertLanguageCode(result.second, ""),
                    tl);
            cache.put(Pair.create(query, tl + "|" + AppSettings.translationProvider), result);
        }

        @Override
        public void onFailure(@NonNull Throwable t) {
            translateCallBack.onError(t);
        }
    }

}

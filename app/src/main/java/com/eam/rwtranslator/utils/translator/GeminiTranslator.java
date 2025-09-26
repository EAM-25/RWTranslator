package com.eam.rwtranslator.utils.translator;

import com.eam.rwtranslator.utils.LanguageVariant;
import com.eam.rwtranslator.utils.Translator;
import com.eam.rwtranslator.ui.setting.AppSettings;
import com.google.gson.Gson;
import android.content.SharedPreferences;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import app.nekogram.translator.Result;
import okhttp3.*;
import timber.log.Timber;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GeminiTranslator extends BaseLLMTranslator {
    private static final String API_KEY = "AIzaSyCmZZ9BV0a5DM9zwIG0Ft4l89T2QAm2vLI";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";
    private static final Gson gson = new Gson();
    
    private final String modelName;
    public GeminiTranslator(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public void translate(String query, String fl, String tl, Translator.TranslateCallBack callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        
        String url = BASE_URL + "/" + modelName + ":generateContent?key=" + API_KEY;
        
        JsonObject systemInstruction = new JsonObject();
        JsonObject systemText = new JsonObject();
        
        // 获取用户自定义的系统提示词，如果没有则使用默认的
        Context context = AppSettings.context;
        String userPrompt = AppSettings.DefaultLLMSystemPrompt;
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("llm_translate_dialog", Context.MODE_PRIVATE);
            userPrompt = prefs.getString("system_prompt", AppSettings.DefaultLLMSystemPrompt);
        }
        
        String systemPrompt = userPrompt.replace("{target_language}", getLanguageName(tl));
        systemText.addProperty("text", systemPrompt);
        systemInstruction.add("parts", gson.toJsonTree(new JsonObject[]{systemText}));
        
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonObject userText = new JsonObject();
        userText.addProperty("text", query);
        userContent.add("parts", gson.toJsonTree(new JsonObject[]{userText}));
        
        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("system_instruction", systemInstruction);
        requestBodyJson.add("contents", gson.toJsonTree(new JsonObject[]{userContent}));
        
        String requestBody = gson.toJson(requestBodyJson);
        
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Timber.e("request failed : %s",e.getMessage());
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code: " + response));
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    Timber.d("response body : %s",responseBody);
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    String translatedText = json.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    //处理掉 \n 换行符
                    callback.onSuccess(translatedText.substring(0,translatedText.length()-1),fl,tl);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    private String getLanguageName(String languageCode) {
        return LanguageVariant.getEnglishNameByCode(languageCode);
    }

    @Override
    protected Result a(String s, String s1, String s2) {
        return null;
    }

    @Override
    public List<String> getTargetLanguages() {
        return Collections.emptyList();
    }
}

package com.eam.rwtranslator.utils.translator;

import java.util.Arrays;
import java.util.stream.Collectors;
import okhttp3.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
/*
public class DeepSeekTranslator implements Translator.ExtraTranslatorInterface {
  private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
  private static final int MAX_RESPONSE_LENGTH = 4000;
  private static final Gson GSON = new Gson();
  private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");
  private final OkHttpClient httpClient;
  private final String apiKey;

  public DeepSeekTranslator(String apiKey) {
    this(apiKey, new OkHttpClient());
  }

  public DeepSeekTranslator(String apiKey, OkHttpClient httpClient) {
    this.apiKey = apiKey;
    this.httpClient = httpClient;
  }

  public List<String> getSupportedLanguages() {
    return Arrays.stream(LanguageVariant.values())
        .map(LanguageVariant::getSuffix)
        .collect(Collectors.toList());
  }

  private boolean isValidLang(String langCode) {
    return Arrays.stream(LanguageVariant.values())
        .anyMatch(l -> l.getSuffix().equalsIgnoreCase(langCode));
  }

  private String buildSystemPrompt(String sourceLang, String targetLang) {
    String sourceName = LanguageVariant.getLanguageNameBySuffix(sourceLang);
    String targetName = LanguageVariant.getLanguageNameBySuffix(targetLang);

    return String.format(
        "作为专业的%s-%s翻译专家，请遵守以下准则：\n"
            + "1. 严格保持原文语义，不添加或删减内容\n"
            + "2. 使用符合%s文化的自然表达\n"
            + "3. 保留专业术语（人名、品牌名等）\n"
            + "4. 处理俚语时要给出最贴切的%s对应表达\n"
            + "5. 当原文存在歧义时，提供最合理的翻译方案\n"
            + "6. 输出仅包含翻译结果，不要添加额外说明",
        sourceName, targetName, targetName, targetName);
  }

  @Override
  public Result translate(String text, String sourceLang, String targetLang) {
    // 验证支持的语言
    if (!getSupportedLanguages().contains(sourceLang.toLowerCase())
        || !getSupportedLanguages().contains(targetLang.toLowerCase())) {
      return Result.error("Unsupported language pair");
    }

    // 构建系统提示
    String systemPrompt =
        String.format(
            "Act as a professional translation assistant specializing in %s-%s translations. "
                + "Provide accurate and contextually appropriate translations while preserving cultural nuances.",
            sourceLang.toUpperCase(), targetLang.toUpperCase());

    try {
      // 构建请求体
      RequestBody body =
          RequestBody.create(GSON.toJson(new DeepSeekRequest(text, systemPrompt)), JSON_MEDIA_TYPE);

      // 创建请求
      Request request =
          new Request.Builder()
              .url(API_URL)
              .header("Authorization", "Bearer " + apiKey)
              .post(body)
              .build();

      // 执行请求
      try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          return Result.error("API request failed: " + response.code());
        }

        // 解析响应
        DeepSeekResponse resp = GSON.fromJson(response.body().charStream(), DeepSeekResponse.class);

        // 处理结果
        if (resp.choices == null || resp.choices.length == 0) {
          return Result.error("Empty response from API");
        }

        String translatedText = resp.choices[0].message.content;
        return Result.success(truncateText(translatedText));
      }
    } catch (IOException e) {
      return Result.error("Network error: " + e.getMessage());
    } catch (Exception e) {
      return Result.error("Unexpected error: " + e.getMessage());
    }
  }

  private String truncateText(String text) {
    return text.length() > MAX_RESPONSE_LENGTH
        ? text.substring(0, MAX_RESPONSE_LENGTH) + "\n[...]"
        : text;
  }

  // 请求/响应数据结构
  private static class DeepSeekRequest {
    final String model = "deepseek-chat";
    final Message[] messages;
    final double temperature = 0.7;
    final int max_tokens = 1000;

    DeepSeekRequest(String userPrompt, String systemPrompt) {
      this.messages =
          new Message[] {new Message("system", systemPrompt), new Message("user", userPrompt)};
    }
  }

  private static class Message {
    final String role;
    final String content;

    Message(String role, String content) {
      this.role = role;
      this.content = content;
    }
  }

  private static class DeepSeekResponse {
    Choice[] choices;
  }

  private static class Choice {
    Message message;
  }
}
*/
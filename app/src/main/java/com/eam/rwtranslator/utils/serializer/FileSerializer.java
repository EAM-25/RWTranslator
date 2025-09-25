package com.eam.rwtranslator.utils.serializer;

import androidx.annotation.Keep;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.lang.reflect.Type;

/**
 * File类型的Gson序列化器
 */
@Keep
public class FileSerializer implements JsonSerializer<File> {
    
    @Override
    public JsonElement serialize(File file, Type typeOfSrc, JsonSerializationContext context) {
        if (file == null) {
            return null;
        }
        return new JsonPrimitive(file.getAbsolutePath());
    }
}
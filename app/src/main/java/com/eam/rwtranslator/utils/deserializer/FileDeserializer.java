package com.eam.rwtranslator.utils.deserializer;

import androidx.annotation.Keep;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.File;
import java.lang.reflect.Type;

/**
 * File类型的Gson反序列化器
 */
@Keep
public class FileDeserializer implements JsonDeserializer<File> {
    
    @Override
    public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        
        String filePath = json.getAsString();
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        
        return new File(filePath);
    }
}
package com.eam.rwtranslator.utils.serializer;

import androidx.annotation.Keep;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.ini4j.Wini;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Keep
public class HashMapWiniSerializer implements JsonSerializer<HashMap<String, Wini>> {
    
    @Override
    public JsonElement serialize(HashMap<String, Wini> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        WiniSerializer winiSerializer = new WiniSerializer();
        
        try {
            if (src != null) {
                for (Map.Entry<String, Wini> entry : src.entrySet()) {
                    String key = entry.getKey();
                    Wini value = entry.getValue();
                    
                    if (key != null && value != null) {
                        JsonElement serializedWini = winiSerializer.serialize(value, Wini.class, context);
                        result.add(key, serializedWini);
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            // 返回空对象而不是抛出异常，避免序列化过程中断
            return new JsonObject();
        }
    }
}
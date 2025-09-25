package com.eam.rwtranslator.utils.deserializer;

import androidx.annotation.Keep;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import org.ini4j.Wini;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Keep
public class HashMapWiniDeserializer implements JsonDeserializer<HashMap<String, Wini>> {
    
    @Override
    public HashMap<String, Wini> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            HashMap<String, Wini> result = new HashMap<>();
            
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                WiniDeserializer winiDeserializer = new WiniDeserializer();
                
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    
                    if (value.isJsonObject()) {
                        try {
                            Wini wini = winiDeserializer.deserialize(value, Wini.class, context);
                            result.put(key, wini);
                        } catch (Exception e) {
                            // 如果反序列化失败，跳过这个条目
                            continue;
                        }
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize HashMap<String, Wini>", e);
        }
    }
}
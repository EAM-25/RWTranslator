package com.eam.rwtranslator.utils.serializer;

import androidx.annotation.Keep;

import com.google.gson.JsonSerializer;
import org.ini4j.Profile;
import org.ini4j.Wini;
import java.lang.reflect.Type;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;

@Keep
public class WiniSerializer implements JsonSerializer<Wini> {

    @Override
    public JsonElement serialize(Wini wini, Type typeOfSrc, JsonSerializationContext context) {
        try {
            JsonObject jsonObject = new JsonObject();
            
            // 先添加配置文件路径，确保反序列化时能正确获取
            File configFile = wini.getFile();
            if (configFile != null) {
                jsonObject.addProperty("configFile", configFile.getAbsolutePath());
            }
            
            // 序列化各个section
            for (String sectionName : wini.keySet()) {
                Profile.Section section = wini.get(sectionName);
                if (section != null) {
                    JsonObject sectionObject = new JsonObject();
                    
                    for (String optionKey : section.keySet()) {
                        String optionValue = section.get(optionKey);
                        if (optionValue != null) {
                            sectionObject.addProperty(optionKey, optionValue);
                        }
                    }
                    
                    if (sectionObject.size() > 0) {
                        jsonObject.add(sectionName, sectionObject);
                    }
                }
            }
            
            return jsonObject;
        } catch (Exception e) {
            // 返回空对象而不是抛出异常，避免序列化过程中断
            return new JsonObject();
        }
    }
}
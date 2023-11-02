package com.dev.openai.travelassistant.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void configure() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static <T> String toJson(T object) throws JsonProcessingException {
        if (null == object)
            return null;
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        if (null == json)
            return null;
        return objectMapper.readValue(json, clazz);
    }

    public static <T> List<T> fromJson(String json, TypeReference<List<T>> clazz) throws JsonProcessingException {
        if (null == json)
            return null;
        return objectMapper.readValue(json, clazz);
    }
}

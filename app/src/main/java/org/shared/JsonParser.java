package org.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.shared.logs.LogMaker;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseJson(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            LogMaker.error("Erro ao fazer parse do JSON: " + e.getMessage());
            return null;
        }
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            LogMaker.error("Erro ao converter objeto para JSON: " + e.getMessage());
            return null;
        }
    }
}

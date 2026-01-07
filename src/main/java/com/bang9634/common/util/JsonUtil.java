package com.bang9634.common.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * JSON Utility class for serialization and deserialization.
 * Uses Gson library for JSON operations.
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .setPrettyPrinting()
        .create();

    /**
     * Adapter for LocalDateTime serialization and deserialization.
     */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(DATE_TIME_FORMATTER));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String dateTimeString = in.nextString();
            return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
        }
    }

    /**
     * Converts an object to its JSON representation.
     * @param obj Object to be converted
     * @return JSON String
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return gson.toJson(obj);
    }

    /**
     * Converts a JSON string to and object of specified class.
     * @param <T> Type of the object
     * @param json JSON String
     * @param classOfT Class of type T
     * @return Object of type T
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse JSON: {}", json, e);
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
    }

    private JsonUtil() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate JsonUtil");
    }
}

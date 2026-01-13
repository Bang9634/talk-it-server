package com.bang9634.common.util;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    public static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public static int getIntEnv(String key, int defaultValue) {
        return getEnv(key, defaultValue, Integer::parseInt);
    }

    public static long getLongEnv(String key, long defaultValue) {
        return getEnv(key, defaultValue, Long::parseLong);
    }

    public static boolean getBooleanEnv(String key, boolean defaultValue) {
        return getEnv(key, defaultValue, Boolean::parseBoolean);
    }

    public static String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable " + key +" is not set.");
        }
        return value;
    }

    private static <T> T getEnv(String key, T defaultValue, Function<String, T> parser) {
        String value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return parser.apply(value);
            } catch (Exception e) {
                logger.warn("Invalid value for environment variable {}: '{}', using default: {}",
                    key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    private ConfigUtil() {
        throw new AssertionError("Cannot instantiate ConfigUtil");
    }
}

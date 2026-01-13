package com.bang9634.common.config;

import static com.bang9634.common.util.ConfigUtil.getEnvOrDefault;
import static com.bang9634.common.util.ConfigUtil.getIntEnv;
import static com.bang9634.common.util.ConfigUtil.getLongEnv;

public class ApplicationConfig {
    // Server Config
    public static final int PORT = getIntEnv("TALK_IT_PORT", 8080);
    public static final String WEBSOCKET_PATH = getEnvOrDefault("TALK_IT_WEBSOCKET_PATH", "/chat");

    // Rate Limiter Config
    public static final int MAX_MESSAGE_REQUESTS_PER_MINUTE = getIntEnv(
        "MAX_MESSAGE_REQUESTS_PER_MINUTE", 30);
    public static final int MAX_CONNECT_REQUESTS_PER_MINUTE = getIntEnv(
        "MAX_CONNECT_REQUESTS_PER_MINUTE", 30);
    public static final long RATE_LIMIT_CLEANUP_INTERVAL_MINUTES = getLongEnv(
        "RATE_LIMIT_CLEANUP_INTERVAL_MINUTES", 10);
    
    // Input Validator Config
    public static final int MAX_MESSAGE_LENGTH = getIntEnv(
        "MAX_MESSAGE_LENGTH", 500);
    public static final int MIN_MESSAGE_LENGTH = getIntEnv(
        "MIN_MESSAGE_LENGTH", 1);

    private ApplicationConfig() {
        throw new AssertionError("Cannot instantiate ApplicationConfig");
    }
}

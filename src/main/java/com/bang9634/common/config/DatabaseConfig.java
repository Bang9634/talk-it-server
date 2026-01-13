package com.bang9634.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bang9634.common.util.ConfigUtil.getEnvOrDefault;
import static com.bang9634.common.util.ConfigUtil.getIntEnv;
import static com.bang9634.common.util.ConfigUtil.getLongEnv;

/**
 * Database configuration class.
 * Provides static access to database connection settings.
 */
public final class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    public static final String JDBC_URL = getEnvOrDefault("TALK_IT_DB_URL",
        "jdbc:mysql://localhost:3306/talk_it?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
    public static final String USERNAME = getEnvOrDefault("TALK_IT_DB_USERNAME", "root");
    public static final String PASSWORD = getEnvOrDefault("TALK_IT_DB_PASSWORD", "");
    public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    public static final int MAXIMUM_POOL_SIZE = getIntEnv("TALK_IT_DB_POOL_SIZE", 10);
    public static final int MINIMUM_IDLE = getIntEnv("TALK_IT_DB_MIN_IDLE", 5);
    public static final long CONNECTION_TIMEOUT = getLongEnv("TALK_IT_DB_CONNECTION_TIMEOUT", 30000);
    public static final long IDLE_TIMEOUT = getLongEnv("TALK_IT_DB_IDLE_TIMEOUT", 600000);
    public static final long MAX_LIFETIME = getLongEnv("TALK_IT_DB_MAX_LIFETIME", 1800000);

    static {
        logger.info("DatabaseConfig initialized:");
        logger.info("  JDBC URL: {}", maskPassword(JDBC_URL));
        logger.info("  Username: {}", USERNAME);
        logger.info("  Pool Size: {}", MAXIMUM_POOL_SIZE);
        logger.info("  Min Idle: {}", MINIMUM_IDLE);

        validate();
    }

    /**
     * Mask password in the JDBC URL for logging.
     * 
     * @param url The JDBC URL
     * @return The JDBC URL with the password masked
     * 
     * @apiNote Call this method only for logging purposes to avoid exposing sensitive information.
     */
    private static String maskPassword(String url) {
        if (url.contains("password=")) {
            return url.replaceAll("password=[^&]*", "password=****");
        }
        return url;
    }

    private static void validate() {
        if (JDBC_URL == null || JDBC_URL.trim().isEmpty()) {
            throw new IllegalStateException("Database URL is not configured");
        }
        if (USERNAME == null || USERNAME.trim().isEmpty()) {
            throw new IllegalStateException("Database username is not configured");
        }
        if (MAXIMUM_POOL_SIZE <= 0 || MINIMUM_IDLE <= 0) {
            throw new IllegalStateException("Invalid pool size configuration");
        }
    }

    private DatabaseConfig() {
        throw new AssertionError("Cannot instantiate DatabaseConfig");
    }
}
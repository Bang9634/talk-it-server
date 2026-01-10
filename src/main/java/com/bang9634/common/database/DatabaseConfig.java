package com.bang9634.common.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

/**
 * Configuration class for database connection settings.
 * Loads settings from environment variables or uses default values.
 * Provides getters for configuration properties.
 * Validates essential configuration parameters.
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    // Database connection properties
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String driverClassName;

    // Connection pool properties
    private final int maximumPoolSize;
    private final int minimumIdle;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;

    @Singleton
    public DatabaseConfig() {
        // Load from environment variables or use defaults
        this.jdbcUrl = getEnvOrDefault("TALK_IT_DB_URL", "jdbc:mysql://localhost:3306/talk_it?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        this.username = getEnvOrDefault("TALK_IT_DB_USERNAME", "root");
        this.password = getEnvOrDefault("TALK_IT_DB_PASSWORD", "");
        this.driverClassName = "com.mysql.cj.jdbc.Driver";

        // Connection pool settings
        this.maximumPoolSize = Integer.parseInt(getEnvOrDefault("TALK_IT_DB_POOL_SIZE", "10"));
        this.minimumIdle = Integer.parseInt(getEnvOrDefault("TALK_IT_DB_MIN_IDLE", "5"));
        this.connectionTimeout = Long.parseLong(getEnvOrDefault("TALK_IT_DB_CONNECTION_TIMEOUT", "30000")); // 30 seconds
        this.maxLifetime = Long.parseLong(getEnvOrDefault("TALK_IT_DB_MAX_LIFETIME", "1800000")); // 30 minutes
        this.idleTimeout = Long.parseLong(getEnvOrDefault("TALK_IT_DB_IDLE_TIMEOUT", "600000")); // 10 minutes

        logger.info("DatabaseConfig initialized:");
        logger.info("  JDBC URL: {}", maskPassword(jdbcUrl));
        logger.info("  Username: {}", username);
        logger.info("  Pool Size: {}", maximumPoolSize);
        logger.info("  Min Idle: {}", minimumIdle);
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Mask password in JDBC URL for logging.
     * 
     * @param url The JDBC URL
     * @return The JDBC URL with password masked
     */
    private String maskPassword(String jdbcUrl) {
        if (jdbcUrl.contains("password=")) {
            return jdbcUrl.replaceAll("password=[^&]*", "password=****");
        }
        return jdbcUrl;
    }

    // Getters
    public String getJdbcUrl() { return jdbcUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDriverClassName() { return driverClassName; }
    
    public int getMaximumPoolSize() { return maximumPoolSize; }
    public int getMinimumIdle() { return minimumIdle; }
    public long getConnectionTimeout() { return connectionTimeout; }
    public long getIdleTimeout() { return idleTimeout; }
    public long getMaxLifetime() { return maxLifetime; }

    /**
     * Validate essential configuration parameters.
     * 
     * @return True if valid, false otherwise
     */
    public boolean validate() {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            logger.error("Database URL is not configured.");
            return false;
        }
        if (username == null || username.isEmpty()) {
            logger.error("Database username is not configured.");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "jdbcUrl='" + maskPassword(jdbcUrl) + '\'' +
                ", username='" + username + '\'' +
                ", maximumPoolSize=" + maximumPoolSize +
                ", minimumIdle=" + minimumIdle +
                '}';
    }

}

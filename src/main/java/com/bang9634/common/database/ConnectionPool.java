package com.bang9634.common.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database connection pool using HikariCP.
 * Initializes the connection pool with settings from DatabaseConfig.
 */
@Singleton
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private final HikariDataSource dataSource;
    private final DatabaseConfig databaseConfig;

    @Inject
    public ConnectionPool(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        
        if (!databaseConfig.validate()) {
            throw new IllegalStateException("Invalid database configuration");
        }

        this.dataSource = initializeDataSource();
        logger.info("ConnectionPool initialized successfully.");
    }

    private HikariDataSource initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            // basic settings
            config.setJdbcUrl(databaseConfig.getJdbcUrl());
            config.setUsername(databaseConfig.getUsername());
            config.setPassword(databaseConfig.getPassword());
            config.setDriverClassName(databaseConfig.getDriverClassName());

            // Pool settings
            config.setMaximumPoolSize(databaseConfig.getMaximumPoolSize());
            config.setMinimumIdle(databaseConfig.getMinimumIdle());
            config.setConnectionTimeout(databaseConfig.getConnectionTimeout());
            config.setIdleTimeout(databaseConfig.getIdleTimeout());
            config.setMaxLifetime(databaseConfig.getMaxLifetime());

            // Performance settings
            config.setAutoCommit(true);
            config.setConnectionTestQuery("SELECT 1");

            // Pool name
            config.setPoolName("TalkIt-HikariCP");

            // Additional settings for MySQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            logger.info("Initializing HikariCP with config: {}", databaseConfig);
            return new HikariDataSource(config);
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            logger.debug("Connection obtained from pool. Active: {}, Idle: {}, Total: {}",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections());

            return connection;
        } catch (SQLException e) {
            logger.error("Falied to get connection from pool: {}", e.getMessage());
            throw e;
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn.isValid(5);
            if(isValid) {
                logger.info("Database connection test: SUCCESS");
            } else {
                logger.warn("Database connection test: FAILED: invalid connection");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("Database connection test: FAILED: {}", e.getMessage());
            return false;
        }
    }

    public String getPoolStats() {
        if (dataSource.isClosed()) {
            return "Connection pool is closed.";
        }

        return String.format(
            "HikariCP Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    public boolean isClosed() {
        return dataSource.isClosed();
    }

    /**
     * Close the connection pool and release all resources.
     * @apiNote Should be called on application shutdown
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing connection pool...");
            logger.info("Final stats: {}", getPoolStats());
            dataSource.close();
            logger.info("Connection pool closed successfully.");
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}

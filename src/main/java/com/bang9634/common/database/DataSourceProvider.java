package com.bang9634.common.database;

import com.bang9634.common.config.DatabaseConfig;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceProvider implements Provider<DataSource>{
    private static final Logger logger = LoggerFactory.getLogger(DataSourceProvider.class);

    // marked as volatile to prevent instruction reordering issues
    private volatile DataSource dataSource;

    /**
     * Creates and configures the DataSource instance.
     * 
     * @return configured DataSource
     */
    @Override
    public DataSource get() {
        if (dataSource == null) {
            // First check (no locking)
            // Checks if the instance is already initialized to avoid the overhead of synchronization
            synchronized (this) {
                // Second check (with locking)
                // Ensures that another thread hasn't initialized the instance
                // while this thread was waiting to acquire the lock
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }


    private DataSource createDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            // basic settings
            config.setJdbcUrl(DatabaseConfig.JDBC_URL);
            config.setUsername(DatabaseConfig.USERNAME);
            config.setPassword(DatabaseConfig.PASSWORD);
            config.setDriverClassName(DatabaseConfig.DRIVER_CLASS_NAME);

            // Pool settings
            config.setMaximumPoolSize(DatabaseConfig.MAXIMUM_POOL_SIZE);
            config.setMinimumIdle(DatabaseConfig.MINIMUM_IDLE);
            config.setConnectionTimeout(DatabaseConfig.CONNECTION_TIMEOUT);
            config.setIdleTimeout(DatabaseConfig.IDLE_TIMEOUT);
            config.setMaxLifetime(DatabaseConfig.MAX_LIFETIME);
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

            logger.info("Creating HikariCP DataSource");
            return new HikariDataSource(config);
        } catch (Exception e) {
            logger.error("Failed to create DataSource: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize DataSource", e);
        }
    }
}

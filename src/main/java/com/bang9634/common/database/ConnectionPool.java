package com.bang9634.common.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database connection pool using HikariCP.
 * Initializes the connection pool with settings from DatabaseConfig.
 */
@Singleton
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private final DataSource dataSource;

    @Inject
    public ConnectionPool(DataSource dataSource) {
        this.dataSource = dataSource;
        logger.info("ConnectionPool initialized successfully.");
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();

            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                logger.debug("Connection obtained from pool. Active: {}, Idle: {}, Total: {}",
                hikariDataSource.getHikariPoolMXBean().getActiveConnections(),
                hikariDataSource.getHikariPoolMXBean().getIdleConnections(),
                hikariDataSource.getHikariPoolMXBean().getTotalConnections());
            }

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
        if (!(dataSource instanceof HikariDataSource)) {
            return "Pool stats not available for  " + dataSource.getClass().getSimpleName();
        }

        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;

        if (hikariDataSource.isClosed()) {
            return "Connection pool is closed.";
        }

        return String.format(
            "HikariCP Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
            hikariDataSource.getHikariPoolMXBean().getActiveConnections(),
            hikariDataSource.getHikariPoolMXBean().getIdleConnections(),
            hikariDataSource.getHikariPoolMXBean().getTotalConnections(),
            hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    public boolean isClosed() {
        if (dataSource instanceof HikariDataSource) {
            return ((HikariDataSource) dataSource).isClosed();
        }
        return false;
    }

    /**
     * Close the connection pool and release all resources.
     * @apiNote Should be called on application shutdown
     */
    public void close() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            if (hikariDataSource != null && !hikariDataSource.isClosed()) {
                logger.info("Closing connection pool...");
                logger.info("Final stats: {}", getPoolStats());
                hikariDataSource.close();
                logger.info("Connection pool closed successfully.");
            }
        }

    }

    public DataSource getDataSource() {
        return dataSource;
    }
}

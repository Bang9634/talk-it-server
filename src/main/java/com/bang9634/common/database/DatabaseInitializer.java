package com.bang9634.common.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes the database schema.
 * Creates necessary tables if they do not exist.
 */
@Singleton
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final ConnectionPool connectionPool;

    @Inject
    public DatabaseInitializer(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void initialize() {
        logger.info("Starting database initialization...");

        try {
            createUsersTable();
            createMessagesTable();
            createBlockedIpsTable();
            createSessionsTable();
            
            logger.info("Database initialization completed successfully.");
        } catch (SQLException e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }


    private void createUsersTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(36) PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) UNIQUE,
                password_hash VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_login TIMESTAMP NULL,
                is_active BOOLEAN DEFAULT TRUE,
                INDEX idx_username (username),
                INDEX idx_email (email),
                INDEX idx_created_at (created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        executeUpdate(sql, "users table");
    }

    private void createMessagesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS messages (
                id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(36),
                username VARCHAR(50) NOT NULL,
                content TEXT NOT NULL,
                message_type ENUM('CHAT', 'JOIN', 'LEAVE', 'SYSTEM') DEFAULT 'CHAT',
                ip_address VARCHAR(45),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_user_id (user_id),
                INDEX idx_created_at (created_at),
                INDEX idx_message_type (message_type),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        executeUpdate(sql, "messages table");
    }

    private void createBlockedIpsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS blocked_ips (
                id INT AUTO_INCREMENT PRIMARY KEY,
                ip_address VARCHAR(45) NOT NULL UNIQUE,
                reason TEXT,
                blocked_by VARCHAR(50),
                blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NULL,
                INDEX idx_ip_address (ip_address),
                INDEX idx_expires_at (expires_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        executeUpdate(sql, "blocked_ips table");
    }

    private void createSessionsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS sessions (
                id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(36),
                ip_address VARCHAR(45) NOT NULL,
                user_agent TEXT,
                connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                disconnected_at TIMESTAMP NULL,
                INDEX idx_user_id (user_id),
                INDEX idx_ip_address (ip_address),
                INDEX idx_connected_at (connected_at),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        executeUpdate(sql, "sessions table");
    }

    private void executeUpdate(String sql, String tableName) throws SQLException {
        try (Connection conn = connectionPool.getConnection();
            Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            logger.info("Created/verified table: {}", tableName);
        } catch (SQLException e) {
            logger.error("Failed to create table: {}", tableName, e);
            throw e;
        }
    }

    public void dropAllTables() {
        logger.warn("Dropping all tables...");

        String[] tables = {"sessions", "messages", "blocked_ips", "users"};

        try (Connection conn = connectionPool.getConnection();
            Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            for (String table : tables) {
                try {
                    stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
                    logger.info("Dropped table: {}", table);
                } catch (SQLException e) {
                    logger.error("Failed to drop table: {}", table, e);
                }
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            logger.warn("All tables dropped successfully.");
        } catch (SQLException e) {
            logger.error("Failed to drop tables", e);
        }
    }

    public boolean isInitialized() {
        String sql = "SHOW TABLES LIKE 'users'";

        try (Connection conn = connectionPool.getConnection();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            return rs.next();
            
        } catch (SQLException e) {
            logger.error("Failed to check database initialization", e);
            return false;
        }
    }
}

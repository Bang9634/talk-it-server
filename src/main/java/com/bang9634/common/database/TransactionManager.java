package com.bang9634.common.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private final ConnectionPool connectionPool;

    @Inject
    public TransactionManager(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public <T> T executeInTransaction(TransactionOperation<T> operation) throws SQLException {
        Connection connection = null;
        boolean originalAutoCommit = true;

        try {
            connection = connectionPool.getConnection();
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            logger.debug("Transaction started");

            T result = operation.execute(connection);

            connection.commit();
            logger.debug("Transaction commited successfully");

            return result;
        } catch (SQLException e) {
            logger.error("Transaction failed, rolling back", e);
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.debug("Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Failed to close connection", e);
                }
            }
        }
    }

    public void executeInTransactionVoid(VoidTransactionOperation operation) throws SQLException {
        executeInTransaction(connection -> {
            operation.execute(connection);
            return null;
        });
    }

    @FunctionalInterface
    public interface TransactionOperation<T> {
        T execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface VoidTransactionOperation {
        void execute(Connection connection) throws SQLException;
    }
}

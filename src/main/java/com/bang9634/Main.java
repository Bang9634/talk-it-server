package com.bang9634;

import java.time.Duration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.common.config.ServiceInjector;
import com.bang9634.common.database.ConnectionPool;
import com.bang9634.common.database.DatabaseInitializer;
import com.bang9634.websocket.ChatWebSocketHandler;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String WEBSOCKET_PATH = "/chat";
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("TALK_IT_PORT", "8080"));
    public static void main(String[] args) {
        logger.info("Starting Talk_It Server...");

        try {
            ServiceInjector.initialize();
            initializeDatabase();
            
            Server server = new Server(PORT);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
                wsContainer.setMaxTextMessageSize(65535);
                wsContainer.setIdleTimeout(Duration.ofMinutes(10));

                wsContainer.addMapping(WEBSOCKET_PATH, ChatWebSocketHandler.class);

                logger.info("WebSocket mapping added at path: {}", WEBSOCKET_PATH);
            });
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down server...");
                try {
                    server.stop();
                    closeDatabase();
                    logger.info("Server stopped successfully");
                } catch (Exception e) {
                    logger.error("Error during shutdown", e);
                }
            }));

            server.start();
            logger.info("Server started successfully on port {}", PORT);
            server.join();
        } catch (Exception e) {
            logger.error("Error starting server: {}", e.getMessage());
            System.exit(1);
        }
    }

    private static void initializeDatabase() {
        logger.info("Initializing database...");

        try {
            ConnectionPool connectionPool = ServiceInjector.getInstance(ConnectionPool.class);
            DatabaseInitializer dbInitializer = ServiceInjector.getInstance(DatabaseInitializer.class);

            if (!connectionPool.testConnection()) {
                throw new RuntimeException("Database connection test failed");
            }

            dbInitializer.initialize();

            logger.info("Database initialized successfully");
            logger.info("Connection pool state: {}", connectionPool.getPoolStats());
        } catch (Exception e) {
            logger.error("Failed to initialize database: {}", e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void closeDatabase() {
        logger.info("Closing database connection pool...");
        try {
            ConnectionPool connectionPool = ServiceInjector.getInstance(ConnectionPool.class);
            connectionPool.close();
        } catch (Exception e) {
            logger.error("Error closing database connection pool", e);
        }
    }
}
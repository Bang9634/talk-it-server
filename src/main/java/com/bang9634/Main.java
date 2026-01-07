package com.bang9634;

import java.time.Duration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.common.config.ServiceInjector;
import com.bang9634.websocket.ChatWebSocketHandler;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String WEBSOCKET_PATH = "/chat";
    public static void main(String[] args) {
        ServiceInjector.initialize();

        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
            wsContainer.setMaxTextMessageSize(65535);
            wsContainer.setIdleTimeout(Duration.ofMinutes(10));

            wsContainer.addMapping(WEBSOCKET_PATH, ChatWebSocketHandler.class);

            logger.info("WebSocket mapping added at path: {}", WEBSOCKET_PATH);
        });
        try {
            server.start();

            logger.info("Server started on port 8080");
            server.join();
        } catch (Exception e) {
            logger.error("Error starting server: {}", e.getMessage());
            System.exit(1);
        }
    }
}
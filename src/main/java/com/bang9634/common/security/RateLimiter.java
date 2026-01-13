package com.bang9634.common.security;

import com.google.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bang9634.common.config.ApplicationConfig.MAX_MESSAGE_REQUESTS_PER_MINUTE;
import static com.bang9634.common.config.ApplicationConfig.MAX_CONNECT_REQUESTS_PER_MINUTE;
import static com.bang9634.common.config.ApplicationConfig.RATE_LIMIT_CLEANUP_INTERVAL_MINUTES;

/**
 * RateLimiter class to limit the rate of messages and connections per IP address.
 * Implements a token bucket algorithm for rate limiting.
 */
@Singleton
public class RateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);



    private final Map<String, MessageWindow> messageWindows = new ConcurrentHashMap<>();
    private final Map<String, ConnectionWindow> connectionWindows = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructor.
     * Initializes the RateLimiter and starts the cleanup scheduler.
     */
    public RateLimiter() {
        // Schedule periodic cleanup of old entries
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanup, 
            MAX_CONNECT_REQUESTS_PER_MINUTE, 
            RATE_LIMIT_CLEANUP_INTERVAL_MINUTES,
            TimeUnit.MINUTES);
        logger.info("RateLimiter initialized. - messages/min: {}, connections/min: {}",
            MAX_MESSAGE_REQUESTS_PER_MINUTE, MAX_CONNECT_REQUESTS_PER_MINUTE);
    }

    /**
     * Check if a message from the given IP is allowed.
     * 
     * @param ip The IP address of the sender
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowMessage(String ip) {
        if (ip == null || ip.equals("unknown")) {
            return true; // Bypass rate limiting for unknown IPs
        }

        MessageWindow window = messageWindows.computeIfAbsent(ip, k -> new MessageWindow());
        boolean allowed = window.tryConsume();

        if (!allowed) {
            logger.warn("IP {} exceeded message rate limit.", ip);
        }

        return allowed;
    }

    /**
     * Check if a new connection from the given IP is allowed.
     * 
     * @param ip The IP address of the connector
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowConnection(String ip) {
        if (ip == null || ip.equals("unknown")) {
            return true; // Bypass rate limiting for unknown IPs
        }

        ConnectionWindow window = connectionWindows.computeIfAbsent(ip, k -> new ConnectionWindow());
        boolean allowed = window.tryConsume();

        if (!allowed) {
            logger.warn("IP {} exceeded connection rate limit.", ip);
        }
        return allowed;
    }

    /**
     * Cleanup old entries from the rate limiter maps.
     */
    private void cleanup() {
        long now = System.currentTimeMillis();
        long tenMinutesAgo = now - (10 * 60_000);

        messageWindows.entrySet().removeIf(entry -> entry.getValue().isInactive(tenMinutesAgo));
        connectionWindows.entrySet().removeIf(entry -> entry.getValue().isInactive(tenMinutesAgo));
        
        logger.debug("RateLimiter cleanup completed. Remaining - messages: {}, connections: {}", 
            messageWindows.size(), connectionWindows.size());
    }


    /**
     * Inner class representing a sliding window for message rate limiting.
     */
    private static class MessageWindow {
        private final long[] timestamps = new long[MAX_MESSAGE_REQUESTS_PER_MINUTE];
        private int index = 0;
        private long lastAccessTime = System.currentTimeMillis();
        
        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            lastAccessTime = now;
            long oneMinuteAgo = now - 60_000;
            
            // Count messages in the last minute
            int count = 0;
            for (long timestamp : timestamps) {
                if (timestamp > oneMinuteAgo) {
                    count++;
                }
            }
            
            if (count >= MAX_MESSAGE_REQUESTS_PER_MINUTE) {
                return false; // Rate limit exceeded
            }
            
            // Record the new message timestamp
            timestamps[index] = now;
            index = (index + 1) % MAX_MESSAGE_REQUESTS_PER_MINUTE;
            return true;
        }
        
        boolean isInactive(long threshold) {
            return lastAccessTime < threshold;
        }
    }

    /**
     * Inner class representing a sliding window for connection rate limiting.
     */
    private static class ConnectionWindow {
        private final long[] timestamps = new long[MAX_CONNECT_REQUESTS_PER_MINUTE];
        private int index = 0;
        private long lastAccessTime = System.currentTimeMillis();
        
        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            lastAccessTime = now;
            long oneMinuteAgo = now - 60_000;
            
            int count = 0;
            for (long timestamp : timestamps) {
                if (timestamp > oneMinuteAgo) {
                    count++;
                }
            }
            
            if (count >= MAX_CONNECT_REQUESTS_PER_MINUTE) {
                return false;
            }
            
            timestamps[index] = now;
            index = (index + 1) % MAX_CONNECT_REQUESTS_PER_MINUTE;
            return true;
        }
        
        boolean isInactive(long threshold) {
            return lastAccessTime < threshold;
        }
    }

    /**
     * Get current rate limiter statistics.
     * 
     * @return Map of statistics
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "trackedMessageIps", messageWindows.size(),
            "trackedConnectionIps", connectionWindows.size(),
            "maxMessagesPerMinute", MAX_MESSAGE_REQUESTS_PER_MINUTE,
            "maxConnectionsPerMinute", MAX_CONNECT_REQUESTS_PER_MINUTE
        );
    }

    /**
     * Shutdown the RateLimiter and its scheduler.
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("RateLimiter 종료");
    }
}

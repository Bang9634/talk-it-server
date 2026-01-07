package com.bang9634.common.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for generating unique IDs.
 * Uses AtomicLong for thread-safe ID generation.
 */
public class IdGenerator {
    private static final AtomicLong messageCounter = new AtomicLong(0);

    /**
     * Generates a unique user ID using UUID.
     * @return a unique user ID
     */
    public static String generateUserId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a unique message ID using current time and an atomic counter.
     * @return a unique message ID
     */
    public static String generateMessageId() {
        return System.currentTimeMillis() + "_" + messageCounter.incrementAndGet();
    }

    private IdGenerator() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate IdGenerator");
    }
}

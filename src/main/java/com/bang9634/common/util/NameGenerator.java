package com.bang9634.common.util;

import java.util.Random;

import com.bang9634.common.constant.NameConstants;

/**
 * Utility class for generating names.
 */
public class NameGenerator {
    private static final Random random = new Random();

    /**
     * Generates a random anonymous name by combining an adjective and a noun.
     * defined in NameConstants.
     * @return a randomly generated anonymous name
     * @see NameConstants
     */
    public static String generateAnonymousName() {
        return "Guest" + (new Random().nextInt(10000));
    }

    private NameGenerator() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate NameGenerator");
    }
}

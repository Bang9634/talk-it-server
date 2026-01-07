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
        String adjective = NameConstants.ADJECTIVES[random.nextInt(NameConstants.ADJECTIVES.length)];
        String noun = NameConstants.NOUNS[random.nextInt(NameConstants.NOUNS.length)];
        return adjective + " " + noun;
    }

    private NameGenerator() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate NameGenerator");
    }
}

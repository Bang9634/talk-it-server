package com.bang9634.common.constant;

/**
 * Constants for names used in the application.
 */
public class NameConstants {
    /** Adjectives used for generating names */
    public static final String[] ADJECTIVES = {
        "멋진", "행복한", "즐거운", "신나는"
    };
    
    /** Nouns used for generating names */
    public static final String[] NOUNS = {
        "판다", "강아지", "펭귄", "고양이", "토끼"
    };

    private NameConstants() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate NameConstants");
    }
}

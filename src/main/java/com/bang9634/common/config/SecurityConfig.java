package com.bang9634.common.config;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bang9634.common.util.ConfigUtil.getEnvOrDefault;

/**
 * Security configuration class.
 * Manages CORS settings and environment-specific security configurations.
 */
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // Define allowed origins for CORS
    private static final Set<String> ALLOWED_ORIGINS = new HashSet<>();

    public static final String ENVIRONMENT = getEnvOrDefault("TALK_IT_ENV", "development");
    public static final boolean IS_PRODUCTION = ENVIRONMENT.equals("production");

    /**
     * Static initializer to set up allowed origins based on environment.
     * In production, reads from ALLOWED_ORIGINS env variable.
     */
    static {
        initializeAllowedOrigins();
        logger.info("SecurityConfig initialized. Environment: {}, Allowed Origins: {}",
            ENVIRONMENT, ALLOWED_ORIGINS
        );
    }

    /**
     * Initialize allowed origins based on envrironment.
     * In production, reads from ALLOWED_ORIGINS env variable.
     * In non-production, allows localhost origins for development.
     */
    private static void initializeAllowedOrigins() {
        if (IS_PRODUCTION) {
            String allowedOriginsEnv = getEnvOrDefault("ALLOWED_ORIGINS", "");
            if (!allowedOriginsEnv.trim().isEmpty()) {
                String[] origins = allowedOriginsEnv.split(",");
                for (String origin : origins) {
                    ALLOWED_ORIGINS.add(origin.trim());
                }
            } else {
                // env variable not set, log a warning
                logger.warn("ALLOWED_ORIGINS environment variable is not set. No origins will be allowed.");
            }
        } else {
            // In non-production environments, allow all origins for development convenience
            ALLOWED_ORIGINS.add("http://localhost:3000");
            ALLOWED_ORIGINS.add("http://localhost:5173"); // Vite
            ALLOWED_ORIGINS.add("http://localhost:8080");
        }
    }

    /**
     * Check if the given origin is allowed.
     * 
     * @param origin The origin to check
     * @return True if the origin is allowed, false otherwise
     */
    public static boolean isOriginAllowed(String origin) {
        // Allow all origins (including null/empty) in non-production environments
        if (origin == null || origin.isEmpty()) {
            return !IS_PRODUCTION;
        }

        // In production, only allow specified origins
        if (ALLOWED_ORIGINS.contains(origin)) {
            return true;
        }

        // Allow file:// origins in non-production for local testing
        if (!IS_PRODUCTION && origin.startsWith("file://")) {
            return true;
        }
        return false;
    }

    public static Set<String> getAllowedOrigins() {
        return new HashSet<>(ALLOWED_ORIGINS);
    }

    private SecurityConfig() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate SecurityConfig");
    }
}

package com.bang9634.common.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Service Injector using Google Guice for dependency injection.
 * Initializes and provides access to application services.
 */
public class ServiceInjector {
    private static final Logger logger = LoggerFactory.getLogger(ServiceInjector.class);
    private static Injector injector;

    /**
     * Initialize the DI container.
     * Should be called once at application startup.
     */
    public static void initialize() {
        if (injector == null) {
            injector = Guice.createInjector(new ApplicationModule());
            logger.info("DI Container initialized.");
        }
    }

    /**
     * Get an instance of the specified service type.
     * 
     * @param <T> The type of the service
     * @param type The class of the service
     * @return Instance of the requested service type
     */
    public static <T> T getInstance(Class<T> type) {
        if (injector == null) {
            throw new IllegalStateException("ServiceInjector is not initialized. Call initialize() first.");
        }
        return injector.getInstance(type);
    }
    
    /**
     * Get the Guice Injector.
     * 
     * @return The Injector instance
     */
    public static Injector getInjector() {
        return injector;
    }
}

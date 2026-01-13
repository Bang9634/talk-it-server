package com.bang9634.common.di;

import com.bang9634.chat.service.ChatRoomService;
import com.bang9634.chat.service.MessageService;
import com.bang9634.common.database.ConnectionPool;
import com.bang9634.common.database.DatabaseInitializer;
import com.bang9634.common.database.TransactionManager;
import com.bang9634.common.security.RateLimiter;
import com.bang9634.user.service.IpBlockService;
import com.bang9634.user.service.UserSessionManager;
import com.google.inject.AbstractModule;

/**
 * Guice module for configuring application services.
 * Binds services as singletons for dependency injection.
 */
public class ApplicationModule extends AbstractModule {
    /**
     * Configure bindings.
     * use asEagerSingleton() for services that should be initialized at startup.
     * use in(Singleton.class) for lazy initialization.
     * 
     * @see com.google.inject.AbstractModule#configure()
     * @implNote recommended to use asEagerSingleton for core services to ensure they are ready at startup.
     */
    @Override
    protected void configure() {
        // common/database
        bind(ConnectionPool.class).asEagerSingleton();
        bind(DatabaseInitializer.class).asEagerSingleton();
        bind(TransactionManager.class).asEagerSingleton();

        // common/security
        bind(RateLimiter.class).asEagerSingleton();

        // chat
        bind(MessageService.class).asEagerSingleton();
        bind(ChatRoomService.class).asEagerSingleton();

        // user
        bind(IpBlockService.class).asEagerSingleton();
        bind(UserSessionManager.class).asEagerSingleton();
    }
}

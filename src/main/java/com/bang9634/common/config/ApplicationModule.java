package com.bang9634.common.config;

import com.bang9634.chat.service.ChatRoomService;
import com.bang9634.chat.service.MessageService;
import com.bang9634.user.service.IpBlockService;
import com.bang9634.user.service.UserSessionManager;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Guice module for configuring application services.
 * Binds services as singletons for dependency injection.
 */
public class ApplicationModule extends AbstractModule {
    /**
     * Configure bindings.
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(UserSessionManager.class).in(Singleton.class);
        bind(MessageService.class).in(Singleton.class);
        bind(ChatRoomService.class).in(Singleton.class);
        bind(IpBlockService.class).in(Singleton.class);
    }
}

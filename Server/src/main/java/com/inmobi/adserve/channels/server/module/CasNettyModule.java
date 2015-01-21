package com.inmobi.adserve.channels.server.module;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.template.NativeTemplateAttributeFinder;
import com.inmobi.adserve.channels.server.CasTimeoutHandler;
import com.inmobi.adserve.channels.server.ChannelServerPipelineFactory;
import com.inmobi.adserve.channels.server.ChannelStatServerPipelineFactory;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.netty.CasNettyServer;
import com.inmobi.adserve.channels.util.annotations.ServerChannelInitializer;
import com.inmobi.adserve.channels.util.annotations.ServerConfiguration;
import com.inmobi.adserve.channels.util.annotations.StatServerChannelInitializer;
import com.inmobi.adserve.channels.util.annotations.WorkerExecutorService;


/**
 * @author abhishek.parwal
 * 
 */
public class CasNettyModule extends AbstractModule {

    private final Configuration serverConfiguration;

    public CasNettyModule(final Configuration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).annotatedWith(ServerConfiguration.class).toInstance(serverConfiguration);
        bind(LoggingHandler.class).toInstance(new LoggingHandler());

        // server pipelines
        final TypeLiteral<ChannelInitializer<SocketChannel>> channelInitializerType =
                new TypeLiteral<ChannelInitializer<SocketChannel>>() { };
        bind(channelInitializerType).annotatedWith(ServerChannelInitializer.class)
                .to(ChannelServerPipelineFactory.class).asEagerSingleton();
        bind(channelInitializerType).annotatedWith(StatServerChannelInitializer.class)
                .to(ChannelStatServerPipelineFactory.class).asEagerSingleton();

        bind(CasNettyServer.class).asEagerSingleton();
        bind(AsyncHttpClientProvider.class).asEagerSingleton();
        bind(NativeTemplateAttributeFinder.class).asEagerSingleton();
        bind(IPRepository.class).asEagerSingleton();

        // thread pool to be used in AsyncHttpClient
        bind(ExecutorService.class).annotatedWith(WorkerExecutorService.class).toInstance(
                Executors.newCachedThreadPool());
        requestStaticInjection(CasTimeoutHandler.class);
    }

    @Provides
    @Singleton
    ConnectionLimitHandler incomingConnectionLimitHandler(final ServerConfig serverConfig) throws Exception {
        return new ConnectionLimitHandler(serverConfig);
    }

}

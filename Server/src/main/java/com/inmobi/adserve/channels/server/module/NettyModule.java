package com.inmobi.adserve.channels.server.module;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

import javax.inject.Singleton;

import org.apache.commons.configuration.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.inmobi.adserve.channels.server.ChannelServerPipelineFactory;
import com.inmobi.adserve.channels.server.ChannelStatServerPipelineFactory;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.annotations.BossGroup;
import com.inmobi.adserve.channels.server.annotations.IncomingConnectionLimitHandler;
import com.inmobi.adserve.channels.server.annotations.ServerChannelInitializer;
import com.inmobi.adserve.channels.server.annotations.ServerConfiguration;
import com.inmobi.adserve.channels.server.annotations.StatServerChannelInitializer;
import com.inmobi.adserve.channels.server.annotations.WorkerGroup;
import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.config.ServerConfig;
import com.inmobi.adserve.channels.server.netty.CasNettyServer;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;


/**
 * @author abhishek.parwal
 * 
 */
public class NettyModule extends AbstractModule {

    private final Configuration serverConfiguration;

    public NettyModule(final Configuration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    protected void configure() {

        bind(Configuration.class).annotatedWith(ServerConfiguration.class).toInstance(serverConfiguration);

        bind(LoggingHandler.class).toInstance(new LoggingHandler());

        bind(CasNettyServer.class).asEagerSingleton();

        // event loopGroup
        bind(EventLoopGroup.class).annotatedWith(BossGroup.class).to(NioEventLoopGroup.class).asEagerSingleton();
        bind(EventLoopGroup.class).annotatedWith(WorkerGroup.class).to(NioEventLoopGroup.class).asEagerSingleton();

        // server pipelines

        TypeLiteral<ChannelInitializer<SocketChannel>> typeLiteral = new TypeLiteral<ChannelInitializer<SocketChannel>>() {
        };

        bind(typeLiteral).annotatedWith(ServerChannelInitializer.class).to(ChannelServerPipelineFactory.class)
                .asEagerSingleton();
        bind(typeLiteral).annotatedWith(StatServerChannelInitializer.class).to(ChannelStatServerPipelineFactory.class)
                .asEagerSingleton();

    }

    // ning client for out-bound calls, as for some partners netty client does not work
    @Provides
    @Singleton
    AsyncHttpClient provideAsyncHttpClient(@WorkerGroup final EventLoopGroup workerGroup) {
        AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                .setRequestTimeoutInMs(serverConfiguration.getInt("readtimeoutMillis"))
                .setConnectionTimeoutInMs(serverConfiguration.getInt("readtimeoutMillis"))
                .setMaximumConnectionsTotal(serverConfiguration.getInt("dcpOutGoingMaxConnections", 200))
                .setAllowPoolingConnection(true).setExecutorService(workerGroup).build();

        return new AsyncHttpClient(asyncHttpClientConfig);
    }

    @Provides
    @Singleton
    @IncomingConnectionLimitHandler
    ConnectionLimitHandler incomingConnectionLimitHandler(final ServerConfig serverConfig) {
        return new ConnectionLimitHandler(serverConfig, ConnectionType.INCOMING);
    }

}

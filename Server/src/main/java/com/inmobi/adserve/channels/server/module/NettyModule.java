package com.inmobi.adserve.channels.server.module;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import org.apache.commons.configuration.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.inmobi.adserve.channels.server.ChannelServerPipelineFactory;
import com.inmobi.adserve.channels.server.ChannelStatServerPipelineFactory;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.DcpClientChannelInitializer;
import com.inmobi.adserve.channels.server.RtbClientChannelInitializer;
import com.inmobi.adserve.channels.server.annotations.BossGroup;
import com.inmobi.adserve.channels.server.annotations.DcpClientBoostrap;
import com.inmobi.adserve.channels.server.annotations.DcpConnectionLimitHandler;
import com.inmobi.adserve.channels.server.annotations.IncomingConnectionLimitHandler;
import com.inmobi.adserve.channels.server.annotations.RtbClientBoostrap;
import com.inmobi.adserve.channels.server.annotations.RtbConnectionLimitHandler;
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

        // client pipelines
        bind(DcpClientChannelInitializer.class).asEagerSingleton();
        bind(RtbClientChannelInitializer.class).asEagerSingleton();

    }

    // ning client for out-bound calls, as for some partners netty client does not work
    @Provides
    @Singleton
    AsyncHttpClient provideAsyncHttpClient(@WorkerGroup final EventLoopGroup workerGroup) {
        AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                .setRequestTimeoutInMs(serverConfiguration.getInt("readtimeoutMillis") - 100)
                .setConnectionTimeoutInMs(serverConfiguration.getInt("readtimeoutMillis") - 200)
                .setAllowPoolingConnection(true).setExecutorService(workerGroup).build();
        return new AsyncHttpClient(asyncHttpClientConfig);
    }

    // client bootstrap
    @Provides
    @Singleton
    @DcpClientBoostrap
    Bootstrap provideDcpClientBootStrap(final DcpClientChannelInitializer dcpClientChannelInitializer,
            @WorkerGroup final EventLoopGroup workerGroup) {
        return new Bootstrap().group(workerGroup).channel(NioSocketChannel.class).handler(dcpClientChannelInitializer)
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true);

    }

    // client bootstrap
    @Provides
    @Singleton
    @RtbClientBoostrap
    Bootstrap provideDcpClientBootStrap(final RtbClientChannelInitializer rtbClientChannelInitializer,
            @WorkerGroup final EventLoopGroup workerGroup) {
        return new Bootstrap().group(workerGroup).channel(NioSocketChannel.class).handler(rtbClientChannelInitializer)
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true);

    }

    @Provides
    @Singleton
    @IncomingConnectionLimitHandler
    ConnectionLimitHandler incomingConnectionLimitHandler(final ServerConfig serverConfig) {
        return new ConnectionLimitHandler(serverConfig, ConnectionType.INCOMING);
    }

    @Provides
    @Singleton
    @RtbConnectionLimitHandler
    ConnectionLimitHandler rtbConnectionLimitHandler(final ServerConfig serverConfig) {
        return new ConnectionLimitHandler(serverConfig, ConnectionType.RTBD_OUTGOING);
    }

    @Provides
    @Singleton
    @DcpConnectionLimitHandler
    ConnectionLimitHandler dcpConnectionLimitHandler(final ServerConfig serverConfig) {
        return new ConnectionLimitHandler(serverConfig, ConnectionType.DCP_OUTGOING);
    }

}

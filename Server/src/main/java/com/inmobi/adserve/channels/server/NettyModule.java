package com.inmobi.adserve.channels.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Marker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.inmobi.adserve.channels.server.api.Servlet;


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

        bind(Timer.class).toInstance(new HashedWheelTimer(5, TimeUnit.MILLISECONDS));
        bind(Configuration.class).annotatedWith(ServerConfiguration.class).toInstance(serverConfiguration);

        SimpleScope simpleScope = new SimpleScope();
        bindScope(BatchScoped.class, simpleScope);
        bind(SimpleScope.class).toInstance(simpleScope);
        bind(Marker.class).toProvider(SimpleScope.<Marker> seededKeyProvider()).in(BatchScoped.class);
        bind(Servlet.class).toProvider(SimpleScope.<Servlet> seededKeyProvider()).in(BatchScoped.class);
    }

    @Provides
    SocketAddress provideSocketAddress() {
        return new InetSocketAddress(8800);
    }

    @Provides
    @Singleton
    ChannelGroup provideChannelGroup() {
        return new DefaultChannelGroup("http-server");
    }

    @Provides
    ChannelFactory provideChannelFactory() {
        return new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
    }

}

package com.inmobi.adserve.channels.server.module;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.inmobi.adserve.channels.server.ChannelServerPipelineFactory;
import com.inmobi.adserve.channels.server.ChannelStatServerPipelineFactory;
import com.inmobi.adserve.channels.server.annotations.ServerConfiguration;


/**
 * @author abhishek.parwal
 * 
 */
public class NettyModule extends AbstractModule {

    private final Configuration serverConfiguration;
    private final Integer       port;

    public NettyModule(final Configuration serverConfiguration, final Integer port) {
        this.serverConfiguration = serverConfiguration;
        this.port = port;
    }

    @Override
    protected void configure() {

        bind(Timer.class).toInstance(new HashedWheelTimer(5, TimeUnit.MILLISECONDS));
        bind(Configuration.class).annotatedWith(ServerConfiguration.class).toInstance(serverConfiguration);

        if (port == 8800) {
            bind(ChannelPipelineFactory.class).to(ChannelServerPipelineFactory.class).asEagerSingleton();
        }
        else if (port == 8801) {
            bind(ChannelPipelineFactory.class).to(ChannelStatServerPipelineFactory.class).asEagerSingleton();
        }
    }

    @Provides
    SocketAddress provideSocketAddress() {
        return new InetSocketAddress(port);
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

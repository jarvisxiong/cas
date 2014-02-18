package com.inmobi.adserve.channels.server.netty;

import com.google.common.util.concurrent.AbstractIdleService;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;

import javax.inject.Inject;
import java.net.SocketAddress;


/**
 * @author abhishek.parwal
 * 
 */
public class NettyServer extends AbstractIdleService {
    private final ChannelGroup                 allChannels;
    private final SocketAddress                address;
    private final ChannelFactory               factory;
    private final ServerBootstrap              bootstrap;
    private final ChannelPipelineFactory       channelServerPipelineFactory;

    @Inject
    NettyServer(final ChannelFactory factory, final ChannelGroup allChannels, final SocketAddress address,
            final ChannelPipelineFactory channelServerPipelineFactory) {
        this.factory = factory;
        this.bootstrap = new ServerBootstrap(factory);
        this.allChannels = allChannels;
        this.address = address;
        this.channelServerPipelineFactory = channelServerPipelineFactory;
    }

    @Override
    protected void startUp() throws Exception {
        bootstrap.setPipelineFactory(channelServerPipelineFactory);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.reuseAddress", true);
        bootstrap.setOption("child.connectTimeoutMillis", 5);
        Channel channel = bootstrap.bind(address);
        allChannels.add(channel);
    }

    @Override
    protected void shutDown() throws Exception {
        allChannels.close().awaitUninterruptibly();
        factory.releaseExternalResources();
    }
}

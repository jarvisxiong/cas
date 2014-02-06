package com.inmobi.adserve.channels.server.netty;

import java.net.SocketAddress;

import javax.inject.Inject;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;

import com.google.common.util.concurrent.AbstractIdleService;
import com.inmobi.adserve.channels.server.ChannelServerPipelineFactory;


/**
 * @author abhishek.parwal
 * 
 */
public class NettyServer extends AbstractIdleService {
    private final ChannelGroup                 allChannels;
    private final SocketAddress                address;
    private final ChannelFactory               factory;
    private final ServerBootstrap              bootstrap;
    private final ChannelServerPipelineFactory channelServerPipelineFactory;

    @Inject
    NettyServer(final ChannelFactory factory, final ChannelGroup allChannels, final SocketAddress address,
            final ChannelServerPipelineFactory channelServerPipelineFactory) {
        this.factory = factory;
        this.bootstrap = new ServerBootstrap(factory);
        this.channelServerPipelineFactory = channelServerPipelineFactory;
        this.allChannels = allChannels;
        this.address = address;
    }

    @Override
    protected void startUp() throws Exception {

        this.bootstrap.setPipelineFactory(channelServerPipelineFactory);
        this.bootstrap.setOption("child.keepAlive", true);
        this.bootstrap.setOption("child.tcpNoDelay", true);
        this.bootstrap.setOption("child.reuseAddress", true);
        this.bootstrap.setOption("child.connectTimeoutMillis", 5);

        Channel channel = bootstrap.bind(address);
        allChannels.add(channel);
    }

    @Override
    protected void shutDown() throws Exception {
        allChannels.close().awaitUninterruptibly();
        factory.releaseExternalResources();
    }
}

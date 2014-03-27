package com.inmobi.adserve.channels.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ChannelServerPipelineFactory;
import com.inmobi.adserve.channels.server.ChannelStatServerPipelineFactory;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class CasNettyServer {
    private final ServerBootstrap                  serverBootstrap;
    private final EventLoopGroup                   bossGroup;
    private final EventLoopGroup                   workerGroup;
    private final ChannelStatServerPipelineFactory statServerChannelInitializer;
    private final ChannelServerPipelineFactory     serverChannelInitializer;
    private final ServerBootstrap                  statServerBootstrap;

    @Inject
    public CasNettyServer(final ChannelServerPipelineFactory serverChannelInitializer,
            final ChannelStatServerPipelineFactory statServerChannelInitializer) {

        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap();
        this.statServerBootstrap = new ServerBootstrap();
        this.serverChannelInitializer = serverChannelInitializer;
        this.statServerChannelInitializer = statServerChannelInitializer;
    }

    @PostConstruct
    public void setup() throws InterruptedException {
        PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);
        // initialize and start server
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8800)).childHandler(serverChannelInitializer)
                // disable nagle's algorithm
                .childOption(ChannelOption.TCP_NODELAY, true)
                // allow binding channel on same ip, port
                .childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.ALLOCATOR, allocator).bind()
                .sync();

        // initialize and start stat server
        statServerBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8801)).childHandler(statServerChannelInitializer)
                .childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.ALLOCATOR, allocator).bind()
                .sync();

    }

    @PreDestroy
    public void tearDown() throws InterruptedException {
        // Shut down all event loops to terminate all threads.
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        // Wait until all threads are terminated.
        bossGroup.terminationFuture().sync();
        workerGroup.terminationFuture().sync();

    }

}

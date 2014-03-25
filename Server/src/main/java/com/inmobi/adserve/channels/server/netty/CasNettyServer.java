package com.inmobi.adserve.channels.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.util.annotations.ServerChannelInitializer;
import com.inmobi.adserve.channels.util.annotations.StatServerChannelInitializer;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class CasNettyServer {
    private ServerBootstrap                         serverBootstrap;
    private final EventLoopGroup                    bossGroup;
    private final EventLoopGroup                    workerGroup;
    private final ChannelInitializer<SocketChannel> statServerChannelInitializer;
    private final ChannelInitializer<SocketChannel> serverChannelInitializer;
    private ServerBootstrap                         statServerBootstrap;

    @Inject
    public CasNettyServer(@ServerChannelInitializer final ChannelInitializer<SocketChannel> serverChannelInitializer,
            @StatServerChannelInitializer final ChannelInitializer<SocketChannel> statServerChannelInitializer) {

        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap();
        this.statServerBootstrap = new ServerBootstrap();
        this.serverChannelInitializer = serverChannelInitializer;
        this.statServerChannelInitializer = statServerChannelInitializer;
    }

    @PostConstruct
    public void setup() throws InterruptedException {
        // initialize and start server
        serverBootstrap = serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8800)).childHandler(serverChannelInitializer)
                // disable nagle's algorithm
                .childOption(ChannelOption.TCP_NODELAY, true)
                // allow binding channel on same ip, port
                .childOption(ChannelOption.SO_REUSEADDR, true);

        ChannelFuture serverChannelFuture = serverBootstrap.bind().sync();
        serverChannelFuture.channel().closeFuture().sync();

        // initialize and start stat server
        statServerBootstrap = statServerBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8801)).childHandler(statServerChannelInitializer)
                .childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_REUSEADDR, true);
        ChannelFuture statFuture = statServerBootstrap.bind().sync();
        statFuture.channel().closeFuture().sync();

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

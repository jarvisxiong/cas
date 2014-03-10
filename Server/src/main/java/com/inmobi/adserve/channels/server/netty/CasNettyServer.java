package com.inmobi.adserve.channels.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.net.InetSocketAddress;

import javax.inject.Inject;

import com.google.common.util.concurrent.AbstractIdleService;
import com.inmobi.adserve.channels.server.annotations.BossGroup;
import com.inmobi.adserve.channels.server.annotations.ServerChannelInitializer;
import com.inmobi.adserve.channels.server.annotations.StatServerChannelInitializer;
import com.inmobi.adserve.channels.server.annotations.WorkerGroup;


/**
 * @author abhishek.parwal
 * 
 */
public class CasNettyServer extends AbstractIdleService {
    private ServerBootstrap                         serverBootstrap;
    private final EventLoopGroup                    bossGroup;
    private final EventLoopGroup                    workerGroup;
    private final ChannelInitializer<SocketChannel> statServerChannelInitializer;
    private final ChannelInitializer<SocketChannel> serverChannelInitializer;
    private final ServerBootstrap                   statServerBootstrap;

    @Inject
    CasNettyServer(@ServerChannelInitializer final ChannelInitializer<SocketChannel> serverChannelInitializer,
            @StatServerChannelInitializer final ChannelInitializer<SocketChannel> statServerChannelInitializer,
            @BossGroup final EventLoopGroup bossGroup, @WorkerGroup final EventLoopGroup workerGroup) {

        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serverBootstrap = new ServerBootstrap();
        this.statServerBootstrap = new ServerBootstrap();
        this.serverChannelInitializer = serverChannelInitializer;
        this.statServerChannelInitializer = statServerChannelInitializer;

    }

    @Override
    protected void startUp() throws Exception {

        // TODO: remove this
        ResourceLeakDetector.setLevel(Level.PARANOID);

        // initialize and start server
        serverBootstrap = serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8800)).childHandler(serverChannelInitializer)
                .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100);

        ChannelFuture serverChannelFuture = serverBootstrap.bind().sync();
        serverChannelFuture.channel().closeFuture().sync();

        // initialize and start stat server
        // statServerBootstrap = statServerBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
        // .localAddress(new InetSocketAddress(8801)).childHandler(statServerChannelInitializer)
        // .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
        // .childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5);
        // ChannelFuture statFuture = statServerBootstrap.bind().sync();
        // statFuture.channel().closeFuture().sync();

    }

    @Override
    protected void shutDown() throws Exception {
        // Shut down all event loops to terminate all threads.
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        // Wait until all threads are terminated.
        bossGroup.terminationFuture().sync();
        workerGroup.terminationFuture().sync();

    }
}

package com.inmobi.adserve.channels.server.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import javax.inject.Inject;

import com.google.common.util.concurrent.AbstractIdleService;
import com.inmobi.adserve.channels.server.DcpClientChannelInitializer;
import com.inmobi.adserve.channels.server.RtbClientChannelInitializer;
import com.inmobi.adserve.channels.server.annotations.BossGroup;
import com.inmobi.adserve.channels.server.annotations.DcpClientBoostrap;
import com.inmobi.adserve.channels.server.annotations.RtbClientBoostrap;
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
    private final Bootstrap                         dcpClientBoostrap;
    private final Bootstrap                         rtbClientBoostrap;
    private final RtbClientChannelInitializer       rtbClientChannelInitializer;
    private final DcpClientChannelInitializer       dcpClientChannelInitializer;
    private ServerBootstrap                         statServerBootstrap;

    @Inject
    CasNettyServer(@ServerChannelInitializer final ChannelInitializer<SocketChannel> serverChannelInitializer,
            @StatServerChannelInitializer final ChannelInitializer<SocketChannel> statServerChannelInitializer,
            final DcpClientChannelInitializer dcpClientChannelInitializer,
            final RtbClientChannelInitializer rtbClientChannelInitializer, @BossGroup final EventLoopGroup bossGroup,
            @WorkerGroup final EventLoopGroup workerGroup, @RtbClientBoostrap final Bootstrap rtbClientBoostrap,
            @DcpClientBoostrap final Bootstrap dcpClientBoostrap) {

        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serverBootstrap = new ServerBootstrap();
        this.statServerBootstrap = new ServerBootstrap();
        this.serverChannelInitializer = serverChannelInitializer;
        this.statServerChannelInitializer = statServerChannelInitializer;

        this.dcpClientChannelInitializer = dcpClientChannelInitializer;
        this.rtbClientChannelInitializer = rtbClientChannelInitializer;
        this.rtbClientBoostrap = rtbClientBoostrap;
        this.dcpClientBoostrap = dcpClientBoostrap;
    }

    @Override
    protected void startUp() throws Exception {

        // initialize and start server
        serverBootstrap = serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8800)).childHandler(serverChannelInitializer)
                .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5);

        ChannelFuture serverChannelFuture = serverBootstrap.bind().sync();
        serverChannelFuture.channel().closeFuture().sync();

        // initialize and start stat server
        statServerBootstrap = statServerBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8801)).childHandler(statServerChannelInitializer)
                .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5);
        ChannelFuture statFuture = statServerBootstrap.bind().sync();
        statFuture.channel().closeFuture().sync();

        // TODO: move clients to a separate module
        // clients
        dcpClientBoostrap.group(workerGroup).channel(NioSocketChannel.class).handler(dcpClientChannelInitializer)
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true);

        rtbClientBoostrap.group(workerGroup).channel(NioSocketChannel.class).handler(rtbClientChannelInitializer)
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true);

    }

    @Override
    protected void shutDown() throws Exception {
        // shut down event loop group
        bossGroup.shutdownGracefully().sync();
        workerGroup.shutdownGracefully().sync();

    }
}

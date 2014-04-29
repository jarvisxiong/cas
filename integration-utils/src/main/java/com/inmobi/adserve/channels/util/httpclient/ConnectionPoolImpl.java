package com.inmobi.adserve.channels.util.httpclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import net.sf.ehcache.util.NamedThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author abhishek.parwal
 * 
 */
public class ConnectionPoolImpl implements ConnectionPool {

    private final static Logger                                              LOG                = LoggerFactory
                                                                                                        .getLogger(ConnectionPoolImpl.class);
    public static final int                                                  MAX_HELPER_THREADS = 20;

    private final ConcurrentHashMap<PoolKey, ConcurrentLinkedQueue<Channel>> freeChannels       = new ConcurrentHashMap<>();

    private final int                                                        connectTimeoutInMillis;

    private final Object                                                     lock               = new Object();

    private final int                                                        maximumConnectionsTotal;

    private final Executor                                                   executor           = Executors
                                                                                                        .newFixedThreadPool(
                                                                                                                MAX_HELPER_THREADS,
                                                                                                                new NamedThreadFactory(
                                                                                                                        "httpHelpers"));

    private final AtomicInteger                                              connectionCount    = new AtomicInteger(0);

    public ConnectionPoolImpl(final int connectTimeoutInMillis, final int requestTimeoutInMillis,
            final int maximumConnectionsTotal) {
        this.connectTimeoutInMillis = connectTimeoutInMillis;
        this.maximumConnectionsTotal = maximumConnectionsTotal;
    }

    @Data
    public static class PoolKey {
        private final URI       uri;
        private final EventLoop eventLoop;
    }

    @Override
    public void getChannel(final NettyRequest nettyRequest, final EventLoop eventLoop,
            final ChannelWriteListener channelWriteListener) throws Exception {

        final PoolKey poolKey = new PoolKey(nettyRequest.getUri(), eventLoop);
        borrowChannel(poolKey, channelWriteListener);

    }

    @Override
    public void freeChannel(final Channel channel, final PoolKey poolKey) {
        LOG.debug("Freeing a channel {}  of pool key {}", channel, poolKey);
        synchronized (lock) {
            freeChannels.get(poolKey).offer(channel);
        }
    }

    private void borrowChannel(final PoolKey poolKey, final ChannelWriteListener channelWriteListener) {
        Channel channel = getAvailableChannel(poolKey);
        if (channel == null) {
            newChannel(channelWriteListener, poolKey);
        }
        else {
            try {
                channelWriteListener.operationComplete(channel.newSucceededFuture());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void newChannel(final ChannelWriteListener channelWriteListener, final PoolKey poolKey) {

        if (connectionCount.incrementAndGet() > maximumConnectionsTotal) {
            connectionCount.decrementAndGet();
            throw new RuntimeException(String.format("Cant open more than  %s Connection", maximumConnectionsTotal));
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.channel(NioSocketChannel.class).option(ChannelOption.AUTO_READ, false)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInMillis)
                        .group(poolKey.getEventLoop()).handler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(final Channel ch) throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast("httpRequestEncoder", new HttpRequestEncoder());
                                p.addLast("httpObjectAggregator", new HttpObjectAggregator(4 * 1024 * 1024));
                                p.addLast("httpResponseNotifier", new HttpResponseNotifier(ConnectionPoolImpl.this,
                                        poolKey));
                                p.addLast("httpResponseEncoder", new HttpResponseDecoder());
                            }
                        });

                ChannelFuture channelFuture = bootstrap.connect(poolKey.getUri().getHost(), poolKey.getUri().getPort());
                channelFuture.addListener(channelWriteListener);
                Channel channel = channelFuture.channel();

                LOG.debug("Opened a new channel {}", channel);
            }
        });

    }

    private Channel getAvailableChannel(final PoolKey poolKey) {
        synchronized (lock) {
            Channel channel;
            while ((channel = freeChannels.get(poolKey).poll()) != null) {
                if (channel.isWritable()) {
                    LOG.debug("Using Existing channel {}, of pool key {}", channel, poolKey);
                    return channel;
                }
            }
        }
        return null;
    }

}

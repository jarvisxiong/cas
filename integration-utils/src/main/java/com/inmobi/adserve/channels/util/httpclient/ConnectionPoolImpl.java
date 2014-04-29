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

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.util.httpclient.NettyHttpClientImpl.ChannelWriteListener;
import com.inmobi.adserve.channels.util.httpclient.NettyHttpClientImpl.HttpResponseNotifier;


/**
 * @author abhishek.parwal
 * 
 */
public class ConnectionPoolImpl implements ConnectionPool {

    private final static Logger                                              LOG          = LoggerFactory
                                                                                                  .getLogger(ConnectionPoolImpl.class);

    private final ConcurrentHashMap<PoolKey, ConcurrentLinkedQueue<Channel>> freeChannels = new ConcurrentHashMap<>();

    private final int                                                        connectTimeoutInMillis;

    private final Object                                                     lock        = new Object();

    private final int                                                        maximumConnectionsTotal;

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
    public Channel getChannel(final NettyRequest nettyRequest, final EventLoop eventLoop,
            final ChannelWriteListener channelWriteListener) throws Exception {

        final PoolKey poolKey = new PoolKey(nettyRequest.getUri(), eventLoop);
        Channel channel = borrowChannel(poolKey, channelWriteListener);
        return channel;

    }

    @Override
    public void freeChannel(final Channel channel, final PoolKey poolKey) {
        LOG.debug("Freeing a channel {}  of pool key {}", channel, poolKey);
        synchronized (lock) {
            freeChannels.get(poolKey).offer(channel);
        }
    }

    private Channel borrowChannel(final PoolKey poolKey, final ChannelWriteListener channelWriteListener) {
        Channel channel = getAvailableChannel(poolKey);
        if (channel == null) {
            channel = openChannel(channelWriteListener, poolKey);
        }
        else {
            LOG.debug("Using Existing channel {}, of pool key {}", channel, poolKey);
            try {
                channelWriteListener.operationComplete(channel.newSucceededFuture());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return channel;

    }

    private Channel openChannel(final ChannelWriteListener channelWriteListener, final PoolKey poolKey) {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class).option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInMillis).group(poolKey.getEventLoop())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(final Channel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("httpRequestEncoder", new HttpRequestEncoder());
                        p.addLast("httpObjectAggregator", new HttpObjectAggregator(4 * 1024 * 1024));
                        p.addLast("httpResponseNotifier", new HttpResponseNotifier(ConnectionPoolImpl.this, poolKey));
                        p.addLast("httpResponseEncoder", new HttpResponseDecoder());
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect(poolKey.getUri().getHost(), poolKey.getUri().getPort());
        channelFuture.addListener(channelWriteListener);

        Channel channel = channelFuture.channel();
        LOG.debug("Opened a new channel {}", channel);

        return channel;
    }

    private Channel getAvailableChannel(final PoolKey poolKey) {
        synchronized (lock) {
            Channel channel;
            while ((channel = freeChannels.get(poolKey).poll()) != null) {
                if (channel.isWritable()) {
                    return channel;
                }
            }
        }
        return null;
    }

}

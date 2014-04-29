package com.inmobi.adserve.channels.util.httpclient;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;

import com.inmobi.adserve.channels.util.httpclient.ConnectionPoolImpl.PoolKey;


/**
 * @author abhishek.parwal
 * 
 */
public interface ConnectionPool {

    void getChannel(final NettyRequest nettyRequest, final EventLoop eventLoop,
            final ChannelWriteListener channelWriteListener) throws Exception;

    void freeChannel(final Channel channel, final PoolKey poolKey);

}

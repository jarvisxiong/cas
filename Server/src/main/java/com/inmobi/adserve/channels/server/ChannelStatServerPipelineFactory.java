package com.inmobi.adserve.channels.server;

import javax.inject.Inject;

import com.inmobi.adserve.channels.server.handler.NettyRequestScopeSeedHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class ChannelStatServerPipelineFactory extends ChannelInitializer<SocketChannel> {
    private final RequestIdHandler requestIdHandler;
    private final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler;
    private final RequestParserHandler requestParserHandler;

    @Inject
    public ChannelStatServerPipelineFactory(final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler,
            final RequestParserHandler requestParserHandler) {
        this.nettyRequestScopeSeedHandler = nettyRequestScopeSeedHandler;
        requestIdHandler = new RequestIdHandler();
        this.requestParserHandler = requestParserHandler;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoderEncoder", new HttpServerCodec());
        // 1 MB data size
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("requestIdHandler", requestIdHandler);
        pipeline.addLast("nettyRequestScopeSeedHandler", nettyRequestScopeSeedHandler);
        pipeline.addLast("requestParserHandler", requestParserHandler);
    }

}

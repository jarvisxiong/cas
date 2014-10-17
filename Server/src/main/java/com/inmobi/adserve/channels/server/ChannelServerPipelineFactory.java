package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;

import javax.inject.Inject;

import lombok.Getter;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.handler.NettyRequestScopeSeedHandler;

@Singleton
public class ChannelServerPipelineFactory extends ChannelInitializer<SocketChannel> {

  private final RequestIdHandler requestIdHandler;
  private final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler;
  @Getter
  private final ConnectionLimitHandler incomingConnectionLimitHandler;
  private final ServerConfig serverConfig;
  private final LoggingHandler loggingHandler;
  private final RequestParserHandler requestParserHandler;

  @Inject
  ChannelServerPipelineFactory(final ServerConfig serverConfig,
      final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler,
      final ConnectionLimitHandler incomingConnectionLimitHandler, final LoggingHandler loggingHandler,
      final RequestParserHandler requestParserHandler) {

    this.serverConfig = serverConfig;
    this.nettyRequestScopeSeedHandler = nettyRequestScopeSeedHandler;
    requestIdHandler = new RequestIdHandler();
    this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
    this.loggingHandler = loggingHandler;
    this.requestParserHandler = requestParserHandler;
  }

  @Override
  protected void initChannel(final SocketChannel ch) throws Exception {
    final ChannelPipeline pipeline = ch.pipeline();
    // enable logging handler only for dev purpose
    // pipeline.addLast("logging", loggingHandler);
    pipeline.addLast("incomingLimitHandler", incomingConnectionLimitHandler);
    pipeline.addLast("decoderEncoder", new HttpServerCodec());
    // 1 MB max request size
    pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
    pipeline.addLast("casTimeoutHandler", new CasTimeoutHandler(serverConfig.getServerTimeoutInMillisForRTB(),
        serverConfig.getServerTimeoutInMillisForDCP()));
    pipeline.addLast("requestIdHandler", requestIdHandler);
    pipeline.addLast("nettyRequestScopeSeedHandler", nettyRequestScopeSeedHandler);
    pipeline.addLast("requestParserHandler", requestParserHandler);
  }
}

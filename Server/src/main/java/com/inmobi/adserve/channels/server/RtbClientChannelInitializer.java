package com.inmobi.adserve.channels.server;

import io.netty.handler.logging.LoggingHandler;

import javax.inject.Inject;

import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.config.ServerConfig;


public class RtbClientChannelInitializer extends ClientChannelInitializer {

    @Inject
    RtbClientChannelInitializer(final ServerConfig serverConfig, final LoggingHandler loggingHandler) {
        super(serverConfig, ConnectionType.RTBD_OUTGOING, loggingHandler);
    }

}

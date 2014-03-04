package com.inmobi.adserve.channels.server;

import javax.inject.Inject;

import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.config.ServerConfig;


public class RtbClientChannelInitializer extends ClientChannelInitializer {

    @Inject
    RtbClientChannelInitializer(final ServerConfig serverConfig) {
        super(serverConfig, ConnectionType.RTBD_OUTGOING);
    }

}

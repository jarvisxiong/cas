package com.inmobi.adserve.channels.server;

import javax.inject.Inject;

import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.config.ServerConfig;


public class DcpClientChannelInitializer extends ClientChannelInitializer {

    @Inject
    DcpClientChannelInitializer(final ServerConfig serverConfig) {
        super(serverConfig, ConnectionType.DCP_OUTGOING);
    }

}

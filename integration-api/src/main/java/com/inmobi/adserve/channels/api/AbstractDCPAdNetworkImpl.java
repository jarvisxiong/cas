package com.inmobi.adserve.channels.api;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.server.HttpRequestHandlerBase;


/**
 * @author abhishek.parwal
 * 
 */
public class AbstractDCPAdNetworkImpl extends BaseAdNetworkImpl {

    protected final Configuration config;

    protected AbstractDCPAdNetworkImpl(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(baseRequestHandler, serverChannel);
        this.config = config;
        this.clientBootstrap = clientBootstrap;
    }

}

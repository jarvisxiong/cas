package com.inmobi.adserve.channels.api;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;


/**
 * @author abhishek.parwal
 * 
 */
public class AbstractDCPAdNetworkImpl extends BaseAdNetworkImpl {

    protected final Configuration config;

    protected AbstractDCPAdNetworkImpl(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent);
        this.config = config;
        this.clientBootstrap = clientBootstrap;
    }

}

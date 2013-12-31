package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;


public class MullahMediaNetwork extends BaseMoolahMediaNetworkImpl {

    public MullahMediaNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
        host = config.getString("mullahmedia.host");
        publisherId = config.getString("mullahmedia.publisherId");
        advertiserId = config.getString("mullahmedia.advertiserId");
        advertiserName = "mullahmedia";
    }

}
package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;


public class MoolahMediaPremiumAdnetwork extends BaseMoolahMediaNetworkImpl {

    public MoolahMediaPremiumAdnetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
        host = config.getString("mmpremium.host");
        publisherId = config.getString("mmpremium.publisherId");
        advertiserId = config.getString("mmpremium.advertiserId");
        advertiserName = "mmpremium";
    }

}

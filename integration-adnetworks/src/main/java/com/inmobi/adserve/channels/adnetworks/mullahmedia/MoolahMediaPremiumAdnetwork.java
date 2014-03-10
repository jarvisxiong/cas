package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.server.HttpRequestHandlerBase;


public class MoolahMediaPremiumAdnetwork extends BaseMoolahMediaNetworkImpl {

    public MoolahMediaPremiumAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        host = config.getString("mmpremium.host");
        publisherId = config.getString("mmpremium.publisherId");
        advertiserId = config.getString("mmpremium.advertiserId");
        advertiserName = "mmpremium";
    }

}

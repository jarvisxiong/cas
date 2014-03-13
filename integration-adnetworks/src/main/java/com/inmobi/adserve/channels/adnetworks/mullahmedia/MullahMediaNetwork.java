package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;


public class MullahMediaNetwork extends BaseMoolahMediaNetworkImpl {

    public MullahMediaNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        host = config.getString("mullahmedia.host");
        publisherId = config.getString("mullahmedia.publisherId");
        advertiserId = config.getString("mullahmedia.advertiserId");
        advertiserName = "mullahmedia";
    }

}
package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.DebugLogger;


public class MoolahMediaPremiumAdnetwork extends BaseMoolahMediaNetworkImpl
{

    public MoolahMediaPremiumAdnetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent)
    {
        super(logger, config, clientBootstrap, baseRequestHandler, serverEvent);
        host = config.getString("mmpremium.host");
        publisherId = config.getString("mmpremium.publisherId");
        advertiserId = config.getString("mmpremium.advertiserId");
        advertiserName = "mmpremium";
    }

}

package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import org.apache.commons.configuration.Configuration;


public class MullahMediaReporting extends BaseMoolahMediaReporting
{

    public MullahMediaReporting(Configuration config)
    {
        super(config);
        email = config.getString("mullahmedia.email");
        password = config.getString("mullahmedia.password");
        host = config.getString("mullahmedia.host");
        advertiserName = "MullahMedia";
        advertiserId = config.getString("mullahmedia.advertiserId");
    }

}
